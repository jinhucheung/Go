package com.imagine.go.activity;

import static com.imagine.go.Constants.IS_DEBUG;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;

import com.amap.api.services.core.PoiItem;
import com.imagine.go.R;
import com.imagine.go.ar.ARData;
import com.imagine.go.ar.ARIconMarker;
import com.imagine.go.ar.ARMarker;
import com.imagine.go.ar.AugmentedView;
import com.imagine.go.ar.RadarView;
import com.imagine.go.control.CameraController;
import com.imagine.go.model.GeoPoint;
import com.imagine.go.util.PoiTypeMatcher;

/**
 * 增强层
 * 
 * @author Jinhu
 * @date 2016/4/13
 */
public abstract class ARActivity extends ARCoordActivity implements
		OnTouchListener {
	private static final String TAG = ARActivity.class.getSimpleName();

	// ---------------界面相关------------
	/* 摄像层浏览控件 . */
	protected TextureView mCameraTextureView;

	/* 增强视图 . */
	protected AugmentedView mAugmentedView;

	/* 雷达图 . */
	protected RadarView mRadarView;

	// ----------------控制相关-----------
	/* 摄像层控制 . */
	protected CameraController mCameraController;

	/* 屏幕控制 . */
	private WakeLock mWakeLock;

	/* 已点击的Marker . */
	protected ARMarker mTouchedMarker;

	// ------------------------ 生命周期 ------------------------
	//
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 自定义标题栏
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_ar);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.bar_title);

		// 初始化视图
		// 实景层
		mCameraTextureView = (TextureView) findViewById(R.id.id_textureView_camera);
		mCameraController = new CameraController(this, mCameraTextureView);
		mCameraController.initPreview(getScreenWidth(), getScreenHeight(),
				getRotation());
		mCameraController
				.setCameraTextureListener(mCameraController.new CameraTextureListener());

		// 增强层
		mAugmentedView = (AugmentedView) findViewById(R.id.id_augmentedView_ar);
		mAugmentedView.setOnTouchListener(this);
		// 雷达图
		mRadarView = (RadarView) findViewById(R.id.id_radarView_radar);
		mRadarView
				.setLocation(getScreenWidth() - RadarView.RADIUS * 2 - 10, 30);

		// 屏幕控制
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				"DimScreen");

		if (IS_DEBUG) {
			Log.d(TAG, " --onCreated()--");
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (IS_DEBUG) {
			Log.d(TAG, "--OnStared()--");
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		mWakeLock.acquire();
		if (IS_DEBUG) {
			Log.d(TAG, "--OnResumed()--");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mCameraController.onPause();
		mWakeLock.release();
		if (IS_DEBUG) {
			Log.d(TAG, "--OnPaused()--");
		}
	}

	/**
	 * 更新视图
	 */
	protected void updateView() {
		mRadarView.postInvalidate();
		mAugmentedView.postInvalidate();
		mCameraTextureView.postInvalidate();
	}

	/**
	 * 更新Marker数据
	 * 
	 * @param markers
	 */
	protected void updateARMarkers(List<PoiItem> pois) {
		if (null == pois)
			return;
		List<ARMarker> markers = new ArrayList<ARMarker>();
		for (PoiItem poi : pois) {
			ARMarker m = new ARMarker(poi);
			markers.add(m);
		}
		ARData.getInstance().addMarkers(markers);
	}

	/**
	 * 生成一个ARIconMarker
	 * 
	 * @param poi
	 * @return
	 */
	private ARIconMarker buildARIconMarker(PoiItem poi) {
		ARIconMarker m = new ARIconMarker(poi);
		m.initView(getLayoutInflater());
		m.setPoiIcon(PoiTypeMatcher.getPoiIcon(PoiTypeMatcher
				.getCurrentLableName()));
		return m;
	}

	/**
	 * 生成一个ARIconMarker
	 * 
	 * @param poi
	 * @return
	 */
	private ARIconMarker buildARIconMarker(GeoPoint poi) {
		ARIconMarker m = new ARIconMarker(poi);
		m.initView(getLayoutInflater());
		m.setPoiIcon(PoiTypeMatcher.getPoiIcon(""));
		return m;
	}

	/**
	 * 更新IconMarker
	 * 
	 * @param pois
	 */
	protected void updateARIconMarkers(List<PoiItem> pois) {
		if (null == pois)
			return;
		List<ARMarker> markers = new ArrayList<ARMarker>();
		for (PoiItem poi : pois) {
			ARIconMarker m = buildARIconMarker(poi);
			markers.add(m);
		}
		ARData.getInstance().addMarkers(markers);
	}

	/**
	 * 更新IconMarker
	 * 
	 * @param pois
	 */
	protected void updateARIconMarkersByGeoPoint(List<GeoPoint> pois) {
		if (null == pois)
			return;
		List<ARMarker> markers = new ArrayList<ARMarker>();
		for (GeoPoint poi : pois) {
			ARIconMarker m = buildARIconMarker(poi);
			markers.add(m);
		}
		ARData.getInstance().addMarkers(markers);
	}

	/**
	 * 更新IconMarker
	 * 
	 * @param poiitems
	 * @param geopoits
	 */
	protected void updateARIconMarker(List<PoiItem> poiitems,
			List<GeoPoint> geopois) {
		if (null == poiitems && null != geopois) {
			updateARIconMarkersByGeoPoint(geopois);
			return;
		}

		if (null != poiitems && null == geopois) {
			updateARIconMarkers(poiitems);
			return;
		}

		List<ARMarker> markers = new ArrayList<ARMarker>();
		for (PoiItem poi : poiitems) {
			ARIconMarker m = buildARIconMarker(poi);
			markers.add(m);
		}
		for (GeoPoint poi : geopois) {
			ARIconMarker m = buildARIconMarker(poi);
			markers.add(m);
		}
		ARData.getInstance().addMarkers(markers);
	}

	// ------------------------ Marker点击处理------------------------
	@Override
	public synchronized boolean onTouch(View v, MotionEvent event) {
		if (null != mTouchedMarker) {
			mTouchedMarker.setAlphaOff(ARMarker.DEFAULT_ALPHA_OFF);
		}
		for (ARMarker m : ARData.getInstance().getMarkers()) {
			if (m.handleClick(event.getX(), event.getY())) {
				onMarkerTouch(m);
				m.setAlphaOff(70);
				mTouchedMarker = m;
				return true;
			}
		}
		return super.onTouchEvent(event);
	}

	protected abstract void onMarkerTouch(ARMarker marker);

	// ------------------------ 传感器数据处理------------------------
	/**
	 * 传感器数据处理
	 */
	@Override
	protected synchronized void onSensorAccess() {
		super.onSensorAccess();

		float pitch = (float) ((mOrientation[1] * 180) / Math.PI);
		// 当设备水平放置时触发
		if (pitch >= -15 && pitch <= 25) {
			mRadarView.setVisibility(View.INVISIBLE);
			mAugmentedView.setVisibility(View.INVISIBLE);
			mCameraTextureView.setVisibility(View.INVISIBLE);
			inHorizontal(true);
		} else {
			mRadarView.setVisibility(View.VISIBLE);
			mAugmentedView.setVisibility(View.VISIBLE);
			mCameraTextureView.setVisibility(View.VISIBLE);
			inHorizontal(false);
		}

		// 更新视图
		updateView();
	}

	/**
	 * 设备水平放置回调
	 */
	protected abstract void inHorizontal(boolean isHorizontal);

}
