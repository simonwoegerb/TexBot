import eu.simonw.texbot.paste.PasteHandler;
import eu.simonw.texbot.paste.PastebinPaste;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PastebinPasteTest {
    private final PasteHandler pasteHandler = new PastebinPaste();
    @Test
    void get() {
        pasteHandler.get("https://paste.simonw.eu/oFdPu").thenAccept(s -> {
            Assertions.assertEquals("$\\pi=3$", s);
        });

    }
}
