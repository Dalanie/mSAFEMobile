package melb.mSafe.opengl.drawable;

import android.opengl.GLES20;

import melb.mSafe.opengl.animation.SizeAnimation;
import melb.mSafe.opengl.utilities.FloatBufferHelper;
import melb.mSafe.opengl.utilities.Helper;
import melb.mSafe.opengl.utilities.ShaderProgram;
import melb.mSafe.model.Vector3D;

public class UserPositionGL implements IDrawableObject {

    private static final float arrowSize = 30f;
    private static final float circleRadius = 15;
    private boolean visible;
    private IDrawableObject directionArrow;
    private IDrawableObject currentPositionRadius;
    private boolean shouldAnimate;
    private SizeAnimation animation;
    private Vector3D mUserPosition;
    private float mUserOrientation = 0;

    public UserPositionGL(boolean shouldAnimate, Vector3D userPosition) {
        this.shouldAnimate = shouldAnimate;
        mUserPosition = userPosition;
        if (shouldAnimate) {
            animation = new SizeAnimation(SizeAnimation.INFINITY, 2000, 1f, 1f,
                    1f, 3f, 3f, 3f);
            animation.start();
        }
        float[] arrowColor = {1.0f, 1.0f, 1.0f, 1.0f};
        directionArrow = new PolygonGL(FloatBufferHelper.createArrow(
                arrowSize, arrowColor));
        float[] circleColor = {0.58f, 1f, 0.52f, 1.0f};
        float[] centerCircleColor = {0.58f, 1f, 0.52f, 0.3f};
        currentPositionRadius = new PolygonGL(
                FloatBufferHelper.createCircle(20, circleRadius, 0, 0, 0,
                        circleColor, centerCircleColor), GLES20.GL_TRIANGLE_FAN, 0, true);
    }

    @Override
    public void draw(float[] mvpMatrix, ShaderProgram program) {
        if (mUserPosition != null) {
            float postionOffset = 1f;
            float[] translateMatrix = Helper.translateModel(
                    new float[] { mUserPosition.getX(), mUserPosition.getY(),
                            mUserPosition.getZ() + postionOffset}, mvpMatrix);
            if (shouldAnimate) {
                float[] newValues = animation.animate();
                currentPositionRadius.draw(
                        Helper.scaleModel(newValues, translateMatrix), program);
            }
			/*
			 * first get center of arrow
			 *
			 */

            float[] rotatedMatrix = new float[16];
            System.arraycopy(translateMatrix, 0, rotatedMatrix, 0, 16);
            Helper.rotateModel(rotatedMatrix, null, null, mUserOrientation, false, arrowSize, arrowSize, 0f);
            rotatedMatrix = Helper.translateModel(0, 0
                    , 1f, rotatedMatrix);
            directionArrow.draw(rotatedMatrix, program);
        }
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setUserPosition(Vector3D userPosition) {
        this.mUserPosition = userPosition;
    }

    public void setUserOrientation(float userOrientation) {
        mUserOrientation = userOrientation;
    }
}
