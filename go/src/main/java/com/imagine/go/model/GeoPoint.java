package com.imagine.go.model;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;

/**
 * GeoPoint:
 * 
 * @保存地理点信息
 * @author Jinhu
 * @date 2016/4/7
 */
public class GeoPoint {

	private volatile String mId;

	private volatile LatLng mLatLng;

	private volatile double mlatitude;
	private volatile double mlongitude;
	private volatile double mAltitude;

	private volatile String mName;

	private volatile String mAddress;

	private volatile String mCity;

	private volatile String mCityCode;

	private volatile String mSnippet;

	private volatile String mURL;

	public GeoPoint() {

	}

	public GeoPoint(LatLng latlng) {
		mLatLng = latlng;
		mlatitude = latlng.latitude;
		mlongitude = latlng.longitude;
	}

	public synchronized LatLng getLatLng() {
		if (0 != mlatitude && 0 != mlongitude) {
			mLatLng = new LatLng(mlatitude, mlongitude);
		}
		return mLatLng;
	}

	public synchronized void setLatLng(LatLng mLatLng) {
		this.mLatLng = mLatLng;
		setlatitude(mLatLng.latitude);
		setlongitude(mLatLng.longitude);
	}

	public synchronized double getlatitude() {
		return mlatitude;
	}

	public synchronized void setlatitude(double mlatitude) {
		this.mlatitude = mlatitude;
	}

	public synchronized double getlongitude() {
		return mlongitude;
	}

	public synchronized void setlongitude(double mlongitude) {
		this.mlongitude = mlongitude;
	}

	public synchronized double getAltitude() {
		return mAltitude;
	}

	public synchronized void setAltitude(double mAltitude) {
		this.mAltitude = mAltitude;
	}

	public synchronized String getName() {
		return mName;
	}

	public synchronized void setName(String mName) {
		this.mName = mName;
	}

	public synchronized String getAddress() {
		return mAddress;
	}

	public synchronized void setAddress(String mAddress) {
		this.mAddress = mAddress;
	}

	public synchronized String getCity() {
		return mCity;
	}

	public synchronized void setCity(String city) {
		mCity = city;
	}

	public synchronized String getCityCode() {
		return mCityCode;
	}

	public synchronized void setCityCode(String cityCode) {
		mCityCode = cityCode;
	}

	public synchronized String getSnippet() {
		return mSnippet;
	}

	public synchronized void setSnippet(String snippet) {
		mSnippet = snippet;
	}

	public synchronized String getURL() {
		return mURL;
	}

	public synchronized void setURL(String url) {
		mURL = url;
	}

	public synchronized void setId(String Id) {
		mId = Id;
	}

	public synchronized String getId() {
		return mId;
	}

	@Override
	public String toString() {
		if (null != mLatLng)
			return "LatLng : (" + mLatLng.latitude + " , " + mLatLng.longitude
					+ " )";
		return "";
	}

	@Override
	public GeoPoint clone() {
		GeoPoint point = new GeoPoint(mLatLng);
		point.setlatitude(mlatitude);
		point.setlongitude(mlongitude);
		point.setAltitude(mAltitude);
		point.setName(mName);
		point.setAddress(mAddress);
		point.setCity(mCity);
		point.setCityCode(mCityCode);
		point.setSnippet(mSnippet);
		point.setURL(mURL);
		point.setId(mId);
		return point;
	}

	/**
	 * 工具方法:高德Marker类型转换至GeoPoint
	 * 
	 * @param poi
	 * @param point
	 */
	public static void poiMarkerToGeoPoint(Marker poi, GeoPoint point) {
		if (null == poi || null == point)
			throw new NullPointerException();
		point.setLatLng(poi.getPosition());
		point.setAltitude(0.0d);
		point.setName(poi.getTitle());
		point.setAddress(poi.getSnippet());
		point.setCity(poi.getSnippet());
		point.setCityCode("");
		point.setSnippet(poi.getSnippet());
		point.setURL("");
	}

	/**
	 * 高德AMapLocation转换至GeoPoint
	 * 
	 * @param location
	 * @param point
	 */
	public static void AMapLocationToGeoPoint(AMapLocation amapLocation,
			GeoPoint point) {
		if (null == amapLocation || null == point)
			throw new NullPointerException();

		LatLng latlng = new LatLng(amapLocation.getLatitude(),
				amapLocation.getLongitude());
		point.setLatLng(latlng);
		point.setAltitude(amapLocation.getAltitude());
		point.setName(amapLocation.getPoiName());
		// 去掉省会级的地址信息
		String address = amapLocation.getAddress();
		if (null != address && !"".equals(address)) {
			point.setAddress(address);
			point.setSnippet(address);
		}

		String city = amapLocation.getCity();
		if (null != city && !"".equals(city)) {
			point.setCity(city);
		}

		String cityCode = amapLocation.getCityCode();
		if (null != city && !"".equals(cityCode)) {
			point.setCityCode(cityCode);
		}

	}
}
