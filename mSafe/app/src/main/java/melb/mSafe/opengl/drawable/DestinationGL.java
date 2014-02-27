package melb.mSafe.opengl.drawable;

import android.opengl.GLES20;

import java.util.ArrayList;
import java.util.List;

import melb.mSafe.model.Vector3D;
import melb.mSafe.opengl.animation.SizeAnimation;
import melb.mSafe.opengl.animation.ValueAnimation;
import melb.mSafe.opengl.utilities.FloatBufferHelper;
import melb.mSafe.opengl.utilities.Helper;
import melb.mSafe.opengl.utilities.ShaderProgram;

public class DestinationGL implements IDrawableObject {

    private static final float arrowSize = 30f;
    private static final float circleRadius = 15;
    private boolean visible;
    private List<IDrawableObject> circles;
    private IDrawableObject directionArrow;
    private boolean shouldAnimate;
    private SizeAnimation sizeAnimation;
    private ValueAnimation rotationAnimation;
    private ValueAnimation translationAnimation;
    private Vector3D mPosition;

    public DestinationGL(boolean shouldAnimate, Vector3D position) {
        this.shouldAnimate = shouldAnimate;
        mPosition = position;
        if (shouldAnimate) {
            sizeAnimation = new SizeAnimation(SizeAnimation.INFINITY, 3000, 1f, 1f,
                    1f, 2f, 2f, 2f);
            sizeAnimation.start();
            rotationAnimation = new ValueAnimation(ValueAnimation.INFINITY,3000, new float[]{0}, new float[]{360});
            rotationAnimation.start();

            translationAnimation = new ValueAnimation(ValueAnimation.INFINITY, 2500, new float[]{1.5f}, new float[]{0.5f});
            translationAnimation.start();
        }
        float[] arrowColor = {1.0f, 1.0f, 1.0f, 1.0f};
        directionArrow = new PolygonGL(FloatBufferHelper.createArrow(
                arrowSize, arrowColor));

        circles = new ArrayList<IDrawableObject>();
        int numberOfCircles = 5;
        float percentageLessPerCircle = 1f/ numberOfCircles;

        float[][] circleColors = {new float[]{1.0f, 0.0f, 0.0f, 1.0f}, new float[]{1.0f, 1.0f, 1.0f, 1.0f}};
        for(int i = 0; i < numberOfCircles; i++){
            circles.add(new PolygonGL(FloatBufferHelper.createCircle(20, circleRadius - (i*percentageLessPerCircle*circleRadius), 0, 0, 0,
                    circleColors[i % 2], circleColors[i % 2]), GLES20.GL_TRIANGLE_FAN, 0, true));
        }
    }

    @Override
    public void draw(float[] mvpMatrix, ShaderProgram program) {
        if (mPosition != null) {
            float postionOffset = 1f;
            float[] translateMatrix = Helper.translateModel(
                    new float[] { mPosition.getX(), mPosition.getY(),
                            mPosition.getZ() + postionOffset}, mvpMatrix);
            float[] rotatedMatrix = new float[16];

            System.arraycopy(translateMatrix, 0, rotatedMatrix, 0, 16);
            if (shouldAnimate) {
                float[] newValues = sizeAnimation.animate();
                for (int i = 0; i < circles.size(); i++){
                    IDrawableObject object = circles.get(i);
                    translateMatrix = Helper.translateModel(0,0, 0.1f, translateMatrix);
                    object.draw(Helper.scaleModel(newValues, translateMatrix), program);
                }
            }

            //first rotate by y to get a normal
            Helper.rotateModel(rotatedMatrix, null, 90f, null, false, 0f, 0f, 0f);

            if(shouldAnimate){
                Helper.rotateModel(rotatedMatrix,rotationAnimation.animate()[0], null, null , true, 0f, arrowSize, 0f);
            }

            rotatedMatrix = Helper.translateModel(-30f * translationAnimation.animate()[0], 0, 0, rotatedMatrix);


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

}
