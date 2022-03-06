package info.hubaut;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

public class SiteConverter {
    public static void main(String[] args) throws URISyntaxException, IOException {
        Instant before = Instant.now();
        System.out.println("Starting conversion");
        SiteConverter converter = new SiteConverter();
        URL inputURL = SiteConverter.class.getResource("/original/coursmath");
        Path inputPath = Paths.get(inputURL.toURI());
        Path output = Files.createDirectories(Path.of("site"));
        converter.convertToHugo(inputPath, output);
        Instant after = Instant.now();
        System.out.println("Conversion finished in " + Duration.between(before, after));
    }

    private void convertToHugo(Path input, Path output) throws IOException {
        HugoConverter converter = new HugoConverter(input, output);
        converter.convert();
    }
}
