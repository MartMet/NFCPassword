package tech.mm.nfcpassword;

import android.security.keystore.KeyProperties;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Encryption {

    private SecretKey secretKey;

    Encryption(SecretKey key) {
        secretKey = key;
    }

    public byte[] encodeMessage(String string) throws InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] realIV = cipher.getIV();
        byte[] stringBytes = string.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = cipher.doFinal(stringBytes);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(realIV);
        out.write(encryptedBytes);

        return out.toByteArray();
    }

    public String decodeMessage(byte[] bytes) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        byte[] realIV = new byte[16];
        bais.read(realIV);
        byte[] content = new byte[bytes.length - 16];
        bais.read(content);

        Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        IvParameterSpec ivSpec;

        ivSpec = new IvParameterSpec(realIV);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        byte[] decryptedBytes = cipher.doFinal(content);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

}
