package melb.mSafe.events;

import melb.mSafe.common.ExtendedWay;

/**
 * Created by Daniel on 26.12.13.
 */
public class RouteChangedEvent {
    public ExtendedWay extendedWay;
    public enum RouteChangedType {NEW_WAY, NEW_POSITION, NEW_NODES, NOTHING}
    public RouteChangedType routeChangedType;

    public RouteChangedEvent(ExtendedWay way, RouteChangedType type){
        this.extendedWay = way;
        this.routeChangedType = type;
    }
}
