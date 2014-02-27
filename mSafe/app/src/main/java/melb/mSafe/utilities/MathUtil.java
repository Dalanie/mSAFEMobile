package melb.mSafe.utilities;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedList;

import melb.mSafe.common.RotationPoint;


public class MathUtil {

	private static final int DEF_DIV_SCALE = 10;

	public static double add(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.add(b2).doubleValue();
	}

	public static double sub(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.subtract(b2).doubleValue();
	}

	public static double mul(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.multiply(b2).doubleValue();
	}

	public static double div(double v1, double v2) {
		return div(v1, v2, DEF_DIV_SCALE);
	}

	public static double div(double v1, double v2, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException(
					"The scale must be a positive integer or zero");
		}
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static double round(double v, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException(
					"The scale must be a positive integer or zero");
		}
		BigDecimal b = new BigDecimal(Double.toString(v));
		BigDecimal one = new BigDecimal("1");
		return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static RotationPoint movingAverage(
			LinkedList<RotationPoint> valueList) {
		synchronized (valueList) {
			if (valueList.isEmpty()) {
				return null;
			}
            float bearing = 0f;
			int m = 0;
			Iterator<RotationPoint> i = valueList.iterator();
			while (i.hasNext()) {
				RotationPoint point = i.next();
                bearing += point.bearing;
				m++;
			}

            bearing = (m != 0) ? bearing / m : bearing;

			return new RotationPoint(bearing);
		}
	}

	/**
	 * same as movingAverage but deals with angles, so 1� and 359� would result
	 * in 0� instead of 180�
	 * 
	 * @param valueList
	 * @return
	 */
	public static RotationPoint movingAverage2(
			LinkedList<RotationPoint> valueList) {
		synchronized (valueList) {
			if (valueList.isEmpty()) {
				return null;
			}
            float[] sumArray = new float[2];
			int m = 0;
			Iterator<RotationPoint> i = valueList.iterator();
			while (i.hasNext()) {
				RotationPoint point = i.next();
				sumArray[0] += Math.sin(Math.toRadians(point.bearing));
				sumArray[1] += Math.cos(Math.toRadians(point.bearing));
				m++;
			}

			/*
			 * avoid negative bearing
			 */
            float bearing = (float)(Math.toDegrees(Math.atan2(sumArray[0],
					sumArray[1])) + 360) % 360;

			return new RotationPoint(bearing);
		}
	}

	/**
	 * same as weightedMovingAverage but deals with angles, so 1� and 359� would
	 * result in 0� instead of 180�
	 * 
	 * @param valueList
	 * @return
	 */
	public static RotationPoint weightedMovingAverage(
			LinkedList<RotationPoint> valueList) {
		synchronized (valueList) {
			if (valueList.isEmpty()) {
				return null;
			}
            float[] sumArray = new float[2];
			int m = 0;
            float size = triangularNumber(valueList.size());
			Iterator<RotationPoint> i = valueList.iterator();
			while (i.hasNext()) {
				RotationPoint point = i.next();
				for (int j = 0; j < m + 1; j++) {
					sumArray[0] += Math.sin(Math.toRadians(point.bearing));
					sumArray[1] += Math.cos(Math.toRadians(point.bearing));
				}
				m++;
			}

			/*
			 * avoid negative bearing
			 */
            float bearing = (float)(Math.toDegrees(Math.atan2(sumArray[0],
					sumArray[1])) + 360) % 360;

			return new RotationPoint(bearing);
		}
	}

	/**
	 * Dreiecksnummern berechnen
	 * 
	 * @param n
	 * @return
	 */
	public static int triangularNumber(int n) {
		int fact = 1;
		for (int i = 1; i <= n; i++) {
			fact += i;
		}
		return fact;
	}

}
