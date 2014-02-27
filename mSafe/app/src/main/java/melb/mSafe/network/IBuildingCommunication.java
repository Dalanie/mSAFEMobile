package melb.mSafe.network;

import com.android.volley.Response;
import com.android.volley.Response.Listener;

import melb.mSafe.model.Model3D;
import melb.mSafe.model.RouteGraph;
import melb.mSafe.model.Vector3D;

public interface IBuildingCommunication {
	void getBuilding(Listener<Model3D> successListener,
                     Response.ErrorListener errorListener);
}
