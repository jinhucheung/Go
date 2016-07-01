package com.imagine.go.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.imagine.go.control.AOfflineMapManager;

/**
 * 离线城市下载管理适配器
 * 
 * @author Jinhu
 * @date 2016/5/19
 */
public class OfflineMapDownloadCityListAdapter extends
		OfflineMapCityListAdapter {

	public OfflineMapDownloadCityListAdapter(Context context,
			AOfflineMapManager offlineMapMgr) {
		super(context, offlineMapMgr);
		initCityList();
	}

	@Override
	public void notifyDataChange() {
		initCityList();
		notifyDataSetChanged();
	}

	/**
	 * 初始化城市列表
	 */
	private void initCityList() {

		List<OfflineMapCity> mCities = new ArrayList<OfflineMapCity>();
		mCities.addAll(mOfflineMapMgr.getDownloadingCityList());
		mCities.addAll(mOfflineMapMgr.getDownloadOfflineMapCityList());

		super.initCityList(mCities);

	}

}
