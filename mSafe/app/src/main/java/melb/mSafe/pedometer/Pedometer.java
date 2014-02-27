/*
 *  Pedometer - Android App
 *  Copyright (C) 2009 Levente Bagi
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package melb.mSafe.pedometer;


import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import melb.mSafe.utilities.PrefUtilities;


public class Pedometer extends Fragment {
	private static final String TAG = "Pedometer";

    private int mStepValue;
    private float mDistanceValue;
    private float mSpeedValue;


    /**
     * True, when service is running.
     */
    private boolean mIsRunning;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "[ACTIVITY] onCreate");
        super.onCreate(savedInstanceState);

        mStepValue = 0;
    }
    
    @Override
    public void onStart() {
        Log.i(TAG, "[ACTIVITY] onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "[ACTIVITY] onResume");
        super.onResume();

        
        // Read from preferences if the service was running on the last onPause
        mIsRunning = PrefUtilities.getInstance().isServiceRunning();
        
        // Start the service if this is considered to be an application start (last onPause was long ago)
        if (!mIsRunning) {
            startStepService();
            bindStepService();
        }
        else if (mIsRunning) {
            bindStepService();
        }

        PrefUtilities.getInstance().clearServiceRunning();

        boolean mIsMetric = PrefUtilities.getInstance().isMetric();

        int mMaintain = PrefUtilities.getInstance().getMaintainOption();
        if (mMaintain == PrefUtilities.M_PACE) {
            float mMaintainInc = 5f;
             }
        else 
        if (mMaintain == PrefUtilities.M_SPEED) {
            float mMaintainInc = 0.1f;
        }

    }

    @Override
    public void onPause() {
        Log.i(TAG, "[ACTIVITY] onPause");
        if (mIsRunning) {
            unbindStepService();
        }
        boolean mQuitting = false;
        if (mQuitting) {
            PrefUtilities.getInstance().saveServiceRunningWithNullTimestamp(mIsRunning);
        }
        else {
            PrefUtilities.getInstance().saveServiceRunningWithTimestamp(mIsRunning);
        }

        super.onPause();
    }

    @Override
    public void onStop() {
        Log.i(TAG, "[ACTIVITY] onStop");
        super.onStop();
    }

    public void onDestroy() {
        Log.i(TAG, "[ACTIVITY] onDestroy");
        super.onDestroy();
    }
    
    protected void onRestart() {
        Log.i(TAG, "[ACTIVITY] onRestart");
        super.onDestroy();
    }


    private StepService mService;
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = ((StepService.StepBinder)service).getService();

            mService.registerCallback(mCallback);
            mService.reloadSettings();
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };
    

    private void startStepService() {
        if (! mIsRunning) {
            Log.i(TAG, "[SERVICE] Start");
            mIsRunning = true;
            getActivity().startService(new Intent(getActivity(),
                    StepService.class));
        }
    }
    
    private void bindStepService() {
        Log.i(TAG, "[SERVICE] Bind");
        getActivity().bindService(new Intent(getActivity(),
                StepService.class), mConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
    }

    private void unbindStepService() {
        Log.i(TAG, "[SERVICE] Unbind");
        getActivity().unbindService(mConnection);
    }
    
    private void stopStepService() {
        Log.i(TAG, "[SERVICE] Stop");
        if (mService != null) {
            Log.i(TAG, "[SERVICE] stopService");
            getActivity().stopService(new Intent(getActivity(),
                    StepService.class));
        }
        mIsRunning = false;
    }
    
    private void resetValues(boolean updateDisplay) {
        if (mService != null && mIsRunning) {
            mService.resetValues();                    
        }
        else {
            PrefUtilities.getInstance().setSteps(0);
            PrefUtilities.getInstance().setDistance(0);
            PrefUtilities.getInstance().setSpeed(0);
        }
    }
 
    // TODO: unite all into 1 type of message
    private StepService.ICallback mCallback = new StepService.ICallback() {
        public void stepsChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));
        }
        public void distanceChanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(DISTANCE_MSG, (int)(value*1000), 0));
        }
        public void speedChanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(SPEED_MSG, (int)(value*1000), 0));
        }
    };
    
    private static final int STEPS_MSG = 1;
    private static final int DISTANCE_MSG = 3;
    private static final int SPEED_MSG = 4;
    
    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case STEPS_MSG:
                    mStepValue = msg.arg1;
                    break;
                case DISTANCE_MSG:
                    mDistanceValue = ( msg.arg1) / 1000f;
                    break;
                case SPEED_MSG:
                    mSpeedValue = ( msg.arg1) / 1000f;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };
    

}