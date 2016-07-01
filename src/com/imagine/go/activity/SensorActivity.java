package com.imagine.go.activity;

import static com.imagine.go.Constants.IS_DEBUG;

import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import com.imagine.go.ar.LowPassFilter;

/**
 * SensorActivity<br/>
 * 获取传感器数据
 * 
 * @author Jinhu
 * @date 2016/4/12
 */
public abstract class SensorActivity extends OriginActivity implements
		SensorEventListener {
	private static final String TAG = SensorActivity.class.getSimpleName();

	/* 传感器速率 . */
	protected int mDelayRate = SensorManager.SENSOR_DELAY_NORMAL;

	/* 传感器管理器 . */
	protected SensorManager mSensorMag;

	/* 加速度传感器. */
	protected Sensor mGravSensor;
	/* 磁场传感器. */
	protected Sensor mMagSensor;

	/* 加速度传感器数据. */
	protected float[] mGrav = new float[3];
	protected float[] mGravSensorVals = new float[3];

	/* 磁场传感器数据. */
	protected float[] mMag = new float[3];
	protected float[] mMagSensorVals = new float[3];

	/* 旋转矩阵 . */
	protected float[] mRotation = new float[9];
	protected float[] mRTmp = new float[9];

	/* 旋转矩阵的方向向量. */
	protected volatile float[] mOrientation = new float[3];

	/**
	 * 回调接口
	 */
	protected abstract void onSensorAccess();

	// ------------------------ 生命周期 ------------------------
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSensorMag = (SensorManager) getSystemService(SENSOR_SERVICE);

		if (IS_DEBUG) {
			Log.d(TAG, " --onCreated()--");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// 获取加速度传感器
		List<Sensor> mSensors = mSensorMag
				.getSensorList(Sensor.TYPE_ACCELEROMETER);
		
		if (mSensors.size() > 0) {
			mGravSensor = mSensors.get(0);
		}

		// 获取磁场加速度传感器
		mSensors = mSensorMag.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
		if (mSensors.size() > 0) {
			mMagSensor = mSensors.get(0);
		}

		// 注册传感器监听
		mSensorMag.registerListener(this, mGravSensor, mDelayRate);

		mSensorMag.registerListener(this, mMagSensor, mDelayRate);

		if (IS_DEBUG) {
			Log.d(TAG, "--OnResumed()--");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 取消监听
		mSensorMag.unregisterListener(this, mGravSensor);
		mSensorMag.unregisterListener(this, mMagSensor);

		if (IS_DEBUG) {
			Log.d(TAG, "--OnPaused()--");
		}
	}

	@Override
	public synchronized void onSensorChanged(SensorEvent event) {

		if (Sensor.TYPE_ACCELEROMETER == event.sensor.getType()) {
			mGravSensorVals = LowPassFilter.filter(0.5f, 1.0f,
					event.values.clone(), mGravSensorVals);
			mGrav[0] = mGravSensorVals[0];
			mGrav[1] = mGravSensorVals[1];
			mGrav[2] = mGravSensorVals[2];

		} else if (Sensor.TYPE_MAGNETIC_FIELD == event.sensor.getType()) {
			mMagSensorVals = LowPassFilter.filter(2.0f, 4.0f,
					event.values.clone(), mMagSensorVals);
			mMag[0] = mMagSensorVals[0];
			mMag[1] = mMagSensorVals[1];
			mMag[2] = mMagSensorVals[2];

		}

		if (null != mGravSensorVals && null != mMagSensorVals) {
			SensorManager.getRotationMatrix(mRTmp, null, mGrav, mMag);

			// 重新规定传感器坐标系
			switch (getRotation()) {
			case Surface.ROTATION_0:
				SensorManager.remapCoordinateSystem(mRTmp,
						SensorManager.AXIS_X, SensorManager.AXIS_Y, mRotation);
				break;
			case Surface.ROTATION_90: // 横屏 顶部向左
				SensorManager.remapCoordinateSystem(mRTmp,
						SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
						mRotation);
				break;
			case Surface.ROTATION_180: // 横屏 顶部向右
				break;
			case Surface.ROTATION_270: // 竖屏 顶部向下
				break;
			}

			// 获取方向向量
			SensorManager.getOrientation(mRotation, mOrientation);

			// 回调
			onSensorAccess();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		if (null == sensor)
			throw new NullPointerException();

		if (Sensor.TYPE_MAGNETIC_FIELD == sensor.getType()
				&& SensorManager.SENSOR_STATUS_UNRELIABLE == accuracy) {
			Log.e(TAG, "Compass data unreliable");
		}
	}

}
