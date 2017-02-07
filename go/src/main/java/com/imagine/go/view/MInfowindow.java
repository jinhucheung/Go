package com.imagine.go.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.model.Marker;
import com.imagine.go.R;

/**
 * MInfowindow:地图Marker的Infowindow
 * 
 * @author Jinhu
 * @date 2016/3/26
 */
public class MInfowindow implements InfoWindowAdapter {

	private Context mContext;

	/* 主窗口. */
	protected View mWindow;
	/* 标题. */
	protected TextView mTitle;
	/* 地址. */
	protected TextView mAddress;
	/* 路径规划按钮. */
	private ImageView mWalkRouteBtn;
	/* 标记显示. */
	protected boolean isShow;

	/* 所属Marker. */
	protected Marker mMarker;

	/* 回调接口. */
	private onInfowindowClickedLinstener mOnInfowindowClickedLinstener;

	public interface onInfowindowClickedLinstener {
		// 路径规划
		void searchWalkRoute(Marker marker);
	}

	public MInfowindow(Context context) {
		mContext = context;
	}

	/**
	 * 初始化
	 */
	private void init() {
		// 初始化组件
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mWindow = inflater.inflate(R.layout.infowindow_activity_map, null);

		mTitle = (TextView) mWindow
				.findViewById(R.id.id_textView_infowindow_name);
		mAddress = (TextView) mWindow
				.findViewById(R.id.id_textView_infowindow_address);
		mWalkRouteBtn = (ImageView) mWindow
				.findViewById(R.id.id_imgView_infowindow_walkRoute);

		// 监听回调
		mWalkRouteBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != mOnInfowindowClickedLinstener) {
					mOnInfowindowClickedLinstener.searchWalkRoute(mMarker);
				}
			}
		});
	}

	/**
	 * Infowindow是否显示
	 * 
	 * @return
	 */
	public boolean IsShow() {
		return isShow;
	}

	@Override
	public View getInfoWindow(Marker paramMarker) {
		init();
		mTitle.setText(paramMarker.getTitle());
		mAddress.setText("地址:" + paramMarker.getSnippet());
		mMarker = paramMarker;
		isShow = true;
		return mWindow;
	}

	@Override
	public View getInfoContents(Marker paramMarker) {
		this.getInfoWindow(paramMarker);
		return mWindow;
	}

	/**
	 * 回调接口
	 * 
	 * @param mOnInfowindowClickedLinstener
	 */
	public void setOnInfowindowClickedLinstener(
			onInfowindowClickedLinstener mOnInfowindowClickedLinstener) {
		this.mOnInfowindowClickedLinstener = mOnInfowindowClickedLinstener;
	}

}
