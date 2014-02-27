package melb.mSafe.network;

import java.util.Map;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

public class PushCommunication extends AbstractNetworkCommunication
		implements IPushCommunication {
	
	private static final String REGISTER_PARAM = "/register";
	private static final String UNREGISTER_PARAM = "/unregister";

	@Override
	public void registerPushService(final Map<String, String> params,
			Listener<String> successListener,
			ErrorListener errorListener) {
		if (params != null) {
			Request<?> request = new CustomStringRequest(Method.POST,
					getUrlWithParams(REGISTER_PARAM), successListener,
					errorListener, params);
			addRequest(request);
		}
	}

	@Override
	public void unregisterPushService(final Map<String, String> params,
			Listener<String> successListener, ErrorListener errorListener) {
		if (params != null) {
			Request<?> request = new CustomStringRequest(Method.POST,
					getUrlWithParams(UNREGISTER_PARAM), successListener,
					errorListener, params);
			addRequest(request);
		}
	}

	@Override
	public String getResource() {
		return ""; // no ressource
	}

}
