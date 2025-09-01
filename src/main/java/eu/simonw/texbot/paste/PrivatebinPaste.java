package eu.simonw.texbot.paste;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
// TODO: actually implement this properly. this is not working.
public class PrivatebinPaste implements PasteHandler {
    private final PasteHttpClient pasteHttpClient;
    private final Logger LOGGER;
    public PrivatebinPaste() {
        pasteHttpClient = new PasteHttpClient();
        LOGGER = LoggerFactory.getLogger(getClass());
    }

    @Override
    public CompletableFuture<String> get(String link) {
        CompletableFuture<String> content = pasteHttpClient.getContent("https://privatebin.io/?e3a089df3b60682d#2o2iqxVu7QWqpQA4ARUp948m7jibtqVUyKE6qed4Cb4v",
                Map.of("X-Requested-With", "JSONHttpRequest"));
        content.thenAccept(LOGGER::info);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    //{"status":0,
    // "id":"e3a089df3b60682d",
    // "url":"\/?e3a089df3b60682d?e3a089df3b60682d",
    // "adata":[["9Fn3NE+Xg1ApYX1FUOiBjg==","UauI2RvWhYA=",100000,256,128,"aes","gcm","zlib"],"plaintext",0,0],
    // "ct":"lrjjbQGRrcayxx++YU\/ikiHiCAzPXVLqWo\/dGFnxJk9FZ7JZykI=","comments":[],
    // "comment_count":0,"comment_offset":0,"@context":"?jsonld=paste"}


}
