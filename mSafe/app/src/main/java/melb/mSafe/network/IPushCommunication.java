package melb.mSafe.network;

import java.util.Map;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

public interface IPushCommunication {

	void registerPushService(final Map<String, String> params,
                             Listener<String> successListener,
                             ErrorListener errorListener);

	void unregisterPushService(final Map<String, String> params,
                               Listener<String> successListener, ErrorListener errorListener
                             );

}
