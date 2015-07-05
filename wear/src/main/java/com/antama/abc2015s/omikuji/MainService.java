package com.antama.abc2015s.omikuji;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
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
    public static final String ACTION_SIGNIFICANT = "action_significant";
    public static final String EXTRA_NORM = "norm";
    public static final String EXTRA_ROTATION = "rotation";

    private SensorAction mSensorAction = new SensorAction();
    private RotationSensorAction mRotationSensorAction = new RotationSensorAction();

    public GoogleApiClient mClient;

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
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
        sensorManager.registerListener(mRotationSensorAction, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_UI);
    }

    private void unwatch() {
        final SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensorManager.unregisterListener(mSensorAction);
        sensorManager.unregisterListener(mRotationSensorAction);
    }

    private class SensorAction implements SensorEventListener {
        public float[] v = new float[3];

        @Override
        public void onSensorChanged(SensorEvent event) {
            v[0] = event.values[0];
            v[1] = event.values[1];
            v[2] = event.values[2];
            final float norm = (float)Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);
            if (norm > 20.0f) {
                final Intent intent = new Intent(ACTION_SIGNIFICANT);
                intent.putExtra(EXTRA_NORM, norm);
                intent.putExtra(EXTRA_ROTATION, mRotationSensorAction.v);
                LocalBroadcastManager.getInstance(MainService.this).sendBroadcast(intent);
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
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
    private class RotationSensorAction implements SensorEventListener {
        public float[] v = new float[3];
        @Override
        public void onSensorChanged(SensorEvent event) {
            v[0] = event.values[0];
            v[1] = event.values[1];
            v[2] = event.values[2];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}