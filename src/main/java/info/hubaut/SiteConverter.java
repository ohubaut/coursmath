package info.hubaut;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SiteConverter {
    public static void main(String[] args) throws URISyntaxException, IOException {
        System.out.println("Starting conversion");
        SiteConverter converter = new SiteConverter();
        URL inputURL = SiteConverter.class.getResource("/original/coursmath");
        Path inputPath = Paths.get(inputURL.toURI());
        Path output = Files.createDirectories(Path.of("site", "content"));
        converter.convertToHugo(inputPath, output);
        System.out.println("Conversion finished");
    }

    private void convertToHugo(Path input, Path output) throws IOException {
        HugoConverter converter = new HugoConverter(input, output);
        converter.convert();
    }
}
