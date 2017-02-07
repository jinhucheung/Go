package com.imagine.go.model;

import static com.imagine.go.Constants.EVENT_OFFLINEMAP_DOWNLOAD;
import java.util.concurrent.atomic.AtomicBoolean;
import android.R.color;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.imagine.go.AppManager;
import com.imagine.go.R;
import com.imagine.go.control.AOfflineMapManager;
import com.imagine.go.util.ToastUtil;
import com.imagine.go.view.BottomBtnDialog;
import com.imagine.go.view.TextProgressBar;

/**
 * 离线地图城市项
 * 
 * @author Jinhu
 * @date 2016/5/17
 */
public class OfflineMapCityItem implements OnClickListener,
		OnLongClickListener, OnSweetClickListener {
	private static final String TAG = OfflineMapCityItem.class.getSimpleName();

	private Context mContext;

	// -------------界面相关-------------
	/* 布局 . */
	private View mCityItemLayout;

	/* 城市名 . */
	private TextView mCityNameTxt;

	/* 地图大小 . */
	private TextView mCitySizeTxt;

	/* 下载相关状态提示. */
	private TextView mDownloadStateTxt;

	/* 下载进度条. */
	private TextProgressBar mDownloadProgressBar;

	/* 下载进度提示颜色 . */
	private int mDownloadStateTxtNormalColor;
	private int mDownloadStateTxtPauseColor;
	private int mDownloadStateTxtDisableColor;

	/* 对话框 . */
	private BottomBtnDialog mDialog;

	// -------------数据相关-------------
	/* 离线地图管理 . */
	private AOfflineMapManager mOfflineMapMgr;

	/* 离线城市模型. */
	private OfflineMapCity mOfflineCity;

	/* 省份标志 . */
	private boolean isProvince = false;

	/* 点击检测. */
	private AtomicBoolean isClicked = new AtomicBoolean(false);

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			int completeCode = (Integer) msg.obj;
			switch (msg.what) {
			case OfflineMapStatus.LOADING:
				displayLoadingStatus();
				break;

			case OfflineMapStatus.PAUSE:
				displayPauseStatus(completeCode);
				break;

			case OfflineMapStatus.STOP:
				break;

			case OfflineMapStatus.SUCCESS:
				displaySuccessStatus();
				break;

			case OfflineMapStatus.UNZIP:
				displayUnZIPStatus();
				break;

			case OfflineMapStatus.WAITING:
				displayWaitingStatus();
				break;

			case OfflineMapStatus.CHECKUPDATES:
				displayDefaultStatus();
				break;

			case OfflineMapStatus.NEW_VERSION:
				displayHasNewVersionStatus();
				break;

			case OfflineMapStatus.ERROR:

			case OfflineMapStatus.EXCEPTION_AMAP:

			case OfflineMapStatus.EXCEPTION_NETWORK_LOADING:

			case OfflineMapStatus.EXCEPTION_SDCARD:
				displayExceptionStatus();
				break;

			}
		};
	};

	public OfflineMapCityItem(Context context, AOfflineMapManager offlineMapMgr) {
		mContext = context;
		mOfflineMapMgr = offlineMapMgr;
		initView();
	}

	/**
	 * 初始化视图组件
	 */
	@SuppressLint("InflateParams")
	private void initView() {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mCityItemLayout = inflater.inflate(R.layout.item_offlinemap_city, null);
		mCityNameTxt = (TextView) mCityItemLayout
				.findViewById(R.id.id_textView_cityName);
		mCitySizeTxt = (TextView) mCityItemLayout
				.findViewById(R.id.id_textView_downloadSize);
		mDownloadStateTxt = (TextView) mCityItemLayout
				.findViewById(R.id.id_textView_downloadState);
		mDownloadProgressBar = (TextProgressBar) mCityItemLayout
				.findViewById(R.id.id_progerssBar_download);

		mDownloadStateTxtNormalColor = mContext.getResources().getColor(
				R.color.offlinemap_download_normal);
		mDownloadStateTxtPauseColor = mContext.getResources().getColor(
				R.color.offlinemap_download_pause);
		mDownloadStateTxtDisableColor = mContext.getResources().getColor(
				R.color.offlinemap_download_disable);

		mDialog = new BottomBtnDialog(mContext);

		registerViewListener();
	}

	/**
	 * 初始化视图监听器
	 */
	private void registerViewListener() {
		// ------注册监听器-------
		mDownloadStateTxt.setOnClickListener(this);
		mCityItemLayout.setOnLongClickListener(this);
	}

	/**
	 * 返回省份标志
	 * 
	 * @return
	 */
	public boolean isProvince() {
		return isProvince;
	}

	/**
	 * 设置省份标志
	 * 
	 * @param isProvince
	 */
	public void setProvince(boolean isProvince) {
		this.isProvince = isProvince;
	}

	/**
	 * 返回布局组件
	 * 
	 * @return
	 */
	public View getView() {
		return mCityItemLayout;
	}

	// ------------------------ 业务逻辑 ------------------------
	/**
	 * 暂停当前下载
	 */
	private synchronized void pauseDownload() {
		// 暂停正在进行的下载任务
		mOfflineMapMgr.pause();
		// 暂停下载之后，开启下一个等待中的任务
		mOfflineMapMgr.restart();
	}

	/**
	 * 启动下载任务
	 * 
	 * @return
	 */
	private synchronized boolean startDownload() {
		try {
			if (isProvince) {
				mOfflineMapMgr.downloadByProvinceName(mOfflineCity.getCity());
			} else {
				mOfflineMapMgr.downloadByCityCode(mOfflineCity.getCode());
			}
			return true;
		} catch (AMapException e) {
			e.printStackTrace();
			Log.w(TAG, e.getErrorMessage());
			return false;
		}

	}

	/**
	 * 获取进度值
	 * 
	 * @param completeCode
	 * @return
	 */
	private int getProgressValue(int completeCode) {
		return (int) (1.0f * completeCode / 100f * mDownloadProgressBar
				.getMaxProgress());
	}

	// ------------------------ 更新UI ------------------------

	/**
	 * 设置离线城市
	 * 
	 * @param offlineCity
	 */
	public void setOfflineCity(OfflineMapCity offlineCity) {
		if (null != offlineCity) {
			mOfflineCity = offlineCity;

			// 设置城市名
			String cityCode = AppManager.getInstance().getLocationPoint()
					.getCityCode();
			if (null != cityCode && cityCode.equals(offlineCity.getCode())) {
				mCityNameTxt.setText(offlineCity.getCity() + "(当前城市)");
			} else {
				mCityNameTxt.setText(offlineCity.getCity());
			}

			// 设置地图大小
			double size = ((int) (offlineCity.getSize() / 1024.0 / 1024.0 * 100)) / 100.0;
			mCitySizeTxt.setText("地图" + size + "M");

			notifyViewDisplay(offlineCity.getState(),
					offlineCity.getcompleteCode());
		}
	}

	/**
	 * 更新显示状态
	 * 
	 * @param state
	 *            状态码
	 * @param completeCode
	 *            完成状态码
	 */
	private void notifyViewDisplay(int state, int completeCode) {
		if (null != mOfflineCity) {
			mOfflineCity.setState(state);
			mOfflineCity.setCompleteCode(completeCode);
		}
		Message msg = new Message();
		msg.what = state;
		msg.obj = completeCode;
		handler.sendMessage(msg);
	}

	/**
	 * 显示下载状态
	 */
	private void displayDefaultStatus() {
		mDownloadProgressBar.setProgress(0);
		mDownloadStateTxt.setTextColor(mDownloadStateTxtNormalColor);
		mDownloadStateTxt
				.setBackgroundResource(R.drawable.shaper_offlinemap_download_state_rect);
		mDownloadStateTxt.setText("下载");
	}

	/**
	 * 显示更新状态
	 */
	private void displayHasNewVersionStatus() {
		mDownloadProgressBar.setProgress(0);
		mDownloadStateTxt.setTextColor(mDownloadStateTxtNormalColor);
		mDownloadStateTxt
				.setBackgroundResource(R.drawable.shaper_offlinemap_download_state_rect);
		mDownloadStateTxt.setText("更新");

	}

	/**
	 * 显示等待状态
	 * 
	 * @param completeCode
	 */
	private void displayWaitingStatus() {
		mDownloadStateTxt.setTextColor(mDownloadStateTxtPauseColor);
		mDownloadStateTxt
				.setBackgroundResource(R.drawable.shaper_offlinemap_download_state_pause_rect);
		mDownloadStateTxt.setText("等待");

	}

	/**
	 * 显示异常状态
	 */
	private void displayExceptionStatus() {
		displayDefaultStatus();
		if (isClicked.compareAndSet(true, false)) {
			ToastUtil.showShort("下载出现异常");
		}
	}

	/**
	 * 显示暂停状态
	 * 
	 * @param completeCode
	 */
	private void displayPauseStatus(int completeCode) {
		if (null != mOfflineCity) {
			completeCode = mOfflineCity.getcompleteCode();
		}
		mDownloadProgressBar.setProgress(getProgressValue(completeCode));
		mDownloadStateTxt.setTextColor(mDownloadStateTxtNormalColor);
		mDownloadStateTxt
				.setBackgroundResource(R.drawable.shaper_offlinemap_download_state_rect);
		mDownloadStateTxt.setText("继续");

	}

	/**
	 * 显示下载成功
	 */
	private void displaySuccessStatus() {
		mDownloadProgressBar.setProgress(0);
		mDownloadStateTxt.setTextColor(mDownloadStateTxtDisableColor);
		mDownloadStateTxt.setBackgroundColor(color.transparent);
		mDownloadStateTxt.setText("已下载");

	}

	/**
	 * 显示正在解压
	 * 
	 * @param completeCode
	 */
	private void displayUnZIPStatus() {
		mDownloadProgressBar.setProgress(0);
		mDownloadStateTxt.setTextColor(mDownloadStateTxtDisableColor);
		mDownloadStateTxt.setBackgroundColor(color.transparent);
		mDownloadStateTxt.setText("解压中");
	}

	/**
	 * 正在下载状态
	 */
	private void displayLoadingStatus() {
		if (null == mOfflineCity)
			return;
		mDownloadProgressBar.setProgress(getProgressValue(mOfflineCity
				.getcompleteCode()));
		mDownloadStateTxt.setTextColor(mDownloadStateTxtPauseColor);
		mDownloadStateTxt
				.setBackgroundResource(R.drawable.shaper_offlinemap_download_state_pause_rect);
		mDownloadStateTxt.setText("暂停");

	}

	// ------------------------ 响应事件 ------------------------
	@Override
	public void onClick(View v) {
		if (null == mOfflineCity)
			return;
		isClicked.set(true);
		int completeCode = mOfflineCity.getcompleteCode();
		int state = mOfflineCity.getState();

		switch (state) {
		case OfflineMapStatus.UNZIP:
		case OfflineMapStatus.SUCCESS:
		case OfflineMapStatus.WAITING:
			break;

		case OfflineMapStatus.LOADING:
			pauseDownload();
			displayPauseStatus(completeCode);
			break;

		case OfflineMapStatus.PAUSE:
		case OfflineMapStatus.CHECKUPDATES:
		case OfflineMapStatus.ERROR:
		case OfflineMapStatus.NEW_VERSION:
		default:
			// 非WIFI网络下进行下载判断
			if (!AppManager.getInstance().getWifiConnectedState()) {
				SweetAlertDialog dialog = SweetAlertDialog.buildConfirmDialog(
						mContext, EVENT_OFFLINEMAP_DOWNLOAD,
						"正处于非WIFI网络环境,请确认是否下载?", this);
				dialog.show();
				break;
			}

			if (startDownload())
				displayWaitingStatus();
			else
				displayExceptionStatus();
			break;
		}

		Log.d(TAG, mOfflineCity.getCity() + " " + mOfflineCity.getState());
	}

	/**
	 * 弹出操作对话框
	 */
	@Override
	public boolean onLongClick(View v) {
		final int state = mOfflineCity.getState();

		if (OfflineMapStatus.UNZIP == state)
			return true;

		mDialog.show();
		switch (state) {
		case OfflineMapStatus.SUCCESS:
			mDialog.setItem1Txt(mContext.getResources().getString(
					R.string.check_update));
			mDialog.setItem2Txt(mContext.getResources().getString(
					R.string.remove_map));
			break;

		default:
			mDialog.setItem1Txt(mContext.getResources().getString(
					R.string.download_map));
			mDialog.setItem2Txt(mContext.getResources().getString(
					R.string.download_cancel));
			break;
		}

		// 监听对话框按钮事件
		mDialog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					// 已成功下载对话框操作
					if (state == OfflineMapStatus.SUCCESS) {
						if (R.id.id_textView_item1 == v.getId()) {
							mOfflineMapMgr.checkUpdateDowloadedCity();
						} else {
							mOfflineMapMgr.remove(mOfflineCity.getCity());
						}
					} else {
						if (R.id.id_textView_item1 == v.getId()) {
							if (state == OfflineMapStatus.LOADING)
								return;
							mDownloadStateTxt.callOnClick();
						} else {
							mOfflineMapMgr.remove(mOfflineCity.getCity());
						}
					}
					mDialog.dismiss();
				} catch (AMapException e) {
					e.printStackTrace();
					mDialog.dismiss();
				}
			}
		});

		return true;
	}

	/**
	 * 对话框确认点击
	 */
	@Override
	public void onClick(SweetAlertDialog sweetAlertDialog) {
		sweetAlertDialog.dismiss();
		if (EVENT_OFFLINEMAP_DOWNLOAD == sweetAlertDialog.getId()) {
			if (startDownload())
				displayWaitingStatus();
			else
				displayExceptionStatus();
		}
	}
}
