package bp.uhk.arapp.ar;

import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import bp.uhk.arapp.view.MainActivity;

/**
 * Created by vlado on 04.03.2016.
 */
public class WorldRenderer implements Renderer, SensorListener {

    public static float Z_FAR_VALUE = 700f;

    static final float LENSE_ANGLE = 35.0f;     //camera API hlasí getHorizontalViewAngle() 57.6 a getVerticalViewAngle() 43.2
    static final float ROLL_THRESHOLD = (float) Math.toRadians(30);
    static final float PI = 3.146f;
    static final float RAD2DEG = 180 / PI;

    static int smoothingFactor = 10;

    private float yaw = 0, pitch = 0, roll = 0;

    private float yawFix = 0;

    private boolean rollMode = true;

//    private boolean smoothMode = false;
//    private float[] yawHistory;
//    private long [] yawHistoryTimestamp;

    int counter = 0;

    private List<WorldObject> objects = new ArrayList<>();

    public WorldRenderer(SensorProvider sensorListener){
        sensorListener.addOrientationListener(this);

        objects.addAll(WorldObjectFactory.buildPoints(MainActivity.points));
        objects.addAll(WorldObjectFactory.buildMidPoints(MainActivity.midPoints));

//        if (smoothMode) {
//            yawHistory = new float[smoothingFactor + 1];
//            Arrays.fill(yawHistory, -999);
//            yawHistoryTimestamp = new long[smoothingFactor + 1];
//        }
    }
    
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
        // Depth buffer setup.
        gl.glClearDepthf(1.0f);
        // Enables depth testing.
        gl.glEnable(GL10.GL_DEPTH_TEST);
        // The type of depth testing to do.
        gl.glDepthFunc(GL10.GL_LEQUAL);
        // Really nice perspective calculations.
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // Sets the current view port to the new size.
        gl.glViewport(0, 0, width, height);
        // Select the projection matrix
        gl.glMatrixMode(GL10.GL_PROJECTION);
        // Reset the projection matrix
        gl.glLoadIdentity();

        // Calculate the aspect ratio of the window
        float aspectRatio = (float) width / (float) height;

        GLU.gluPerspective(gl, LENSE_ANGLE, aspectRatio, 0.1f, Z_FAR_VALUE);
        // Select the modelview matrix
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        // Reset the modelview matrix
        gl.glLoadIdentity();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

//        float colorTint = getTiltColor();
//        gl.glClearColor(0, colorTint * 0.537f, colorTint * 0.482f, colorTint); //tint bude mít barvu jako primary color

        for (WorldObject o : objects) {

            if (rollMode) gl.glRotatef(roll * RAD2DEG, 0, 0, 1.0f);   //pořadí je důležité!
            gl.glRotatef(pitch * RAD2DEG, 1.0f, 0, 0);
            gl.glRotatef(yaw * RAD2DEG + yawFix, 0, 1.0f, 0);

            o.draw(gl);
        }

    }

    private float getTiltColor() {
        if (Math.abs(roll) > ROLL_THRESHOLD /2){
            return Math.min(Math.abs(roll)/ ROLL_THRESHOLD - 0.5f, 1);
        }
        return 0;
    }

    @Override
    public void rotationChanged(float[] orientationVals) {
        yaw = orientationVals[0];
        pitch = orientationVals[1];
        roll = orientationVals[2];

//        if (counter % 10 == 0) System.out.println(Math.toDegrees(yaw));
//        counter++;
    }


    public void setYawFix(float yawFix) {
        this.yawFix = yawFix;
    }

    public void setElevationFix(float elevationFix){
        for (WorldObject o : objects){
            o.getCoordinatesConverter().setElevationFix(elevationFix);
            o.refreshCoordinates();
        }
    }

//    private void calculateSmoothYaw(float[] orientationVals) {
//
//        float absRoll = Math.abs(roll);
//
//        int yawImportanceFactor = Math.round(smoothingFactor * absRoll / PI); // nejmenší hodnota nejdůležitější, největší nedůležitá
//
//        yawHistory[yawImportanceFactor] = orientationVals[0];
//        yawHistoryTimestamp[yawImportanceFactor] = System.currentTimeMillis();
//
//        float sumValue = 0;
//        int countOfRecords = 0;
//
//        for (int i = 0; i <= yawImportanceFactor; i++){
//            if (yawHistory[i] != -999 && System.currentTimeMillis() - yawHistoryTimestamp[i] < (smoothingFactor - i)*100){ //záznamy s větší přesností jsou brány v potaz delší dobu
//                sumValue += yawHistory[i];
//                countOfRecords++;
//            }
//        }
//
//        //System.out.println("sumValue: " + sumValue + " countOfRecords :" + countOfRecords + " yawImportanceFactor: " + yawImportanceFactor);
//
//        yaw = (2*yaw + sumValue / countOfRecords)/3; //take it slow
//    }

}
