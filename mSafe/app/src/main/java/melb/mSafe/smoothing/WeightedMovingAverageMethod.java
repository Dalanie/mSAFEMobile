package melb.mSafe.smoothing;

import java.util.LinkedList;

import melb.mSafe.common.RotationPoint;
import melb.mSafe.utilities.MathUtil;


/**
 * based on http://fxtrade.oanda.com/lang/de/learn/forex-indicators/weighted-moving-average
 * @author Daniel Langerenken
 *
 */
public class WeightedMovingAverageMethod extends BasicSmoothingSensorMethod {

	@Override
	public String getName() {
		return "WMA";
	}

	@Override
	public RotationPoint getSmoothingOrientationData(
			LinkedList<RotationPoint> rotationPointList) {
		return MathUtil.weightedMovingAverage(rotationPointList);
	}

}
