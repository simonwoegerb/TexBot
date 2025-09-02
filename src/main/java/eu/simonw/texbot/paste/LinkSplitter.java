package eu.simonw.texbot.paste;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LinkSplitter {
    private static Pattern URL_PATTERN = null;

    public LinkSplitter() {
    }

    public record PasteUrl(String url, String id) {

    }

    public static PasteUrl splitUrl(String link) {
        if (URL_PATTERN == null) {
            URL_PATTERN = Pattern.compile("(?<url>https?://.+)/(?<pasteid>.+)");
        }
        Matcher matcher = URL_PATTERN.matcher(link);
        if (matcher.matches())
            return new PasteUrl(matcher.group("url"), matcher.group("pasteid"));
        else
            return new PasteUrl("", "");
    }
}
