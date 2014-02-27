package melb.mSafe.common;

import java.io.Serializable;

public class RotationPoint implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3693372738612942320L;

	public float bearing;

	public RotationPoint(float bearing) {
		this.bearing = bearing;
	}
	
}
