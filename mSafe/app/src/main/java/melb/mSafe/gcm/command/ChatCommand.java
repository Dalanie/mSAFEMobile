package melb.mSafe.gcm.command;

import android.content.Context;

import melb.mSafe.chat.TextMessage;
import melb.mSafe.events.ChatMessageReceivedEvent;
import melb.mSafe.gcm.GCMCommand;
import melb.mSafe.serialization.Serializer;
import melb.mSafe.utilities.BusProvider;
import melb.mSafe.utilities.Toaster;

import static melb.mSafe.utilities.LogUtils.*;


public class ChatCommand extends GCMCommand {
    private static final String TAG = makeLogTag("ChatCommand");

    @Override
    public void execute(Context context, String type, String extraData) {
        LOGI(TAG, "Received GCM message: " + type);
        //Toaster.toast("Chat: " + extraData + " received");
        TextMessage message = Serializer.deserializeMessage(extraData);
        BusProvider.getInstance().post(new ChatMessageReceivedEvent(message));
    }
}
