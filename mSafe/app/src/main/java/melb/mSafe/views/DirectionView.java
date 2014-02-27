package melb.mSafe.views;

import android.graphics.Matrix;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.squareup.otto.Subscribe;

import java.text.DecimalFormat;

import melb.mSafe.RouteGraphManager;
import melb.mSafe.R;
import melb.mSafe.common.ExtendedWay;
import melb.mSafe.events.PositionChangedEvent;
import melb.mSafe.events.RouteChangedEvent;
import melb.mSafe.events.SensorChangedEvent;
import melb.mSafe.model.RouteGraphNode;
import melb.mSafe.model.Vector3D;
import melb.mSafe.utilities.BusProvider;

public class DirectionView {

    private ImageView arrowView;
    private TextView distanceView;
    private boolean isInvisible;
    private float bearingToNextTarget = 0;
    private Vector3D userPosition;

    public DirectionView(ViewGroup directionLayout) {
        this.arrowView = (ImageView) directionLayout.findViewById(R.id.direction_view);
        this.distanceView = (TextView) directionLayout.findViewById(R.id.distance_textview);
        this.arrowView.setVisibility(View.GONE);
        this.distanceView.setVisibility(View.GONE);
        ViewHelper.setAlpha(arrowView, 0.0f);
        ViewHelper.setAlpha(distanceView, 0.0f);
        isInvisible = true;
    }

    public void onPause(){
        BusProvider.getInstance().unregister(this);
    }

    public void onResume(){
        BusProvider.getInstance().register(this);
    }

    @Subscribe
    public void onSensorChangedEvent(SensorChangedEvent event){
        refreshArrowBearing(event.userOrientation.bearing);
    }

    @Subscribe
    public void onPositionChangedEvent(PositionChangedEvent event){
        userPosition = event.userPosition;
    }


    @Subscribe
    public void onRouteChangedEvent(RouteChangedEvent event){
        ExtendedWay extendedWay = event.extendedWay;
        DecimalFormat df = new DecimalFormat("#.##");
        this.distanceView.setText(String.format("%s m to exit", df.format(RouteGraphManager.getDistanceInM(extendedWay.distanceToExit))));
        if (extendedWay != null && extendedWay.way != null){
            RouteGraphNode node = extendedWay.getNextNode();
            if (node != null && userPosition != null){
                bearingToNextTarget = (float)Math.toDegrees(extendedWay.getDirection(extendedWay.getCurrentNode()));
            }
        }

        if (isInvisible){
            isInvisible = false;
            arrowView.setVisibility(View.VISIBLE);
            distanceView.setVisibility(View.VISIBLE);
            animate(arrowView).alpha(1.0f).setDuration(1500).setStartDelay(2500).start();
            animate(distanceView).alpha(1.0f).setDuration(1500).setStartDelay(2500).start();
        }
    }


    public void refreshArrowBearing(float bearing){
        float tempBearing = bearing - bearingToNextTarget;
        if (tempBearing > 360) {
            tempBearing -= 360;
        } else if (tempBearing < 0) {
            tempBearing += 360;
        }
        Matrix matrix=new Matrix();
        if (arrowView.getDrawable() != null){
            matrix.postRotate(tempBearing, arrowView.getDrawable().getBounds().width()/2,
                    arrowView.getDrawable().getBounds().height()/2);
            arrowView.setScaleType(ImageView.ScaleType.MATRIX);

            int drawableWidth = arrowView.getDrawable().getBounds().width();
            int drawableHeight = arrowView.getDrawable().getBounds().height();

            //
            // Scale image for view
            //
            float scaleX = (float) arrowView.getWidth() / drawableWidth;
            float scaleY = (float) arrowView.getHeight() / drawableHeight;
            float scale = Math.min(scaleX, scaleY);
            matrix.postScale(scale, scale);

            arrowView.setImageMatrix(matrix);
        }

    }

}
