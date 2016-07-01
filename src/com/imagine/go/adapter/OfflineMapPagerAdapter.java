package com.imagine.go.adapter;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * 离线地图层分页适配器
 * 
 * @author Jinhu
 * @date 2016/5/16
 */
public class OfflineMapPagerAdapter extends PagerAdapter {

	private View mOfflineCityView;
	private View mOfflineDownloadView;

	private ViewPager mContentViewPager;

	public OfflineMapPagerAdapter(ViewPager viewPager, View offlineCityView,
			View offlineDownloadView) {
		mContentViewPager = viewPager;
		mOfflineCityView = offlineCityView;
		mOfflineDownloadView = offlineDownloadView;
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		if (0 == position) {
			mContentViewPager.removeView(mOfflineCityView);
		} else {
			mContentViewPager.removeView(mOfflineDownloadView);
		}
	}

	@Override
	public Object instantiateItem(View container, int position) {
		if (0 == position) {
			mContentViewPager.addView(mOfflineCityView);
			return mOfflineCityView;
		} else {
			mContentViewPager.addView(mOfflineDownloadView);
			return mOfflineDownloadView;
		}
	}

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public boolean isViewFromObject(View v, Object obj) {
		return v == obj;
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void finishUpdate(View arg0) {
	}

	@Override
	public void startUpdate(View arg0) {
	}

}
