package melb.mSafe.events;

import melb.mSafe.model.RouteGraph;

/**
 * Created by Daniel on 26.12.13.
 */
public class RouteGraphModifiedEvent {
    public RouteGraph routeGraph;

    public RouteGraphModifiedEvent(RouteGraph routeGraph) {
        this.routeGraph = routeGraph;
    }
}
