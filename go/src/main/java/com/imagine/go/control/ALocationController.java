package com.imagine.go.control;

import static com.imagine.go.Constants.IS_DEBUG;

import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;

/**
 * ALocationController:高德地图定位控制
 * 
 * @author Jinhu
 * @date 2016/3/22
 */
public class ALocationController implements AMapLocationListener {
	private static final String TAG = ALocationController.class.getSimpleName();

	/* 初次定位标记 . */
	public static boolean Is_Frist_Locate = true;

	/* 定位运行标志. */
	private AtomicBoolean isLocating = new AtomicBoolean(false);

	/* 定位客服端 . */
	private AMapLocationClient mLocationClient;

	/* 声明定位配置参数 . */
	private AMapLocationClientOption mLocationOption = null;
	/* 设置定位模式为高精度模式 . */
	/* 使用网络与GPS进行定位 . */
	private static final AMapLocationMode mLocationMode = AMapLocationMode.Hight_Accuracy;
	/* 设置定位间隔,单位毫秒,默认为10000ms . */
	private static final int mInterval = 10 * 1000;

	/**
	 * 对外回调接口 .
	 */
	private ALocationListener aLocationListener;

	public interface ALocationListener {
		// 定位成功
		void onLocationSucceeded(AMapLocation amapLocation);
	}

	public ALocationController(Context context) {
		onCreate(context);
	}

	/**
	 * 初始化定位客服端
	 */
	private void onCreate(Context context) {
		if (null == context) {
			throw new NullPointerException("AMapLocationClient need Context");
		}
		// 初始化定位
		mLocationClient = new AMapLocationClient(context);
		// 设置定位回调监听
		mLocationClient.setLocationListener(this);

		// 配置定位参数
		mLocationOption = new AMapLocationClientOption();
		// 设置定位模式为高精度模式
		mLocationOption.setLocationMode(mLocationMode);
		// 设置是否返回地址信息（默认返回地址信息）
		mLocationOption.setNeedAddress(true);
		// 设置是否只定位一次,默认为false
		mLocationOption.setOnceLocation(false);
		// 设置是否强制刷新WIFI，默认为强制刷新
		mLocationOption.setWifiActiveScan(true);
		// 设置是否允许模拟位置,默认为false，不允许模拟位置
		mLocationOption.setMockEnable(false);
		// 设置定位间隔
		mLocationOption.setInterval(mInterval);
		// 给定位客户端对象设置定位参数
		mLocationClient.setLocationOption(mLocationOption);

		if (IS_DEBUG) {
			Log.d(TAG, "--ALocationController created---");
		}
	}

	/**
	 * 启动定位
	 */
	public void onStart() {
		mLocationClient.startLocation();
		isLocating.set(true);
		if (IS_DEBUG) {
			Log.d(TAG, "--started LocationServer---");
		}
	}

	/**
	 * 停止定位
	 */
	public void onStop() {
		mLocationClient.stopLocation();
		isLocating.set(false);
		if (IS_DEBUG) {
			Log.d(TAG, "--stoped LocationServer---");
		}
	}

	/**
	 * 销毁定位客服端
	 */
	public void onDestroy() {
		mLocationClient.onDestroy();
		isLocating.set(false);
		if (IS_DEBUG) {
			Log.d(TAG, "--ALocationController destroyed---");
		}
	}

	/**
	 * 定位信息回调接口
	 */
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (null != amapLocation) {
			if (0 == amapLocation.getErrorCode()) {
				// 定位成功回调信息，设置相关消息

				if (null == aLocationListener)
					return;
				aLocationListener.onLocationSucceeded(amapLocation);

			} else {
				// 显示错误信息ErrCode是错误码，errInfo是错误信息，详见高德错误码表
				Log.e(TAG,
						"location Error, ErrCode:"
								+ amapLocation.getErrorCode() + ", errInfo:"
								+ amapLocation.getErrorInfo());
			}
		}
	}

	public void setALocationListener(ALocationListener aLocationListener) {
		this.aLocationListener = aLocationListener;
	}

	public boolean isLocating() {
		return isLocating.get();
	}
}
