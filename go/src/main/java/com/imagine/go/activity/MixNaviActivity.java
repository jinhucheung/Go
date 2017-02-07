package com.imagine.go.activity;

import static com.imagine.go.Constants.EVENT_ACTIVITY_FINISH_MAP_NAVI;
import static com.imagine.go.Constants.EVENT_NAVI_RECALCUL_ROUTE;
import static com.imagine.go.Constants.IS_DEBUG;
import static com.imagine.go.Constants.NAVI_MODE_EMULATOR;
import static com.imagine.go.Constants.NAVI_MODE_GPS;

import java.lang.ref.WeakReference;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener;

import com.amap.api.location.AMapLocation;
import com.amap.api.navi.model.AMapNaviGuide;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviInfo;
import com.imagine.go.AppManager;
import com.imagine.go.R;
import com.imagine.go.ar.ARData;
import com.imagine.go.ar.ARNaviMarker;
import com.imagine.go.control.AMapNaviController;
import com.imagine.go.control.AMapNaviController.OnNavingListener;
import com.imagine.go.util.GeoCalcUtil;
import com.imagine.go.util.TimerUtil;
import com.imagine.go.util.ToastUtil;

/**
 * AR导航层UI<br/>
 * 高层控件处理 导航处理
 * 
 * @author Jinhu
 * @date 2016/4/20
 */
public class MixNaviActivity extends ARNaviActivity implements
		OnNavingListener, OnClickListener, OnSweetClickListener {
	private static final String TAG = MixNaviActivity.class.getSimpleName();

	// ----------------界面相关-------------
	/* 对话框 . */
	private SweetAlertDialog mDialog;

	/* 导航按钮 . */
	private ImageView mNaviBtn;

	/* 导航信息提示 . */
	private TextView mNaviTxt;

	/* 当前路段剩余距离. */
	private TextView mCurrentDistanceTxt;

	/* 全程时间 . */
	private TextView mTotalTimeTxt;

	/* 全程距离 . */
	private TextView mTotalDistanceTxt;

	/* 到达时间 . */
	private TextView mArriveTimeTxt;

	// ----------------控制相关-------------
	/* 导航控制. */
	private AMapNaviController mMapNaviController;

	/* 规划路径标志 . */
	private boolean isCalculRouteSuccess;

	/* 导航模式 . */
	private int mNaviMode = IS_DEBUG ? NAVI_MODE_EMULATOR : NAVI_MODE_GPS;

	// ------------------------ 初始化视图 ------------------------
	/**
	 * 初始化视图组件
	 */
	private void initView() {
		mNaviTxt = (TextView) findViewById(R.id.id_textView_navi_txt);
		mCurrentDistanceTxt = (TextView) findViewById(R.id.id_textView_navi_distance);
		mTotalTimeTxt = (TextView) findViewById(R.id.id_textView_navi_totalTime);
		mTotalDistanceTxt = (TextView) findViewById(R.id.id_textView_navi_totalDistance);
		mArriveTimeTxt = (TextView) findViewById(R.id.id_textView_navi_arriveTime);
	}

	// ------------------------ 生命周期 ------------------------
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initView();

		// 导航控制
		mMapNaviController = new AMapNaviController(getApplicationContext());
		// 监听导航信息回调
		mMapNaviController.setOnNavingListener(this);
		// 配置起点、终点信息
		mMapNaviController.setUpLatLng(mLocationPoint.getLatLng(),
				mDestinationPoint.getLatLng());
		// 准备导航
		mMapNaviController.preNavi(getApplicationContext(), mNaviMode);
		mMapNaviController.onCreate(savedInstanceState);

		// 设置导航起点Marker
		ARData.getInstance().setStartMarker(
				new ARNaviMarker(mLocationPoint.clone()));
		if (IS_DEBUG) {
			Log.d(TAG, " --onCreated()--");
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (IS_DEBUG) {
			Log.d(TAG, "--OnStared()--");
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapNaviController.onResume();
		if (IS_DEBUG) {
			Log.d(TAG, "--OnResumed()--");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapNaviController.onPasue();
		if (IS_DEBUG) {
			Log.d(TAG, "--OnPaused()--");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapNaviController.onDestroy();
		// 删除Activity
		AppManager.getInstance().delActivity(this);
		if (IS_DEBUG) {
			Log.d(TAG, "--OnDestroyed()--");
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapNaviController.onSaveInstanceState(outState);
	}

	@Override
	public void onBackPressed() {
		// 弹出对话框 询问是否结束导航
		SweetAlertDialog dialog = SweetAlertDialog.buildConfirmDialog(this,
				EVENT_ACTIVITY_FINISH_MAP_NAVI, "确定退出导航?", this);
		dialog.show();
	}

	// ------------------------ 处理器 ------------------------
	/**
	 * MHandler:处理子线程分发的事件
	 * 
	 * @author Jinhu
	 * @date 2016/3/21
	 */
	private MHandler mHandler = new MHandler(this);

	public static class MHandler extends Handler {

		private WeakReference<MixNaviActivity> mActivity;

		public MHandler(MixNaviActivity mActivity) {
			this.mActivity = new WeakReference<MixNaviActivity>(mActivity);
		}

		/**
		 * 处理消息
		 * 
		 * @param msg
		 */
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_ACTIVITY_FINISH_MAP_NAVI:
				// 弹出对话框 询问是否结束导航
				SweetAlertDialog.buildConfirmDialog(mActivity.get(),
						EVENT_ACTIVITY_FINISH_MAP_NAVI, "确定退出导航?",
						mActivity.get()).show();
				break;
			case EVENT_NAVI_RECALCUL_ROUTE:
				// 弹出对话框 询问是否重新计算路径
				SweetAlertDialog.buildConfirmDialog(mActivity.get(),
						EVENT_NAVI_RECALCUL_ROUTE, "重新计算路径?", mActivity.get())
						.show();
				break;
			}
		}
	}

	// ------------------------ 导航信息处理 ------------------------
	/**
	 * 导航信息回调
	 */
	@Override
	public void onNaviInfoUpdated(NaviInfo info) {
		// 定位控制器同时维护定位工作
		AMapLocation mLocation = new AMapLocation("MyLocation");
		mLocation.setLatitude(info.getCoord().getLatitude());
		mLocation.setLongitude(info.getCoord().getLongitude());
		mLocation.setAddress(info.getCurrentRoadName());

		this.onLocationSucceeded(mLocation);

		// 更新视图
		mNaviView.setCurrentStep(info.getCurStep());
		mNaviView.setArrowType(info.getIconType());

		mCurrentDistanceTxt.setText(GeoCalcUtil.formatDistance(info
				.getCurStepRetainDistance()));
		mCurrentDistanceTxt.postInvalidate();

		// 定位控制器停止定位
		if (IS_DEBUG) {
			if (mLocationController.isLocating()) {
				mLocationController.onStop();
			}
		}
	}

	/**
	 * 到达目的地
	 */
	@Override
	public void onArriveDestination() {
		mNaviBtn.setImageResource(R.drawable.ic_ar_navi_start);

		if (null == mDialog || !mDialog.isShowing()) {
			mDialog = SweetAlertDialog.buildConfirmDialog(this, -1, "到达目的地",
					null);
			mDialog.show();
		}
	}

	/**
	 * 路径规划成功
	 */
	@Override
	public void onCalculateRouteSuccess(List<AMapNaviGuide> mNaviGuides,
			AMapNaviPath mNaviPath) {
		isCalculRouteSuccess = true;

		updateARNaviMarkers(mNaviGuides); // AR层添加导航点

		ToastUtil.showShort("路径计算就绪");

		// 更新导航信息
		// 获得所有导航路径的时间和路程
		mTotalDistanceTxt.setText("全程 "
				+ GeoCalcUtil.formatDistance(mNaviPath.getAllLength()));
		mTotalTimeTxt.setText("约 "
				+ GeoCalcUtil.formatTime(mNaviPath.getAllTime()));
		mTotalDistanceTxt.postInvalidate();
		mTotalTimeTxt.postInvalidate();

		// 设置到达时间
		String time = GeoCalcUtil.getCurrentSystemTime(mNaviPath.getAllTime());
		mArriveTimeTxt.setText(time + " 到达");
		mArriveTimeTxt.postInvalidate();

		if (mNaviGuides.size() > 0) {
			AMapNaviGuide mGuide = mNaviGuides.get(0);
			mCurrentDistanceTxt.setText(GeoCalcUtil.formatDistance(mGuide
					.getTime()));
			mNaviTxt.setText(mGuide.getName());
			mCurrentDistanceTxt.postInvalidate();
			mNaviTxt.postInvalidate();
		}

		if (null != mDialog && mDialog.isShowing()) {
			mDialog.dismiss();
		}
	}

	/**
	 * 路径规划失败
	 */
	@Override
	public void onCalculateRouteFailure() {
		isCalculRouteSuccess = false;

		if (null != mDialog && mDialog.isShowing()) {
			mDialog.dismiss();
		}
		TimerUtil.schedule(mHandler, EVENT_NAVI_RECALCUL_ROUTE, 1200); // 询问是否重新计算
	}

	/**
	 * 已重新规划路径回调
	 */
	@Override
	public void onReCalculateRoute() {
		// 设置导航起点Marker
		ARData.getInstance().setStartMarker(
				new ARNaviMarker(mLocationPoint.clone()));
	}

	/**
	 * 导航指引信息回调
	 */
	@Override
	public void onGetNavigationText(String text) {
		mNaviTxt.setText(text);
		mNaviTxt.postInvalidate();
	}

	// ------------------------ 处理视图事件 ------------------------
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_imageView_naviBtn:
			mNaviBtn = (ImageView) v;
			if (isCalculRouteSuccess) {
				if (mMapNaviController.IsNaving()) {
					mMapNaviController.pauseNavi();
					mNaviBtn.setImageResource(R.drawable.ic_ar_navi_start);
				} else {
					// 启动导航
					mMapNaviController.startNavi();
					mNaviBtn.setImageResource(R.drawable.ic_ar_navi_pause);
				}
				mNaviView.postInvalidate();
			}
			break;
		}
	}

	/**
	 * 对话框确认按钮响应
	 */
	@Override
	public void onClick(SweetAlertDialog sweetAlertDialog) {
		switch (sweetAlertDialog.getId()) {
		case EVENT_ACTIVITY_FINISH_MAP_NAVI:// 结束导航
			AppManager.getInstance().delActivity(this);
			this.finish();
			break;
		case EVENT_NAVI_RECALCUL_ROUTE:
			mMapNaviController.calculRoute();// 计算路径
			break;
		}
		mDialog = sweetAlertDialog;
	}

}
