package bp.uhk.arapp.ar;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.view.Surface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vlado on 11.03.2016.
 */
public class SensorProvider implements android.hardware.SensorEventListener {

    public static float COMPASS_CALIBRATION_TIMEOUT = 10 * 1000; //10 sekund, pak se přestane počítat kompas a zafixuje se

    private long startTime;
    private int counter = 0;

    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;
    private Sensor gameRotationVectorSensor;
    private Activity activity;

    List<SensorListener> sensorListeners = new ArrayList<>();

    private float[] rotationMatrix = new float [16];
    private float[] rotationMatrixFromVector = new float[16];

    private float[] yawMatrix = new float[16];
    private float[] yawMatrixFromVector = new float[16];

    private float orientedYaw = -99, yaw = -99, yawDiff = -99;
    private ArrayList<Float> yawDiffs = new ArrayList<>();

    private boolean compassCalibrated = false;

    public SensorProvider(Activity activity){

        this.activity = activity;

        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);

        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        gameRotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // It is good practice to check that we received the proper sensor event
        if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            float orientationVals[] = new float[3];
            // Convert the rotation-vector to a 4x4 matrix.
            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, event.values);

            remapCoordinateSystemAccordingToRotation(rotationMatrixFromVector, rotationMatrix);

            SensorManager.getOrientation(rotationMatrix, orientationVals);

            yaw = orientationVals[0];

            if (compassCalibrated) orientationVals[0] += yawDiff;

            notifyRotationChanged(orientationVals);
        }

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] justForYawVals = new float[3];

            SensorManager.getRotationMatrixFromVector(yawMatrixFromVector, event.values);

            remapCoordinateSystemAccordingToRotation(yawMatrixFromVector, yawMatrix);

            SensorManager.getOrientation(yawMatrix, justForYawVals);

            float elapsedTime = System.currentTimeMillis() - startTime;

            orientedYaw = justForYawVals[0];
            if (yaw != -99 && elapsedTime > COMPASS_CALIBRATION_TIMEOUT/5) yawDiffs.add(orientedYaw - yaw);

            if (elapsedTime > COMPASS_CALIBRATION_TIMEOUT){
                float sum = 0;
                for (float f : yawDiffs) sum += f;
                yawDiff = sum / yawDiffs.size();

                compassCalibrated = true;

                System.out.println("TOTO je DIFF: " + yawDiff + "TOTO je YAW: " + yaw + "TOTO je OrientedYAW: " + orientedYaw);

                sensorManager.unregisterListener(this, rotationVectorSensor);
            }
        }
    }

    private void remapCoordinateSystemAccordingToRotation(float[] matrixIn, float[] matrixOut) {

        int x = SensorManager.AXIS_X;
        int y = SensorManager.AXIS_Z;

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        switch (rotation) {
            case Surface.ROTATION_0: break;
            case Surface.ROTATION_90: x = SensorManager.AXIS_Z; y = SensorManager.AXIS_MINUS_X; break;
            case Surface.ROTATION_180: x = SensorManager.AXIS_MINUS_X; y = SensorManager.AXIS_MINUS_Z; break;
            case Surface.ROTATION_270: x = SensorManager.AXIS_MINUS_Z; y = SensorManager.AXIS_X; break;
        }

        SensorManager.remapCoordinateSystem(matrixIn, x, y, matrixOut);

    }

    private void notifyRotationChanged(float[] orientationVals) {
        for (SensorListener sensorListener : sensorListeners)   //notify listeners
            sensorListener.rotationChanged(orientationVals);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void start() {
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, gameRotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);

        startTime = System.currentTimeMillis();
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    public void addOrientationListener(SensorListener sensorListener) {
        sensorListeners.add(sensorListener);
    }
}
