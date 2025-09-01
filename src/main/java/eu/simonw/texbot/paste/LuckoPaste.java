package eu.simonw.texbot.paste;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.concurrent.CompletableFuture;

public class LuckoPaste implements PasteHandler {
    private final PasteHttpClient pasteHttpClient;
    public LuckoPaste() {
        pasteHttpClient = new PasteHttpClient();
    }

   /* public CompletableFuture<String> get(String link) {
        Matcher matcher = URL_PATTERN.matcher(link);
        LOGGER.warn("Matches: {}", matcher.matches());
        String url = matcher.group("url");
        String id = matcher.group("pasteid");
        String actual_uri = url + "/data/" + id;
        LOGGER.info("Actual URI used: {}", actual_uri);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                //.header("User-Agent", "TexBot (github.com/simonwoegerb/TexBot)")
                .header("Accept", "text/plain")
                .timeout(Duration.of(10, ChronoUnit.SECONDS))
                .build();


        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply((s) -> {
            LOGGER.info("STATUS: {}", s.statusCode());
            LOGGER.info("HEADERS: {}", s.headers().toString());
            LOGGER.info("BODY OF REQUEST: {}", s.body());
            return s.body();
        });
    } */
    public CompletableFuture<String> get(String link) {
        LinkSplitter.PasteUrl pasteUrl = LinkSplitter.splitUrl(link);

        String actual_uri = pasteUrl.url() + "/data/" + pasteUrl.id();
        return pasteHttpClient.getContent(actual_uri);

    }



}
