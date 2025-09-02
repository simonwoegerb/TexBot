package eu.simonw.texbot.paste.privatebin;

import java.math.BigInteger;
import java.util.Arrays;

public class Base58Decoder {
    private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final int[] INDEXES = new int[128];

    static {
        Arrays.fill(INDEXES, -1);
        for (int i = 0; i < ALPHABET.length; i++) {
            INDEXES[ALPHABET[i]] = i;
        }
    }

    public static byte[] decode(String input) {
        if (input.isEmpty()) return new byte[0];

        // Convert the base58-encoded string to a big integer
        BigInteger num = BigInteger.ZERO;
        for (char c : input.toCharArray()) {
            int digit = INDEXES[c];
            if (digit < 0) throw new IllegalArgumentException("Invalid character '" + c + "' in Base58 string");
            num = num.multiply(BigInteger.valueOf(58)).add(BigInteger.valueOf(digit));
        }

        // Convert the number to bytes
        byte[] bytes = num.toByteArray();
        if (bytes[0] == 0) {
            bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
        }

        // Add leading zeros
        int zeros = 0;
        for (int i = 0; i < input.length() && input.charAt(i) == '1'; i++) {
            zeros++;
        }

        byte[] result = new byte[zeros + bytes.length];
        System.arraycopy(bytes, 0, result, zeros, bytes.length);
        return result;
    }
}
