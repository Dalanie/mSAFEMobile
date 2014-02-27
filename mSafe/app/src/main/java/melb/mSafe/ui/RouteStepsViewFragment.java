package melb.mSafe.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.squareup.otto.Subscribe;

import java.text.DecimalFormat;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import melb.mSafe.AudioManager;
import melb.mSafe.RouteGraphManager;
import melb.mSafe.R;
import melb.mSafe.common.ExtendedWay;
import melb.mSafe.events.PositionChangedEvent;
import melb.mSafe.events.RouteChangedEvent;
import melb.mSafe.events.RouteGraphChangedEvent;
import melb.mSafe.model.RouteGraphEdge;
import melb.mSafe.model.RouteGraphNode;
import melb.mSafe.model.Vector3D;
import melb.mSafe.utilities.BusProvider;
import melb.mSafe.utilities.Toaster;
import melb.mSafe.utilities.ZoomOutPageTransformer;

import static melb.mSafe.utilities.LogUtils.makeLogTag;

/**
 * Created by Daniel on 26.12.13.
 */
public class RouteStepsViewFragment extends Fragment {


    private static final String TAG = makeLogTag(RouteStepsViewFragment.class);
    public static final float PAGE_WIDTH = 0.2f;
    //start + end - fake pages
    public static final int PAGE_OFFSET = 4;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    private ExtendedWay currentWay;
    private View rootView;
    private boolean isInvisible;
    private ImageView leftImage;
    private ImageView rightImage;
    private ProgressBar progressIndicator;

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.route_steps_view_fragment, container, false);
        rootView.setVisibility(View.INVISIBLE);
        progressIndicator = (ProgressBar) rootView.findViewById(R.id.progress_indicator);
        isInvisible = true;
        rootView.setAlpha(0.0f);
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) rootView.findViewById(R.id.pager);
        initAdapter();

        leftImage = (ImageView)rootView.findViewById(R.id.left_button);
        rightImage = (ImageView)rootView.findViewById(R.id.right_button);

        initImageButtons();
        return rootView;
    }

    private void initAdapter() {
        /*
      The pager adapter, which provides the pages to the view pager widget.
     */
        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOffscreenPageLimit(7);
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                int node = (i / 2);
                if (node < 0){
                    node = 0;
                }
                double distanceFromPager = currentWay.getDistance(node, ExtendedWay.EXIT);
                progressIndicator.setSecondaryProgress(
                        progressIndicator.getMax() - (int) (distanceFromPager * 100));
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private void initImageButtons() {
        leftImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toaster.toast("previous step");
                RouteGraphNode node = currentWay.getPreviousNode();
                if (node != null){
                    BusProvider.getInstance().post(new PositionChangedEvent(node.position));
                }
            }
        });

        rightImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toaster.toast("next step");
                RouteGraphNode node = currentWay.getNextNode();
                if (node != null){
                    BusProvider.getInstance().post(new PositionChangedEvent(node.position));
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }


    @Subscribe
    public void onRouteChanged(RouteChangedEvent event) {
        if (event.extendedWay != null){
            switch (event.routeChangedType){
                case NEW_NODES:
                    newRoute(event.extendedWay);
                    break;
                case NEW_POSITION:
                    progressUpdate();
                    break;
                case NEW_WAY:
                    newRoute(event.extendedWay);
                    break;
            }
        }
    }

    private void newRoute(ExtendedWay extendedWay) {
        currentWay = extendedWay;
        if (isInvisible){
            isInvisible = false;
            rootView.setVisibility(View.VISIBLE);
            animate(rootView).alpha(1.0f).setStartDelay(2500).setDuration(1500).start();
        }

        notifyAdapterWayHasChanged();
    }

    private void playSound(int currentPagerPosition) {
        //TODO play sound
    }

    private void progressUpdate(){
        int currentPagerPosition = (currentWay.currentPositionIndex*2); //+1
        mPager.setCurrentItem(currentPagerPosition);

        double maxDistance = currentWay.getDistance(0, ExtendedWay.EXIT);
        progressIndicator.setMax((int) (maxDistance * 100));

        double distance = maxDistance - currentWay.getDistance(currentWay.currentPositionIndex, ExtendedWay.EXIT);
        progressIndicator.setProgress((int) (distance * 100));

        playSound(currentPagerPosition);
        if (handler != null){
            handler.removeCallbacks(runnable);
        }
        handler = new Handler();
        handler.postDelayed(runnable, 5000);
    }



    private void notifyAdapterWayHasChanged() {
        initAdapter();
        progressUpdate();
    }


    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
           return getSize() + PAGE_OFFSET;
        }

        @Override
        public float getPageWidth(int position) {
            return PAGE_WIDTH;
        }


        @Override
        public Fragment getItem(int position) {
            RouteStepElement elem = null;
            int nodePosition = position-2;
            if (position > 1 && position < getCount()-2){ //1+2, final-1, final are empty views
                if (position % 2 == 0){
                    elem = new RouteStepElement(currentWay.way.getNodes().get(nodePosition/2));
                }else{
                    RouteGraphNode nodeA = currentWay.way.getNodes().get(nodePosition/2);
                    RouteGraphNode nodeB = currentWay.way.getNodes().get((nodePosition/2)+1);
                    for(RouteGraphEdge edge : nodeA.getAdjacentEdges()){
                        if (nodeB.getAdjacentEdges().contains(edge)){
                            elem = new RouteStepElement(edge);
                        }
                    }
                }
            }else{
                elem = new RouteStepElement();
            }
            return elem;
        }
    }

    private int getSize(){
        int size = 0;
        if (currentWay != null){
            int nodes = currentWay.way.getNodes().size();
            int edges = nodes-1;
            size = nodes + edges;
        }
        return size;
    }
    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class RouteStepElement extends Fragment{
        private RouteGraphNode node;
        private RouteGraphEdge edge;
        private double distance;

        public RouteStepElement(RouteGraphNode node){
            this.node = node;
        }

        public RouteStepElement(RouteGraphEdge edge){
            this.edge = edge;
        }
        public RouteStepElement(){
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle bundle){
            //first and last element - placeholder for centering view
            if (node == null && edge == null){
                return new View(getActivity());
            }
            if (node != null){
                return onCreateNodeView(inflater, group, bundle);
            }
            return onCreateEdgeView(inflater, group, bundle);
        }

        private View onCreateEdgeView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {
            View rootView = inflater.inflate(
                    R.layout.single_route_step_edge_fragment, group, false);
            TextView distanceView = (TextView) rootView.findViewById(R.id.distance_text);

            ImageView image = (ImageView) rootView.findViewById(R.id.image);

            Integer id = getDrawableEdgeIdByEdge(edge);
            if (id != null){
                image.setImageResource(id);
            }//TODO
            image.setOnClickListener(new SoundClickListener());

            DecimalFormat df = new DecimalFormat("0.00");
            double distanceInPixel = Vector3D.getDistance(edge.getPointA().position, edge.getPointB().position);
            distance = RouteGraphManager.getDistanceInM(distanceInPixel);
            if (distance > 0){
                distanceView.setText(String.format("%s m", df.format(distance)));
            }
            return rootView;
        }

        private View onCreateNodeView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {
            View rootView = inflater.inflate(
                    R.layout.single_route_step_node_fragment, group, false);
            ImageView image = (ImageView) rootView.findViewById(R.id.image);
            Integer id = getDrawableNodeIdByNode(node);
            if (id != null){
                image.setImageResource(id);
                image.setOnClickListener(new SoundClickListener());
            }
            return rootView;
        }

        private class SoundClickListener implements View.OnClickListener {

            @Override
            public void onClick(View v) {
                if (node != null){
                    AudioManager.getInstance().speak(node.type.toString());
                    //TODO turn right and co
                }
                if (edge!= null){
                    String distanceToSpeech = AudioManager.distanceToSpeech(distance);
                    AudioManager.getInstance().speak("Follow the way for " + distanceToSpeech);
                }

            }
        }

        private Integer getDrawableEdgeIdByEdge(RouteGraphEdge edge) {
            switch (edge.getEdgeType()){
                case STAIRS:
                    if (currentWay.isEdgeDown(edge)){
                        return R.drawable.stairs_down;
                    }else{
                        return R.drawable.stairs_up;
                    }
                case ELEVATOR:
                    return R.drawable.elevator;
                default:
                    return R.drawable.foot;
            }
        }

        private Integer getDrawableNodeIdByNode(RouteGraphNode routeGraphNode){

            switch (routeGraphNode.type){
                case EXIT: return R.drawable.exit;
                case NFC: return R.drawable.qrcode_route;
                default:
                    break;
            }
            float angle = (float) Math.toDegrees(currentWay.getDirection(routeGraphNode));
            while (angle < 0){
                angle += 360;
            }
            Log.e("<<<<<>>>>>>", "Signed Degree is: " + angle);

            if (angle > 180){
                return R.drawable.turn_right;
            }else{
                return R.drawable.turn_left;
            }
        }
    }

    public static Fragment newInstance() {
        RouteStepsViewFragment fragment = new RouteStepsViewFragment();
        return fragment;
    }

    private Handler handler;
    private Runnable runnable = new Runnable(){

        @Override
        public void run() {
            int currentPagerPosition = (currentWay.currentPositionIndex*2+1);
            mPager.setCurrentItem(currentPagerPosition);
            playSound(currentPagerPosition);
        }
    };

}