package melb.mSafe.smoothing;

import java.util.LinkedList;

import melb.mSafe.common.RotationPoint;


public interface ISmoothingSensorDataMethod {

	String getName();
	
	RotationPoint getSmoothingOrientationData(
            LinkedList<RotationPoint> rotationPointList);

}
