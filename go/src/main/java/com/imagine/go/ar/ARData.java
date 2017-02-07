package com.imagine.go.ar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.imagine.go.Constants;
import com.imagine.go.ar.model.Matrix;

/**
 * ARData 封装AR数据
 * 
 * @author Jinhu
 * @date 2016/4/13
 */
public class ARData {
	private static final String TAG = ARData.class.getSimpleName();

	/* 单例 . */
	private static ARData instance;
	/* 缺省经纬度 . */
	public final AMapLocation HardFix = new AMapLocation("Default");
	{
		HardFix.setLatitude(0);
		HardFix.setLongitude(0);
		HardFix.setAltitude(1);

	}

	/* 缺省Marker . */
	public final ARMarker HardFixMarker = new ARMarker("HardFix", "", "", "",
			"", 0.0d, 0.0d, 1.0d);

	/* 当前所在经纬度 . */
	private AMapLocation mLocation = HardFix;

	/* 世界坐标旋转矩阵 . */
	private Matrix mRotationMatrix;

	/* 方向角. */
	private float azimuth;
	private float pitch;
	private float roll;

	/* 搜索半径 . */
	private int radius = Constants.VALUE_DEFAULT_SEARCH_RADIUS;

	/* Markers . */
	private final Map<String, ARMarker> markerList = new ConcurrentHashMap<String, ARMarker>();
	private final List<ARMarker> currentMarkers = new ArrayList<ARMarker>();
	private final List<ARMarker> cacheMarkers = new ArrayList<ARMarker>();
	private final AtomicBoolean mLock = new AtomicBoolean(false);
	private final float[] locationArray = new float[3];

	/* 需要导航的Marker . */
	private volatile ARMarker mDestinationMarker = HardFixMarker;
	/* 导航起点Marker. */
	private volatile ARMarker mStartMarker = HardFixMarker;
	/* 导航段Markers . */
	private final List<ARMarker> mNaviMarkers = new ArrayList<ARMarker>();
	private final List<ARMarker> mNaviCacheMarkers = new ArrayList<ARMarker>();

	private ARData() {

	}

	public static ARData getInstance() {
		if (null == instance) {
			instance = new ARData();
		}
		return instance;
	}

	/**
	 * 设置当前所在经纬度
	 * 
	 * @param mLocation
	 */
	public synchronized void setLocation(AMapLocation mLocation) {
		if (null == instance || null == mLocation)
			throw new NullPointerException();

		instance.mLocation = mLocation;

		instance.onLocationChanged(mLocation);

		Log.d(TAG, "seted current location. location=" + mLocation);
	}

	/**
	 * @return 当前所在经纬度
	 */
	public synchronized AMapLocation getLocation() {
		if (null == instance)
			throw new NullPointerException();
		return instance.mLocation;
	}

	/**
	 * 设置旋转矩阵
	 * 
	 * @param m
	 */
	public synchronized void setRotationMatrix(Matrix m) {
		instance.mRotationMatrix = m;
	}

	/**
	 * @return 当前的旋转矩阵
	 */
	public synchronized Matrix getRotationMatrix() {
		return instance.mRotationMatrix;
	}

	/**
	 * 绕Z轴旋转的角度
	 * 
	 * @param azimuth
	 */
	public synchronized void setAzimuth(float azimuth) {
		instance.azimuth = azimuth;
	}

	/**
	 * @return 当前的方向角
	 */
	public synchronized float getAzimuth() {
		return instance.azimuth;
	}

	/**
	 * 绕X轴旋转的角度
	 * 
	 * @param pitch
	 */
	public synchronized void setPitch(float pitch) {
		instance.pitch = pitch;
	}

	public synchronized float getPitch() {
		return instance.pitch;
	}

	/**
	 * 绕Y轴旋转的角度
	 * 
	 * @param roll
	 */
	public synchronized void setRoll(float roll) {
		instance.roll = roll;
	}

	public synchronized float getRoll() {
		return instance.roll;
	}

	/**
	 * 设置搜索半径
	 * 
	 * @param radius
	 */
	public synchronized void setRadius(int radius) {
		instance.radius = radius;
	}

	public synchronized int getRadius() {
		return instance.radius;
	}

	/**
	 * 当前所在经纬度更新数据状态
	 * 
	 * @param mLocation
	 */
	private synchronized void onLocationChanged(AMapLocation mLocation) {
		Log.d(TAG,
				"New location, updating markers. location="
						+ mLocation.toString());
		for (ARMarker m : currentMarkers) {
			m.calcRelativePosition(mLocation);
		}

		for (ARMarker m : mNaviMarkers) {
			m.calcRelativePosition(mLocation);
		}

		if (null != mStartMarker) {
			mStartMarker.calcRelativePosition(mLocation);
		}

		if (null != mDestinationMarker) {
			mDestinationMarker.calcRelativePosition(mLocation);
		}

		if (mLock.compareAndSet(false, true)) {
			cacheMarkers.clear();
			mNaviCacheMarkers.clear();
		}

	}

	/**
	 * 添加Marker
	 * 
	 * @param markers
	 */
	public synchronized void addMarkers(List<ARMarker> markers) {
		if (null == markers)
			throw new NullPointerException();
		if (0 >= markers.size())
			return;

		currentMarkers.clear();

		for (ARMarker m : markers) {
			m.calcRelativePosition(mLocation);
			currentMarkers.add(m);
			if (!markerList.containsKey(m.getId())) {
				markerList.put(m.getId(), m);
			}
		}

		if (mLock.compareAndSet(false, true)) {
			cacheMarkers.clear();
		}
	}

	/**
	 * 添加NaviMarkers
	 * 
	 * @param markers
	 */
	public synchronized void addNaviMarkers(List<ARMarker> markers) {
		if (null == markers)
			throw new NullPointerException();
		if (0 >= markers.size())
			return;

		mNaviMarkers.clear();

		for (ARMarker m : markers) {
			m.calcRelativePosition(mLocation);
			mNaviMarkers.add(m);
		}

		if (mLock.compareAndSet(false, true)) {
			mNaviCacheMarkers.clear();
		}
	}

	/**
	 * 返回Marker
	 * 
	 * @return
	 */
	public synchronized List<ARMarker> getMarkers() {
		if (mLock.compareAndSet(true, false)) {
			for (ARMarker m : currentMarkers) {
				m.getLocation().get(locationArray);
				locationArray[1] = m.getInitialY();
				m.getLocation().set(locationArray);
			}

			List<ARMarker> copy = new ArrayList<ARMarker>();
			copy.addAll(currentMarkers);
			Collections.sort(copy, comparator);
			cacheMarkers.clear();
			cacheMarkers.addAll(copy);
		}
		return Collections.unmodifiableList(cacheMarkers);
	}

	/**
	 * 清空当前兴趣点Markers
	 */
	public synchronized void clearMarkers() {
		if (mLock.compareAndSet(true, false)) {
			currentMarkers.clear();
		}
	}

	/**
	 * 返回NaviMarker
	 * 
	 * @return
	 */
	public synchronized List<ARMarker> getNaviMarkers() {
		if (mLock.compareAndSet(true, false)) {
			mStartMarker.getLocation().get(locationArray);
			locationArray[1] = mStartMarker.getInitialY();
			mStartMarker.getLocation().set(locationArray);

			for (ARMarker m : mNaviMarkers) {
				m.getLocation().get(locationArray);
				locationArray[1] = m.getInitialY();
				m.getLocation().set(locationArray);
			}

			List<ARMarker> copy = new ArrayList<ARMarker>();
			copy.add(mStartMarker);
			copy.addAll(mNaviMarkers);
			mNaviCacheMarkers.clear();
			mNaviCacheMarkers.addAll(copy);
		}
		return Collections.unmodifiableList(mNaviCacheMarkers);
	}

	/**
	 * 设置需要导航的Marker
	 * 
	 * @param m
	 */
	public synchronized void setDestinationMarker(ARMarker m) {
		if (null == m)
			throw new NullPointerException();
		m.calcRelativePosition(mLocation);
		mDestinationMarker = m;
	}

	/**
	 * 设置导航起点Marker
	 * 
	 * @param m
	 */
	public synchronized void setStartMarker(ARMarker m) {
		if (null == m)
			throw new NullPointerException();
		m.calcRelativePosition(mLocation);
		mStartMarker = m;
	}

	/**
	 * 返回需要导航的Marker
	 * 
	 * @return
	 */
	public synchronized ARMarker getDestinationMarker() {
		return mDestinationMarker;
	}

	private static final Comparator<ARMarker> comparator = new Comparator<ARMarker>() {
		@Override
		public int compare(ARMarker m1, ARMarker m2) {
			return Double.compare(m1.getDistance(), m2.getDistance());
		}
	};

}
