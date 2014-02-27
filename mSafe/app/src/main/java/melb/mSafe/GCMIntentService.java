package melb.mSafe;

import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;
import static melb.mSafe.utilities.LogUtils.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import melb.mSafe.gcm.GCMCommand;
import melb.mSafe.gcm.PushUtilities;
import melb.mSafe.gcm.command.AlarmCommand;
import melb.mSafe.gcm.command.AnnouncementCommand;
import melb.mSafe.gcm.command.ChatCommand;
import melb.mSafe.gcm.command.RouteGraphChangesCommand;
import melb.mSafe.gcm.command.RouteGraphCommand;
import melb.mSafe.gcm.command.TestCommand;
import melb.mSafe.push.PushType;
import melb.mSafe.utilities.PrefUtilities;

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
public class GCMIntentService extends GCMBaseIntentService{
    private static final String TAG = makeLogTag("GCM");

    private static final Map<String, GCMCommand> MESSAGE_RECEIVERS;
    static {
        // Known messages and their GCM message receivers
        Map <String, GCMCommand> receivers = new HashMap<String, GCMCommand>();

        receivers.put(PushType.TEST.toString(), new TestCommand());
        receivers.put(PushType.ANNOUNCEMENT.toString(), new AnnouncementCommand());
        receivers.put(PushType.ROUTEGRAPH.toString(), new RouteGraphCommand());
        receivers.put(PushType.ROUTEGRAPH_CHANGES.toString(), new RouteGraphChangesCommand());
        receivers.put(PushType.CHAT.toString(), new ChatCommand());
        receivers.put(PushType.ALARM.toString(), new AlarmCommand());
        MESSAGE_RECEIVERS = Collections.unmodifiableMap(receivers);
    }

    public GCMIntentService() {
        super(Config.GCM_SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String regId) {
        LOGI(TAG, "Device registered: regId=" + regId);
        PushUtilities.getInstance().register(regId, null, null);
    }

    @Override
    protected void onUnregistered(Context context, String regId) {
        LOGI(TAG, "Device unregistered");
        if (PrefUtilities.getInstance().isRegisteredOnServer()) {
            PushUtilities.getInstance().unregister(regId);
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            LOGD(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        String action = intent.getStringExtra("action");
        String extraData = intent.getStringExtra("extraData");
        if (action == null) {
            LOGE(TAG, "Message received without command action");
            return;
        }

        action = action.toLowerCase();
        GCMCommand command = MESSAGE_RECEIVERS.get(action);
        if (command == null) {
            LOGE(TAG, "Unknown command received: " + action);
        } else {
            command.execute(this, action, extraData);
        }

    }

    @Override
    public void onError(Context context, String errorId) {
        LOGE(TAG, "Received error: " + errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        LOGW(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }

}
