package melb.mSafe.events;

import melb.mSafe.model.Vector3D;

/**
 * Created by Daniel on 26.12.13.
 */
public class PositionChangedEvent {
    public Vector3D userPosition;

    public PositionChangedEvent(Vector3D userPosition) {
        this.userPosition = userPosition;
    }
}
