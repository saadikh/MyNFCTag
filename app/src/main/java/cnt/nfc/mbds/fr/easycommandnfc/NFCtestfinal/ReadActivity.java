package cnt.nfc.mbds.fr.easycommandnfc.NFCtestfinal;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import cnt.nfc.mbds.fr.easycommandnfc.R;

public class ReadActivity extends NfcBaseActivity {


    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String MIME_APP = "application/octet-stream";
    public static final String TAG = "NfcDemo";

    final protected static char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private String type;
    private String[] technology;
    private int size;
    private byte[] ID_tag;
    private String idRest;
    private String idTab;

    private LinearLayout mContentLayout;
    private RelativeLayout mScanLayout;
    private TextView mContentTV;
    private TextView mTypeTV;
    private TextView mTechTV;
    private TextView mSizeTV;
    private TextView mIdTV;
    private TextView idResto;
    private TextView idTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        mScanLayout = (RelativeLayout) findViewById(R.id.scan_layout);
        mContentLayout = (LinearLayout) findViewById(R.id.content_layout);
        mContentTV = (TextView) findViewById(R.id.content_textView);//
        mTypeTV = (TextView) findViewById(R.id.type_textView);
        mTechTV = (TextView) findViewById(R.id.tech_textView);
        mSizeTV = (TextView) findViewById(R.id.size_textView);
        mIdTV = (TextView) findViewById(R.id.id_textView);
        idResto = (TextView)findViewById(R.id.idResto_textView);
        idTable = (TextView)findViewById(R.id.idTable_textView);
    }


    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            technology = tag.getTechList();
            Ndef ndef = Ndef.get(tag);
            size = ndef.getMaxSize();
            type = intent.getType();
            ID_tag = tag.getId();

            if (MIME_TEXT_PLAIN.equals(type) || MIME_APP.equals(type)) {
                new NdefReaderTask().execute(tag);
            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            technology = tag.getTechList();
            Ndef ndef = Ndef.get(tag);
            size = ndef.getMaxSize();
            type = intent.getType();
            ID_tag = tag.getId();
            String searchedTech = Ndef.class.getName();

            for (String tech : technology) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
        else if (intent.getType() != null && intent.getType().equals("application/cnt.nfc.mbds.fr.easycommandnfc")) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefRecord relayRecord = ((NdefMessage) rawMsgs[0]).getRecords()[0];
            String nfcData = new String(relayRecord.getPayload());
            Toast.makeText(this, nfcData, Toast.LENGTH_SHORT).show();
        }
    }

    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }
            NdefMessage ndefMessage = ndef.getCachedNdefMessage();
            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                } else if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_URI)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }
            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
            byte[] payload = record.getPayload();
            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? new String("UTF-8") : "UTF-16";
            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;
            // Get the Text
            String resut = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
            String[] res1 = resut.split(" ");
            String res2 = res1[0];
            String res3 = res1[1];
            /*idResto.setText(res2.trim());
            idTable.setText(res3);*/
            idRest = res2;
            idTab = res3;
            //String resfinal = "<<restaurantId: "+res2+", tableId: "+res3+">>";

            return resut;
        }

        @Override
        protected void onPostExecute(String result) {

            StringBuilder sb = new StringBuilder();
            if (result != null) {
                sb.append(technology[0].toString().split("\\.")[3]);
                for (int i = 1; i < technology.length; i++) {
                    sb.append(", " + technology[i].toString().split("\\.")[3]);
                }
                mContentTV.setText("   " + result);
                mTypeTV.setText("   " + type);
                mTechTV.setText("   " + sb.toString());
                mSizeTV.setText("   " + size + " Bytes");
                mIdTV.setText("   " + bytesToHex(ID_tag));
                String[] res1 = result.split(" ");
                String res2 = res1[0];
                String res3 = res1[1];
                idResto.setText(" "+ res2);
                idTable.setText(" "+res3);
            } else {
                mContentTV.append("TAG vide");
            }

            mScanLayout.setVisibility(View.GONE);
            mContentLayout.setVisibility(View.VISIBLE);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[((bytes.length) * 3) - 1];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            if (j != bytes.length - 1)
                hexChars[j * 3 + 2] = ':';
        }
        return new String(hexChars);
    }

    public void onNewTag(Tag tag) {
        handleIntent(getIntent());
    }
}
