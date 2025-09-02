import eu.simonw.texbot.paste.privatebin.Base58Decoder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Base58DecoderTest {
    @Test
    public void testDecodeValidBase58() {
        // Base58 for "hello" is "Cn8eVZg"
        byte[] decoded = Base58Decoder.decode("Cn8eVZg");
        byte[] expected = "hello".getBytes();
        Assertions.assertArrayEquals(decoded, expected, "Decoded bytes should match 'hello'");
    }

    @Test
    public void testDecodeEmptyString() {
        byte[] decoded = Base58Decoder.decode("");
        Assertions.assertEquals(0, decoded.length, "Empty input should return empty byte array");
    }

    @Test
    public void testDecodeLeadingOnesAsZeroBytes() {
        // "1" is leading zero byte in Base58
        byte[] decoded = Base58Decoder.decode("11");
        // Should return two leading 0x00 bytes
        Assertions.assertTrue(decoded.length == 2 && decoded[0] == 0 && decoded[1] == 0, "Leading '1's should be zero bytes");
    }

    @Test
    public void testDecodeWithInvalidCharacter() {
        try {
            Base58Decoder.decode("O0Il"); // O and 0 are invalid in Base58
            Assertions.fail("Expected exception for invalid characters");
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true, "Exception was correctly thrown for invalid Base58");
        }
    }

    @Test
    public void testDecodeKnownValue() {
        byte[] decoded = Base58Decoder.decode("2NEpo7TZRFNEKpLHM");
        byte[] expected = "Hello Base58".getBytes();
        Assertions.assertArrayEquals(decoded, expected, "Should decode to 'Hello Base58'");
    }
}
