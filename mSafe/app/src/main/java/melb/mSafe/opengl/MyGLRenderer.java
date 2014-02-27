/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package melb.mSafe.opengl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import melb.mSafe.model.Vector3D;
import melb.mSafe.opengl.animation.PathAnimation;
import melb.mSafe.opengl.drawable.Model3DGL;
import melb.mSafe.opengl.utilities.FPSHelper;

/**
 * Provides drawing instructions for a GLSurfaceView object. This class must
 * override the OpenGL ES drawing lifecycle methods:
 * <ul>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    public static final float DEFAULT_ROTATION_PERSPECTIVE = 27f;
    public static final float DEFAULT_ROTATION_BIRD = 0f;
    public static final String TAG = "MyGLRenderer";

    /**
     * Store the model matrix. This matrix is used to move models from object
     * space (where each model can be thought of being located at the center of
     * the universe) to world space.
     */
    private float[] mModelMatrix = new float[16];

    /**
     * Allocate storage for the final combined matrix. This will be passed into
     * the shader program. mMVPMatrix is an abbreviation for
     * "Model View Projection Matrix"
     */
    private final float[] mMVMatrix = new float[16];

    /**
     * Store the projection matrix. This is used to project the scene onto a 2D
     * viewport.
     */
    private final float[] mProjectionMatrix = new float[16];

    private static final float nearPlaneDistance = 1f;//1f;
    private static final float farPlaneDistance = 20f;//16f;

    private float mZoomLevel = MyGLSurfaceView.DEFAULT_SCALE;

    /*
     * building otherwise on the wrong side
     */
    private float defaultRotationX = 27f;//100;
    private float defaultRotationZ = 0;//180;

    private float rotationX = defaultRotationX;
    private float rotationY = 0.0f;
    private float rotationZ = 0.0f;

    private float translateX = 0.0f;
    private float translateY = 0.0f;
    private float translateZ = 0.0f;

    private float ratio;
    private float width;
    private float height;
    private float distance = -2f;
    private boolean focusOnTarget = true;

    public Model3DGL model3d;
    private float[] backgroundColor = { 0f,0f,0f,0f};

    private Context context;
    private Camera camera;



    public MyGLRenderer(Model3DGL model3d, Context context) {
        this.model3d = model3d;
        this.context = context;
        this.camera = new Camera();
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(backgroundColor[0], backgroundColor[1],
                backgroundColor[2], backgroundColor[3]);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        model3d.initWithGLContext(context);
    }

    static private float tmpMatrix[] = new float[16];
    static private float resMatrix[] = new float[16];


    @Override
    public void onDrawFrame(GL10 unused) {
        //less battery drain
        FPSHelper.limitFPS();

		/*
		 * Draw background color
		 */
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		/*
		 * scale model down to smaller values
		 */
        float r = model3d.getRatio();
        float s = mZoomLevel;
        Matrix.setIdentityM(mModelMatrix, 0);

        // move model origin to its center
        Matrix.setIdentityM(tmpMatrix, 0);
        Matrix.translateM(tmpMatrix, 0, -model3d.getWidth() / 2f,
                -model3d.getLength() / 2f, -model3d.getHeight() / 2f);
        Matrix.multiplyMM(resMatrix, 0, tmpMatrix, 0, mModelMatrix, 0);
        System.arraycopy(resMatrix, 0, mModelMatrix, 0, 16);

        // translate to world position
        Matrix.setIdentityM(tmpMatrix, 0);
        Matrix.translateM(tmpMatrix, 0, translateX, translateY, translateZ);
        Matrix.multiplyMM(resMatrix, 0, tmpMatrix, 0, mModelMatrix, 0);
        System.arraycopy(resMatrix, 0, mModelMatrix, 0, 16);

        // rotate around center
        Matrix.setIdentityM(tmpMatrix, 0);
        if (rotationX != 0) {
            Matrix.rotateM(tmpMatrix, 0, rotationX, 1.0f, 0.0f, 0.0f);
        }
        if (rotationY != 0) {
            Matrix.rotateM(tmpMatrix, 0, rotationY, 0.0f, 1.0f, 0.0f);
        }
        if (rotationZ != 0) {
            Matrix.rotateM(tmpMatrix, 0, rotationZ, 0.0f, 0.0f, 1.0f);
        }

        Matrix.multiplyMM(resMatrix, 0, tmpMatrix, 0, mModelMatrix, 0);
        System.arraycopy(resMatrix, 0, mModelMatrix, 0, 16);

        // scale down
        Matrix.setIdentityM(tmpMatrix, 0);
        Matrix.scaleM(tmpMatrix, 0, r * s, r * s, r * s);
        Matrix.multiplyMM(resMatrix, 0, tmpMatrix, 0, mModelMatrix, 0);
        System.arraycopy(resMatrix, 0, mModelMatrix, 0, 16);

        // stick camera to modelmatrix
        if (focusOnTarget) {
            camera.setModelMatrix(mModelMatrix);
            camera.setLookAtPosition(model3d.getUserPosition());
            //camera.setRotation(model3d.getOrientationToNextTarget());
            camera.setDistance(distance);

            /*
            Vector3D orientationVector = model3d.getOrientationToNextTarget();
            if (orientationVector != null){
                rotationZ = orientationVector.getZ();
                camera.setRotation(new Vector3D(0, 0, rotationZ));
            }
            */
        }
        /*
      Store the view matrix. This can be thought of as our camera. This matrix
      transforms world space to eye space; it positions things relative to our
      eye.
     */
        float[] mViewMatrix = camera.getViewMatrix();

		/*
		 * combine the model with the view matrix
		 */
        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

		/*
		 * this projection matrix is applied to object coordinates in the
		 * onDrawFrame() method
		 */
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, 1, -1,
                nearPlaneDistance, farPlaneDistance);

		/*
		 * Calculate the projection and view transformation
		 */
        float[] mMVPMatrix = new float[16];
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);

		/*
		 * all the drawing stuff inside the model-object (otherwise
		 * translation/rotation wouldn't affect every object)
		 */
        model3d.draw(mMVPMatrix);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
		/*
		 * Adjust the viewport based on geometry changes, such as screen
		 * rotation
		 */
        GLES20.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;
        ratio = (float) width / height;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getDistance() {
        return distance;
    }

    public float getRotationX() {
        return rotationX;
    }

    public void setRotationX(float rotationX) {
        this.rotationX = rotationX;
        //this.rotationX = defaultRotationX + rotationX;
    }

    public void addTranslation(float mPosX, float mPosY) {
        this.translateX = mPosX;
        this.translateY = mPosY;
    }

    public void downPressed() {
        focusOnTarget = false;
    }

    public void upPressed() {
        focusOnTarget = true;
        int timeToFinish = 30000;
        List<Vector3D> points = new ArrayList<Vector3D>();
        points.add(new Vector3D(0, 0, 0));
        points.add(new Vector3D(model3d.getLength(), 0f, 0f));
        points.add(new Vector3D(model3d.getLength(), model3d.getWidth(), 0f));
        points.add(new Vector3D(0f, model3d.getWidth(), 0f));
        points.add(new Vector3D(0, 0, 0));

        camera.setAnimation(new PathAnimation(timeToFinish,
                PathAnimation.INFINITY, points));
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void setTranslation(Float x, Float y, Float z) {
        if (x != null) {
            this.translateX = -x;
        }
        if (y != null) {
            this.translateY = -y;
        }
        if (z != null) {
            this.translateZ = -z;
        }
    }

    public void setRotation(Float x, Float y, Float z) {
        if (x != null) {
            //
            this.rotationX = x;
            //this.rotationX = defaultRotationX + x;
        }
        if (y != null) {
            this.rotationY = y;
        }
        if (z != null) {
            this.rotationZ = z;
            //this.rotationZ = defaultRotationZ + z;
        }
    }

    public void setScale(float scale) {
        this.mZoomLevel = scale;
    }

    public float getDefaultRotationX() {
        return defaultRotationX;
    }
}