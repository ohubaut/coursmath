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
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SectionConverter implements FileVisitor<Path> {

    private final Path baseDir;
    private final Path baseOutput;
    private final Set<String> invalidSections = Set.of("coursmath", "bio", "cita", "midi", "jpol", "videos");
    private final Map<String, Integer> sectionOrder = new HashMap<>() {{
        put("mat", 2);
        put("2de", 3);
        put("com", 4);
        put("pol", 5);
        put("ana", 6);
        put("sta", 7);
        put("var", 8);
        put("app", 9);
        put("vie", 10);
        put("info", 11);
        put("3di", 12);
        put("doc", 13);
    }};
    private final Map<String, Map<String, PageMetadata>> sectionPages = new HashMap<>();
    private final FrontMatterHelper frontMatterHelper;

    public SectionConverter(Path baseDir, Path output) {
        this.baseDir = baseDir;
        this.baseOutput = output;
        frontMatterHelper = new FrontMatterHelper();
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
        if (isSectionPage(file)) {
            convertContent(file, getOutputFolder(file));
            return FileVisitResult.SKIP_SIBLINGS;
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

    public boolean isSectionPage(Path file) {
        String pageName = file.getFileName().toString();
        String section = file.getParent().getFileName().toString();
        return ("index.html".equals(pageName) || "index.htm".equals(pageName))
                && !invalidSections.contains(section);
    }

    public Optional<PageMetadata> get(Path file) {
        Path section = file.getParent();
        if (section != null) {
            return Optional.ofNullable(sectionPages.get(section.getFileName().toString()))
                           .map(m -> m.get(file.getFileName().toString()));
        }
        return Optional.empty();
    }

    private void convertContent(Path file, Path outputFolder) throws IOException {
        Document document = Jsoup.parse(Files.newInputStream(file), StandardCharsets.UTF_8.name(), "");
        Element container = document.selectFirst(".central .texte");
        String section = file.getParent().getFileName().toString();
        String frontMatter = generateFrontMatter(section, document);
        String content = extractContent(container);
        Map<String, PageMetadata> metadataMap = extractSectionPages(section, container);
        sectionPages.put(section, metadataMap);

        Path outputPath = computeOutputPath(file, outputFolder);
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.append(frontMatter);
            writer.append(content);
        }
    }

    private Map<String, PageMetadata> extractSectionPages(String section, Element container) {
        Map<String, PageMetadata> result = new HashMap<>();
        Elements items = container.select("li");
        for (int i = 0; i < items.size(); i++) {
            Element item = items.get(i);
            Element link = item.selectFirst("a");
            result.put(link.attr("href"), new PageMetadata(section, link.html(), i + 1, item.ownText()));
        }
        return result;
    }

    private String extractContent(Element content) {
        return content.select("p")
                      .stream()
                      .map(Element::html)
                      .collect(Collectors.joining("\n\n"));
    }

    private String generateFrontMatter(String section, Document document) {
        frontMatterHelper.addTitle(document);
        frontMatterHelper.addMeta(document.head()
                                          .select("meta"));
        frontMatterHelper.addMenu(section, sectionOrder);
        return frontMatterHelper.convert();
    }

    private Path computeOutputPath(Path file, Path outputFolder) {
        Path fileName = file.getFileName();
        String originalName = fileName.toString();
        String newName = "_" + originalName.substring(0, originalName.lastIndexOf(".")) + ".md";
        return outputFolder.resolve(newName);
    }

    private Path getOutputFolder(Path file) throws IOException {
        Path output = baseOutput.resolve("content");
        if (!Files.exists(output)) {
            Files.createDirectory(output);
        }
        Path relativePath = getRelativePath(file);
        Path parentPath = relativePath.getParent();
        if (parentPath == null) {
            return output;
        }
        return this.createOutputFolder(parentPath.toString(), output);
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
