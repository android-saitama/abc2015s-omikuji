package com.antama.abc2015s.omikuji;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "MS";

    private SensorAction mSensorAction = new SensorAction();
    private PeakCounter mCounter = new PeakCounter(this, 13, 3000); // 13 (~3 strokes) shakes in 3 seconds to trip

    private static final String ACTION_BLOCK = "block";
    private static final String ACTION_UNBLOCK = "unblock";

    private GoogleApiClient mClient;

    public static Intent call(final Context from) {
        return new Intent(from, MainService.class);
    }

    public static Intent block(final Context from) {
        final Intent i = new Intent(from, MainService.class);
        i.setAction(ACTION_BLOCK);
        return i;
    }

    public static Intent unblock(final Context from) {
        final Intent i = new Intent(from, MainService.class);
        i.setAction(ACTION_UNBLOCK);
        return i;
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String action = intent.getAction();
        if (action != null) {
            if (ACTION_BLOCK.equals(action)) {
                mCounter.block();
            } else if (ACTION_UNBLOCK.equals(action)) {
                mCounter.unblock();
            }
            return START_STICKY;
        } else {
            return START_STICKY;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        mClient.connect();
        watch();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mClient.disconnect();
        unwatch();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "api is ready");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "cannot connect to gms");
    }

    private void watch() {
        final SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        sensorManager.registerListener(mSensorAction, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
    }

    private void unwatch() {
        final SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensorManager.unregisterListener(mSensorAction);
    }

    private class SensorAction implements SensorEventListener {
        public float[] v = new float[3];
        private Handler mHandler = new Handler();

        @Override
        public void onSensorChanged(SensorEvent event) {
            v[0] = event.values[0];
            v[1] = event.values[1];
            v[2] = event.values[2];
            if (!mCounter.tick((float)Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]))) {
                if (mCounter.isTripped()) {
                    Log.d(TAG, String.format("stage2: [+] %f", mCounter.getWeightenedNorm()));
                    LocalBroadcastManager.getInstance(MainService.this).sendBroadcast(MyActivity.notifyTripped());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            triggerStage2();
                        }
                    });
                    mCounter.reset();
                }
            }
            mCounter.check();
        }

        private void triggerStage2() {
            Wearable.NodeApi.getConnectedNodes(mClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                    final Node host = getConnectedNodesResult.getNodes().get(0);
                    Wearable.MessageApi.sendMessage(mClient, host.getId(), "/event", "bump".getBytes()).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Log.d(TAG, "message sent");
                        }
                    });
                }
            });
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private static class PeakCounter {
        private Context mContext;
        private int mTripAt;
        private int mInterval;
        private long mCheckedAt;
        private boolean mBlocked = false;

        private int mCount = 0;
        private float mWeightenedNorm = 0.0f;
        private final float mWeightFactor = 0.5f;
        private final float mNormTheshold = 20.0f;

        public PeakCounter(final Context c, final int tripAt, final int interval) {
            mContext = c;
            mTripAt = tripAt;
            mInterval = interval;
        }

        public boolean isTripped() {
            return mCount >= mTripAt;
        }

        public void reset() {
            mCount = 0;
            mCheckedAt = 0;
        }

        public boolean tick(float norm) {
            if (!mBlocked) {
                mWeightenedNorm = (1-mWeightFactor)*mWeightenedNorm + mWeightFactor*norm;
                if (mWeightenedNorm > mNormTheshold) {
                    ++mCount;
                    Log.d(TAG, String.format("tick: [+] %d", mCount));
                    check();
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        public void check() {
            final long now = System.currentTimeMillis();
            if ((now - mCheckedAt) > mInterval) {
                try {
                    if (mCount > 0) {
                        Log.d(TAG, String.format("check: [-] %d", mCount));
                    }
                    mCount = 0;
                    return;
                } finally {
                    mCheckedAt = now;
                }
            }
        }

        public float getWeightenedNorm() {
            return mWeightenedNorm;
        }

        public float getTension() {
            return mCount / (float)mTripAt;
        }

        public void block() {
            mBlocked = true;
            reset();
        }

        public void unblock() {
            mBlocked = false;
        }
    }

}
