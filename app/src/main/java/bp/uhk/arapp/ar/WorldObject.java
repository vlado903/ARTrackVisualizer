package bp.uhk.arapp.ar;

import android.location.Location;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import bp.uhk.arapp.geo.CoordinatesConverter;
import bp.uhk.arapp.view.MainActivity;

/**
 * Created by vlado on 04.03.2016.
 */
public class WorldObject
{

    public float x, y, z;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mColorBuffer;
    private ByteBuffer mIndexBuffer;
    private Location userLocation;
    private CoordinatesConverter coordinatesConverter;
    private double lastLatitude;
    private float rotation = 0;

    private float vertices[] = {
            1, 3, -1, -1, 3, -1,
            1, 3, 1, -1, 3, 1,
            0, 0, 0
    };
    private float colors[] = {
            0.5f, 0.5f, 0.5f, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 0.5f, 0.5f, 0.5f, 1,
            0.5f, 0.5f, 0.5f, 1
    };

    private byte indices[] = {
            0, 1, 2, 1, 2, 3,
            0, 2, 4, 2, 3, 4,
            1, 3, 4, 0, 1, 4
    };

    public WorldObject(Location objectLocation, boolean midpoint)
    {

        if (midpoint)
        {
            for (int i = 0; i < vertices.length; i++) vertices[i] = vertices[i] / 2;
        }

        userLocation = MainActivity.userLocation;

        coordinatesConverter = new CoordinatesConverter(objectLocation, userLocation);

        ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuf.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(colors.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mColorBuffer = byteBuf.asFloatBuffer();
        mColorBuffer.put(colors);
        mColorBuffer.position(0);

        mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
        mIndexBuffer.put(indices);
        mIndexBuffer.position(0);

    }

    public void draw(GL10 gl)
    {

        if (lastLatitude != userLocation.getLatitude())
        {

            coordinatesConverter.setUserLocation(userLocation);

            loadCoordinates(coordinatesConverter.getCoordinates());

            lastLatitude = userLocation.getLatitude();
        }

        gl.glTranslatef(x, z, -y);

        gl.glRotatef(rotation, 0, 1, 0); //rotuj objekt kolem osy y
        rotation += 0.5f;

        render(gl);

        gl.glLoadIdentity();
    }

    private void render(GL10 gl)
    {
        //vykreslovat přední stranu ploch
        gl.glFrontFace(GL10.GL_CW);

        //každý 3 hodnoty distAlt vertexBufferu float určují vrchol
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);

        //každý 4 hodnoty distAlt colorBufferu float určují RGBA kanál
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        gl.glDrawElements(GL10.GL_TRIANGLES, 18, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    }

    private void loadCoordinates(float[] coordinatesArray)
    {
        x = coordinatesArray[0];      //plusové hodnoty směřují po ose x doprava
        y = coordinatesArray[1];      //plusové hodnoty směřují po ose y nahoru
        z = coordinatesArray[2];      //nadmořská výška + opravná položka
    }

    public CoordinatesConverter getCoordinatesConverter()
    {
        return coordinatesConverter;
    }

    public void refreshCoordinates()
    {
        coordinatesConverter.setUserLocation(userLocation);
        loadCoordinates(coordinatesConverter.getCoordinates());
        lastLatitude = userLocation.getLatitude();
    }
}
