package melb.mSafe.network;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import melb.mSafe.model.Model3D;
import melb.mSafe.model.RouteGraph;
import melb.mSafe.model.Vector3D;

public class RouteGraphCommunication extends AbstractNetworkCommunication
		implements IRouteGraphCommunication {

	private static final String ROUTEGRAPH_RESOURCE = "";
	private static final String RESOURCE_NAME = "routegraph";
    private static final String POSITION_KEY = "position";
    private static final String INFORMATION_KEY = "info";
    private static final String SEND_INFORMATION = "sendinfo";
    private static final String GET_INFORMATION = "getinfo";
    private static final String GRAPH = "graph";
    private static final String LAST_CHANGE = "lastChange";


	@Override
	public String getResource() {
		return RESOURCE_NAME;
	}


	@Override
	public void getRouteGraph(Listener<RouteGraph> successListener,
			ErrorListener errorListener) {
		Request<?> request = new GsonRequest<RouteGraph>(Method.POST,
				getUrlWithParams("", ROUTEGRAPH_RESOURCE), RouteGraph.class,
				successListener, errorListener);
		addRequest(request);
	}

    @Override
    public void getRouteGraphChanges(long lastUpdate, Listener<RouteGraph> successListener,
                                     ErrorListener errorListener) {
        Request<?> request = new GsonRequest<RouteGraph>(Method.POST,
                getUrlWithParams(GRAPH)+"?lastChange=" + lastUpdate, RouteGraph.class,
                successListener, errorListener);
        addRequest(request);
    }

    @Override
    public void getInformationByPositionId(long positionId, Listener<String> successListener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(POSITION_KEY, positionId + "");
        Request<?> request = new CustomStringRequest(Method.POST,
                getUrlWithParams(GET_INFORMATION), successListener, errorListener, map);
        addRequest(request);
    }

    @Override
    public void sendInformationFromPosition(Vector3D position, String information, Listener<String> successListener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(POSITION_KEY, new Gson().toJson(position));
        map.put(INFORMATION_KEY, information);
        Request<?> request = new CustomStringRequest(Method.POST,
                getUrlWithParams(SEND_INFORMATION), successListener, errorListener, map);
        addRequest(request);
    }


}
