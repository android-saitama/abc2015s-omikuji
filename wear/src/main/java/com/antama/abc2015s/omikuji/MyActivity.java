package com.antama.abc2015s.omikuji;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

public class MyActivity extends Activity {

    public static final String TAG = "MA";

    private TextView mTextView;
    private BroadcastReceiver mSignificantAction = new SignificantAction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Intent intent = new Intent(this, MainService.class);
        startService(intent);
        LocalBroadcastManager.getInstance(this).registerReceiver(mSignificantAction, new IntentFilter(MainService.ACTION_STAGE_2));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mSignificantAction);
    }

    public class SignificantAction extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final float norm = intent.getFloatExtra(MainService.EXTRA_NORM, 0.0f);
            final float[] v = intent.getFloatArrayExtra(MainService.EXTRA_ROTATION);

            mTextView.setText(String.format("[%f] %f, %f, %f", norm, v[0], v[1], v[2]));
        }
    }
}
