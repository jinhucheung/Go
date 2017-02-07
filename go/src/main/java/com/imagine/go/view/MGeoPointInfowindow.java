package com.imagine.go.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.amap.api.maps.model.Marker;
import com.imagine.go.Constants;
import com.imagine.go.R;

/**
 * 已标记Marker的Infowindow
 * 
 * @author Jinhu
 * @date 2016/5/22
 */

public class MGeoPointInfowindow extends MInfowindow {

	private Context mContext;

	/* 路径规划按钮. */
	private ImageView mDelBtn;

	/* 回调接口. */
	private onInfowindowClickedLinstener mOnInfowindowClickedLinstener;

	public interface onInfowindowClickedLinstener {
		// 删除标记
		void deleteMarker(Marker marker);
	}

	public MGeoPointInfowindow(Context context) {
		super(context);
		mContext = context;
	}

	/**
	 * 初始化
	 */
	@SuppressLint("InflateParams")
	private void init() {
		// 初始化组件
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mWindow = inflater.inflate(
				R.layout.infowindow_activity_user_defined_point, null);

		mTitle = (TextView) mWindow
				.findViewById(R.id.id_textView_infowindow_name);
		mAddress = (TextView) mWindow
				.findViewById(R.id.id_textView_infowindow_address);
		mDelBtn = (ImageView) mWindow
				.findViewById(R.id.id_imgView_infowindow_del);

		// 监听回调
		mDelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != mOnInfowindowClickedLinstener) {
					mOnInfowindowClickedLinstener.deleteMarker(mMarker);
				}
			}
		});
	}

	@Override
	public View getInfoWindow(Marker paramMarker) {
		init();
		mTitle.setText(paramMarker.getTitle());
		mAddress.setText("地址:" + paramMarker.getSnippet());
		mMarker = paramMarker;
		isShow = true;

		String tab = paramMarker.getObject().toString();
		if (!tab.startsWith(Constants.TAB_USER_DEFINED_POINT_MARKER)) {
			mDelBtn.setVisibility(View.GONE);
		}
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
