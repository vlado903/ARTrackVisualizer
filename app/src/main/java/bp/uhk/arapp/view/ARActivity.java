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

import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import bp.uhk.arapp.R;
import bp.uhk.arapp.ar.SensorProvider;
import bp.uhk.arapp.ar.WorldRenderer;
import bp.uhk.arapp.view.util.CountDown;
import bp.uhk.arapp.view.util.CountDownCallback;
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
 *
 */
public class ARActivity extends Activity implements CountDownCallback, ScrollGestureListener {

    static final float GESTURE_FIX_SPEED = 0.1f;

    private GLSurfaceView glSurfaceView;
    private WorldRenderer renderer;
    private SensorProvider sensorProvider;

    private TextView countdownTextView, holdSteadyTextView, fixTextView;
    private Button calibrateButton;
    private long lastFixTextViewUpdate;

    private ScrollGestureDetector gestureDetector;

    private float yawFix = 0, elevationFix = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocaleHelper.onCreate(this);

        setContentView(R.layout.activity_ar);
        hideStatusBar();

        // Vytvoř SensorProvider a dodej mu kontext
        sensorProvider = new SensorProvider(this);

        countdownTextView = (TextView) findViewById(R.id.tv_countdown);
        holdSteadyTextView = (TextView) findViewById(R.id.tv_hold_steady);
        fixTextView = (TextView) findViewById(R.id.tv_fix);
        calibrateButton = (Button) findViewById(R.id.button_startCalibration);

        renderer = new WorldRenderer(sensorProvider);
        glSurfaceView = (GLSurfaceView)findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setZOrderOnTop(true);

        gestureDetector = new ScrollGestureDetector(this, this);

        fixTextView.setVisibility(View.INVISIBLE);

        showCalibrationUI();
    }

    public void startCalibration(View view){
        sensorProvider.start();
        calibrateButton.setVisibility(View.INVISIBLE);
        startCalibrationCountdown();
    }

    private void showCalibrationUI(){
        glSurfaceView.setVisibility(View.INVISIBLE);

        countdownTextView.setText(Math.round(SensorProvider.COMPASS_CALIBRATION_TIMEOUT / 1000) + "");
        countdownTextView.setVisibility(View.VISIBLE);
        holdSteadyTextView.setVisibility(View.VISIBLE);
        calibrateButton.setVisibility(View.VISIBLE);
    }

    private void hideCalibrationUI(){
        countdownTextView.setVisibility(View.INVISIBLE);
        holdSteadyTextView.setVisibility(View.INVISIBLE);

        glSurfaceView.setVisibility(View.VISIBLE);
    }

    private void startCalibrationCountdown(){
        new CountDown((long) SensorProvider.COMPASS_CALIBRATION_TIMEOUT, 1000, this).start();
    }


    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();

        hideStatusBar();

        showCalibrationUI();

        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        sensorProvider.stop();
        glSurfaceView.onPause();
    }

    private void hideStatusBar(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public void countDownTick(int i) {
        countdownTextView.setText(i + "");
    }

    @Override
    public void countDownFinish() {
        hideCalibrationUI();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void onFlingUp() {
        updateElevationFix(+GESTURE_FIX_SPEED);
    }

    @Override
    public void onFlingDown() {
        updateElevationFix(-GESTURE_FIX_SPEED);
    }

    @Override
    public void onFlingLeft() {
        updateYawFix(GESTURE_FIX_SPEED);
    }

    @Override
    public void onFlingRight() {
        updateYawFix(-GESTURE_FIX_SPEED);
    }

    private void updateYawFix(float fixStep) {
        yawFix += fixStep;
        int roundYawFix = Math.round(yawFix);

        if (roundYawFix > 0){
            setAndShowTextView("+" + roundYawFix + "°");
        }else {
            setAndShowTextView(roundYawFix + "°");
        }
        renderer.setYawFix(roundYawFix);
    }

    private void updateElevationFix(float fixStep) {
        elevationFix += fixStep;
        int roundElevationFix = Math.round(elevationFix);

        if (roundElevationFix > 0){
            setAndShowTextView("+" + roundElevationFix + " m");
        }else {
            setAndShowTextView(roundElevationFix + " m");
        }
        renderer.setElevationFix(elevationFix);
    }

    private void setAndShowTextView(String text){
        fixTextView.setText(text);
        fixTextView.setVisibility(View.VISIBLE);

        showTextViewForWhile();
    }

    private void showTextViewForWhile(){

        lastFixTextViewUpdate = System.currentTimeMillis();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (System.currentTimeMillis() - lastFixTextViewUpdate >= 1000) fixTextView.setVisibility(View.INVISIBLE);
            }
        }, 1000);
    }
}

