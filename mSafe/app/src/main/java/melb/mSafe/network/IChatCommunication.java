package melb.mSafe.network;


import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

public interface IChatCommunication {
	void sendMessage(final String textToSend, Listener<String> successListener,
                     ErrorListener errorListener
                     );

	void getMessages(Listener<String> successListener,
                     ErrorListener errorListener,
                     Integer count, Integer page);

	void getMessage(Listener<String> successListener,
                    ErrorListener errorListener, Integer id);
}
