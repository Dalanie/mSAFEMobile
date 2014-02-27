/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package melb.mSafe.gcm;

import static melb.mSafe.utilities.LogUtils.*;

import java.util.HashMap;
import java.util.Map;

import melb.mSafe.Config;
import melb.mSafe.R;
import melb.mSafe.network.PushCommunication;
import melb.mSafe.utilities.CommonUtilities;
import melb.mSafe.utilities.PrefUtilities;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.google.android.gcm.GCMRegistrar;

/**
 * Helper class used to communicate with the server.
 */
public final class PushUtilities {


    private static final String TAG = makeLogTag("GCM");
    private static PushUtilities _instance;
    private Context context;
    public static void init(Context context) {
        _instance = new PushUtilities(context);
    }

    private PushUtilities(Context context){
        this.context = context;
    }

    public static PushUtilities getInstance(){
        return _instance;
    }

    /**
	 * Register this account/device pair within the server.
	 * 
	 */
	public void register(final String regId, final Listener<String> dialogSuccessListener, final ErrorListener dialogErrorListener) {
		LOGI(TAG, "registering device (regId = " + regId + ")");
		Map<String, String> params = new HashMap<String, String>();
		params.put("regId", regId);
		new PushCommunication().registerPushService(params,
				new Listener<String>() {

					@Override
					public void onResponse(String response) {
						GCMRegistrar.setRegisteredOnServer(context, true);
						String message = context
								.getString(R.string.server_registered);
						CommonUtilities.displayMessage(context, message);
                        PrefUtilities.getInstance().setRegisteredOnServer(true, regId);
                        if (dialogSuccessListener != null){
                            dialogSuccessListener.onResponse("success");
                        }
					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						// Here we are simplifying and retrying on
						// any error; in a
						// real
						// application, it should retry only on
						// unrecoverable errors
						// (like HTTP error code 503).
						LOGE(TAG, "Failed to register after several retries",
								error);
                        GCMRegistrar.unregister(context);
                        if (dialogErrorListener != null){
                            dialogErrorListener.onErrorResponse(null);
                        }
					}
				});
    }

	/**
	 * Unregister this account/device pair within the server.
	 */
	public void unregister(final String regId) {
		LOGI(TAG, "unregistering device (regId = " + regId + ")");
		Map<String, String> params = new HashMap<String, String>();
		params.put("regId", regId);
		new PushCommunication().unregisterPushService(params,
				new Listener<String>() {

					@Override
					public void onResponse(String response) {
						GCMRegistrar.setRegisteredOnServer(context, false);
						String message = context
								.getString(R.string.server_unregistered);
						CommonUtilities.displayMessage(context, message);
                        // Regardless of server success, clear local preferences
                        PrefUtilities.getInstance().setRegisteredOnServer(false, null);
					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						// At this point the device is unregistered from GCM,
						// but still
						// registered in the server.
						// We could try to unregister again, but it is not
						// necessary:
						// if the server tries to send a message to the device,
						// it will get
						// a "NotRegistered" error message and should unregister
						// the device.
						String message = context.getString(
								R.string.server_unregister_error,
								error.getMessage());
						CommonUtilities.displayMessage(context, message);
                        // Regardless of server success, clear local preferences
                        PrefUtilities.getInstance().setRegisteredOnServer(false, null);
					}
				});
	}



    /**
     *  Unregister the current GCM ID when we sign-out
     *
     */
    public void onSignOut() {
        String gcmId = PrefUtilities.getInstance().getGcmId();
        if (gcmId != null) {
            unregister(gcmId);
        }
    }

    public void relog() {
        PrefUtilities.getInstance().setRegisteredOnServer(false, "");
        registerGCMClient(null, null);
    }

    public void relog(Listener<String> listener, ErrorListener errorListener) {
        PrefUtilities.getInstance().setRegisteredOnServer(false, "");
        registerGCMClient(listener, errorListener);
    }

    public void registerGCMClient(Listener<String> listener, ErrorListener errorListener) {
        GCMRegistrar.checkDevice(context);
        GCMRegistrar.checkManifest(context);

        final String regId = GCMRegistrar.getRegistrationId(context);

        if (TextUtils.isEmpty(regId)) {
            // Automatically registers application on startup.
            GCMRegistrar.register(context, Config.GCM_SENDER_ID);
        }
        // Device is already registered on GCM, needs to check if it is
        // registered on our server as well.
        if (PrefUtilities.getInstance().isRegisteredOnServer()) {
            // Skips registration.
            LOGI(TAG, "Already registered on the server");
            if (listener != null){
                listener.onResponse("success");
            }
        } else {
            register(regId, listener, errorListener);
        }
    }

    public void unregisterDevice() {
        try {
            GCMRegistrar.onDestroy(context);
        } catch (Exception e) {
            LOGW(TAG, "C2DM unregistration error", e);
        }
    }


}
