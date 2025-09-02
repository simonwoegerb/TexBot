import eu.simonw.texbot.paste.privatebin.PrivatebinPaste;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class PrivatebinPasteTest {
    @Test
    @Disabled("Tested class not implemented/prod-ready")
    public void test() {
        new PrivatebinPaste().get("https://privatebin.io/?e3a089df3b60682d#2o2iqxVu7QWqpQA4ARUp948m7jibtqVUyKE6qed4Cb4v");
        Assertions.assertTrue(true);
    }
}
