package com.imagine.go.adapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.imagine.go.control.AOfflineMapManager;
import com.imagine.go.model.OfflineMapCityItem;

/**
 * 离线地图城市列表适配器
 * 
 * @author Jinhu
 * @date 2016/5/18
 */
public class OfflineMapCityListAdapter extends BaseAdapter {
	private static final String TAG = OfflineMapCityListAdapter.class
			.getSimpleName();

	protected Context mContext;

	/* 离线地图管理 . */
	protected AOfflineMapManager mOfflineMapMgr;

	/* 城市列表. */
	protected final List<OfflineMapCity> mCities = new ArrayList<OfflineMapCity>();

	public OfflineMapCityListAdapter(Context context,
			AOfflineMapManager offlineMapMgr) {
		mContext = context;
		mOfflineMapMgr = offlineMapMgr;
	}

	/**
	 * 重新初始化数据
	 */
	public void notifyDataChange() {
		long start = System.currentTimeMillis();
		notifyDataSetChanged();
		Log.d(TAG,
				"Offline Downloading notifyData cost: "
						+ (System.currentTimeMillis() - start));
	}

	/**
	 * 初始城市列表
	 */
	public void initCityList(List<OfflineMapCity> cities) {
		if (null == cities || cities.size() <= 0)
			return;

		// 清空城市列表
		if (null != mCities) {
			long start = System.currentTimeMillis();
			for (Iterator<OfflineMapCity> it = mCities.iterator(); it.hasNext();) {
				it.next();
				it.remove();
			}
			Log.d(TAG, "Offline Downloading notifyData cities iterator cost: "
					+ (System.currentTimeMillis() - start));
		}

		long start = System.currentTimeMillis();
		mCities.addAll(cities);
		Log.d(TAG,
				"Offline Downloading notifyData getDownloadingCityList cost: "
						+ (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		notifyDataSetChanged();
		Log.d("amap",
				"Offline Downloading notifyData notifyDataSetChanged cost: "
						+ (System.currentTimeMillis() - start));
	}

	@Override
	public int getCount() {
		return mCities.size();
	}

	@Override
	public Object getItem(int position) {
		return mCities.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (null != convertView) {
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			OfflineMapCityItem currentOfflineCityItem = new OfflineMapCityItem(
					mContext, mOfflineMapMgr);
			convertView = currentOfflineCityItem.getView();

			viewHolder = new ViewHolder();
			viewHolder.mOfflineCityItem = currentOfflineCityItem;

			convertView.setTag(viewHolder);
		}

		OfflineMapCity mOfflineMapCity = (OfflineMapCity) getItem(position);
		viewHolder.mOfflineCityItem.setOfflineCity(mOfflineMapCity);

		return convertView;
	}

	/**
	 * View数据项
	 */
	public final class ViewHolder {
		public OfflineMapCityItem mOfflineCityItem;
	}

}
