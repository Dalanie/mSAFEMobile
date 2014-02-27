package melb.mSafe.network;

import java.util.HashMap;
import java.util.Map;


import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.google.zxing.common.StringUtils;

public class ChatCommunication extends AbstractNetworkCommunication
		implements IChatCommunication {

	private static final String MESSAGE_TEXT_KEY = "message";
	private static final String COUNT_KEY = "count";
	private static final String PAGE_KEY = "page";
	private static final String ID_KEY = "id";
	private static final String ADD_METHOD = "add";
	private static final String RESOURCE_NAME = "chat";

	@Override
	public void sendMessage(final String textToSend,
			Listener<String> successListener,
			ErrorListener errorListener) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(MESSAGE_TEXT_KEY, textToSend);
		if (textToSend != null && !textToSend.isEmpty()) {
			Request<?> request = new CustomStringRequest(Method.POST,
					getUrlWithParams(ADD_METHOD), successListener,
					errorListener, map);
			addRequest(request);
		}
	}

	@Override
	public void getMessages(Listener<String> successListener,
			ErrorListener errorListener, Integer count, Integer page) {
		Map<String, String> map = new HashMap<String, String>();
		if (count != null) {
			map.put(COUNT_KEY, count + "");
		}
		if (page != null) {
			map.put(PAGE_KEY, page + "");
		}
		Request<?> request = new CustomStringRequest(Method.POST,
				getUrlWithParams(""), successListener, errorListener, map);
		addRequest(request);
	}

	@Override
	public void getMessage(Listener<String> successListener,
			ErrorListener errorListener, Integer id) {
		Map<String, String> map = new HashMap<String, String>();
		if (id != null) {
			map.put(ID_KEY, id + "");
			Request<?> request = new CustomStringRequest(Method.POST,
					getUrlWithParams(""), successListener, errorListener, map);
			addRequest(request);
		}
	}

	@Override
	public String getResource() {
		return RESOURCE_NAME;
	}
}
