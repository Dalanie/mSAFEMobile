package melb.mSafe.ui;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.squareup.otto.Subscribe;

import melb.mSafe.R;
import melb.mSafe.events.PositionChangedEvent;
import melb.mSafe.events.RouteChangedEvent;
import melb.mSafe.events.RouteGraphChangedEvent;
import melb.mSafe.events.SensorChangedEvent;
import melb.mSafe.utilities.BusProvider;
import melb.mSafe.utilities.Toaster;
import melb.mSafe.views.CameraPreviewView;

import static melb.mSafe.utilities.LogUtils.LOGD;
import static melb.mSafe.utilities.LogUtils.makeLogTag;

public class AugmentedViewFragment extends Fragment {
    private static final String TAG = makeLogTag(AugmentedViewFragment.class);
	private MainActivity parentActivity;
	private CameraPreviewView previewView;
    private FrameLayout rootView;

    public AugmentedViewFragment() {
    }

    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.augmented_menu, menu);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Activity activity = getActivity();
		if (activity instanceof MainActivity) {
			parentActivity = (MainActivity) activity;
		} else {
			Log.e(TAG, "ParentActivity is not MainActivity. Big Error!");
			throw new ClassCastException();
		}
	}


	@Override
	public void onResume() {
		super.onResume();
        BusProvider.getInstance().register(this);
        rootView.removeAllViews();
        if (previewView != null){
            rootView.addView(previewView);
        }
	}

	@Override
	public void onPause() {
		super.onPause();
		if (previewView != null) {
			previewView.stopPreviewAndFreeCamera();
		}
        rootView.removeAllViews();
        BusProvider.getInstance().unregister(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        rootView = (FrameLayout) inflater.inflate(R.layout.augmented_view, container, false);

		/*
		 * Initializing ProjectionCamera-Preview and adding to the basic layout
		 */
        previewView = new CameraPreviewView(parentActivity);
        rootView.removeAllViews();
        rootView.addView(previewView);
		return rootView;
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

    @Subscribe
    public void onPositionChanged(PositionChangedEvent event) {
        // TODO: React to the event somehow!
    }

    @Subscribe
    public void onSensorChanged(SensorChangedEvent event) {
        // TODO: React to the event somehow!
        LOGD(TAG, "onSensorChanged()");
    }

    @Subscribe
    public void onRouteGraphChanged(RouteGraphChangedEvent event) {
        // TODO: React to the event somehow!
        LOGD(TAG, "onRouteGraphChanged()");
    }

    @Subscribe
    public void onRouteChanged(RouteChangedEvent event) {
        // TODO: React to the event somehow!
        LOGD(TAG, "onRouteChanged()");
    }

    public static AugmentedViewFragment newInstance() {
        AugmentedViewFragment fragment = new AugmentedViewFragment();
        return fragment;
    }
}
