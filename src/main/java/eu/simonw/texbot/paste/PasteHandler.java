package eu.simonw.texbot.paste;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface PasteHandler {
    public CompletableFuture<String> get(String link);
}
