package melb.mSafe.events;

import melb.mSafe.common.RotationPoint;

/**
 * Created by Daniel on 26.12.13.
 */
public class SensorChangedEvent {

    public RotationPoint userOrientation;

    public SensorChangedEvent(RotationPoint newOrientation) {
        this.userOrientation = newOrientation;
    }
}
