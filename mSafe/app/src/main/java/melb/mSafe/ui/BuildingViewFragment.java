package melb.mSafe.ui;

import melb.mSafe.common.ExtendedWay;
import melb.mSafe.database.DatabaseHandler;
import melb.mSafe.events.LayerVisibilityChangeEvent;
import melb.mSafe.events.PositionChangedEvent;
import melb.mSafe.events.RouteChangedEvent;
import melb.mSafe.events.RouteGraphChangedEvent;
import melb.mSafe.events.SensorChangedEvent;
import melb.mSafe.model.Vector3D;
import melb.mSafe.network.BuildingCommunication;
import melb.mSafe.opengl.MyGLRenderer;
import melb.mSafe.opengl.MyGLSurfaceView;
import melb.mSafe.opengl.drawable.Layer3DGL;
import melb.mSafe.opengl.drawable.Model3DGL;
import melb.mSafe.opengl.utilities.FPSHelper;
import melb.mSafe.opengl.utilities.Helper;
import melb.mSafe.utilities.BusProvider;
import melb.mSafe.utilities.Toaster;
import melb.mSafe.views.CustomSeekBar;
import melb.mSafe.model.Model3D;
import melb.mSafe.model.RouteGraph;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ToggleButton;

import melb.mSafe.R;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.squareup.otto.Subscribe;


public class BuildingViewFragment extends DialogFragment {

	private MyGLSurfaceView mGLView;
	private MyGLRenderer mGLRenderer;
	private Model3DGL model3d;
	private CustomSeekBar seekBarX;
	private LinearLayout listOfFloors;
	private RouteGraph routeGraph;
    private Vector3D userPosition;
    private SparseArray<ToggleButton> buttonsForLayer;
    private ExtendedWay way;

    public BuildingViewFragment(Vector3D userPosition, RouteGraph graph, ExtendedWay way) {
        this.userPosition = userPosition;
        Log.e("Model3d", "<<<<<<<<<<<<<<<<< BuildingViewFragment" + userPosition);
        this.routeGraph = graph;
        this.way = way;
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        model3d = new Model3DGL(null, userPosition);
        Log.e("Model3d", "<<<<<<<<<<<<<<<<< onCreate" + userPosition);
        new LoadModelAsyncTask().execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		FrameLayout rootView = (FrameLayout) inflater.inflate(R.layout.gl_view,
				null);

		if (hasGLES20()) {
			mGLView = new MyGLSurfaceView(getActivity(), model3d);
            if (rootView != null){
                rootView.addView(mGLView, 0);
            }
		}
	    listOfFloors = (LinearLayout) (rootView != null ? rootView.findViewById(R.id.listOfFloors) : null);
        seekBarX = (CustomSeekBar) rootView.findViewById(R.id.seekBarX);
		initSeekBars();
		return rootView;
	}

	private void initListView() {
		int i = 0;

        Context context = getActivity();
        if (context != null){
            buttonsForLayer = new SparseArray<ToggleButton>();
            for (final Layer3DGL layer : model3d.getLayers()) {
                final ToggleButton button = new ToggleButton(context);
                button.setBackgroundDrawable(getResources().getDrawable(R.drawable.layer_toggle));
                button.setChecked(true);
                button.setText(i + "");
                button.setTextOn(i + "");
                button.setTextOff(i + "");
                button.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        layer.setVisible(!layer.isVisible());
                        button.setSelected(!button.isSelected());
                    }
                });
                listOfFloors.addView(button, 0);
                buttonsForLayer.put(layer.getLayer().getId(), button);
                i++;
            }
        }
	}

	private void initSeekBars() {
		if (mGLView != null) {
			mGLRenderer = mGLView.getRenderer();

			seekBarX.setMin(0);
			seekBarX.setMax(80);
			seekBarX.setProgress((int) (mGLRenderer.getRotationX()));
			seekBarX.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override public void onStopTrackingTouch(SeekBar seekBar) {}
				@Override public void onStartTrackingTouch(SeekBar seekBar) {}

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					mGLRenderer.setRotationX((float) progress);
				}
			});
		}
	}

    public void setVisible(boolean visible) {
        if (mGLView != null){
            if (visible){
                mGLView.setVisibility(View.VISIBLE);
                FPSHelper.resumeFPS();
            }else{
                mGLView.setVisibility(View.INVISIBLE);
                FPSHelper.pauseFPS();
            }
        }
    }

    private class LoadModelAsyncTask extends AsyncTask<Void, Void, Model3D> {

        @Override
        protected Model3D doInBackground(Void... params) {
            Model3D model =  DatabaseHandler.getInstance().getLatestModel();
            if (model == null){
                try {
                    model = new Gson().fromJson(Helper.getStringFromRaw(
                            getActivity(), R.raw.building_model), Model3D.class);
                    DatabaseHandler.getInstance().addModel(model);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return model;
        }

        @Override
        protected void onPostExecute(Model3D model) {
            if (model != null){
                onModelLoaded(model);
            }else{
                /*
                 * if model not in database or raw-file  try to load from network
                 */
                loadModelFromNetwork();
            }
        }
    }

    private void onModelLoaded(Model3D model){
        model3d.initialize(model);
        initListView();
        model3d.setUserPosition(userPosition);
        Log.e("Model3d", "<<<<<<<<<<<<<<<<< onModelLoaded" + userPosition);
        model3d.setRouteGraphChanged(new RouteGraphChangedEvent(routeGraph));
        onRouteChanged(new RouteChangedEvent(way, RouteChangedEvent.RouteChangedType.NEW_WAY));
    }

    private void loadModelFromNetwork(){
        new BuildingCommunication().getBuilding(
                new Response.Listener<Model3D>() {
                    @Override
                    public void onResponse(Model3D model3d) {
                        DatabaseHandler.getInstance().addModel(model3d);
                        onModelLoaded(model3d);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toaster.toast(volleyError.getMessage() + " Volleyerror");
                    }
                }
        );
    }

	private boolean hasGLES20() {
		ActivityManager am = (ActivityManager) getActivity().getSystemService(
				Context.ACTIVITY_SERVICE);
		ConfigurationInfo info = am.getDeviceConfigurationInfo();
		return (info != null ? info.reqGlEsVersion : 0) >= 0x20000;
	}

	@Override
	public void onPause() {
		super.onPause();
		/*
		 * The following call pauses the rendering thread. If your OpenGL
		 * application is memory intensive, you should consider de-allocating
		 * objects that consume significant memory here.
		 */
		mGLView.onPause();
        BusProvider.getInstance().unregister(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		/*
		 * The following call resumes a paused rendering thread. If you
		 * de-allocated graphic objects for onPause() this is a good place to
		 * re-allocate them.
		 */
		mGLView.onResume();
        BusProvider.getInstance().register(this);
	}

    @Subscribe
    public void onPositionChanged(PositionChangedEvent event) {
        model3d.setUserPosition(event.userPosition);
    }

    @Subscribe
    public void onSensorChanged(SensorChangedEvent event) {
        model3d.setUserOrientation(event.userOrientation);
    }

    @Subscribe
    public void onRouteChanged(RouteChangedEvent event) {
        if (model3d != null){
            model3d.updateWay(event);
            Log.e("Model3d", "<<<<<<<<<<<<<<<<< onRouteChanged" + userPosition);
        }
    }

    @Subscribe
    public void onRouteGraphChanged(RouteGraphChangedEvent event) {
        if (model3d != null){
            model3d.setRouteGraphChanged(event);
            Log.e("Model3d", "<<<<<<<<<<<<<<<<< onRouteGraphChanged" + userPosition);
        }
    }


    @Subscribe
    public void layerVisibilityChanged(LayerVisibilityChangeEvent event){
        if (buttonsForLayer != null){
            ToggleButton button = buttonsForLayer.get(event.layer.getLayer().getId());
            if (button != null){
                button.setChecked(event.visible);
            }
        }
    }

    public static BuildingViewFragment getInstance(Vector3D userPosition, RouteGraph graph, ExtendedWay extendedWay) {
        BuildingViewFragment fragment = new BuildingViewFragment(userPosition, graph, extendedWay);
        return fragment;
    }
}
