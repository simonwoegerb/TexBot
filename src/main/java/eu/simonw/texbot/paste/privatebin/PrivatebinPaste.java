package eu.simonw.texbot.paste.privatebin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.simonw.texbot.paste.PasteHandler;
import eu.simonw.texbot.paste.PasteHttpClient;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

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
        CompletableFuture<String> content = pasteHttpClient.getContent(link,
                Map.of("X-Requested-With", "JSONHttpRequest"));
        content.thenApply(s -> {
            byte[] keyBytes = decodeKeyFromUrl(link);

            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode payload = mapper.readTree(s);

                return decrypt(payload, keyBytes, "");
            } catch (Exception e) {
                LOGGER.error("Error on decryption of Privatebin {}. {}", link, e.getMessage());
            }
            return null;
        });
        try {
            Thread.sleep(2000);

        } catch (Exception _) {

        }


        return null;
    }

    public String decrypt(JsonNode payload, byte[] keyBytes, String password) throws Exception {
        JsonNode adata = payload.get("adata");
        JsonNode spec = adata.get(0);

        byte[] iv = Base64.getDecoder().decode(spec.get(0).asText());
        byte[] salt = Base64.getDecoder().decode(spec.get(1).asText());
        int iterations = spec.get(2).asInt();
        int keySizeBits = spec.get(3).asInt();
        int tagSizeBits = spec.get(4).asInt();
        String compression = spec.get(7).asText();

        byte[] derivedKey = deriveKey(keyBytes, password.getBytes(StandardCharsets.UTF_8), salt, iterations, keySizeBits / 8);


        byte[] ciphertextAndTag = Base64.getDecoder().decode(payload.get("ct").asText());
        int tagLenBytes = tagSizeBits / 8;

        byte[] ciphertext = new byte[ciphertextAndTag.length - tagLenBytes];
        byte[] tag = new byte[tagLenBytes];

        System.arraycopy(ciphertextAndTag, 0, ciphertext, 0, ciphertext.length);
        System.arraycopy(ciphertextAndTag, ciphertext.length, tag, 0, tag.length);

        byte[] fullCiphertext = new byte[ciphertext.length + tag.length];
        System.arraycopy(ciphertext, 0, fullCiphertext, 0, ciphertext.length);
        System.arraycopy(tag, 0, fullCiphertext, ciphertext.length, tag.length);

        byte[] adataBytes = new ObjectMapper().writeValueAsBytes(adata);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(derivedKey, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(tagSizeBits, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
        cipher.updateAAD(adataBytes);

        byte[] decrypted = cipher.doFinal(fullCiphertext);

        String jsonString;
        if ("zlib".equalsIgnoreCase(compression)) {
            jsonString = decompressZlib(decrypted);
        } else {
            jsonString = new String(decrypted, StandardCharsets.UTF_8);
        }

        JsonNode result = new ObjectMapper().readTree(jsonString);
        return result.get("paste").asText();
    }

    private byte[] deriveKey(byte[] baseKey, byte[] password, byte[] salt, int iterations, int keyLen) {
        byte[] input = new byte[baseKey.length + password.length];
        System.arraycopy(baseKey, 0, input, 0, baseKey.length);
        System.arraycopy(password, 0, input, baseKey.length, password.length);

        PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(new SHA256Digest());
        generator.init(input, salt, iterations);
        return ((KeyParameter) generator.generateDerivedParameters(keyLen * 8)).getKey();
    }

    private String decompressZlib(byte[] compressed) throws Exception {
        Inflater inflater = new Inflater();
        InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(compressed), inflater);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;
        while ((len = iis.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }

        return baos.toString(StandardCharsets.UTF_8);
    }

    // ✅ NEW FUNCTION: Extract and decode Base58 key from PrivateBin link
    public byte[] decodeKeyFromUrl(String url) {
        int hashIndex = url.indexOf('#');
        if (hashIndex == -1 || hashIndex == url.length() - 1) {
            throw new IllegalArgumentException("No key found in URL fragment");
        }

        String base58Key = url.substring(hashIndex + 1).trim();
        return Base58Decoder.decode(base58Key);
    }

}
