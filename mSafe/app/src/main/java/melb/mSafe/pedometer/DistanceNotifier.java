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


import melb.mSafe.utilities.PrefUtilities;

/**
 * Calculates and displays the distance walked.  
 * @author Levente Bagi
 */
public class DistanceNotifier implements StepListener {

    public interface Listener {
        public void valueChanged(float value);
        public void passValue();
    }
    private Listener mListener;
    
    float mDistance = 0;

    
    boolean mIsMetric;
    float mStepLength;

    public DistanceNotifier(Listener listener) {
        mListener = listener;
        reloadSettings();
    }
    public void setDistance(float distance) {
        mDistance = distance;
        notifyListener();
    }
    
    public void reloadSettings() {
        mIsMetric = PrefUtilities.getInstance().isMetric();
        mStepLength = PrefUtilities.getInstance().getStepLength();
        notifyListener();
    }
    
    public void onStep() {
        
        if (mIsMetric) {
            mDistance += (float)(// kilometers
                mStepLength // centimeters
                / 100000.0); // centimeters/kilometer
        }
        else {
            mDistance += (float)(// miles
                mStepLength // inches
                / 63360.0); // inches/mile
        }
        
        notifyListener();
    }
    
    private void notifyListener() {
        mListener.valueChanged(mDistance);
    }


}

