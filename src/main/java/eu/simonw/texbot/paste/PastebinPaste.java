package eu.simonw.texbot.paste;

import java.util.concurrent.CompletableFuture;

public class PastebinPaste implements PasteHandler {
    private final PasteHttpClient pasteHttpClient;

    public PastebinPaste() {
        pasteHttpClient = new PasteHttpClient();
    }

    @Override
    public CompletableFuture<String> get(String link) {
        LinkSplitter.PasteUrl pasteUrl = LinkSplitter.splitUrl(link);

        String actual_uri = pasteUrl.url() + "/raw/" + pasteUrl.id();
        return pasteHttpClient.getContent(actual_uri);
    }
}
