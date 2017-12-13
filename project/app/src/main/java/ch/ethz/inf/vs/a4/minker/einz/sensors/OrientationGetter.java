package ch.ethz.inf.vs.a4.minker.einz.sensors;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;

import ch.ethz.inf.vs.a4.minker.einz.client.ClientMessengerCallback;
import ch.ethz.inf.vs.a4.minker.einz.client.EinzClient;

import static java.lang.Thread.sleep;

/**
 * Created by Chris on 12.12.2017.
 */

public class OrientationGetter implements SensorEventListener {
    private static SensorManager sensorMgr;
    private static Sensor orientationSensor;

    private static ArrayList<Double> values = new ArrayList<>();

    boolean gotFirstValue = false;

    public OrientationGetter(Context context){
        sensorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        orientationSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }

    public void startSensor(){
        sensorMgr.registerListener(this,orientationSensor,SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void stopSensor(){
        sensorMgr.unregisterListener(this);
    }

    public void getOrientation(final EinzClient messengerCallback){
        startSensor();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(values.size() < 10);
                double sumOfSin = 0;
                double sumOfCos = 0;

                for(double currVal:values){
                    sumOfSin += Math.sin(Math.toRadians(currVal));
                    sumOfCos += Math.cos(Math.toRadians(currVal));
                }

                stopSensor();
                messengerCallback.setOrientation(Math.atan2(sumOfSin,sumOfCos));
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(gotFirstValue) {
            double rotZ = sensorEvent.values[2];
            values.add(rotZ);
        }
        gotFirstValue = true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //--
    }
}
