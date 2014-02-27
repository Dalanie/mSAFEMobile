package melb.mSafe.network;

import com.android.volley.Response;
import com.android.volley.Response.Listener;

import melb.mSafe.model.Model3D;
import melb.mSafe.model.RouteGraph;
import melb.mSafe.model.Vector3D;

public interface IRouteGraphCommunication {
	void getRouteGraph(Listener<RouteGraph> successListener,
                       Response.ErrorListener errorListener);

    void getRouteGraphChanges(long lastUpdate, Listener<RouteGraph> successListener,
                              Response.ErrorListener errorListener);

    void getInformationByPositionId(long positionId, Listener<String> successListener, Response.ErrorListener errorListener);
    void sendInformationFromPosition(Vector3D position, String information, Listener<String> successListener, Response.ErrorListener errorListener);
}
