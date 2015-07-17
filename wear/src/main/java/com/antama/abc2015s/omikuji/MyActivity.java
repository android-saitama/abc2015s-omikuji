package com.antama.abc2015s.omikuji;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

public class MyActivity extends Activity {

    public static final String TAG = "MA";

    private TensionMeterAction mTensionMeterAction = new TensionMeterAction();

    public static Intent notifyTripped() {
        final Intent i = new Intent(TensionMeterAction.ACTION_TRIPPED);
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
        private static final String ACTION_TRIPPED = "tripped";

        public void register() {
            LocalBroadcastManager.getInstance(MyActivity.this).registerReceiver(this, new IntentFilter(ACTION_TRIPPED));
        }

        public void unregister() {
            LocalBroadcastManager.getInstance(MyActivity.this).unregisterReceiver(this);
        }

        @Override
        public void onReceive(final Context c, final Intent data) {
            final Context a = MyActivity.this;
            startService(MainService.block(a));
            new AlertDialog.Builder(a)
                .setMessage("！！！")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        startService(MainService.unblock(a));
                    }
                })
                .create().show();
        }

    }

}
