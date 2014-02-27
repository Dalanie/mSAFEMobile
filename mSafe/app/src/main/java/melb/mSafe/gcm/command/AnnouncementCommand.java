package melb.mSafe.gcm.command;

import android.content.Context;

import melb.mSafe.R;
import melb.mSafe.gcm.GCMCommand;
import melb.mSafe.utilities.Toaster;

import static melb.mSafe.utilities.LogUtils.*;


public class AnnouncementCommand extends GCMCommand {
    private static final String TAG = makeLogTag("AnnouncementCommand");

    @Override
    public void execute(Context context, String type, String extraData) {
        LOGI(TAG, "Received GCM message: " + type);
        Toaster.toast("Announcement: " + extraData + " received");
        displayNotification(context, extraData, null, R.drawable.ic_stat_gcm);
    }

}
