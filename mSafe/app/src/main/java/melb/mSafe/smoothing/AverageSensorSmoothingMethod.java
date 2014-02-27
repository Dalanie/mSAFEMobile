package melb.mSafe.smoothing;

import java.util.LinkedList;

import melb.mSafe.common.RotationPoint;
import melb.mSafe.utilities.MathUtil;

public class AverageSensorSmoothingMethod extends BasicSmoothingSensorMethod {

	@Override
	public RotationPoint getSmoothingOrientationData(
			LinkedList<RotationPoint> rotationPointList) {
		return MathUtil.movingAverage2(rotationPointList);
	}

	@Override
	public String getName() {
		return "Average";
	}

}
