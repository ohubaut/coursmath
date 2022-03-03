package info.hubaut;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HugoConverter implements FileVisitor<Path> {

    private final Path baseDir;
    private final Path output;
    private final Map<String, Path> folders = new HashMap<>();
    private final Dump yamlDump;

    public HugoConverter(Path baseDir, Path output) {
        this.baseDir = baseDir;
        this.output = output;
        DumpSettings dumpSettings = DumpSettings.builder()
                                                .setDefaultFlowStyle(FlowStyle.BLOCK)
                                                .build();
        yamlDump = new Dump(dumpSettings);
    }

    public void convert() throws IOException {
        Files.walkFileTree(baseDir, this);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path outputFolder = getOutputFolder(file);
        if (file.toFile()
                .getName()
                .matches(".*\\.htm(l?)")) {
            convertContent(file, outputFolder);
        } else {
            transfer(file, outputFolder);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        exc.printStackTrace();
        return FileVisitResult.TERMINATE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return exc != null
                ? FileVisitResult.TERMINATE
                : FileVisitResult.CONTINUE;
    }

    private void transfer(Path file, Path outputFolder) throws IOException {
        Files.copy(Files.newInputStream(file), outputFolder.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
    }

    private void convertContent(Path file, Path outputFolder) throws IOException {
        Document document = Jsoup.parse(Files.newInputStream(file), StandardCharsets.UTF_8.name(), "");
        String frontMatter = generateFrontMatter(document, getRelativePath(file));
        Elements elements = document.select(".central > .texte");
        if (elements.isEmpty()) {
            System.out.println("No main content for " + file);
        } else if (elements.size() > 1) {
            System.out.println("More than one main content for " + file);
        } else {
            Element content = elements.first();
            Path outputPath = computeOutputPath(file, outputFolder);
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                writer.append("---");
                writer.newLine();
                writer.append(frontMatter);
                writer.append("---");
                writer.newLine();
                writer.append(content.html());
            }
        }
    }

    private String generateFrontMatter(Document document, Path originalPath) {
        Map<String, Object> frontMatter = new HashMap<>();
        List<Map<String, String>> metadata = document.head()
                                                     .select("meta")
                                                     .stream()
                                                     .filter(element -> element.hasAttr("name"))
                                                     .map(element -> Map.of("name", element.attr("name"), "value", element.attr("content")))
                                                     .toList();
        Map<String, Object> genericData = Map.of(
                "title", document.title(),
                "meta", metadata);
        frontMatter.putAll(genericData);
        if (!isIndexPage(originalPath)) {
            frontMatter.put("aliases", List.of("/" + originalPath));
        }
        return yamlDump.dumpToString(frontMatter);
    }

    private Path computeOutputPath(Path file, Path outputFolder) {
        Path fileName = file.getFileName();
        String originalName = fileName.toString();
        String newName = originalName.substring(0, originalName.lastIndexOf(".")) + ".md";
        newName = isIndexPage(file) ? "_" + newName : newName;
        return outputFolder.resolve(newName);
    }

    private Path getOutputFolder(Path file) {
        Path relativePath = getRelativePath(file);
        Path parentPath = relativePath.getParent();
        return parentPath == null
                ? output
                : folders.computeIfAbsent(parentPath.toString(), this::createOutputFolder);
    }

    private boolean isIndexPage(Path file) {
        String pageName = file.getFileName().toString();
        return "index.html".equals(pageName) || "index.htm".equals(pageName);
    }

    private Path getRelativePath(Path file) {
        return baseDir.relativize(file);
    }

    private Path createOutputFolder(String relativeFolder) {
        Path outputFolder = output.resolve(relativeFolder);
        try {
            Files.createDirectories(outputFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputFolder;
    }

}
