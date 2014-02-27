package melb.mSafe.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import melb.mSafe.model.RouteGraphEdge;
import melb.mSafe.model.RouteGraphNode;
import melb.mSafe.model.Vector3D;
import melb.mSafe.model.Way;

/**
 * Created by Daniel on 13.01.14.
 */
public class ExtendedWay {

    public Way way;
    public double distanceToNextStep;
    public double distanceToExit;
    public Vector3D currentPosition;
    public int lastVisitedNodeIndex; //Needed for "Backtracing"
    public int currentPositionIndex;
    public static final int EXIT = -42;

    public ExtendedWay(Way way, int lastVisitedNodeIndex, int currentPositionIndex, Vector3D currentPosition) throws Exception {
        this.way = way;
        this.lastVisitedNodeIndex = lastVisitedNodeIndex;
        recalculateCurrentDistances();
        this.currentPosition = currentPosition;
        this.currentPositionIndex = currentPositionIndex;
    }

    public void setCurrentPositionIndex(int currentPositionIndex){
        this.currentPositionIndex = currentPositionIndex;
        recalculateCurrentDistances();
    }

    public ExtendedWay(Way way) {
        this.way = way;
        this.lastVisitedNodeIndex = 0;
        distanceToExit = getDistance(0, EXIT);
        distanceToNextStep = getDistance(0, currentPositionIndex+1);
        if (way.getNodes() != null && !way.getNodes().isEmpty()){
            this.currentPosition = way.getNodes().get(0).position;
            this.currentPositionIndex = 0;
        }
    }

    public double getDistance(int position, int finalPosition) {
        int maxSize = way.getNodes().size();

        if (position < 0 || (finalPosition != EXIT && position > finalPosition)){
            position = 0;
        }
        if (finalPosition == EXIT || finalPosition > maxSize - 1 || finalPosition < position){
            finalPosition = maxSize - 1;
        }


        double tempLength = 0;
        for (int i = position; i < finalPosition; i++) {
            tempLength += Vector3D.getDistance(way.getNodes().get(i).position,
                    way.getNodes().get(i + 1).position);
        }
        return tempLength;
    }

    public void addTempNode(TempRouteGraphNode tempNode, boolean removeAllTempNodes, boolean newPosition){
        if (removeAllTempNodes){
            removeTempNodesFromGraph();
        }
        RouteGraphNode startNode = tempNode.from;
        RouteGraphNode endNode = tempNode.to;
        RouteGraphEdge via = getEdgeBetween(startNode, endNode);

        //save old link between from and to for later
        tempNode.setTempRouteGraphEdge(via);

        //remove edge from both sides
        startNode.getAdjacentEdges().remove(via);
        endNode.getAdjacentEdges().remove(via);

        //create to new edges
        RouteGraphEdge startToTempEdge = copyEdge(via);
        RouteGraphEdge tempToEndEdge = copyEdge(via);
        startToTempEdge.setPointA(startNode);
        startToTempEdge.setPointB(tempNode);
        tempToEndEdge.setPointA(endNode);
        tempToEndEdge.setPointB(tempNode);

        List<RouteGraphEdge> adjacentEdges = new ArrayList<RouteGraphEdge>();
        adjacentEdges.add(startToTempEdge);
        adjacentEdges.add(tempToEndEdge);

        //add edges to tempnode
        tempNode.setAdjacentEdges(adjacentEdges);

        //add single edge to to and from
        endNode.getAdjacentEdges().add(tempToEndEdge);
        startNode.getAdjacentEdges().add(startToTempEdge);

        //add new node between from and to
        synchronized (way.getNodes()){
            int fromIndex = way.getNodes().indexOf(startNode);
            int toIndex = way.getNodes().indexOf(endNode);
            int addIndex = -1;
            if (fromIndex > toIndex){
                addIndex = fromIndex;
            }else{
                addIndex = toIndex;
            }

            //insert is before the current index, so pick the "highest index" and insert it before this node
            //we get the result ... from,temp,to... (...from,to... before)
            way.getNodes().add(addIndex, tempNode);

            if (newPosition){
                currentPosition = tempNode.position;
                currentPositionIndex = addIndex;
            }
        }

    }

    public RouteGraphEdge copyEdge(RouteGraphEdge via){
        if (via == null){
            return null;
        }

        RouteGraphEdge edge = new RouteGraphEdge();
        edge.setAccessible(via.getAccessible());
        edge.setBlocked(via.isBlocked());
        edge.setEdgeType(via.getEdgeType());
        edge.setWeight(via.getWeight());
        edge.setSensors(via.getSensors());
        return edge;
    }

    public RouteGraphEdge getEdgeBetween(RouteGraphNode from, RouteGraphNode to) {
        for(RouteGraphEdge edge : from.getAdjacentEdges()){
            if (edge.getPointA().equals(from) || edge.getPointA().equals(to)){
                if (edge.getPointB().equals(from) || edge.getPointB().equals(to)){
                    return edge;
                }
            }
        }
        return null;
    }


    private void removeTempNodesFromGraph(){
        synchronized (way.getNodes()){
            for (Iterator<RouteGraphNode> it = way.getNodes().iterator(); it.hasNext();){
                RouteGraphNode curNode = it.next();
                if (curNode instanceof TempRouteGraphNode){
                    TempRouteGraphNode tempNode = (TempRouteGraphNode) curNode;

                    //restore edge
                    tempNode.from.getAdjacentEdges().add(tempNode.tempRouteGraphEdge);
                    tempNode.to.getAdjacentEdges().add(tempNode.tempRouteGraphEdge);

                    // remove old edges
                    RouteGraphEdge edgeFrom = getEdgeBetween(tempNode, tempNode.from);
                    RouteGraphEdge edgeTo = getEdgeBetween(tempNode, tempNode.to);
                    tempNode.from.getAdjacentEdges().remove(edgeFrom);
                    tempNode.to.getAdjacentEdges().remove(edgeTo);

                    it.remove();
                }
            }
        }
    }

    public RouteGraphNode getCurrentNode() {
        if (way.getNodes().size() > currentPositionIndex){
            return way.getNodes().get(currentPositionIndex);
        }
        return null;
    }

    public RouteGraphNode getNextNode(){
        if (way.getNodes().size() > currentPositionIndex + 1){
            return way.getNodes().get(currentPositionIndex+1);
        }else{
            return null;
        }
    }

    public RouteGraphNode getPreviousNode(){
        if ( currentPositionIndex - 1 >= 0){
            return way.getNodes().get(currentPositionIndex-1);
        }else{
            return null;
        }
    }

    public double getDistance(RouteGraphNode node, int offset) {
        if (way.getNodes() != null){
            int index = way.getNodes().indexOf(node);
            if (index > -1){
                if (offset > 0){
                    return getDistance(index, index+offset);
                }else{
                    return getDistance(index+offset, index);
                }
            }
        }
        return -1;
    }

    public RouteGraphNode getPreviousNode(RouteGraphNode pointA, RouteGraphNode pointB) {
        if (way.getNodes() != null){
            int indexA = way.getNodes().indexOf(pointA);
            int indexB = way.getNodes().indexOf(pointB);
            if (indexA > indexB){
                return pointB;
            }
        }
        return pointA;
    }

    public float getDirection(RouteGraphNode routeGraphNode) {
        if (routeGraphNode != null){
            int indexA = way.getNodes().indexOf(routeGraphNode);
            if (way.getNodes().size() > indexA+1 && indexA > 0){
                RouteGraphNode nextNode = way.getNodes().get(indexA+1);
                RouteGraphNode previousNode = way.getNodes().get(indexA-1);

                float anglePath1Radians = (float) Math.atan2(routeGraphNode.position.getY() - previousNode.position.getY(),
                        routeGraphNode.position.getX() - previousNode.position.getX());
                float anglePath2Radians = (float) Math.atan2(nextNode.position.getY() - routeGraphNode.position.getY(),
                        nextNode.position.getX() - routeGraphNode.position.getX());
                return  anglePath1Radians - anglePath2Radians;
            }
        }
        return 0;
    }

    public void setPositionTo(Vector3D userPosition) {
        if (way.getNodes() != null){
            for(int i = 0; i < way.getNodes().size(); i++){
                if (way.getNodes().get(i).position.equals(userPosition)){
                    lastVisitedNodeIndex = i;
                    currentPositionIndex = i;
                    currentPosition = userPosition;
                    recalculateCurrentDistances();
                    return;
                }
            }
        }
    }

    private void recalculateCurrentDistances(){
        distanceToExit = getDistance(currentPositionIndex, EXIT);
        distanceToNextStep = getDistance(currentPositionIndex, currentPositionIndex + 1);
    }

    public RouteGraphEdge getNextEdge(int nodeIndex){
        if (way.getNodes() != null){
            if (way.getNodes().size() > nodeIndex + 1){
                return getEdgeBetween(way.getNodes().get(nodeIndex), way.getNodes().get(nodeIndex+1));
            }
        }
        return null;
    }

    public List<Vector3D> getPointList(){
        List<Vector3D> pointList = new ArrayList<Vector3D>();
        if (way.getNodes() != null){
            for(RouteGraphNode node : way.getNodes()){
                pointList.add(node.position);
            }
        }
        return pointList;
    }

    public boolean isEdgeDown(RouteGraphEdge edge) {
        if (way.getNodes() != null){
            if (way.getNodes().indexOf(edge.getPointA()) > way.getNodes().indexOf(edge.getPointB())){
                return edge.getPointA().position.getZ() <= edge.getPointB().position.getZ();
            }else{
                return edge.getPointB().position.getZ() < edge.getPointA().position.getZ();
            }
        }
        return true;
    }
}
