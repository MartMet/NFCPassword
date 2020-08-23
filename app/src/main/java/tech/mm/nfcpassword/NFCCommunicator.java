package tech.mm.nfcpassword;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class NFCCommunicator {

    private Activity activity;
    private NfcAdapter nfcAdpt;


    public NFCCommunicator(Activity activity) {

        this.activity = activity;
        nfcAdpt = NfcAdapter.getDefaultAdapter(activity);
    }

    public void verifyNFC() throws NFCNotSupported, NFCNotEnabled {

        if (nfcAdpt == null)
            throw new NFCNotSupported();

        if (!nfcAdpt.isEnabled())
            throw new NFCNotEnabled();

    }

    public void enableDispatch() {
        Intent nfcIntent = new Intent(activity, activity.getClass());
        nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, nfcIntent, 0);
        IntentFilter[] intentFiltersArray = new IntentFilter[]{};
        String[][] techList = new String[][]{{android.nfc.tech.Ndef.class.getName()}, {android.nfc.tech.NdefFormatable.class.getName()}};
        nfcAdpt.enableForegroundDispatch(activity, pendingIntent, intentFiltersArray, techList);
    }

    public void disableDispatch() {
        nfcAdpt.disableForegroundDispatch(activity);
    }

    public static class NFCNotSupported extends Exception {

        public NFCNotSupported() {
            super();
        }
    }

    public static class NFCNotEnabled extends Exception {

        public NFCNotEnabled() {
            super();
        }
    }


    public byte[] readTag(Tag currentTag) {
        Ndef ndef = Ndef.get(currentTag);
        if (ndef != null) {
            NdefMessage ndefMessage = ndef.getCachedNdefMessage();
            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                byte[] type = ndefRecord.getType();
                //application/tech.mm.nfcpassword
                String string = new String(type, StandardCharsets.US_ASCII);

                if (ndefRecord.getTnf() == NdefRecord.TNF_MIME_MEDIA && string.equals("application/" + activity.getPackageName())) {
                    return ndefRecord.getPayload();
                }
            }
        }
        return new byte[]{};
    }

    public boolean writeTag(Tag tag, NdefMessage message) {
        if (tag != null) {
            try {
                Ndef ndefTag = Ndef.get(tag);

                if (ndefTag == null) {
                    // format tag in NDEF
                    NdefFormatable nForm = NdefFormatable.get(tag);
                    if (nForm != null) {
                        nForm.connect();
                        nForm.format(message);
                        nForm.close();
                    }
                } else {
                    ndefTag.connect();
                    // assure tag is writable
                    if (!ndefTag.isWritable()) {
                        return false;
                    }

                    // enough space to write on tag?
                    int size = message.toByteArray().length;
                    int x = ndefTag.getMaxSize();
                    if (ndefTag.getMaxSize() < size) {
                        return false;
                    }
                    ndefTag.writeNdefMessage(message);
                    ndefTag.close();

                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;

            }
            return true;
        }
        return false;
    }

    //creates a custom NDEF message ("mime: mm.tech.nfpassword)
    public NdefMessage createNFCPassword(byte[] content) {
        try {
            NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                    new String("application/" + activity.getPackageName())
                            .getBytes(Charset.forName("UTF-8")),
                    new byte[0], content);
            return new NdefMessage(new NdefRecord[]{record});
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //for debugging purposes
    public NdefMessage createTextMessage(String content) {
        try {
            // Get UTF-8 byte
            byte[] lang = Locale.getDefault().getLanguage().getBytes(StandardCharsets.UTF_8);
            byte[] text = content.getBytes(StandardCharsets.UTF_8); // Content in UTF-8

            int langSize = lang.length;
            int textLength = text.length;

            ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + langSize + textLength);
            payload.write((byte) (langSize & 0x1F));
            payload.write(lang, 0, langSize);
            payload.write(text, 0, textLength);
            NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload.toByteArray());
            return new NdefMessage(new NdefRecord[]{record});
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}