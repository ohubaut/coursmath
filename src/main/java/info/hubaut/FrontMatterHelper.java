package info.hubaut;

import org.jsoup.select.Elements;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrontMatterHelper {

    private final Dump yamlDump;
    private final Map<String, Object> frontMatter = new HashMap<>();

    public FrontMatterHelper() {
        DumpSettings dumpSettings = DumpSettings.builder()
                                                .setDefaultFlowStyle(FlowStyle.BLOCK)
                                                .build();
        yamlDump = new Dump(dumpSettings);
    }

    public void addMeta(Elements meta) {
        meta.stream()
            .filter(element -> element.hasAttr("name"))
            .forEach(element -> frontMatter.put(element.attr("name"), element.attr("content")));
    }

    public void addTitle(String title) {
        frontMatter.put("title", title);
    }

    public void addExtras(Map<String, Object> extras) {
        frontMatter.putAll(extras);
    }

    public void addScripts(Elements scripts, Path currentDirectory) {
        List<String> externalScripts = scripts.stream()
                                              .filter(element -> element.hasAttr("src"))
                                              .filter(element -> !element.attr("src").contains(":"))
                                              .map(element -> element.attr("src"))
                                              .map(path -> currentDirectory != null
                                                      ? currentDirectory.resolve(path).normalize().toString()
                                                      : path)
                                              .toList();
        frontMatter.put("scripts", externalScripts);
    }

    public String convert() {
        String result = "---\n" + yamlDump.dumpToString(frontMatter) + "---\n";
        frontMatter.clear();
        return result;
    }

    public void addSectionPageMetadata(PageMetadata pageMetadata) {
        frontMatter.put("pageName", pageMetadata.pageName());
        frontMatter.put("weight", pageMetadata.weight());
        frontMatter.put("summary", pageMetadata.summary());
    }
}
