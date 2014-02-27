package melb.mSafe.network;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;

import melb.mSafe.utilities.PrefUtilities;

public abstract class AbstractNetworkCommunication {

	private static final int BACKOFF_MULTIPLIER = 2;
	private static final int MAXIMUM_RETRIES = 5;
	private static final int DEFAULT_TIMEOUT_MS = 5000;

	private static final boolean DEFAULT_RETRY_POLICY_ENABLED = true;

	public static RetryPolicy defaultRetryPolicy = new DefaultRetryPolicy(
			DEFAULT_TIMEOUT_MS, MAXIMUM_RETRIES, BACKOFF_MULTIPLIER);

	public static void addRequest(Request<?> request) {
		if (DEFAULT_RETRY_POLICY_ENABLED) {
			request.setRetryPolicy(defaultRetryPolicy);
		}
		VolleySingleton.getInstance().getRequestQueue().add(request);
	}

	public String getUrlWithParams(String... params) {
		String url = PrefUtilities.getInstance().getServerURL() + "/" + getResource();
		for (String param : params) {
			if (!url.endsWith("/")) {
				url = url + "/";
			}
			url = url + param.replace("/", "");
		}
        //TODO validate url
		return url;
	}

	public abstract String getResource();
}
