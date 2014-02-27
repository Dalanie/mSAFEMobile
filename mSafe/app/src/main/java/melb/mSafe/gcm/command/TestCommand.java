package melb.mSafe.gcm.command;

import android.content.Context;

import melb.mSafe.gcm.GCMCommand;
import melb.mSafe.utilities.Toaster;

import static melb.mSafe.utilities.LogUtils.LOGI;
import static melb.mSafe.utilities.LogUtils.makeLogTag;


public class TestCommand extends GCMCommand {
    private static final String TAG = makeLogTag("TestCommand");

    @Override
    public void execute(Context context, String type, String extraData) {
        LOGI(TAG, "Received GCM message: " + type);
        Toaster.toast("Test: " + extraData + " received");
    }
}
