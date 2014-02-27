package melb.mSafe.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Date;

import melb.mSafe.model.Accessibility;
import melb.mSafe.model.Vector3D;

import static melb.mSafe.utilities.LogUtils.LOGD;
import static melb.mSafe.utilities.LogUtils.LOGV;
import static melb.mSafe.utilities.LogUtils.makeLogTag;


/**
 * Created by Daniel on 09.01.14.
 */
public class PrefUtilities {
    private static final String TAG = makeLogTag("Prefs");
    private SharedPreferences preferences;
    private static PrefUtilities _instance;


    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static final String DISTANCE = "distance";
    private static final String STEPS = "steps";
    private static final String SPEED = "speed";
    private static final String SENSITIVITY = "sensitivity";
    private static final String ELEVATOR = "should_use_elevator";
    private static final String POSITION_X = "position_x";
    private static final String POSITION_Y = "position_y";
    private static final String POSITION_Z = "position_z";
    private static final String HAS_POSITION = "has_position";
    private static final String PROPERTY_REGISTERED_TS = "registered_ts";
    private static final String PROPERTY_REG_ID = "reg_id";
    private static final String SERVER_URL = "server_url";
    private static final String LAST_ROUTEGRAPH_UPDATE = "routegraph_update";
    private static final String ACCESSIBILITY_ORDINAL = "accessibility";
    private static final String STAIRS = "should_use_stairs";

    private PrefUtilities(Context context){
        preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
    }

    public float getDistance() {
        return preferences.getFloat(DISTANCE, 0);
    }

    public boolean hasUserLearnedDrawer(){
        return preferences.getBoolean(PREF_USER_LEARNED_DRAWER, false);
    }

    public static void init(Context context) {
        _instance = new PrefUtilities(context);
    }

    public static PrefUtilities getInstance(){
        return _instance;
    }

    public void setUserLearnedDrawer(boolean b) {
        preferences.edit().putBoolean(PREF_USER_LEARNED_DRAWER, b).commit();
    }

    /**
     * Pedometer-Preferences
     */
    public static int M_NONE = 1;
    public static int M_PACE = 2;
    public static int M_SPEED = 3;

    public boolean isMetric() {
        return preferences.getString("units", "imperial").equals("metric");
    }

    public float getStepLength() {
        try {
            return Float.valueOf(preferences.getString("step_length", "20").trim());
        }
        catch (NumberFormatException e) {
            // TODO: reset value, & notify user somehow
            return 0f;
        }
    }

    public int getMaintainOption() {
        String p = preferences.getString("maintain", "none");
        return
                p.equals("none") ? M_NONE : (
                        p.equals("pace") ? M_PACE : (
                                p.equals("speed") ? M_SPEED : (
                                        0)));
    }

    //-------------------------------------------------------------------
    // Desired pace & speed:
    // these can not be set in the preference activity, only on the main
    // screen if "maintain" is set to "pace" or "speed"

    public void savePaceOrSpeedSetting(int maintain, float desiredPaceOrSpeed) {
        SharedPreferences.Editor editor = preferences.edit();
        if (maintain == M_PACE) {
            editor.putInt("desired_pace", (int)desiredPaceOrSpeed);
        }
        else
        if (maintain == M_SPEED) {
            editor.putFloat("desired_speed", desiredPaceOrSpeed);
        }
        editor.commit();
    }

    public boolean wakeAggressively() {
        return preferences.getString("operation_level", "run_in_background").equals("wake_up");
    }
    public boolean keepScreenOn() {
        return preferences.getString("operation_level", "run_in_background").equals("keep_screen_on");
    }

    //
    // Internal

    public void saveServiceRunningWithTimestamp(boolean running) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("service_running", running);
        editor.putLong("last_seen", new Date().getTime());
        editor.commit();
    }

    public void saveServiceRunningWithNullTimestamp(boolean running) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("service_running", running);
        editor.putLong("last_seen", 0);
        editor.commit();
    }

    public void clearServiceRunning() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("service_running", false);
        editor.putLong("last_seen", 0);
        editor.commit();
    }

    public boolean isServiceRunning() {
        return preferences.getBoolean("service_running", false);
    }

    public void setSteps(int mSteps) {
        preferences.edit().putInt(STEPS, mSteps).commit();
    }

    public void setSpeed(float mSteps) {
        preferences.edit().putFloat(SPEED, mSteps).commit();
    }

    public void setDistance(float mDistance) {
        preferences.edit().putFloat(DISTANCE, mDistance).commit();
    }

    public void setSensitivity(String sensitivity) {
        preferences.edit().putString(SENSITIVITY, sensitivity).commit();
    }

    public String getSensitivity() {
        return preferences.getString(SENSITIVITY, "10");
    }

    public boolean setUseElevator(boolean shouldUseElevator) {
        boolean isAllowed = useElevators();
        preferences.edit().putBoolean(ELEVATOR, shouldUseElevator).commit();
        return isAllowed != shouldUseElevator;
    }

    public boolean useElevators() {
        return preferences.getBoolean(ELEVATOR, false);
    }

    public boolean setUseStairs(boolean shouldUseStairs) {
        boolean isAllowed = useStairs();
        preferences.edit().putBoolean(STAIRS, shouldUseStairs).commit();
        return isAllowed != shouldUseStairs;
    }

    public boolean useStairs() {
        return preferences.getBoolean(STAIRS, true);
    }

    public boolean hasLatestPosition(){
        return preferences.getBoolean(HAS_POSITION, false);
    }

    public void setLatestPosition(Vector3D position) {
        preferences.edit().putFloat(POSITION_X, position.getX()).commit();
        preferences.edit().putFloat(POSITION_Y, position.getY()).commit();
        preferences.edit().putFloat(POSITION_Z, position.getZ()).commit();
        preferences.edit().putBoolean(HAS_POSITION, true).commit();
    }

    public Vector3D getLatestPosition() {
        if (hasLatestPosition()){
            float x = preferences.getFloat(POSITION_X, -1);
            float y = preferences.getFloat(POSITION_Y, -1);
            float z = preferences.getFloat(POSITION_Z, -1);
            return new Vector3D(x,y,z);
        }
        return null;
    }

    public boolean isRegisteredOnServer() {
        // Find registration threshold
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        long yesterdayTS = cal.getTimeInMillis();
        long regTS = preferences.getLong(PROPERTY_REGISTERED_TS, 0);
        if (regTS > yesterdayTS) {
            LOGV(TAG, "GCM registration current. regTS=" + regTS + " yesterdayTS=" + yesterdayTS);
            return true;
        } else {
            LOGV(TAG, "GCM registration expired. regTS=" + regTS + " yesterdayTS=" + yesterdayTS);
            return false;
        }
    }

    /**
     * Sets whether the device was successfully registered in the server side.
     *
     * @param flag    True if registration was successful, false otherwise
     * @param gcmId    True if registration was successful, false otherwise
     */
    public void setRegisteredOnServer(boolean flag, String gcmId) {
        LOGD(TAG, "Setting registered on server status as: " + flag);
        SharedPreferences.Editor editor = preferences.edit();
        if (flag) {
            editor.putLong(PROPERTY_REGISTERED_TS, new Date().getTime());
            editor.putString(PROPERTY_REG_ID, gcmId);
        } else {
            editor.remove(PROPERTY_REG_ID);
            editor.remove(PROPERTY_REGISTERED_TS);
        }
        editor.commit();
    }

    public String getGcmId() {
        return preferences.getString(PROPERTY_REG_ID, null);
    }

    public void setServerUrl(String url){
        preferences.edit().putString(SERVER_URL, url).commit();
    }

    public String getServerURL() {
        return preferences.getString(SERVER_URL, CommonUtilities.SERVER_URL);
    }

    public long getLastRouteGraphUpdate(){
        return preferences.getLong(LAST_ROUTEGRAPH_UPDATE, 0);
    }

    public void setLastRouteGraphUpdate(long time){
        preferences.edit().putLong(LAST_ROUTEGRAPH_UPDATE, time).commit();
    }

    public boolean setAccessibility(Accessibility accessibility) {
        Accessibility currentAccessibility = getAccessibility();
        preferences.edit().putInt(ACCESSIBILITY_ORDINAL, accessibility.ordinal()).commit();
        return accessibility != currentAccessibility;
    }

    public Accessibility getAccessibility(){
        return Accessibility.values()[preferences.getInt(ACCESSIBILITY_ORDINAL, 0)];
    }
}
