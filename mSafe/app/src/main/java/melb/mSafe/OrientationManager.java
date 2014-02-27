package melb.mSafe;

import java.util.Date;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Surface;

import melb.mSafe.common.RotationPoint;
import melb.mSafe.events.SensorChangedEvent;
import melb.mSafe.smoothing.ISmoothingSensorDataMethod;
import melb.mSafe.smoothing.LowPassSensorSmoothingMethod;
import melb.mSafe.utilities.BusProvider;
import melb.mSafe.utilities.LimitedLinkedList;

public class OrientationManager {
    /*
     * SensorManager and Listener
     */
    private SensorManager sensorManager;
    private MSensorListener sensorListener;
    private Sensor gravity;
    private Sensor magField;
    private ISmoothingSensorDataMethod smoothingSensorDataMethod;

    /*
     * Position data containers
     */
    public LinkedList<RotationPoint> orientationList = new LimitedLinkedList<RotationPoint>(
            Config.MAX_LIST_SIZE);
    private WakeLock wakeLock;
    private Activity context;

    private static final String TAG = OrientationManager.class.getSimpleName();
    private long lastUpdate;

    public OrientationManager(Activity context) {
        this.context = context;
        float smoothAlpha = 0.10f;
        smoothingSensorDataMethod = new LowPassSensorSmoothingMethod(smoothAlpha);
        initSensors();
    }

    /**
     * This method should be called, if the activity, which uses the
     * positionManager, is resumed
     */
    public void onResume() {
        orientationList.clear();
		/*
		 * Register the compass sensor listener
		 */
        sensorManager.registerListener(sensorListener, gravity,
                SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorListener, magField,
                SensorManager.SENSOR_DELAY_UI);

        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK,
                Config.SYS_WAKE_LOCK);
        wakeLock.acquire();
    }

    /**
     * This method should be called, if the activity, which uses the
     * positionManager, is paused
     */
    public void onPause() {
        sensorManager.unregisterListener(sensorListener);
        wakeLock.release();
    }

    public void initSensors() {
        if (sensorManager == null) {
			/*
			 * Initialize the Sensor Manager
			 */
            sensorManager = (SensorManager) context
                    .getSystemService(Context.SENSOR_SERVICE);
        }
		/*
		 * Initialize a SensorListener
		 */
        sensorListener = new MSensorListener();

		/*
		 * Initialize the Sensor Objects from SensorManager
		 */
        gravity = sensorManager
                .getDefaultSensor(Sensor.TYPE_GRAVITY);
        magField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    private static final float BEARING_DIFFERENCE_MELBOURNE = -85f; //TODO need to be loaded from the model!

    public void refreshSensorData(float bearing) {
            orientationList.add(new RotationPoint(bearing + BEARING_DIFFERENCE_MELBOURNE));
        long DELAY = 30;
        if (lastUpdate  + DELAY < new Date().getTime()){
            RotationPoint newOrientation = smoothingSensorDataMethod.getSmoothingOrientationData(orientationList);

            BusProvider.getInstance().post(new SensorChangedEvent(newOrientation));
            lastUpdate = new Date().getTime();
        }
    }

    private class MSensorListener implements SensorEventListener {
        /*
         * magnetic field sensor values
         */
        private float[] mGravity = new float[3];
        private float[] mMagnetic = new float[3];

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                mGravity = event.values.clone();
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mMagnetic  = event.values.clone();
            }

            if (mGravity != null && mMagnetic != null) {

            /* Create rotation Matrix */
                float[] rotationMatrix = new float[9];
                if (SensorManager.getRotationMatrix(rotationMatrix, null,
                        mGravity, mMagnetic)) {

                /* Compensate device orientation */
                    // http://android-developers.blogspot.de/2010/09/one-screen-turn-deserves-another.html
                    float[] remappedRotationMatrix = new float[9];
                    switch (context.getWindowManager().getDefaultDisplay()
                            .getRotation()) {
                        case Surface.ROTATION_0:
                            SensorManager.remapCoordinateSystem(rotationMatrix,
                                    SensorManager.AXIS_X, SensorManager.AXIS_Y,
                                    remappedRotationMatrix);
                            break;
                        case Surface.ROTATION_90:
                            SensorManager.remapCoordinateSystem(rotationMatrix,
                                    SensorManager.AXIS_Y,
                                    SensorManager.AXIS_MINUS_X,
                                    remappedRotationMatrix);
                            break;
                        case Surface.ROTATION_180:
                            SensorManager.remapCoordinateSystem(rotationMatrix,
                                    SensorManager.AXIS_MINUS_X,
                                    SensorManager.AXIS_MINUS_Y,
                                    remappedRotationMatrix);
                            break;
                        case Surface.ROTATION_270:
                            SensorManager.remapCoordinateSystem(rotationMatrix,
                                    SensorManager.AXIS_MINUS_Y,
                                    SensorManager.AXIS_X, remappedRotationMatrix);
                            break;
                    }

                /* Calculate Orientation */
                    float results[] = new float[3];
                    SensorManager.getOrientation(remappedRotationMatrix,
                            results);

                /* Get measured value */
                    float current_measured_bearing = (float) (results[0] * 180 / Math.PI);
                    if (current_measured_bearing < 0) {
                        current_measured_bearing += 360;
                    }

                    updateOrientation(current_measured_bearing);
                }
            }
        }

        private void updateOrientation(float bearing) {
            refreshSensorData(bearing);
        }


    }

}

