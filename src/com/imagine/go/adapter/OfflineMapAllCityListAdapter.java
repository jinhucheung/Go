package com.imagine.go.adapter;

import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import com.imagine.go.R;
import com.imagine.go.control.AOfflineMapManager;
import com.imagine.go.model.OfflineMapCityItem;

/**
 * 离线地图所有城市二级列表适配器
 * 
 * @author Jinhu
 * @date 2016/5/18
 */
public class OfflineMapAllCityListAdapter extends BaseExpandableListAdapter
		implements OnGroupCollapseListener, OnGroupExpandListener {

	private Context mContext;

	/* 离线地图管理控件 . */
	private AOfflineMapManager mOfflineMapMgr;

	/* 省级列表 . */
	private List<OfflineMapProvince> mProvinceList = null;

	/* 记录一级目录是否打开. */
	private boolean[] isOpen;

	public OfflineMapAllCityListAdapter(Context context,
			AOfflineMapManager offlineMapMgr,
			List<OfflineMapProvince> provinceList) {
		mContext = context;
		mOfflineMapMgr = offlineMapMgr;
		mProvinceList = provinceList;
		isOpen = new boolean[provinceList.size()];
	}

	/**
	 * 获取组数
	 */
	@Override
	public int getGroupCount() {
		return mProvinceList.size();
	}

	/**
	 * 获取子项数
	 */
	@Override
	public int getChildrenCount(int groupPosition) {
		if (isNormalProvinceGroup(groupPosition)) {
			return mProvinceList.get(groupPosition).getCityList().size() + 1;
		}
		return mProvinceList.get(groupPosition).getCityList().size();
	}

	/**
	 * 获取一级省份标签内容
	 */
	@Override
	public Object getGroup(int groupPosition) {
		return mProvinceList.get(groupPosition).getProvinceName();
	}

	/**
	 * 获取一级标签下二级标签的内容
	 */
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	/**
	 * 获取一级标签的ID
	 */
	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	/**
	 * z指定位置相应的组视图
	 */
	@Override
	public boolean hasStableIds() {
		return true;
	}

	/**
	 * 返回一级组件
	 */
	@SuppressLint("InflateParams")
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		TextView groupTxt;
		ImageView groupImg;
		if (null == convertView) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.item_offlinemap_province,
					null);
		}
		groupTxt = (TextView) convertView
				.findViewById(R.id.id_textView_provinceName);
		groupImg = (ImageView) convertView
				.findViewById(R.id.id_imageView_switch);
		groupTxt.setText(mProvinceList.get(groupPosition).getProvinceName());
		if (isOpen[groupPosition]) {
			groupImg.setImageResource(R.drawable.ic_arrow_open);
		} else {
			groupImg.setImageResource(R.drawable.ic_arrow_close);
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (null != convertView) {
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			OfflineMapCityItem mOfflineCityItem = new OfflineMapCityItem(
					mContext, mOfflineMapMgr);
			convertView = mOfflineCityItem.getView();

			viewHolder = new ViewHolder();
			viewHolder.mOfflineCityItem = mOfflineCityItem;
			convertView.setTag(viewHolder);
		}

		OfflineMapCity mCity = null;
		viewHolder.mOfflineCityItem.setProvince(false);

		if (isNormalProvinceGroup(groupPosition)) {
			if (isProvinceItem(groupPosition, childPosition)) {
				// 普通省份第1项为省份项
				mCity = ProvinceToCity(mProvinceList.get(groupPosition));
				viewHolder.mOfflineCityItem.setProvince(true);
			} else {
				// 普通省份第2项起为城市项
				mCity = mProvinceList.get(groupPosition).getCityList()
						.get(childPosition - 1);
			}
		} else {
			// 特殊地区
			mCity = mProvinceList.get(groupPosition).getCityList()
					.get(childPosition);
		}

		viewHolder.mOfflineCityItem.setOfflineCity(mCity);
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public void onGroupExpand(int groupPosition) {
		isOpen[groupPosition] = true;
	}

	@Override
	public void onGroupCollapse(int groupPosition) {
		isOpen[groupPosition] = false;
	}

	/**
	 * 普通省份中第1项为省份项
	 * 
	 * @param groupPosition
	 * @param childPosition
	 * @return
	 */
	private boolean isProvinceItem(int groupPosition, int childPosition) {
		return isNormalProvinceGroup(groupPosition) && 0 == childPosition;
	}

	/**
	 * 是否为普通省份 <br/>
	 * 第1、2组分别为直辖市，港澳地区
	 * 
	 * @param groupPosition
	 * @return
	 */
	private boolean isNormalProvinceGroup(int groupPosition) {
		return groupPosition > 1;
	}

	/**
	 * 将一个省份对象转换成一个城市对象
	 * 
	 * @param province
	 * @return
	 */
	public OfflineMapCity ProvinceToCity(OfflineMapProvince province) {
		OfflineMapCity city = new OfflineMapCity();
		city.setCity(province.getProvinceName());
		city.setSize(province.getSize());
		city.setCompleteCode(province.getcompleteCode());
		city.setState(province.getState());
		city.setUrl(province.getUrl());
		return city;
	}

	/**
	 * View数据项
	 */
	public final class ViewHolder {
		public OfflineMapCityItem mOfflineCityItem;
	}

}
