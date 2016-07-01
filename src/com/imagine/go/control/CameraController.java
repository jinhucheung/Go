package com.imagine.go.control;

import static com.imagine.go.Constants.DEFAULT_CAMERA_DEGREE;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.WindowManager;

/**
 * CameraController
 * 
 * @摄像层控制
 * @author Jinhu
 * @date 2016/4/8
 */
public class CameraController {
	private static final String TAG = CameraController.class.getSimpleName();

	/* 环境 . */
	private Context mContext = null;

	/* 后置摄像头。 */
	private Camera mBackCamera = null;

	/* 可视控件。 */
	private TextureView mTextureView = null;

	/* 预览宽度 . */
	private int mPreviewWidth = -1;
	/* 预览高度 . */
	private int mPreviewHeight = -1;
	/* 预览方向. */
	private int mRotation = -1;

	/** 标记是否已经开始预览。 */
	private boolean mIsPreviewStart = false;

	public CameraController(Context context, TextureView textureView) {
		mContext = context;
		mTextureView = textureView;
	}

	/**
	 * 初始化摄像层预览状态
	 * 
	 * @param previewWidth
	 *            预览宽度
	 * @param previewHeight
	 *            预览高度
	 * @param rotation
	 *            预览方向
	 */
	public void initPreview(int previewWidth, int previewHeight, int rotation) {
		mPreviewWidth = previewWidth;
		mPreviewHeight = previewHeight;
		mRotation = rotation;
	}

	/**
	 * 可视控件状态回调
	 * 
	 * @param callback
	 */
	public void setCameraTextureListener(CameraTextureListener callback) {
		if (null != mTextureView) {
			mTextureView.setSurfaceTextureListener(callback);
		} else {
			Log.w(TAG, "camera preview not init");
		}
	}

	/**
	 * 检查一下给定的预览框尺寸，如果有小于等于0的，就使用默认的尺寸。<br/>
	 * 另外为了保持合理的宽高比，还要在竖屏的时候对宽和高的尺寸进行一下计算。
	 */
	@SuppressWarnings("deprecation")
	private void initPreviewResolution() {
		if (mPreviewWidth <= 0 || mPreviewHeight <= 0) {// 检测是否使用默认值。
			WindowManager wm = (WindowManager) mContext
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			mPreviewWidth = mPreviewWidth <= 0 ? display.getWidth()
					: mPreviewWidth;
			mPreviewHeight = mPreviewHeight <= 0 ? display.getHeight()
					: mPreviewHeight;
		}
		if (mPreviewWidth < mPreviewHeight) { // 预览的时候若是竖屏，则交换宽与高
			mPreviewWidth = mPreviewWidth + mPreviewHeight;
			mPreviewHeight = mPreviewWidth - mPreviewHeight;
			mPreviewWidth = mPreviewWidth - mPreviewHeight;
		}
	}

	/**
	 * 获得最佳预览分辨率。
	 * 
	 * @return 最佳预览分辨率。
	 */
	@SuppressWarnings("deprecation")
	private void initBestPreviewResolution() {
		int screenSize = mPreviewWidth * mPreviewHeight;
		Parameters parameters = mBackCamera.getParameters();
		List<Size> supportedSizes = parameters.getSupportedPreviewSizes();
		if (supportedSizes == null) {
			useDefaultResolution();
			return;
		}
		int optionalPreviewWidth = -1;
		int optionalpreviewHeight = -1;
		int diff = -1;
		Iterator<Size> it = supportedSizes.iterator();
		while (it.hasNext()) {
			Size supportedResolution = it.next();
			int supportedWidth = supportedResolution.width;
			int supportedHeight = supportedResolution.height;
			if (supportedWidth < supportedHeight) {
				supportedWidth = supportedWidth + supportedHeight;
				supportedHeight = supportedWidth - supportedHeight;
				supportedWidth = supportedWidth - supportedHeight;
			}
			if (supportedWidth == mPreviewWidth
					&& supportedHeight == mPreviewHeight) {
				useDefaultResolution();
				return;
			}
			int supportedSize = supportedWidth * supportedHeight;
			int currentDiff = Math.abs(supportedSize - screenSize);
			diff = diff == -1 ? currentDiff : diff;
			if (currentDiff < diff) {
				diff = currentDiff;
				optionalPreviewWidth = supportedWidth;
				optionalpreviewHeight = supportedHeight;
			}
		}
		try {
			parameters.setPreviewSize(optionalPreviewWidth,
					optionalpreviewHeight);
			mBackCamera.setParameters(parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 使用给定的默认分辨率进行预览。
	 */
	@SuppressWarnings("deprecation")
	private void useDefaultResolution() {
		try {
			Parameters parameters = mBackCamera.getParameters();
			parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
			mBackCamera.setParameters(parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化摄像头
	 * 
	 * @return 摄像头对象
	 */
	@SuppressWarnings("deprecation")
	private Camera openCamera() {
		Camera mCamera = null;
		if (Build.VERSION.SDK_INT >= 9) {
			CameraInfo mCameraInfo = new CameraInfo();
			int mCameraID = -1;
			int cameraCount = Camera.getNumberOfCameras(); // 获取摄像头个数
			// 获取后置摄像头的ID
			for (int i = 0; i < cameraCount; i++) {
				Camera.getCameraInfo(i, mCameraInfo);
				if (CameraInfo.CAMERA_FACING_BACK == mCameraInfo.facing) {
					mCameraID = i;
					break;
				}
			}
			if (mCameraID >= 0) {
				mCamera = Camera.open(mCameraID); // 打开摄像头。
				setCameraDisplayOrientation(mCamera, mCameraInfo, true);
				return mCamera;
			}
		}
		if (Build.VERSION.SDK_INT < 9 || null == mCamera) { // 低版本SDK或者上边初始化失败，直接打开摄像头
			mCamera = Camera.open();
			setCameraDisplayOrientation(mCamera, null, false);
		}
		return mCamera;
	}

	/**
	 * 设置后置摄像头的方向<br/>
	 * 不能再预览过程中调用,在预览之前或者之后调用都可以
	 * 
	 * @param camera
	 *            需要进行调整的摄像头
	 * @param cameraInfo
	 *            摄像头的信息
	 * @param useNewMethod
	 *            是否使用新的调整方向的方向
	 */
	@SuppressWarnings("deprecation")
	private void setCameraDisplayOrientation(Camera camera,
			CameraInfo cameraInfo, boolean useNewMethod) {
		if (useNewMethod) {
			int degrees = 0; // 视图需要旋转的角度
			switch (mRotation) {
			case Surface.ROTATION_0: // 竖屏 顶部向上
				degrees = 0;
				break;
			case Surface.ROTATION_90: // 横屏 顶部向左
				degrees = 90;
				break;
			case Surface.ROTATION_180: // 横屏 顶部向右
				degrees = 180;
				break;
			case Surface.ROTATION_270: // 竖屏 顶部向下
				degrees = 270;
				break;
			}
			int result = (cameraInfo.orientation - degrees + 360) % 360;
			camera.setDisplayOrientation(result); // 摄像头一直正对前方,不随视图方位变化而变化
		} else {
			camera.setDisplayOrientation(DEFAULT_CAMERA_DEGREE);
		}
	}

	/**
	 * 释放资源
	 */
	public void onPause() {
		if (null != mBackCamera) {
			mBackCamera.stopPreview();
			mBackCamera.lock();
			mBackCamera.release();
			mIsPreviewStart = false;
			mBackCamera = null;
		}
	}

	public boolean isCameraOpen() {
		return null != mBackCamera;
	}

	/**
	 * 可视控件状态回调
	 * 
	 * @author Jinhu
	 * @date 2016/4/8
	 */
	public class CameraTextureListener implements SurfaceTextureListener {

		public CameraTextureListener() {
			initPreviewResolution();
		}

		@SuppressWarnings("deprecation")
		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface,
				int width, int height) {
			mBackCamera = openCamera();
			initBestPreviewResolution();
			try {
				// 通过给定的holder控制的surfaceView来取景
				mBackCamera.setPreviewTexture(surface);
				mBackCamera.startPreview();
				mIsPreviewStart = true;
			} catch (IOException e) {
				e.printStackTrace();
				mIsPreviewStart = false;
			}

		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
				int width, int height) {

		}

		@SuppressWarnings("deprecation")
		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
			if (null != mBackCamera && mIsPreviewStart) {
				mBackCamera.stopPreview();
				mBackCamera.release();
				mBackCamera = null;
			}
			return true;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {

		}

	}
}
