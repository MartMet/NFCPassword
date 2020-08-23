package tech.mm.nfcpassword;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {
    private NFCCommunicator nfcCOM;
    private ProgressDialog dialog;
    private Tag currentTag;
    private NdefMessage message = null;
    private View v;
    private Vibrator vibrator;
    private KeyGen keygen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        try {
            keygen = new KeyGen(this, getApplicationContext());
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        nfcCOM = new NFCCommunicator(this);
        try {
            nfcCOM.verifyNFC();
        } catch (NFCCommunicator.NFCNotSupported nfcnsup) {
            Snackbar.make(v, "NFC not supported", Snackbar.LENGTH_LONG).show();
        } catch (NFCCommunicator.NFCNotEnabled nfcnEn) {
            Snackbar.make(v, "NFC Not enabled", Snackbar.LENGTH_LONG).show();
        }

        v = findViewById(R.id.mainAct);

        final EditText et = findViewById(R.id.editText);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nfcCOM.enableDispatch();
                String stringContent = et.getText().toString();

                byte[] content = new byte[0];
                try {
                    Encryption encryption = new Encryption(keygen.getKey());
                    content = encryption.encodeMessage(stringContent);
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                message = nfcCOM.createNFCPassword(content);
                if (message != null) {

                    dialog = new ProgressDialog(MainActivity.this);
                    dialog.setMessage("please tag your Tag");
                    dialog.show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);


    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d("Nfc", "New intent");
        // write the tag
        currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (message != null) {
            new Thread(new Runnable() {
                public void run() {
                    boolean returnValue = nfcCOM.writeTag(currentTag, message);
                    if (returnValue) {
                        Snackbar.make(v, "Tag written", Snackbar.LENGTH_LONG).show();
                        vibrator.vibrate(100);
                        message = null;
                    } else {
                        Snackbar.make(v, "Tag not written", Snackbar.LENGTH_LONG).show();
                        vibrator.vibrate(100);
                        SystemClock.sleep(500);
                        vibrator.vibrate(100);
                    }

                }
            }).start();
            dialog.dismiss();

        } else {
            byte[] payload;
            try {
                payload = nfcCOM.readTag(currentTag);
                Encryption encryption = new Encryption(keygen.getKey());
                String passwordString = encryption.decodeMessage(payload);

                Snackbar.make(v, "Password: " + passwordString, Snackbar.LENGTH_LONG).show();

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", passwordString);

                if (clipboard == null) return;
                clipboard.setPrimaryClip(clip);

                vibrator.vibrate(100);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                vibrator.vibrate(100);
                SystemClock.sleep(500);
                vibrator.vibrate(100);
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        try {
            nfcCOM.verifyNFC();
            nfcCOM.enableDispatch();
        } catch (NFCCommunicator.NFCNotSupported nfcnsup) {
            Snackbar.make(v, "NFC not supported", Snackbar.LENGTH_LONG).show();
        } catch (NFCCommunicator.NFCNotEnabled nfcnEn) {
            Snackbar.make(v, "NFC Not enabled", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dialog != null) dialog.dismiss();
    }
}
