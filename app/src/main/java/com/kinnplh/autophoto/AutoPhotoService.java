package com.kinnplh.autophoto;

import static com.kinnplh.autophoto.MainActivity.delta;
import static com.kinnplh.autophoto.MainActivity.th;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

public class AutoPhotoService /*extends AccessibilityService*/ {
    public static AutoPhotoService instance;
    private SensorManager sensorManager;
    private Sensor type_TYPE_LINEAR_ACCELERATION;

    /*@Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }*/


    MySensorEventListener ml = new MySensorEventListener();
    public AutoPhotoService(Context context){
        instance = this;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        type_TYPE_LINEAR_ACCELERATION = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(ml, type_TYPE_LINEAR_ACCELERATION, SensorManager.SENSOR_DELAY_FASTEST);
    }

    class MySensorEventListener implements SensorEventListener {
        long lastMoveTime;
        public MySensorEventListener(){
            lastMoveTime = System.currentTimeMillis();
        }
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            double accV = Math.sqrt(sensorEvent.values[0] * sensorEvent.values[0] +
                    sensorEvent.values[1] * sensorEvent.values[1] +
                    sensorEvent.values[2] * sensorEvent.values[2]);
            if(accV > th){
                lastMoveTime = System.currentTimeMillis();
            }
            if(accV <= th && lastMoveTime > 0 && System.currentTimeMillis() - lastMoveTime > delta){
                if(PhotoView.ins != null && PhotoView.ins.myCamera != null){
                    PhotoView.ins.myCamera.startPreview();
                    PhotoView.ins.getPreViewImage();
                    lastMoveTime = -1;
//                Toast.makeText(context, "拍照", Toast.LENGTH_SHORT).show();
                    PhotoView.ins.num += 1;
                    Log.i(PhotoView.TAG, "onSensorChanged: photo" + PhotoView.ins.num);
                }
            }
        };

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };
    /*@Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }
*/
    public void stop(){
        if(sensorManager != null && type_TYPE_LINEAR_ACCELERATION != null){
            sensorManager.unregisterListener(ml);
        }
        sensorManager = null;
        type_TYPE_LINEAR_ACCELERATION = null;
    }
}
