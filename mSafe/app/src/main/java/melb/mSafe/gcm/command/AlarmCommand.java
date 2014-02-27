package melb.mSafe.gcm.command;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;

import java.util.Date;

import melb.mSafe.R;
import melb.mSafe.gcm.GCMCommand;
import melb.mSafe.model.Information;
import melb.mSafe.ui.MainActivity;

import static melb.mSafe.utilities.LogUtils.LOGI;
import static melb.mSafe.utilities.LogUtils.makeLogTag;

/**
 * Created by Daniel on 29.01.14.
 */
public class AlarmCommand extends GCMCommand {
    private static final String TAG = makeLogTag("AlarmCommand");

    @Override
    public void execute(Context context, String type, String extraData) {
        LOGI(TAG, "Received GCM message: " + type);
        Information information = new Gson().fromJson(extraData, Information.class);
        String title = "";
        long time = new Date().getTime();
        if (information != null){
            title = information.getMessage();
            time = information.getDate();
        }
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        displayNotification(context, title, pIntent, R.drawable.logo);
    }
}
