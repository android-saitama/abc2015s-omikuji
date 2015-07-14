package com.antama.abc2015s.omikuji;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;


public class MyActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MA";

    private GoogleApiClient mClient;

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

                    testPrint();

                    Log.d(TAG, "bump!");
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


    public void testPrint(){
        int[] status = new int[1];
        status[0] = 0;

        try {
            Print printer = new Print(getApplicationContext());

            printer.openPrinter(Print.DEVTYPE_TCP, "192.168.21.93", Print.FALSE,
                    Print.PARAM_DEFAULT, Print.PARAM_DEFAULT);

//Builder クラスのインスタンスを初期化
            Builder builder = new Builder("TM-T70", Builder.MODEL_JAPANESE);
// 印刷ドキュメントの作成
            builder.addTextLang(Builder.LANG_JA);
            builder.addTextSmooth(Builder.TRUE);
            builder.addTextFont(Builder.FONT_A);
            builder.addTextSize(3, 3);
            builder.addText("Hello,\t");
            builder.addText("World!\n");
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

            if(errStatus == EposException.ERR_PARAM){

            }
        }

    }
}
