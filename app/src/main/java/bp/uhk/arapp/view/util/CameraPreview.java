package bp.uhk.arapp.view.util;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback
{

    private Activity activity;
    private SurfaceHolder surfaceHolder;
    private Camera camera;

    public CameraPreview(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);

        if (!isInEditMode())
        {
            this.activity = (Activity) context;
            surfaceHolder = getHolder();
            surfaceHolder.addCallback(this);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
    {
        camera.startPreview();
        adjustOrientation();
    }

    private void adjustOrientation()
    {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        int degrees = 0;
        switch (rotation)
        {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = (info.orientation - degrees + 360) % 360;

        camera.setDisplayOrientation(result);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        camera = Camera.open();
        try
        {
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
        }
        catch (IOException exception)
        {
            camera.release();
            camera = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0)
    {
        camera.stopPreview();
        camera.release();
        camera = null;
    }
}
