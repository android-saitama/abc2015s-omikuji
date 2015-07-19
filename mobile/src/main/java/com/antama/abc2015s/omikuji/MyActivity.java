package com.antama.abc2015s.omikuji;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.Arrays;


public class MyActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MA";

    private GoogleApiClient mClient;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        mClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Log.d(TAG, String.format("requestCode: %d, resultCode: %d, data: %s", requestCode, resultCode, data.getData()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        mClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "api is ready");
        Wearable.MessageApi.addListener(mClient, new MessageApi.MessageListener() {
            @Override
            public void onMessageReceived(MessageEvent messageEvent) {
                if ("bump".equals(new String(messageEvent.getData()))) {
                    // TODO: write what should be done on Omikuji roll (#3)

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            printResult();
                            Toast.makeText(MyActivity.this, "印刷中", Toast.LENGTH_SHORT).show();

                            Log.d(TAG, "bump!");
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, String.format("connection suspended: %d", i));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, String.format("connection failed: %s", connectionResult));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_my, container, false);
            return rootView;
        }
    }


    public void printResult() {
        int[] status = new int[1];
        status[0] = 0;

        final Cursor c = getContentResolver().query(ProviderMap.getContentUri(ProviderMap.ORACLE), null, null, null, null);

        String total = null;
        String number = null;
        String colorName = null;
        String colorRGB = null;
        String area = null;
        String title = null;
        String description = null;
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            total = c.getString(OracleProvider.COL_TOTAL);
            number = c.getString(OracleProvider.COL_NUMBER);
            colorName = c.getString(OracleProvider.COL_COLOR_NAME);
            colorRGB = c.getString(OracleProvider.COL_COLOR_RGB);
            area = c.getString(OracleProvider.COL_AREA);
            title = c.getString(OracleProvider.COL_TITLE);
            description = c.getString(OracleProvider.COL_DESCRIPTION);
        }

        try {
            Print printer = new Print(getApplicationContext());

            String printerIPAddress = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.PREF_PRINTER_IP_ADDRESS, "");

            printer.openPrinter(Print.DEVTYPE_TCP, printerIPAddress, Print.FALSE,
                    Print.PARAM_DEFAULT, Print.PARAM_DEFAULT);

            Builder builder = new Builder("TM-T70", Builder.MODEL_JAPANESE);
            // 印刷ドキュメントの作成
            builder.addTextLang(Builder.LANG_JA);
            builder.addTextSmooth(Builder.TRUE);
            builder.addTextFont(Builder.FONT_A);
            builder.addText("━━━━━━━━━━━━━━━━━━━━━━━━\n");
            builder.addTextSize(8, 8);
            builder.addTextAlign(Builder.ALIGN_CENTER);
            builder.addText(String.format("%s\n", total));
            builder.addTextSize(1, 1);
            builder.addTextAlign(Builder.ALIGN_LEFT);
            builder.addText("━━━━━━━━━━━━━━━━━━━━━━━━\n");

            printer.sendData(builder, 10000, status);
            builder.clearCommandBuffer();
            builder.addTextLang(Builder.MODEL_JAPANESE);

            builder.addText(String.format("ラッキーナンバー : %s\n", number));
            builder.addText(String.format("ラッキーカラー : %s(%s)\n",colorName,colorRGB));
            builder.addText(String.format("埼玉のおすすめエリア : %s(%s)\n",area,title));
            builder.addText(description + "\n");
            builder.addText("━━━━━━━━━━━━━━━━━━━━━━━━\n");
            builder.addText("埼玉支部とは Android が好きな人、それを使ってなにかするのが好き な人が集まっている場所です。ゆるい雰囲気が特徴で、技術的な相談 から開発管理手法まで親身になって相談にのってくれます。\n");
            builder.addText("ML、および Facebook グループへ参加することで、気軽に参加できます。\n");
            builder.addText("コミュニケーションは Facebook グループ中心です!是非参加してみてください。\n");


            printer.sendData(builder, 10000, status);
            builder.clearCommandBuffer();
            builder.addTextLang(Builder.MODEL_JAPANESE);

            builder.addText("━━━━━━━━━━━━━━━━━━━━━━━━\n");
            builder.addText("facebookコミュニティ\n");
            builder.addText("https://www.facebook.com/groups/antama/\n");
            builder.addSymbol("https://www.facebook.com/groups/antama/", Builder.SYMBOL_QRCODE_MODEL_2,
                    Builder.PARAM_UNSPECIFIED, Builder.PARAM_UNSPECIFIED,
                    Builder.PARAM_UNSPECIFIED, Builder.PARAM_UNSPECIFIED);

            builder.addText("google groupコミュニティ\n");
            builder.addText("https://sites.google.com/site/androidsaitama/\n");
            builder.addSymbol("https://sites.google.com/site/androidsaitama/", Builder.SYMBOL_QRCODE_MODEL_2,
                    Builder.PARAM_UNSPECIFIED, Builder.PARAM_UNSPECIFIED,
                    Builder.PARAM_UNSPECIFIED, Builder.PARAM_UNSPECIFIED);

            builder.addText("\n");

            builder.addCut(Builder.CUT_FEED);

            printer.sendData(builder, 10000, status);

            if ((status[0] & Print.ST_PRINT_SUCCESS) == Print.ST_PRINT_SUCCESS) {
                builder.clearCommandBuffer();
            }

            printer.closePrinter();
        } catch (EposException e) {
            int errStatus = e.getErrorStatus();
            android.util.Log.e(TAG, "errStatus = " + errStatus);

            e.printStackTrace();

            if (errStatus == EposException.ERR_PARAM) {

            }
        }

    }
}
