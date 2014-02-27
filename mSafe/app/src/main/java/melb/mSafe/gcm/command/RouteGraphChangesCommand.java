package melb.mSafe.gcm.command;

import android.content.Context;

import com.google.gson.Gson;

import melb.mSafe.events.RouteGraphModifiedEvent;
import melb.mSafe.gcm.GCMCommand;
import melb.mSafe.model.RouteGraph;
import melb.mSafe.utilities.BusProvider;

/**
 * Created by Daniel on 28.01.14.
 */
public class RouteGraphChangesCommand extends GCMCommand {
    @Override
    public void execute(Context context, String type, String extraData) {
        RouteGraph routeGraph = new Gson().fromJson(extraData, RouteGraph.class);
        BusProvider.getInstance().post(new RouteGraphModifiedEvent(routeGraph));
    }
}
