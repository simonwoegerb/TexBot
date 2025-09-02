import eu.simonw.texbot.tex.TexHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TexHandlerTests {
    private final TexHandler texHandler = new TexHandler();

    @Test
    void testSafeLatexInputs() {
        assertTrue(texHandler.isSafeLatex("Hello world"));
        assertTrue(texHandler.isSafeLatex("\\textit{Italic}"));
        assertTrue(texHandler.isSafeLatex("\\frac{1}{2}"));
        assertTrue(texHandler.isSafeLatex("\\begin{itemize}\\item One\\end{itemize}"));
    }

    @Test
    void testUnsafeLatexInputs() {
        assertFalse(texHandler.isSafeLatex("\\input{file}"));
        assertFalse(texHandler.isSafeLatex("\\write18{rm -rf /}"));
        assertFalse(texHandler.isSafeLatex("\\usepackage{shellesc}"));
        assertFalse(texHandler.isSafeLatex("\\catcode`\\@=11"));
        assertFalse(texHandler.isSafeLatex("\\openout1=evil.txt"));
        assertFalse(texHandler.isSafeLatex("\\csname input\\endcsname"));
    }

    @Test
    void testCaseInsensitiveDanger() {
        assertFalse(texHandler.isSafeLatex("\\Input{file}"));
        assertFalse(texHandler.isSafeLatex("\\Write18{echo}"));
    }

    @Test
    void testEdgeCases() {
        assertTrue(texHandler.isSafeLatex(""));
        assertTrue(texHandler.isSafeLatex(null)); // Or assertThrows if null is invalid
    }
}
