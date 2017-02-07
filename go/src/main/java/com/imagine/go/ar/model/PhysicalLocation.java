package com.imagine.go.ar.model;

import android.location.Location;

import com.amap.api.location.AMapLocation;

/**
 * 封装地理信息
 * 
 * @author Jinhu
 * @date 2016/4/15
 */
public class PhysicalLocation {

	/* 经纬度信息 . */
	private double mLatitude = 0.0d;
	private double mLongitude = 0.0d;
	private double mAltitude = 0.0d;

	/* 物理信息转换向量 . */
	private static float[] x = new float[1];
	private static double y = 0.0d;
	private static float[] z = new float[1];

	public PhysicalLocation() {
	}

	public PhysicalLocation(PhysicalLocation pl) {
		if (null == pl)
			throw new NullPointerException();
		set(pl.mLatitude, pl.mLongitude, pl.mAltitude);
	}

	public PhysicalLocation(double latitude, double longitude, double altitude) {
		set(latitude, longitude, altitude);
	}

	public void set(double latitude, double longitude, double altitude) {
		mLatitude = latitude;
		mLongitude = longitude;
		mAltitude = altitude;
	}

	public void setLatitude(double latitude) {
		mLatitude = latitude;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public void setLongitude(double longitude) {
		mLongitude = longitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setAltitude(double altitude) {
		mAltitude = altitude;
	}

	public double getAltitude() {
		return mAltitude;
	}

	/**
	 * 地理经纬度转换至向量
	 * 
	 * @param mylocation
	 *            用户当前所在位置
	 * @param v
	 *            返回的向量
	 */
	public synchronized void convLocationToVector(AMapLocation mylocation,
			Vector v) {
		if (null == mylocation || null == v)
			throw new NullPointerException(
					"Location and Vector cannot be NULL.");

		Location.distanceBetween(mylocation.getLatitude(),
				mylocation.getLongitude(), this.mLatitude,
				mylocation.getLongitude(), z);

		Location.distanceBetween(mylocation.getLatitude(),
				mylocation.getLongitude(), mylocation.getLatitude(),
				this.mLongitude, x);

		y = this.mAltitude - mylocation.getAltitude();

		if (mylocation.getLatitude() < this.mLatitude) {
			z[0] *= -1;
		}
		if (mylocation.getLongitude() > this.mLongitude) {
			x[0] *= -1;
		}

		v.set(x[0], (float) y, z[0]);
	}

	@Override
	public String toString() {
		return "(lat=" + mLatitude + ", lng=" + mLongitude + ", alt="
				+ mAltitude + ")";
	}

}
