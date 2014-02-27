package melb.mSafe.smoothing;

import java.util.LinkedList;

import melb.mSafe.common.RotationPoint;
import melb.mSafe.utilities.MathUtil;


/**
 * based on
 * http://fxtrade.oanda.com/lang/de/learn/forex-indicators/weighted-moving
 * -average
 * 
 * @author Daniel Langerenken
 * 
 */
public class LowPassWeightedAverageMethod extends BasicSmoothingSensorMethod {

	private float alpha;
	private RotationPoint lastPoint;

	public LowPassWeightedAverageMethod(float alpha) {
		this.alpha = alpha;
	}

	@Override
	public String getName() {
		return "LP+WMA";
	}

	@Override
	public RotationPoint getSmoothingOrientationData(
			LinkedList<RotationPoint> rotationPointList) {
		RotationPoint point = MathUtil
				.weightedMovingAverage(rotationPointList);

		if (lastPoint == null) {
			lastPoint = point;
		} else {
			lastPoint.bearing = lowPass(point.bearing, lastPoint.bearing, alpha);
		}
		return lastPoint;
	}

}
