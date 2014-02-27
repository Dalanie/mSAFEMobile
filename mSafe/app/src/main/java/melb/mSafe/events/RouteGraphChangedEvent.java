package melb.mSafe.events;

import melb.mSafe.model.RouteGraph;

/**
 * Created by Daniel on 26.12.13.
 */
public class RouteGraphChangedEvent {
    public RouteGraph routeGraph;

    public RouteGraphChangedEvent(RouteGraph routeGraph) {
        this.routeGraph = routeGraph;
    }
}
