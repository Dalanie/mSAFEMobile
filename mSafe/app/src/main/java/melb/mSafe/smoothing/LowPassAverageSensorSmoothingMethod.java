package melb.mSafe.smoothing;

import java.util.LinkedList;

import melb.mSafe.common.RotationPoint;
import melb.mSafe.utilities.MathUtil;


public class LowPassAverageSensorSmoothingMethod extends
		BasicSmoothingSensorMethod {

	private float alpha;
	private RotationPoint lastPoint;

	public LowPassAverageSensorSmoothingMethod(float alpha) {
		this.alpha = alpha;
	}

	@Override
	public RotationPoint getSmoothingOrientationData(
			LinkedList<RotationPoint> rotationPointList) {
		RotationPoint point = MathUtil.movingAverage2(rotationPointList);
		if (lastPoint == null) {
			lastPoint = point;
		} else {
			lastPoint.bearing = lowPass(point.bearing, lastPoint.bearing, alpha);
		}
		return lastPoint;
	}

	@Override
	public String getName() {
		return "LP+AV";
	}

}
