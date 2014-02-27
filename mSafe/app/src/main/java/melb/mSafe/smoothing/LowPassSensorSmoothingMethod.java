package melb.mSafe.smoothing;

import java.util.LinkedList;

import melb.mSafe.common.RotationPoint;


/**
 * based on
 * http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
 */
public class LowPassSensorSmoothingMethod extends BasicSmoothingSensorMethod {

	private float alpha;
	private RotationPoint lastPoint;

	public LowPassSensorSmoothingMethod(float alpha) {
		this.alpha = alpha;
	}

	@Override
	public RotationPoint getSmoothingOrientationData(
			LinkedList<RotationPoint> rotationPointList) {
		if (!rotationPointList.isEmpty()) {
			RotationPoint point = rotationPointList.getLast();
			if (lastPoint == null) {
				lastPoint = point;
			} else {
				lastPoint.bearing = lowPass(point.bearing, lastPoint.bearing,
						alpha);
			}
		}
		return lastPoint;
	}

	@Override
	public String getName() {
		return "LowPass";
	}

}
