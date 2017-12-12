package ch.ethz.inf.vs.a4.minker.einz.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * Created by Chris on 12.12.2017.
 */

public class OrientationGetter implements SensorEventListener {

    double lastOrientation;
    public OrientationGetter(){

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
