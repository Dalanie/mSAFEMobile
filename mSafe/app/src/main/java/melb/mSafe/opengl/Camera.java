package melb.mSafe.opengl;

import melb.mSafe.model.Vector3D;
import melb.mSafe.opengl.animation.PathAnimation;

import android.opengl.Matrix;

public class Camera {

	private float mDistance;
	private Vector3D mRotationVector;
	private Vector3D mLookAtVector;
	private float[] mModelMatrix;
	private boolean mRecalculate = true;
    private float[] mLookingTo;
	private PathAnimation mPathAnimation;

	public Camera() {
		mDistance = 2;
		mRotationVector = new Vector3D(0, 0, 0);
		mLookAtVector = new Vector3D(0, 0, 0);
		mModelMatrix = new float[16];
		Matrix.setIdentityM(mModelMatrix, 0);
	}

	public float[] getViewMatrix() {
		if (mRecalculate) {
			mLookingTo = new float[16];
			float[] currentTarget;
			if (mPathAnimation != null && mPathAnimation.isRunnning()){
				float[] values;
				values = mPathAnimation.animate();
				currentTarget = new float[] { mLookAtVector.getX() + values[2],
						mLookAtVector.getY() + values[3], mLookAtVector.getZ() + values[4], 1 };
			}else{
				currentTarget = new float[] { mLookAtVector.getX(),
						mLookAtVector.getY(), mLookAtVector.getZ(), 1 };
			}
			/*
			 * Set the camera position (View matrix) by multipling the desired point 
			 * with the already calculated mModelMatrix
			 */
			float[] targetVector = new float[4];
			Matrix.multiplyMV(targetVector, 0, mModelMatrix, 0, currentTarget,
					0);
			Vector3D transformedTarget = new Vector3D(targetVector[0],
					targetVector[1], targetVector[2]);
			mLookingTo = getLookAtM(transformedTarget);
		}
		return mLookingTo;
	}

	private float[] getLookAtM(Vector3D lookAtTarget) {
		float[] lookingTo = new float[16];

		double x = mDistance
				* Math.sin(Math.toRadians(mRotationVector.getZ())
						* Math.abs(Math.toRadians(Math.cos(mRotationVector
								.getX()))));
		double y = mDistance * Math.sin(Math.toRadians(mRotationVector.getX()));
		double z = mDistance
				* Math.cos(Math.toRadians(mRotationVector.getZ())
						* Math.abs(Math.toRadians(Math.cos(mRotationVector
								.getX()))));

		Vector3D dist = new Vector3D((float) x, (float) y, (float) z);
		Vector3D eyePt = Vector3D.subtract(lookAtTarget, dist);
		Vector3D up = new Vector3D(0.0f, 1.0f, 0.0f);

        int mOffset = 0;
        Matrix.setLookAtM(lookingTo, mOffset, eyePt.getX(), eyePt.getY(),
				eyePt.getZ(), lookAtTarget.getX(), lookAtTarget.getY(), lookAtTarget.getZ(),
				up.getX(), up.getY(), up.getZ());

		mRecalculate = false;
		return lookingTo;
	}

	public void setLookAtPosition(Vector3D lookAtVector) {
		mLookAtVector = lookAtVector;
		mRecalculate = true;
	}

	public void setDistance(float distance) {
		mDistance = distance;
		mRecalculate = true;
	}

	public void setRotation(Vector3D rotationVector) {
		mRotationVector = rotationVector;
		mRecalculate = true;
	}

	public void setModelMatrix(float[] modelMatrix) {
		mModelMatrix = modelMatrix;
		mRecalculate = true;
	}

	public void setAnimation(PathAnimation pathAnimation) {
		mPathAnimation = pathAnimation;
		mPathAnimation.start();
		mRecalculate = true;
	}
}
