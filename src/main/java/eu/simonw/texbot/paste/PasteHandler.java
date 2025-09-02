package eu.simonw.texbot.paste;

import java.util.concurrent.CompletableFuture;

public interface PasteHandler {
    CompletableFuture<String> get(String link);
}
