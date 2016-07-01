package com.imagine.go.model;

import java.util.List;

import com.amap.api.services.core.PoiItem;

/**
 * PoiSearchData
 * 
 * @记录Poi搜索半径及结果集
 * @author Jinhu
 * @date 2016/4/7
 */
public class PoiSearchData {

	private int mRadius;

	private List<PoiItem> mPois;

	public void setRadius(int radius) {
		mRadius = radius;
	}

	public void setPois(List<PoiItem> pois) {
		mPois = pois;
	}

	public int getRadius() {
		return mRadius;
	}

	public List<PoiItem> getPois() {
		return mPois;
	}

	public void clearPois() {
		if (null != mPois) {
			mPois.clear();
		}
		mPois = null;
	}

}
