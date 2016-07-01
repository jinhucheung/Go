package com.imagine.go.activity;

import static com.imagine.go.Constants.IS_DEBUG;
import android.hardware.GeomagneticField;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.imagine.go.ar.ARData;
import com.imagine.go.ar.model.Matrix;

/**
 * AR坐标系转换
 * 
 * @author Jinhu
 * @date 2016/4/20
 */
public class ARCoordActivity extends SensorActivity {
	private static final String TAG = ARCoordActivity.class.getSimpleName();

	// ---------------------传感器数据--------------------
	/* 传感器返回的世界坐标旋转矩阵 . */
	private static final Matrix worldCoord = new Matrix();

	/* 世界坐标旋转矩阵进过补偿后的矩阵 . */
	private static final Matrix magneticCompensatedCoord = new Matrix();

	/* 地理位置磁场补偿矩阵 . */
	private static final Matrix magneticNorthCompensation = new Matrix();
	/* 绕X轴旋转矩阵. */
	private static final Matrix XAxisRotation = new Matrix();

	/* 估算正北磁偏量 . */
	private static GeomagneticField gmf = null;

	static {
		double angleX = Math.toRadians(-90);
		XAxisRotation.set(1f, 0f, 0f, 0f, (float) Math.cos(angleX),
				(float) -Math.sin(angleX), 0f, (float) Math.sin(angleX),
				(float) Math.cos(angleX));
	}

	/**
	 * 生成补偿矩阵
	 */
	private void generateCompenstationMatrix() {
		// 通过地理点估算正北磁偏量
		gmf = new GeomagneticField((float) ARData.getInstance().getLocation()
				.getLatitude(), (float) ARData.getInstance().getLocation()
				.getLongitude(), (float) ARData.getInstance().getLocation()
				.getAltitude(), System.currentTimeMillis());
		// 用于补偿传感器获得的世界坐标
		double angleY = Math.toRadians(-gmf.getDeclination());

		synchronized (magneticNorthCompensation) {
			magneticNorthCompensation.toIdentity();

			// 绕Y轴旋转 进行地理位置磁偏量的补偿
			magneticNorthCompensation.set((float) Math.cos(angleY), 0f,
					(float) Math.sin(angleY), 0f, 1f, 0f,
					(float) -Math.sin(angleY), 0f, (float) Math.cos(angleY));

			// 绕X轴旋转 进行设备摆放状态的补偿
			magneticNorthCompensation.prod(XAxisRotation);
		}
	}

	// ------------------------ 生命周期 ------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (IS_DEBUG) {
			Log.d(TAG, " --onCreated()--");
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		generateCompenstationMatrix();

		if (IS_DEBUG) {
			Log.d(TAG, "--OnStared()--");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (IS_DEBUG) {
			Log.d(TAG, "--OnResumed()--");
		}
	}

	/**
	 * 传感器数据处理
	 */
	@Override
	protected void onSensorAccess() {
		worldCoord.set(mRotation[0], mRotation[1], mRotation[2], mRotation[3],
				mRotation[4], mRotation[5], mRotation[6], mRotation[7],
				mRotation[8]);

		// 生成补偿矩阵
		magneticCompensatedCoord.toIdentity();

		synchronized (magneticNorthCompensation) {
			magneticCompensatedCoord.prod(magneticNorthCompensation);
		}

		magneticCompensatedCoord.prod(worldCoord);

		magneticCompensatedCoord.invert();

		ARData.getInstance().setRotationMatrix(magneticCompensatedCoord);

		if (IS_DEBUG) {
			float[] mOrientation = new float[3];
			SensorManager.getOrientation(mRotation, mOrientation);
			float azimuth = (float) ((mOrientation[0] * 180) / Math.PI);
			Log.d(TAG, "azimuth= " + azimuth);
		}
	}

	/**
	 * 定位信息回调
	 */
	@Override
	public void onLocationSucceeded(AMapLocation amapLocation) {
		super.onLocationSucceeded(amapLocation);
		ARData.getInstance().setLocation(amapLocation);

		generateCompenstationMatrix();
	}

}
