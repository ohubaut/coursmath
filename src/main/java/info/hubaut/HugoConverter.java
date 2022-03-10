package info.hubaut;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
    private final Path baseOutput;
    private final Map<String, Path> staticFolders = new HashMap<>();
    private final Map<String, Path> contentFolders = new HashMap<>();
    private final Map<String, String> contentTagLinks = Map.of("a", "href", "img", "src");
    private final SectionConverter sectionConverter;
    private final FrontMatterHelper frontMatterHelper;

    public HugoConverter(Path baseDir, Path output) {
        this.baseDir = baseDir;
        this.baseOutput = output;
        frontMatterHelper = new FrontMatterHelper();
        sectionConverter = new SectionConverter(baseDir, baseOutput);
    }

    public void convert() throws IOException {
        sectionConverter.convert();
        Files.walkFileTree(baseDir, this);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file.toFile()
                .getName()
                .matches(".*\\.htm(l?)")
                && !sectionConverter.isSectionPage(file)) {
            convertContent(file, getOutputFolder(file, true));
        } else {
            transfer(file, getOutputFolder(file, false));
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
        Path relativePath = getRelativePath(file);
        String frontMatter = generateFrontMatter(document, relativePath);
        Elements elements = document.select(".central .texte");
        if (elements.isEmpty()) {
            System.out.println("No main content for " + file);
        } else if (elements.size() > 1) {
            System.out.println("More than one main content for " + file);
        } else {
            Element content = adjustContent(elements, relativePath);
            Path outputPath = computeOutputPath(file, outputFolder);
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                writer.append(frontMatter);
                writer.append(content.html());
            }
        }
    }

    private Element adjustContent(Elements elements, Path originalPath) {
        Element content = elements.first();
        Path currentDirectory = originalPath.getParent();
        if (currentDirectory != null) {
            // We are not at the root folder. Let's make all the paths explicit.
            contentTagLinks.forEach((key, value) -> {
                        Elements links = content.select(key);
                        links.stream()
                             .filter(element -> !element.attr(value).contains(":"))
                             .forEach(element -> {
                                 String originalHref = element.attr(value);
                                 Path newHref = currentDirectory.resolve(originalHref).normalize();
                                 element.attr(value, newHref.toString());
                             });
                    }
            );
        }
        return content;
    }

    private String generateFrontMatter(Document document, Path originalPath) {
        Path currentDirectory = originalPath.getParent();
        frontMatterHelper.addTitle(document);
        frontMatterHelper.addMeta(document.head().select("meta"));
        frontMatterHelper.addScripts(document.head().select("script"), currentDirectory);

        Map<String, Object> extras = new HashMap<>();
        if (!isIndexPage(originalPath)) {
            extras.put("aliases", List.of("/" + originalPath));
        }
        Element pageSubtitle = document.selectFirst(".stitre");
        if (pageSubtitle != null) {
            extras.put("subtitle", pageSubtitle.text());
        }
        frontMatterHelper.addExtras(extras);
        sectionConverter.get(originalPath)
                        .ifPresent(frontMatterHelper::addMenu);
        return frontMatterHelper.convert();
    }

    private Path computeOutputPath(Path file, Path outputFolder) {
        Path fileName = file.getFileName();
        String originalName = fileName.toString();
        String newName = originalName.substring(0, originalName.lastIndexOf(".")) + ".md";
        newName = isIndexPage(file) ? "_" + newName : newName;
        return outputFolder.resolve(newName);
    }

    private Path getOutputFolder(Path file, boolean html) throws IOException {
        Path output = html ? baseOutput.resolve("content") : baseOutput.resolve("static");
        if (!Files.exists(output)) {
            Files.createDirectory(output);
        }
        Path relativePath = getRelativePath(file);
        Path parentPath = relativePath.getParent();
        if (parentPath == null) {
            return output;
        }
        return html
                ? contentFolders.computeIfAbsent(parentPath.toString(), folder -> this.createOutputFolder(folder, output))
                : staticFolders.computeIfAbsent(parentPath.toString(), folder -> this.createOutputFolder(folder, output));
    }

    private boolean isIndexPage(Path file) {
        String pageName = file.getFileName().toString();
        return "index.html".equals(pageName) || "index.htm".equals(pageName);
    }

    private Path getRelativePath(Path file) {
        return baseDir.relativize(file);
    }

    private Path createOutputFolder(String relativeFolder, Path output) {
        Path outputFolder = output.resolve(relativeFolder);
        try {
            Files.createDirectories(outputFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputFolder;
    }

}
