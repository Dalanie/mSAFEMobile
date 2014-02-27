package melb.mSafe.gcm.command;

import android.content.Context;

import java.util.Date;

import melb.mSafe.events.RouteGraphChangedEvent;
import melb.mSafe.events.UpdateRouteGraphEvent;
import melb.mSafe.gcm.GCMCommand;
import melb.mSafe.utilities.BusProvider;
import melb.mSafe.utilities.Toaster;

import static melb.mSafe.utilities.LogUtils.*;


public class RouteGraphCommand extends GCMCommand{
    private static final String TAG = makeLogTag("RouteGraphCommand");

    @Override
    public void execute(Context context, String type, String extraData) {
        LOGI(TAG, "Received GCM message: " + type);
        BusProvider.getInstance().post(new UpdateRouteGraphEvent(new Date().getTime()));
    }
}
