package melb.mSafe.events;

import melb.mSafe.common.ExtendedWay;

/**
 * Created by Daniel on 26.12.13.
 */
public class RouteUpdateEvent {
    public ExtendedWay extendedWay;
    public RouteUpdateEvent(ExtendedWay way){
        this.extendedWay = way;
    }
}
