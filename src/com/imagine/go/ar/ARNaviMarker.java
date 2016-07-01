package com.imagine.go.ar;

import com.amap.api.navi.model.AMapNaviGuide;
import com.imagine.go.model.GeoPoint;

/**
 * 导航段标记
 * 
 * @author Jinhu
 * @date 2016/4/25
 */
public class ARNaviMarker extends ARMarker {

	/* 标记类型 . */
	private static final String TYPE = "NAVI";

	/* 导航段类型 . */
	protected int naviType;
	/* 导航段长度. */
	protected int length;
	/* 导航段时间 . */
	protected int time;

	public ARNaviMarker(String id, String name, String address, String info,
			String type, double latitude, double longitude, double altitude,
			int naviType, int length, int time) {
		super(id, name, address, info, type, latitude, longitude, altitude);
		this.naviType = naviType;
		this.length = length;
		this.time = time;
	}

	public ARNaviMarker(AMapNaviGuide guide) {
		this(buildId(guide.getName(), guide.getCoord().getLatitude(), guide
				.getCoord().getLongitude()),//
				guide.getName(),//
				"", //
				"",//
				TYPE,//
				guide.getCoord().getLatitude(), //
				guide.getCoord().getLongitude(),//
				0.0d, //
				guide.getIconType(), //
				guide.getLength(),//
				guide.getTime());
	}

	public ARNaviMarker(GeoPoint point) {
		this(
				buildId(point.getName(), point.getlatitude(),
						point.getlongitude()),//
				point.getName(),//
				point.getAddress(),//
				point.getAddress(),//
				TYPE, //
				point.getlatitude(), //
				point.getlongitude(), //
				point.getAltitude(), 0, 0, 0);
	}

	// ------------------------------访问属性---------------------------
	public synchronized int getNaviType() {
		return naviType;
	}

	public synchronized int getLength() {
		return length;
	}

	public synchronized int getTime() {
		return time;
	}

	/**
	 * 生成MarkerId
	 * 
	 * @return Id
	 */
	private static String buildId(String name, double latitude, double longitude) {
		return name + latitude + "/" + longitude;
	}

}
