package info.hubaut;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class HugoConverter implements FileVisitor<Path> {

    private final Path baseDir;
    private final Path output;
    private final Map<String, Path> folders = new HashMap<>();

    public HugoConverter(Path baseDir, Path output) {
        this.baseDir = baseDir;
        this.output = output;
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

    private Path getOutputFolder(Path file) {
        Path relativePath = baseDir.relativize(file);
        Path parentPath = relativePath.getParent();
        return parentPath == null
               ? output
               : folders.computeIfAbsent(parentPath.toString(), this::createOutputFolder);
    }

    private Path createOutputFolder(String relativeFolder) {
        Path outputFolder = output.resolve(relativeFolder);
        try {
            System.out.println("Creating folder " + outputFolder);
            Files.createDirectories(outputFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputFolder;
    }

}
