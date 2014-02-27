package melb.mSafe.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.abhi.barcode.frag.libv2.BarcodeFragment;
import com.abhi.barcode.frag.libv2.IScanResultHandler;
import com.abhi.barcode.frag.libv2.ScanResult;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import melb.mSafe.OrientationManager;
import melb.mSafe.RouteGraphManager;
import melb.mSafe.R;
import melb.mSafe.chat.TextMessage;
import melb.mSafe.common.AccessibilityMenu;
import melb.mSafe.events.AccessibilityChangedEvent;
import melb.mSafe.events.ChatMessageReceivedEvent;
import melb.mSafe.events.PositionChangedEvent;
import melb.mSafe.events.RouteGraphChangedEvent;
import melb.mSafe.gcm.PushUtilities;
import melb.mSafe.model.Accessibility;
import melb.mSafe.model.RouteGraph;
import melb.mSafe.model.Vector3D;
import melb.mSafe.network.RouteGraphCommunication;
import melb.mSafe.utilities.AccessibilityAdapter;
import melb.mSafe.utilities.BusProvider;
import melb.mSafe.utilities.DialogHelper;
import melb.mSafe.utilities.PrefUtilities;
import melb.mSafe.utilities.Toaster;

import static melb.mSafe.utilities.LogUtils.LOGD;
import static melb.mSafe.utilities.LogUtils.makeLogTag;

public class MainActivity extends ActionBarActivity
        implements ActionBar.OnNavigationListener, IScanResultHandler {
    public static final String QR_CODE_TAG_MSAFE = "msafe";
    private static final String TAG = makeLogTag(MainActivity.class);

    private OrientationManager orientationManager;
    private RouteGraphManager routeGraphManager;
    private Vector3D userPosition = null;
    private List<TextMessage> chatMessages;
    private BuildingViewFragment buildingViewFragment;
    private ArrayList<AccessibilityMenu> menu;
    //private DirectionView directionView;
    private AugmentedViewFragment cameraFragment;
    private RouteGraph routeGraph;
    private BarcodeFragment barcodeFragment;
    private TextView qrInfoView;

    private boolean buildingReloadRequired = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        qrInfoView = (TextView) findViewById(R.id.qr_info);
        setQRText(false);

        chatMessages = new ArrayList<TextMessage>();

        showDirectionView();
        showProjectionView();
        showNavigationView();

        // Sync data on load
        if (savedInstanceState == null) {
            PushUtilities.getInstance().registerGCMClient(null, null);
        }
        orientationManager = new OrientationManager(this);
        routeGraphManager = new RouteGraphManager(this);
        userPosition = PrefUtilities.getInstance().getLatestPosition();
        showBuildingView();
    }

    private void setQRText(boolean enabled){
        if (enabled){
            qrInfoView.setText("QR-Code-Scan enabled");
            qrInfoView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }else{
            qrInfoView.setText("Press QR-Symbol to scan position");
            qrInfoView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void showDirectionView() {
        ViewGroup group = (ViewGroup) findViewById(R.id.arrow_navigation_layout);
        //directionView = new DirectionView(group);
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        AccessibilityMenu selectedMenu = menu.get(i);
        BusProvider.getInstance().post(new AccessibilityChangedEvent(selectedMenu.accessibility, null, null));
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PushUtilities.getInstance().unregisterDevice();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register self with the only bus that we're using
        BusProvider.getInstance().register(this);
        orientationManager.onResume();
        routeGraphManager.onResume();
        //directionView.onResume();
    }

    @Subscribe
    public void onRouteGraphChanged(RouteGraphChangedEvent event){
        this.routeGraph = event.routeGraph;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister self with the only bus that we're using
        BusProvider.getInstance().unregister(this);

        orientationManager.onPause();
        routeGraphManager.onPause();
        //directionView.onPause();
        buildingReloadRequired = true;
    }

    public void showHideBuildingView(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (buildingViewFragment != null && buildingViewFragment.isVisible()) {
            hideBuildingView();
        } else {
          showBuildingView();
        }
    }

    public void showBuildingView() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (buildingViewFragment != null && buildingViewFragment.isHidden() &&
                !buildingReloadRequired){
            fragmentManager.beginTransaction()
                    .show(buildingViewFragment)
                    .commit();
        }else{
            buildingViewFragment = BuildingViewFragment.getInstance(userPosition, routeGraph, routeGraphManager.getLastWay());
            fragmentManager.beginTransaction()
                    .replace(R.id.containerGL, buildingViewFragment)
                    .commit();
        }
        buildingViewFragment.setVisible(true);
    }

    public void hideBuildingView() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (buildingViewFragment != null && buildingViewFragment.isVisible()) {
            fragmentManager.beginTransaction()
                    .hide(buildingViewFragment)
                    .commit();
            buildingViewFragment.setVisible(false);
        }
    }

    public void showProjectionView() {
        cameraFragment = AugmentedViewFragment.newInstance();

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, cameraFragment)
                .commit();
    }

    public void showHideQRView() {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("Barcode");
        if (fragment == null || !fragment.isAdded()){
            barcodeFragment = new BarcodeFragment();
            barcodeFragment.setScanResultHandler(this);
            fragmentManager.beginTransaction().replace(R.id.container, barcodeFragment, "Barcode")
                    .commit();
            setQRText(true);
        }else {
            setQRText(false);
            showProjectionView();
            showBuildingView();
        }
    }

    public void showNavigationView() {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.bottom_container, RouteStepsViewFragment.newInstance())
                .commit();
    }



    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem elevatorItem = menu.findItem(R.id.elevator);
        if (elevatorItem != null){
            elevatorItem.setIcon(getElevatorImage());
        }
        MenuItem stairsItem = menu.findItem(R.id.stairs);
        if (stairsItem != null){
            stairsItem.setIcon(getStairsImage());
        }
        setUpAccessibleSpinner(menu.findItem(R.id.accessible_spinner));

        restoreActionBar();
        return true;
    }

    private int getElevatorImage(){
        if (PrefUtilities.getInstance().useElevators()){
            return R.drawable.elevator_menu;
        }
        return R.drawable.elevator_restricted_menu;
    }

    private int getStairsImage(){
        if (PrefUtilities.getInstance().useStairs()){
            return R.drawable.stairs_menu;
        }
        return R.drawable.stairs_restricted_menu;
    }


    private void setUpAccessibleSpinner(MenuItem item) {
        View view = item.getActionView();
        if (view instanceof Spinner) {
            Spinner spinner = (Spinner) view;

            menu = new ArrayList<AccessibilityMenu>();
            menu.add(new AccessibilityMenu(Accessibility.PEDESTRIAN, R.drawable.walking_menu));
            menu.add(new AccessibilityMenu(Accessibility.WHEELCHAIR, R.drawable.wheelchair_menu));
            AccessibilityAdapter accessibilityAdapter = new AccessibilityAdapter(this, menu);

            spinner.setAdapter(accessibilityAdapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.qr:
                showHideQRView();
                hideBuildingView();
                return true;
            case R.id.chat:
                showChatView();
                return true;
            case R.id.action_map:
                showHideBuildingView();
                return true;
            case R.id.relog:
                DialogHelper.setServerUrlDialog(this);
                return true;
            case R.id.elevator:
                PrefUtilities prefs = PrefUtilities.getInstance();
                boolean shouldUseElevators = !prefs.useElevators();
                BusProvider.getInstance().post(new AccessibilityChangedEvent(null, shouldUseElevators, null));
                invalidateOptionsMenu();
                return true;
            case R.id.stairs:
                prefs = PrefUtilities.getInstance();
                boolean shouldUseStairs = !prefs.useStairs();
                BusProvider.getInstance().post(new AccessibilityChangedEvent(null, null, shouldUseStairs));
                invalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showChatView() {
        DialogFragment fragment = (DialogFragment) ChatViewFragment.newInstance(chatMessages);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment.show(fragmentManager, "chat");
    }

    private void handlePositionIdentifier(int id) {
        new RouteGraphCommunication().getInformationByPositionId(id, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Toaster.toast("info to your position: " + s);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                    }
                }
        );
    }

    private void switchUserPosition(float x, float y, float z) {
        userPosition = new Vector3D();
        userPosition.set(x, y, z);
        /*
         * inform bus
         */
        BusProvider.getInstance().post(new PositionChangedEvent(userPosition));
    }



    @Subscribe
    public void onChatMessageReceived(ChatMessageReceivedEvent event) {
        Crouton.makeText(this, event.message.toString(), Style.INFO, R.id.crouton_chat_container).show();
        chatMessages.add(event.message);
        LOGD(TAG, "onChatMessageReceived()");
    }

    @Override
    public void scanResult(ScanResult scanResult) {
        ParsedResult scanningResult = scanResult.getParsedResult();
        if (scanningResult != null) {
            // we have a result
            if (scanningResult.getType() == ParsedResultType.TEXT){
                qrCodeScanned(scanningResult.getDisplayResult());
            }
        } else {
            Toast toast = Toast.makeText(this, "No scan data received!",
                    Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /*
     * qr codes need the format: tag;id;position -> e.g. msafe;1;1,1,2
     */
    private void qrCodeScanned(String scanContent) {
        if (scanContent != null){
            String[] elems = scanContent.split("_");
            if (elems != null && elems.length == 5){
                //determine if the scanned qr-code is from msafe
                if (elems[0].equalsIgnoreCase(QR_CODE_TAG_MSAFE)) {
                    //if from msafe - unchecked parsing of the fields
                    int id = Integer.parseInt(elems[1]);
                    handlePositionIdentifier(id);
                    float x = Float.parseFloat(elems[2]);
                    float y = Float.parseFloat(elems[3]);
                    float z = Float.parseFloat(elems[4]);
                    switchUserPosition(x, y, z);
                }
            }
        } setQRText(false);
        showProjectionView();
        showBuildingView();
    }
}
