package bp.uhk.arapp.ar;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vlado on 11.03.2016.
 */
public class SensorProvider implements android.hardware.SensorEventListener {

    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;

    List<SensorListener> sensorListeners = new ArrayList<>();


    public SensorProvider(Activity activity){
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrix = new float[16];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

            float[] rotationMatrixRemapped = new float[16];
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, rotationMatrixRemapped);

            notifyRotationChanged(rotationMatrixRemapped);
        }
    }

    private void notifyRotationChanged(float[] rotationMatrix) {
        for (SensorListener sensorListener : sensorListeners)   //notify listeners
            sensorListener.rotationChanged(rotationMatrix);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void start() {
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    public void addOrientationListener(SensorListener sensorListener) {
        sensorListeners.add(sensorListener);
    }
}
