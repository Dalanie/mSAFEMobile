package melb.mSafe.opengl.drawable;

import android.opengl.GLES20;

import java.util.List;

import melb.mSafe.common.ExtendedWay;
import melb.mSafe.model.RouteGraphEdge;
import melb.mSafe.model.Vector3D;
import melb.mSafe.model.Way;
import melb.mSafe.opengl.animation.PathAnimation;
import melb.mSafe.opengl.utilities.FloatBufferHelper;
import melb.mSafe.opengl.utilities.Helper;
import melb.mSafe.opengl.utilities.ShaderProgram;

public class WayGL implements IDrawableObject {
    private boolean shouldAnimate;
    private IDrawableObject glWay;
    private IDrawableObject animatedWayArrow;
    private PathAnimation animation;
    private static final float arrowSize = 30f;
    private float[] arrowColor;
    private boolean isVisible = true;
    private static final int COLOR_COUNT = 4; //r,g,b,a
    private static final int POSITION_COUNT = 3; //x,y,z
    private static final int OFFSET = COLOR_COUNT+POSITION_COUNT;
    private static final float DEFAULT_ALPHA = 0.3f;
    private static final float HIGH_RISK = 2;
    private static final float LOW_RISK = 1;

    public WayGL(ExtendedWay extendedWay, boolean shouldAnimate, long timeToFinish, boolean withAlpha){
        Way way = extendedWay.way;
        this.shouldAnimate = shouldAnimate;
        float[] linesOfWay = new float[way.getNodes().size()*OFFSET];
        for (int i = 0; i < way.getNodes().size(); i++){
            int counter = 0;
            //x,y,z
            linesOfWay[(i * OFFSET) + counter++] = way.getNodes().get(i).position.getX();
            linesOfWay[(i * OFFSET) + counter++] = way.getNodes().get(i).position.getY();
            linesOfWay[(i * OFFSET) + counter++] = way.getNodes().get(i).position.getZ();
            float[] colors;
            //r,g,b,a
            RouteGraphEdge edge = extendedWay.getNextEdge(i);
            if (edge != null){
                colors = getColorByWeight(edge.getWeight());
            }else{
                colors = getColorByWeight(LOW_RISK);
            }

            linesOfWay[(i * OFFSET) + counter++] = colors[0];
            linesOfWay[(i * OFFSET) + counter++] = colors[1];
            linesOfWay[(i * OFFSET) + counter++] = colors[2];
            linesOfWay[(i * OFFSET) + counter++] = colors[3];
        }
        glWay = new PolygonGL(linesOfWay, GLES20.GL_LINE_STRIP, 0, withAlpha);

        if (shouldAnimate) {
            int currentIndex = extendedWay.currentPositionIndex; //TODO arrow only from your start position!
            animation = new PathAnimation(timeToFinish, PathAnimation.INFINITY,
                    extendedWay.getPointList());
            animation.start();
            arrowColor = getColorByWeight(LOW_RISK);
            animatedWayArrow = new PolygonGL(
                    FloatBufferHelper.createArrow(arrowSize, arrowColor));
        }
    }
    private float[] getColorByWeight(double weight){
        float[] tempColor = new float[4];
        if (weight >= HIGH_RISK){
            //red
            tempColor[0] = 1;
            tempColor[1] = 0;
            tempColor[2] = 0;
            tempColor[3] = DEFAULT_ALPHA;
        }else if (weight > LOW_RISK){
            //yellow/orange
            tempColor[0] = 1;
            tempColor[1] = 1;
            tempColor[2] = 0.5f;
            tempColor[3] = DEFAULT_ALPHA;
        }else{
            //green
            tempColor[0] = 0;
            tempColor[1] = 1;
            tempColor[2] = 0;
            tempColor[3] = DEFAULT_ALPHA;
        }
        return tempColor;
    }

    public WayGL(List<Vector3D> points, float[] color, boolean shouldAnimate,
                 long timeToFinish) {
        this.shouldAnimate = shouldAnimate;
        this.arrowColor = color;
        float[] linesOfWay = new float[points.size() *OFFSET];
        for (int i = 0; i < points.size(); i++) {
            int counter = 0;
            // x,y,z
            linesOfWay[(i * OFFSET) + counter++] = points.get(i).getX();
            linesOfWay[(i * OFFSET) + counter++] = points.get(i).getY();
            linesOfWay[(i * OFFSET) + counter++] = points.get(i).getZ();

            // rgba
            linesOfWay[(i * OFFSET) + counter++] = color[0];
            linesOfWay[(i * OFFSET) + counter++] = color[1];
            linesOfWay[(i * OFFSET) + counter++] = color[2];
            linesOfWay[(i * OFFSET) + counter++] = color[3];
        }
        glWay = new PolygonGL(linesOfWay, GLES20.GL_LINE_STRIP, 0);

        if (shouldAnimate) {
            animation = new PathAnimation(timeToFinish, PathAnimation.INFINITY,
                    points);
            animation.start();
            animatedWayArrow = new PolygonGL(
                    FloatBufferHelper.createArrow(arrowSize, arrowColor));
        }
    }

    @Override
    public void draw(float[] mvpMatrix, ShaderProgram program) {
        if (isVisible) {
            float[] translateMatrix = Helper.translateModel(
                    new float[] { 0,0,
                            1 }, mvpMatrix);
            glWay.draw(translateMatrix, program);
            if (shouldAnimate) {
                float[] animatedMatrix = Helper.animateObject(
                        animation.animate(), translateMatrix, false, arrowSize, arrowSize, 0);
                animatedWayArrow.draw(animatedMatrix, program);
            }
        }
    }

    @Override
    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

}
