package melb.mSafe.network;

import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;

public class CustomStringRequest extends StringRequest {

	private Map<String, String> parameter;

	public CustomStringRequest(int method, String url,
			Listener<String> listener, ErrorListener errorListener,
			Map<String, String> parameter) {
		super(method, url, listener, errorListener);
		this.parameter = parameter;
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		return parameter;
	}

}
