package cnt.nfc.mbds.fr.easycommandnfc.NFCtestfinal;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;

import cnt.nfc.mbds.fr.easycommandnfc.R;

public class WriteURLActivity extends NfcBaseActivity {

    private Button buttonW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_url);

        buttonW = (Button) findViewById(R.id.buttonW);
        buttonW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (view.getId() == R.id.buttonW) {
                    displayMessage("Touch and hold tag against phone to write.");
                    enableWriteMode();
                    handleIntent(getIntent());
                }
            }
        });
    }

    private void handleIntent(Intent intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag;
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag == null)
                displayMessage("TAG NULL");
            else
                writeTag(this, tag);
        }
    }

    /**
     * Format a tag and write our NDEF message
     */
    private boolean writeTag(Context context, Tag tag) {

        EditText tonEdit = (EditText) findViewById(R.id.edit_message);
        String NFC_URL = tonEdit.getText().toString();
        Uri uri = Uri.parse(NFC_URL);
        NdefRecord recordNFC = NdefRecord.createUri(uri);
        NdefMessage message = new NdefMessage(recordNFC);


        try {
            // see if tag is already NDEF formatted
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();

                if (!ndef.isWritable()) {
                    displayMessage("Read-only tag.");
                    return false;
                }

                // work out how much space we need for the data
                int size = message.toByteArray().length;
                if (ndef.getMaxSize() < size) {
                    displayMessage("Tag doesn't have enough free space.");
                    return false;
                }

                ndef.writeNdefMessage(message);
                displayMessage("Tag written successfully.");
                return true;
            } else {
                // attempt to format tag
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        displayMessage("Tag written successfully!\nClose this app and scan tag.");
                        return true;
                    } catch (IOException e) {
                        displayMessage("Unable to format tag to NDEF.");
                        return false;
                    }
                } else {
                    displayMessage("Tag doesn't appear to support NDEF format.");
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            displayMessage("Failed to write tag");
        }

        return false;
    }

    @Override
    public void onNewIntent(Intent intent) {

        handleIntent(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        enableWriteMode();
    }

    @Override
    public void onResume() {
        super.onResume();
        disableWriteMode();
    }
}
