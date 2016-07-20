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

    static final float LENSE_ANGLE = 43.2f;     //camera API hlasí getHorizontalViewAngle() 57.6 a getVerticalViewAngle() 43.2

    private float[] rotationMatrix = new float[16];
    private float yawFix = 0;

    private List<WorldObject> objects = new ArrayList<>();

    public WorldRenderer(SensorProvider sensorProvider){
        sensorProvider.addOrientationListener(this);

        objects.addAll(WorldObjectFactory.buildPoints(MainActivity.points));
        objects.addAll(WorldObjectFactory.buildMidPoints(MainActivity.midPoints));

        rotationMatrix[0] = 1;
        rotationMatrix[5] = 1;
        rotationMatrix[10] = 1;
        rotationMatrix[15] = 1;

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

        for (WorldObject o : objects) {

            gl.glMultMatrixf(rotationMatrix, 0);
            gl.glRotatef(90, 1, 0, 0);              //korekce quaternionů, nemám tušení proč jsem to musel udělat

            gl.glRotatef(yawFix, 0, 1.0f, 0);

            o.draw(gl);
        }

    }

    @Override
    public void rotationChanged(float[] rotationMatrix) {
        this.rotationMatrix = rotationMatrix;
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

}
