package melb.mSafe;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.squareup.otto.Subscribe;

import java.util.Date;
import java.util.logging.Logger;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import melb.mSafe.common.ExtendedWay;
import melb.mSafe.common.TempRouteGraphNode;
import melb.mSafe.database.DatabaseHandler;
import melb.mSafe.events.AccessibilityChangedEvent;
import melb.mSafe.events.PositionChangedEvent;
import melb.mSafe.events.RouteChangedEvent;
import melb.mSafe.events.RouteGraphChangedEvent;
import melb.mSafe.events.RouteGraphModifiedEvent;
import melb.mSafe.events.UpdateRouteGraphEvent;
import melb.mSafe.model.Accessibility;
import melb.mSafe.model.RouteGraph;
import melb.mSafe.model.RouteGraphNode;
import melb.mSafe.model.Vector3D;
import melb.mSafe.model.Way;
import melb.mSafe.network.BuildingCommunication;
import melb.mSafe.network.RouteGraphCommunication;
import melb.mSafe.opengl.utilities.Helper;
import melb.mSafe.pathfinding.PathFindingUtilities;
import melb.mSafe.utilities.BusProvider;
import melb.mSafe.utilities.PrefUtilities;
import melb.mSafe.utilities.Toaster;

/**
 * Created by Daniel on 13.01.14.
 */
public class RouteGraphManager {
    private Vector3D userPosition;
    private RouteGraph routeGraph;
    private Activity context;
    private ExtendedWay lastWay;
    //private Crouton positionRequiredCrouton;

    private static final double scale = 0.04608246; //should be information from routegraph TODO
    /**
     * returns the distance in cm
     *
     */
    public static double getDistanceInM(double pixel) {
        return pixel * scale; // Distance in cm
    }

    public RouteGraphManager(Activity context){
        this.context = context;
        userPosition = PrefUtilities.getInstance().getLatestPosition();
    }

    @Subscribe
    public void onPositionChangedEvent(PositionChangedEvent event){
        this.userPosition = event.userPosition;
        if (userPosition != null){
    //        if (positionRequiredCrouton != null){
    //            Crouton.hide(positionRequiredCrouton);
    //        }
            PrefUtilities.getInstance().setLatestPosition(userPosition);
            updateWay();
        }
    }

    private void updateWay(){
        if (userPosition != null){
            RouteChangedEvent.RouteChangedType routeChangedType = RouteChangedEvent.RouteChangedType.NOTHING;
            boolean positionOnRouteGraph = false;

            if (routeGraph != null){
                positionOnRouteGraph = routeGraph.isOnRouteGraph(userPosition);
            }

            TempRouteGraphNode tempNode = null;
            //if position is not a node on the routegraph - create a temporary position
            if (!positionOnRouteGraph && lastWay != null){
                RouteGraphNode previousNode = lastWay.getPreviousNode();
                RouteGraphNode currentNode = lastWay.getCurrentNode();
                RouteGraphNode nextNode = lastWay.getNextNode();
                //check if way shorter than before -> user walks towards the exit
                if (nextNode != null && currentNode != null && Vector3D.getDistance(userPosition, nextNode.position) < Vector3D.getDistance(currentNode.position, nextNode.position)){
                    tempNode = new TempRouteGraphNode(userPosition, currentNode, nextNode);
                    //check if way to the previous-node is shorter than before -> user walks towards the previous node
                }else if (previousNode != null && currentNode != null && Vector3D.getDistance(userPosition, previousNode.position) < Vector3D.getDistance(currentNode.position, previousNode.position)){
                    tempNode = new TempRouteGraphNode(userPosition, previousNode, currentNode);
                }
                if (tempNode != null){
                    synchronized (routeGraph.getCollection()){
                        routeGraph.getCollection().add(tempNode);
                        //not necessary to remove edge between current/prev or current/next
                    }
                }
            }

            // estimate the nearest node as current position and calculates the way to an exit
            Way calculatedWay = calculateNewRoute(routeGraph);
            if (calculatedWay != null){

                //check if the result is a different way (not just a different "part" of the way
                if (lastWay == null || lastWay.way == null || !lastWay.way.isPartOfWay(calculatedWay)){
                    //if different, replace way
                    lastWay = new ExtendedWay(calculatedWay);
                    routeChangedType = RouteChangedEvent.RouteChangedType.NEW_WAY;
                }else{
                    //the way is the same, but the position on the way has changed, add this position to the way
                    if (tempNode != null){
                        //add temp node to way, remove all existing temporary nodes
                        routeChangedType = RouteChangedEvent.RouteChangedType.NEW_NODES;
                        lastWay.addTempNode(tempNode, true, true);
                    }else if (lastWay.getCurrentNode() != null && lastWay.getCurrentNode().position != null
                            && lastWay.getCurrentNode().position.equals(userPosition)
                            && routeChangedType == RouteChangedEvent.RouteChangedType.NOTHING){
                        //no changes at all
                        return;
                    }else{
                        //position already in way -> just updated
                        routeChangedType = RouteChangedEvent.RouteChangedType.NEW_POSITION;
                    }
                }
                lastWay.setPositionTo(userPosition);
                BusProvider.getInstance().post(new RouteChangedEvent(lastWay, routeChangedType));
            }else{
                //no way found (exit or no way available)
            }
        }
    }


    @Subscribe
    public void onRouteGraphModifiedEvent(RouteGraphModifiedEvent event){
        //server has updated some values of the routegraph
        RouteGraph routeGraphChanges = event.routeGraph;
        if (routeGraph != null)
        {
            routeGraph.updateRouteGraph(routeGraphChanges);
        }
        if (userPosition != null){
            updateWay();
        }else{
            //Configuration config = new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build();
            //positionRequiredCrouton = Crouton.makeText(context, "You need to scan your position!", Style.ALERT, R.id.crouton_position_container).setConfiguration(config);
            //positionRequiredCrouton.show();
        }
    }


    @Subscribe
    public void onRouteGraphChangedEvent(RouteGraphChangedEvent event){
        this.routeGraph = event.routeGraph;
        this.routeGraph.initIfNeeded();
        if (userPosition != null){
            updateWay();
        }else{
           // Configuration config = new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build();
            //positionRequiredCrouton = Crouton.makeText(context, "You need to scan your position!", Style.ALERT, R.id.crouton_position_container).setConfiguration(config);
            //positionRequiredCrouton.show();
        }
    }


    @Subscribe
    public void updateRouteGraphEvent(final UpdateRouteGraphEvent event){
        final long lastUpdate = PrefUtilities.getInstance().getLastRouteGraphUpdate();
        if (lastUpdate < event.time){
            new RouteGraphCommunication().getRouteGraphChanges(lastUpdate, new Response.Listener<RouteGraph>() {
                        @Override
                        public void onResponse(RouteGraph graph) {
                            BusProvider.getInstance().post(new RouteGraphModifiedEvent(graph));
                            PrefUtilities.getInstance().setLastRouteGraphUpdate(event.time);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.e("Error", "Volley-Error");
                        }
                    });
        }
    }

    @Subscribe
    public void onAccessibilityChangedEvent(AccessibilityChangedEvent event){
        boolean updateRequired = false;

        if (event.accessibility != null){
            boolean accessibilityChanged = PrefUtilities.getInstance().setAccessibility(event.accessibility);
            if (accessibilityChanged){
                updateRequired = true;
            }
        }
        if (event.stairsAllowed != null){
            boolean stairsChanged =  PrefUtilities.getInstance().setUseStairs(event.stairsAllowed);
            if (stairsChanged){
                updateRequired = true;
            }
        }
        if (event.elevatorAllowed != null){
            boolean elevatorChanged =  PrefUtilities.getInstance().setUseElevator(event.elevatorAllowed);
            if (elevatorChanged){
                updateRequired = true;
            }
        }

        if (updateRequired){
            updateWay();
        }
    }

    public void onResume(){
        BusProvider.getInstance().register(this);
        new LoadRouteGraphAsyncTask().execute();
    }

    public void onPause(){
        BusProvider.getInstance().unregister(this);
    }

    public ExtendedWay getLastWay() {
        return lastWay;
    }

    private class LoadRouteGraphAsyncTask extends AsyncTask<Void, Void, RouteGraph> {

        @Override
        protected RouteGraph doInBackground(Void... params) {
            RouteGraph graph =  DatabaseHandler.getInstance().getLatestRouteGraph();
            if (graph == null){
                try {
                    Log.d("RouteGraphManager", "Try to create RouteGraph from RAW");
                    graph = new Gson().fromJson(Helper.getStringFromRaw(
                            context, R.raw.building_graph_short), RouteGraph.class);
                    if (graph != null){
                        graph.initIfNeeded();
                        DatabaseHandler.getInstance().addRouteGraph(graph);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return graph;
        }

        @Override
        protected void onPostExecute(RouteGraph graph) {
            if (graph != null){
                onRouteGraphLoaded(graph);
            }else{
                /*
                 * if routegraph not in database or raw-file  try to load from network
                 */
                loadRouteGraphFromNetwork();
            }
        }
    }

    private void loadRouteGraphFromNetwork(){
        Log.d("RouteGraphManager", "Try to load RouteGraph from Network");
        new RouteGraphCommunication().getRouteGraph(
                new Response.Listener<RouteGraph>() {
                    @Override
                    public void onResponse(RouteGraph routeGraph) {
                        DatabaseHandler.getInstance().addRouteGraph(routeGraph);
                        onRouteGraphLoaded(routeGraph);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toaster.toast(volleyError.getMessage()+ " Volleyerror");
                    }
                }
        );
    }

    /**
     * RouteGraph loaded - initializing, calculating of routes and post to other listener
     * @param graph
     */
    private void onRouteGraphLoaded(RouteGraph graph){
        BusProvider.getInstance().post(new RouteGraphChangedEvent(graph));
    }

    private Way calculateNewRoute(RouteGraph routeGraph) {
        if (userPosition == null || routeGraph == null){
            return null;
        }
        float distance = Float.MAX_VALUE; //select shortest distance //TODO or ID
        RouteGraphNode curNode = null;
        for(RouteGraphNode node : routeGraph.getCollection()){
            if (node.position != null){
                float tempDistance = Vector3D.getDistance(node.position, userPosition);
                if (tempDistance < distance){
                    distance = tempDistance;
                    curNode = node;
                }
            }
        }
        if (curNode != null){
            Accessibility access = PrefUtilities.getInstance().getAccessibility();
            boolean elevatorAllowed = PrefUtilities.getInstance().useElevators();
            boolean stairsAllowed = PrefUtilities.getInstance().useStairs();

            return PathFindingUtilities.findWayToExit(curNode, routeGraph, access, elevatorAllowed
                    , stairsAllowed);
        }
        return null;
    }

}
