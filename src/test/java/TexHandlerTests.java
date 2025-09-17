import eu.simonw.texbot.tex.TexHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;



public class TexHandlerTests extends TexHandler {
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

    /*
    firejail",
            "--caps.drop=all", // drop all root capabilities
            "--net=none", //no network access
            "--private=%DIRECTORY",
            "--whitelist=/usr/bin/latexmk",
            "--whitelist=/usr/bin/pdflatex",
            "--whitelist=/usr/share/texlive",
            "--whitelist=/usr/share/pdftoppm"
     */
    @Test
    void testFirejail() throws Exception{
        Path path = Path.of("/mocked/path.txt");
        String[] result = texHandler.firejail(new String[]{"a1", "a2"}, path);
        assertEquals("firejail", result[0]);
        assertEquals("--private=/mocked", result[3]);
        assertEquals("--whitelist=/usr/bin/latexmk",result[4]);

        assertEquals("a1", result[result.length-2]);
        assertEquals("a2", result[result.length-1]);
    }
    @Test
    void testConcat() {
        String[] testVal = concatArrays(new String[]{"a1", "a2"}, new String[]{"a3", "a4"}) ;
        assertArrayEquals(new String[] {"a1", "a2", "a3", "a4"}, testVal);
    }
}
