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

public class BuildingCommunication extends AbstractNetworkCommunication
		implements IBuildingCommunication {

	private static final String RESOURCE_NAME = "building";

	@Override
	public void getBuilding(Listener<Model3D> successListener,
			ErrorListener errorListener) {
		Request<?> request = new GsonRequest<Model3D>(Method.POST,
				getUrlWithParams(""), Model3D.class, successListener,
				errorListener);
		addRequest(request);
	}

	@Override
	public String getResource() {
		return RESOURCE_NAME;
	}

}
