package melb.mSafe.views;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * This class shows the preview pictures of the camera
 * 
 * @author Daniel Langerenken
 * 
 */
public class CameraPreviewView extends SurfaceView implements
		SurfaceHolder.Callback {
	private static final String TAG = "Preview";

	SurfaceHolder mHolder;
	Camera mCamera;
	private Activity activity;

	@SuppressWarnings("deprecation")
	public CameraPreviewView(Activity activity) {
		super(activity);
		this.activity = activity;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException exception) {
			mCamera.release();
			mCamera = null;
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.05;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		if (mCamera != null) {
			Camera.Parameters parameters = mCamera.getParameters();

			List<Size> sizes = parameters.getSupportedPreviewSizes();
			Size optimalSize = getOptimalPreviewSize(sizes, w, h);
			parameters.setPreviewSize(optimalSize.width, optimalSize.height);

			mCamera.setParameters(parameters);
			setCameraDisplayOrientation(activity, 0, mCamera);
			mCamera.startPreview();
		}
	}

	public static void setCameraDisplayOrientation(Activity context,
			int cameraId, Camera camera) {

		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);

		int rotation = context.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;

		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}

	/**
	 * When this function returns, mCamera will be null.
	 */
	public void stopPreviewAndFreeCamera() {

		if (mCamera != null) {
			/*
			 * Call stopPreview() to stop updating the preview surface.
			 */
			mCamera.stopPreview();

			/*
			 * Important: Call release() to release the camera for use by other
			 * applications. Applications should release the camera immediately
			 * in onPause() (and re-open() it in onResume()).
			 */
			mCamera.release();
			mCamera = null;
		}
	}

}
