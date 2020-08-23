package tech.mm.nfcpassword;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class KeyGen {


    private SecretKey secretKey;
    private Activity activity;
    private Context context;

    public KeyGen(Activity activity, Context context) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this.activity = activity;
        this.context = context;
        secretKey = generateSecretKeyFromString(getUniqueTokens());

    }

    private SecretKey generateSecretKeyFromString(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        char[] passwordChars = password.toCharArray();

        //explicit no random salt, because we need the same hash(password) everytime
        byte salt[] = new byte[passwordChars.length]; // use salt size at least as long as hash
        Arrays.fill(salt, (byte) 0);


        // generate Hash
        PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, 1011, 32 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        SecretKey secretKey = new SecretKeySpec(hash, 0, hash.length, "AES");
        return secretKey;
    }

    private String getUniqueTokens() {
        String androidId = "";
        androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return (androidId);
    }

    public SecretKey getKey() {
        return secretKey;
    }
}
