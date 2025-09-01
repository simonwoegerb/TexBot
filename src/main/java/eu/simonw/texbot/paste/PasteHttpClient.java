package eu.simonw.texbot.paste;

import okhttp3.*;
import okhttp3.internal.http2.Header;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PasteHttpClient {
    private final Logger LOGGER;
    private final OkHttpClient okHttp;

    public PasteHttpClient() {
        LOGGER = LoggerFactory.getLogger(getClass());
        okHttp = new OkHttpClient();
    }
    public CompletableFuture<String> getContent(String actual_uri) {
        return getContent(actual_uri, new HashMap<>());
    }
    public CompletableFuture<String> getContent(String actual_uri, Map<String,String> additional_headers) {
        Request request = new Request.Builder()
                .url(actual_uri)
                .headers(
                        Headers.of(additional_headers)
                )
                .header("User-Agent", "TexBot (github.com/simonwoegerb/TexBot)")
                .header("Accept", "text/plain")
                .build();

        CompletableFuture<String> future = new CompletableFuture<>();
        LOGGER.info("Accessing: {}", actual_uri);


        okHttp.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    future.completeExceptionally(new RuntimeException("Unexpected code " + response));
                } else {
                    future.complete(response.body().string());
                }
            }
        });
        return future;
    }
}
