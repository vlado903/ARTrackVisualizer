/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bp.uhk.arapp.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.SizeF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import bp.uhk.arapp.R;
import bp.uhk.arapp.ar.SensorProvider;
import bp.uhk.arapp.ar.WorldRenderer;
import bp.uhk.arapp.view.util.LocaleHelper;
import bp.uhk.arapp.view.util.ScrollGestureDetector;
import bp.uhk.arapp.view.util.ScrollGestureListener;

/**
 * Wrapper activity demonstrating the use of the new
 * {@link SensorEvent#values rotation vector sensor}
 * ({@link Sensor#TYPE_ROTATION_VECTOR TYPE_ROTATION_VECTOR}).
 *
 * @see Sensor
 * @see SensorEvent
 * @see SensorManager
 */
public class ARActivity extends Activity implements ScrollGestureListener
{

    static final float GESTURE_FIX_SPEED = 0.1f;

    private GLSurfaceView glSurfaceView;
    private WorldRenderer renderer;
    private SensorProvider sensorProvider;

    private TextView fixTextView;
    private long lastFixTextViewUpdate;

    private ScrollGestureDetector gestureDetector;

    private float yawFix = 0, elevationFix = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        LocaleHelper.onCreate(this);

        setContentView(R.layout.activity_ar);
        hideStatusBar();

        // Vytvoř SensorProvider a dodej mu kontext
        sensorProvider = new SensorProvider(this);
        sensorProvider.start();

        fixTextView = (TextView) findViewById(R.id.tv_fix);

        renderer = new WorldRenderer(sensorProvider);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            float fov = calculateFOVCameraAPI2(cameraManager);
            if (fov > 30 && fov < 60) renderer.setFov(fov);
        }

        glSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setZOrderOnTop(true);

        gestureDetector = new ScrollGestureDetector(this, this);
    }

    @Override
    protected void onResume()
    {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();

        hideStatusBar();

        glSurfaceView.onResume();
    }

    @Override
    protected void onPause()
    {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        sensorProvider.stop();
        glSurfaceView.onPause();
    }

    private void hideStatusBar()
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
        {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void onFlingUp()
    {
        updateElevationFix(+GESTURE_FIX_SPEED);
    }

    @Override
    public void onFlingDown()
    {
        updateElevationFix(-GESTURE_FIX_SPEED);
    }

    @Override
    public void onFlingLeft()
    {
        updateYawFix(GESTURE_FIX_SPEED);
    }

    @Override
    public void onFlingRight()
    {
        updateYawFix(-GESTURE_FIX_SPEED);
    }

    private void updateYawFix(float fixStep)
    {
        yawFix += fixStep;
        int roundYawFix = Math.round(yawFix);

        if (roundYawFix > 0)
        {
            setAndShowTextView("-" + roundYawFix + "°");
        }
        else
        {
            setAndShowTextView("+" + Math.abs(roundYawFix) + "°");
        }
        renderer.setYawFix(roundYawFix);
    }

    private void updateElevationFix(float fixStep)
    {
        elevationFix += fixStep;
        int roundElevationFix = Math.round(elevationFix);

        if (roundElevationFix > 0)
        {
            setAndShowTextView("+" + roundElevationFix + " m");
        }
        else
        {
            setAndShowTextView(roundElevationFix + " m");
        }
        renderer.setElevationFix(elevationFix);
    }

    private void setAndShowTextView(String text)
    {
        fixTextView.setText(text);
        fixTextView.setVisibility(View.VISIBLE);

        showTextViewForWhile();
    }

    private void showTextViewForWhile()
    {

        lastFixTextViewUpdate = System.currentTimeMillis();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            public void run()
            {
                if (System.currentTimeMillis() - lastFixTextViewUpdate >= 1000)
                    fixTextView.setVisibility(View.INVISIBLE);
            }
        }, 1000);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private float calculateFOVCameraAPI2(CameraManager cManager)
    {
        float verticalAngle = 0;
        try
        {
            for (final String cameraId : cManager.getCameraIdList())
            {
                CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CameraCharacteristics.LENS_FACING_BACK)
                {
                    float[] maxFocus = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                    SizeF size = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                    float w = size.getWidth();
                    float h = size.getHeight();
                    verticalAngle = (float) Math.toDegrees(2 * Math.atan(h / (maxFocus[0] * 2)));
                    // horizontalAngle = (float) Math.toDegrees(2 * Math.atan(w / (maxFocus[0] * 2)));
                }
            }
        }
        catch (CameraAccessException e)
        {
            e.printStackTrace();
        }
        return verticalAngle;
    }

}

