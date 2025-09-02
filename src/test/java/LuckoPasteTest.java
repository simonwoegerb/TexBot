import eu.simonw.texbot.paste.LinkSplitter;
import eu.simonw.texbot.paste.LuckoPaste;
import eu.simonw.texbot.paste.PasteHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LuckoPasteTest {
    private final PasteHandler pasteHandler = new LuckoPaste();

    @Test
    void get() {
        pasteHandler.get("https://paste.simonw.eu/oFdPu").thenAccept(s -> {
            Assertions.assertEquals("$\\pi=3$", s);
        });

    }

    @Test
    void linksplitter() {
        var x = LinkSplitter.splitUrl("https://paste.simonw.eu/oFdPu");
        Assertions.assertEquals("oFdPu", x.id());
        Assertions.assertEquals("https://paste.simonw.eu", x.url());
    }
}
