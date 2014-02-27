package melb.mSafe;

import android.app.Application;

import melb.mSafe.database.DatabaseHandler;
import melb.mSafe.gcm.PushUtilities;
import melb.mSafe.network.VolleySingleton;
import melb.mSafe.utilities.PrefUtilities;
import melb.mSafe.utilities.Toaster;


//https://github.com/rdrobinson3/VolleyImageCacheExample/blob/master/CaptechBuzz/src/com/captechconsulting/captechbuzz/MainApplication.java
public class MainApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		VolleySingleton.init(this);
        Toaster.init(this);
        DatabaseHandler.init(this);
        AudioManager.init(this);
        PrefUtilities.init(this);
        PushUtilities.init(this);
	}
}