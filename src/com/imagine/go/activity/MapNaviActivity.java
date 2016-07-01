package com.imagine.go.activity;

import static com.imagine.go.Constants.EVENT_ACTIVITY_FINISH_MAP_NAVI;
import static com.imagine.go.Constants.EVENT_NAVI_RECALCUL_ROUTE;
import static com.imagine.go.Constants.IS_DEBUG;
import static com.imagine.go.Constants.NAVI_MODE_EMULATOR;
import static com.imagine.go.Constants.NAVI_MODE_GPS;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.model.AMapNaviGuide;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviInfo;
import com.imagine.go.AppManager;
import com.imagine.go.R;
import com.imagine.go.control.AMapNaviController;
import com.imagine.go.control.AMapNaviController.OnNavingListener;
import com.imagine.go.util.AnimationFactory;
import com.imagine.go.util.GeoCalcUtil;
import com.imagine.go.util.TimerUtil;
import com.imagine.go.view.RippleLayout;

/**
 * MapNavActivity:地图导航层
 * 
 * @author Jinhu
 * @date 2016/3/28
 */
public class MapNaviActivity extends SensorActivity implements OnClickListener,
		OnSweetClickListener, OnNavingListener {
	private static final String TAG = MapNaviActivity.class.getSimpleName();

	// -------- 界面相关 --------
	/* 导航地图. */
	private AMapNaviView mMapNaviView;

	/* 工具栏. */
	/* 导航按钮. */
	private ImageView mNaviBtn;
	/* 结束按钮. */
	private RippleLayout mExitBtn;
	/* 全览按钮. */
	private RippleLayout mBrowseBtn;
	/* 目的地址提示. */
	private TextView mDestinationAddText;

	/* 转向箭头. */
	private ImageView mTurnView;
	/* 当前距离文本. */
	private TextView mDistanceText;

	/* 道路、总距离、总时间提示文本. */
	private TextView mRouteText;
	private TextView mTotalDistanceText;
	private TextView mTotalTimeText;

	/* 转向图标. */
	private int[] mTurnIcons;

	/* 指南针. */
	private ImageView mCompassView;

	/* 对话框 . */
	private SweetAlertDialog mDialog;

	// -------- 业务相关 --------
	private AMap mMap;

	/* 导航控制. */
	private AMapNaviController mMapNaviController;

	/* 导航模式 . */
	private int mNaviMode = IS_DEBUG ? NAVI_MODE_EMULATOR : NAVI_MODE_GPS;

	/* 当前地图缩放值. */
	private int mZoom = 16;

	/* 记录指南针转过的角度. */
	private float mCurrentDegree = 0f;

	// ------------------------ 初始化视图 ------------------------
	/**
	 * 初始化视图组件
	 */
	private void initView() {
		// ---初始化视图组件---
		// 导航视图布局不可见
		mMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
		AMapNaviViewOptions options = mMapNaviView.getViewOptions();
		options.setLayoutVisible(false);
		options.setZoom(mZoom);
		options.setEndPointBitmap(BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_map_target_2));
		options.setStartPointBitmap(BitmapFactory.decodeResource(
				getResources(), R.drawable.ic_map_start_2));
		mMapNaviView.setViewOptions(options);

		// 工具栏
		// 导航按钮
		mNaviBtn = (ImageView) findViewById(R.id.id_imageView_navi_navBtn);
		// 结束按钮
		mExitBtn = (RippleLayout) findViewById(R.id.id_rippleLayout_toolBar_exitBtn);
		// 全览按钮
		mBrowseBtn = (RippleLayout) findViewById(R.id.id_rippleLayout_toolBar_browseBtn);

		// 目的地址提示
		mDestinationAddText = (TextView) findViewById(R.id.id_textView_navi_mDestinationTip);
		if (null != mDestinationPoint.getAddress()) {
			mDestinationAddText.setText("目的地 :   "
					+ mDestinationPoint.getAddress());
		}

		// 转向箭头
		mTurnView = (ImageView) findViewById(R.id.id_imageView_navi_turn);
		// 当前距离
		mDistanceText = (TextView) findViewById(R.id.id_textView_navi_distance);

		// 道路、总距离、总时间提示文本
		mRouteText = (TextView) findViewById(R.id.id_textView_navi_route);
		mTotalDistanceText = (TextView) findViewById(R.id.id_textView_navi_totalDistance);
		mTotalTimeText = (TextView) findViewById(R.id.id_textView_navi_totalTime);

		// 指南针
		mCompassView = (ImageView) findViewById(R.id.id_imageView_navi_compass);

		// ---初始化转向图标---
		initTurnIconData();
	}

	/**
	 * 初始化转向图标
	 */
	private void initTurnIconData() {
		TypedArray ar = getResources().obtainTypedArray(R.array.navi_turn);
		mTurnIcons = new int[ar.length()];
		for (int i = 0; i < ar.length(); i++) {
			mTurnIcons[i] = ar.getResourceId(i, 0);
		}
		ar.recycle();
	}

	// ------------------------ 生命周期 ------------------------
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 自定义标题栏
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_map_navi);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.bar_title_map_navi);

		// 初始化视图
		initView();

		// 导航控制
		mMapNaviController = new AMapNaviController(getApplicationContext(),
				mMapNaviView);
		// 监听导航信息回调
		mMapNaviController.setOnNavingListener(this);
		// 配置起点、终点信息
		mMapNaviController.setUpLatLng(mLocationPoint.getLatLng(),
				mDestinationPoint.getLatLng());
		// 准备导航
		mMapNaviController.preNavi(getApplicationContext(), mNaviMode);
		mMapNaviController.onCreate(savedInstanceState);

		// 地图控制
		mMap = mMapNaviView.getMap();
		mMap.getUiSettings().setLogoPosition(
				AMapOptions.LOGO_POSITION_BOTTOM_CENTER);

		mDelayRate = SensorManager.SENSOR_DELAY_UI;

		if (IS_DEBUG) {
			Log.d(TAG, "--OnCreated()--");
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (IS_DEBUG) {
			Log.d(TAG, "--OnStarted()--");
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
	protected void onStop() {
		super.onStop();
		if (IS_DEBUG) {
			Log.d(TAG, "--OnStoped()--");
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

		private WeakReference<MapNaviActivity> mActivity;

		public MHandler(MapNaviActivity mActivity) {
			this.mActivity = new WeakReference<MapNaviActivity>(mActivity);
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

	// ------------------------ 响应事件 ------------------------
	/**
	 * 按钮点击回调
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_imageView_navi_locate_btn:
			// 定位
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
					mLocationPoint.getLatLng(), mZoom));
			mMapNaviView.recoverLockMode();
			break;
		case R.id.id_imageView_navi_zoomIn_btn:
			// 地图放大
			mZoom++;
			mMap.animateCamera(CameraUpdateFactory.zoomTo(mZoom));
			break;
		case R.id.id_imageView_navi_zoomOut_btn:
			// 地图缩小
			mZoom--;
			mMap.animateCamera(CameraUpdateFactory.zoomTo(mZoom));
			break;
		case R.id.id_imageView_navi_navBtn:
			mNaviBtn = (ImageView) v;
			// 暂停导航
			if (mMapNaviController.IsNaving()) {
				mMapNaviController.pauseNavi();
				mNaviBtn.setImageResource(R.drawable.ic_toolbar_nav);
			} else {
				// 启动导航
				mMapNaviController.startNavi();
				mNaviBtn.setImageResource(R.drawable.ic_toolbar_pause);
			}
			break;
		case R.id.id_rippleLayout_toolBar_exitBtn:
			// 结束导航
			mExitBtn.showRipple();
			TimerUtil.schedule(mHandler, EVENT_ACTIVITY_FINISH_MAP_NAVI,
					mExitBtn.getAnimDuration());
			break;
		case R.id.id_rippleLayout_toolBar_browseBtn:
			// 全览
			mBrowseBtn.showRipple();
			mMapNaviView.displayOverview();
			break;
		}
	}

	/**
	 * 定位信息更新回调
	 */
	@Override
	public void onLocationSucceeded(AMapLocation amapLocation) {
		super.onLocationSucceeded(amapLocation);
	}

	/**
	 * 对话框确认按钮响应
	 */
	@Override
	public void onClick(SweetAlertDialog sweetAlertDialog) {
		switch (sweetAlertDialog.getId()) {
		case EVENT_ACTIVITY_FINISH_MAP_NAVI:// 结束导航
			this.finish();
			break;
		case EVENT_NAVI_RECALCUL_ROUTE:
			mMapNaviController.calculRoute();// 计算路径
			break;
		}
		mDialog = sweetAlertDialog;

	}

	/**
	 * 路径规划成功
	 */
	@Override
	public void onCalculateRouteSuccess(List<AMapNaviGuide> mNaviGuides,
			AMapNaviPath mNaviPath) {
		// 获得当前一段的路名及路程
		if (mNaviGuides.size() > 0) {
			mRouteText.setText(mNaviGuides.get(0).getName());
			mDistanceText.setText(GeoCalcUtil.formatDistance(mNaviGuides.get(0)
					.getLength()));
		}
		// 获得所有导航路径的时间和路程
		mTotalDistanceText.setText(GeoCalcUtil.formatDistance(mNaviPath
				.getAllLength()));
		mTotalTimeText.setText(GeoCalcUtil.formatTime(mNaviPath.getAllTime()));

		if (null != mDialog && mDialog.isShowing()) {
			mDialog.dismiss();
		}
	}

	/**
	 * 导航信息回调
	 */
	@Override
	public void onNaviInfoUpdated(NaviInfo info) {
		// 更新信息
		mRouteText.setText(info.getCurrentRoadName());
		mTotalDistanceText.setText(GeoCalcUtil.formatDistance(info
				.getPathRetainDistance()));
		mTotalTimeText
				.setText(GeoCalcUtil.formatTime(info.getPathRetainTime()));
		mDistanceText.setText(GeoCalcUtil.formatDistance(info
				.getCurStepRetainDistance()));
		// 设置转向图标
		int iconType = info.getIconType();
		if (iconType < mTurnIcons.length && 0 != mTurnIcons[iconType]) {
			mTurnView.setImageResource(mTurnIcons[iconType]);
			mTurnView.invalidate();
		}

	}

	/**
	 * 到达目的地
	 */
	@Override
	public void onArriveDestination() {
		mNaviBtn.setImageResource(R.drawable.ic_toolbar_nav);
		mTurnView.setImageResource(R.drawable.ic_navi_destation);
	}

	/**
	 * 已重新规划路径回调
	 */
	@Override
	public void onReCalculateRoute() {
		mMapNaviController.pauseNavi();
		mNaviBtn.setImageResource(R.drawable.ic_toolbar_nav);
	}

	/**
	 * 路径规划失败
	 */
	@Override
	public void onCalculateRouteFailure() {
		if (null != mDialog && mDialog.isShowing()) {
			mDialog.dismiss();
		}
		TimerUtil.schedule(mHandler, EVENT_NAVI_RECALCUL_ROUTE, 1200);
	}

	/**
	 * 导航指引信息回调
	 */
	@Override
	public void onGetNavigationText(String text) {

	}

	/**
	 * 传感器数据处理
	 */
	@Override
	protected synchronized void onSensorAccess() {
		float azimuth = (float) ((mOrientation[0] * 180) / Math.PI);
		Animation anim = AnimationFactory.rotateAnimation(mCurrentDegree,
				-azimuth, 200);
		anim.setFillAfter(false);
		// 指南针逆向旋转
		mCompassView.startAnimation(anim);
		mCurrentDegree = -azimuth;

		if (IS_DEBUG)
			Log.d(TAG, "azimuth= " + azimuth);
	}

}
