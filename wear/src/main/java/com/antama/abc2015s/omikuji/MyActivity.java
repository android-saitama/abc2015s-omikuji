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

    private TensionMeterAction mTensionMeterAction = new TensionMeterAction();

    public static Intent notifyTension(final float t) {
        final Intent i = new Intent(TensionMeterAction.ACTION);
        i.putExtra(TensionMeterAction.KEY_TENSION, t);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTensionMeterAction.register();

        startService(MainService.call(this));
    }

    @Override
    protected void onPause() {
        super.onPause();

        mTensionMeterAction.unregister();
    }

    private class TensionMeterAction extends BroadcastReceiver {
        private static final String ACTION = "tension";
        private static final String KEY_TENSION = "tension";

        public void register() {
            LocalBroadcastManager.getInstance(MyActivity.this).registerReceiver(this, new IntentFilter(ACTION));
        }

        public void unregister() {
            LocalBroadcastManager.getInstance(MyActivity.this).unregisterReceiver(this);
        }

        @Override
        public void onReceive(final Context c, final Intent data) {

        }
    }

}
