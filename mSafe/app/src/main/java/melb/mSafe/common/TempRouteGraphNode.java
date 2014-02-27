package melb.mSafe.common;

import java.util.ArrayList;
import java.util.List;

import melb.mSafe.model.RouteGraphEdge;
import melb.mSafe.model.RouteGraphNode;
import melb.mSafe.model.Vector3D;

/**
 * Created by Daniel on 14.01.14.
 */
public class TempRouteGraphNode extends RouteGraphNode{
    Vector3D position;
    RouteGraphNode from;
    RouteGraphNode to;
    RouteGraphEdge tempRouteGraphEdge;

    public TempRouteGraphNode(Vector3D position, RouteGraphNode from, RouteGraphNode to){
        this.position = position;
        this.from = from;
        this.to = to;
    }

    @Override
    public List<RouteGraphNode> getNeighbours() {
        List<RouteGraphNode> nodes = new ArrayList<RouteGraphNode>();
        if (from != null){
            nodes.add(from);
        }
        if (to != null){
            nodes.add(to);
        }
        return nodes;
    }

    public Vector3D getPosition() {
        return position;
    }

    public void setPosition(Vector3D position) {
        this.position = position;
    }

    public RouteGraphNode getFrom() {
        return from;
    }

    public void setFrom(RouteGraphNode from) {
        this.from = from;
    }

    public RouteGraphNode getTo() {
        return to;
    }

    public void setTo(RouteGraphNode to) {
        this.to = to;
    }

    public RouteGraphEdge getTempRouteGraphEdge() {
        return tempRouteGraphEdge;
    }

    public void setTempRouteGraphEdge(RouteGraphEdge tempRouteGraphEdge) {
        this.tempRouteGraphEdge = tempRouteGraphEdge;
    }


}
