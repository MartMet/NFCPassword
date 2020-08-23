package tech.mm.nfcpassword;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.InstrumentationRegistry;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentedTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = mActivityRule.getActivity().getBaseContext();

        assertEquals("tech.mm.nfcpassword", appContext.getPackageName());
    }

    @Test
    public void encrypt_decrypt_isCorrect() throws Exception {
        // Context of the app under test.
        Context appContext = mActivityRule.getActivity().getBaseContext();
        Activity mainActivity = mActivityRule.getActivity();

        String message = "This is the message to be encoded";

        KeyGen keygen;
        keygen = new KeyGen(mainActivity, appContext);
        Encryption encryption = new Encryption(keygen.getKey());

        byte[] encodedMessage = encryption.encodeMessage(message);
        String decodedMessage = encryption.decodeMessage(encodedMessage);

        assertEquals(message, decodedMessage);
    }
}
