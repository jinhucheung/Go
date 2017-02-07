package com.imagine.go.activity;

import static com.imagine.go.Constants.EVENT_ACTIVITY_FINISH_USER_DEFINED_POINT;
import static com.imagine.go.Constants.EVENT_MAP_ZOOM_IN;
import static com.imagine.go.Constants.EVENT_MAP_ZOOM_OUT;
import static com.imagine.go.Constants.EVENT_SEARCH_POI;
import static com.imagine.go.Constants.EVENT_USER_DEFINED_POINT_ADD;
import static com.imagine.go.Constants.EVENT_USER_DEFINED_POINT_DEL;
import static com.imagine.go.Constants.IS_DEBUG;
import static com.imagine.go.Constants.NO_RESULT;
import static com.imagine.go.Constants.TAB_USER_DEFINED_POINT_MARKER;
import static com.imagine.go.Constants.VALUE_DEFAULT_SEARCH_RADIUS;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.route.WalkRouteResult;
import com.balysv.materialmenu.MaterialMenuDrawable.IconState;
import com.balysv.materialmenu.MaterialMenuView;
import com.imagine.go.R;
import com.imagine.go.control.AMapController;
import com.imagine.go.control.AMapController.AMapStatus;
import com.imagine.go.control.AMapController.AMapStatusLinstener;
import com.imagine.go.control.AMapQueryer;
import com.imagine.go.control.AMapQueryer.OnMapQueryListener;
import com.imagine.go.control.APoiSearcher;
import com.imagine.go.control.APoiSearcher.APoiSearchListener;
import com.imagine.go.data.DatabaseManager;
import com.imagine.go.data.GeoPointDao;
import com.imagine.go.model.GeoPoint;
import com.imagine.go.util.PoiTypeMatcher;
import com.imagine.go.util.TimerUtil;
import com.imagine.go.util.ToastUtil;
import com.imagine.go.view.ArcMenu;
import com.imagine.go.view.ArcMenu.OnMenuItemClickListener;
import com.imagine.go.view.GeoPointInfoDialog;
import com.imagine.go.view.GeoPointInfoDialog.OnInputConfirmListener;
import com.imagine.go.view.LayerDialog;
import com.imagine.go.view.LayerDialog.Layer;
import com.imagine.go.view.LayerDialog.OnLayerChangedListener;
import com.imagine.go.view.MGeoPointInfowindow;
import com.imagine.go.view.MGeoPointInfowindow.onInfowindowClickedLinstener;
import com.imagine.go.view.RippleLayout;

/**
 * 自标记地理点管理层
 * 
 * @author Jinhu
 * @date 2016/5/21
 */
public class UserDefinedPointActivity extends OriginActivity implements
		OnClickListener, AMapStatusLinstener, APoiSearchListener,
		OnInputConfirmListener, OnMapQueryListener,
		onInfowindowClickedLinstener, OnLayerChangedListener {
	private static final String TAG = UserDefinedPointActivity.class
			.getSimpleName();

	// -------- 界面相关 --------
	/* 标题栏 . */
	private View mtitlebar;
	/* 退出按钮 . */
	private RippleLayout mBackBtn;
	/* 箭头. */
	private MaterialMenuView mMaterialBtn;

	/* 地图层视图 . */
	private MapView mMapView;

	/* 地图缩放按钮 . */
	private View mZoomOutBtn;
	private View mZoomInBtn;

	/* Poi图层选择 . */
	private ImageView mLayerBtn;

	/* Infowindow . */
	private MGeoPointInfowindow minfowindow;

	/* 搜索标签菜单. */
	private ArcMenu mArcMenu;

	/* 进度对话框 . */
	private SweetAlertDialog mProgressDialog;

	/* 地理信息点输入对空框 . */
	private GeoPointInfoDialog mGeoPointInfoDialog;

	/* Poi层选择对话框 . */
	private LayerDialog mLayerDialog;

	// -------- 业务相关 --------
	/* Poi点搜索. */
	private APoiSearcher mPoiSearcher;

	/* Poi结果 . */
	private PoiResult mCurrentPoiResult;

	/* 搜索组件 . */
	private AMapQueryer mQueryer;

	/* 地图控制 . */
	private AMapController mMapController;

	/* 地理信息点数据访问接口 . */
	private GeoPointDao mGeoPointDao;

	/* 当前所有用户定义信息点 . */
	private List<GeoPoint> mGeoPointList;
	/* 当前用户定义信息点 . */
	private GeoPoint mCurGeoPoint;
	/* 当前用户定义信息点Id . */
	private int mCurGeoPointId;

	/* 开启选点模式 . */
	private AtomicBoolean mSelectMode = new AtomicBoolean(false);

	/* 数据操作锁 . */
	private AtomicBoolean mAffairLock = new AtomicBoolean(false);

	/* 当前的兴趣点层 . */
	private Layer mCurLayer = Layer.USER_POI;

	/* 高德兴趣点显示锁 . */
	private AtomicBoolean mAmapPoiLock = new AtomicBoolean(true);

	// ------------------------ 初始化视图 ------------------------
	/**
	 * 初始化视图组件
	 */
	private void initView() {
		// ---初始化视图组件---
		// 初始化标题栏
		mtitlebar = findViewById(R.id.id_layout_titlebar);
		mBackBtn = (RippleLayout) mtitlebar
				.findViewById(R.id.id_rippleLayout_titleBar_backBtn);
		mMaterialBtn = (MaterialMenuView) mtitlebar
				.findViewById(R.id.id_materialmenu_btn);
		mMaterialBtn.setState(IconState.ARROW);

		// 地图层
		mMapView = (MapView) findViewById(R.id.id_mapView);
		// 地图缩放
		mZoomOutBtn = findViewById(R.id.id_imageView_zoomOut_btn);
		mZoomInBtn = findViewById(R.id.id_imageView_zoomIn_btn);
		// Poi层选
		mLayerBtn = (ImageView) findViewById(R.id.id_imageView_layer);
		// 初始化Infowindow
		minfowindow = new MGeoPointInfowindow(this);

		// 搜索标签菜单
		mArcMenu = (ArcMenu) findViewById(R.id.id_arcMenu);

		mProgressDialog = new SweetAlertDialog(this,
				SweetAlertDialog.PROGRESS_TYPE);

		mGeoPointInfoDialog = new GeoPointInfoDialog(this);

		mLayerDialog = new LayerDialog(this);
		mLayerDialog.setLayer(Layer.USER_POI);

		registerViewListener();

	}

	/**
	 * 初始化视图监听器
	 */
	private void registerViewListener() {
		mBackBtn.setOnClickListener(this);
		mGeoPointInfoDialog.setOnInputConfirmListener(this);
		minfowindow.setOnInfowindowClickedLinstener(this);
		mZoomOutBtn.setOnClickListener(this);
		mZoomInBtn.setOnClickListener(this);
		mLayerBtn.setOnClickListener(this);
		mLayerDialog.setOnLayerChangedListener(this);

		// 搜索标签点击响应
		mArcMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public void onMenuItemClicked(View v, int pos) {
				mArcMenu.setUpOpenStateIcon();
				if (null == mALocation)
					return;
				String labelName = v.getTag() + "";
				String poiType = PoiTypeMatcher.getPoiType(labelName);

				mProgressDialog.show();
				// 清空之前PoiResult
				mPoiSearchData.clearPois();
				// 搜索Poi
				if (null != poiType) {
					// 通过分类搜索Poi 使用最大范围搜索 但显示搜索结果根据设置radius
					mPoiSearcher.searchNearbyType(mALocation, poiType,
							VALUE_DEFAULT_SEARCH_RADIUS);
				} else {
					// 通过关键字搜索Poi
					mPoiSearcher.searchNearbyKeyword(mALocation, labelName,
							VALUE_DEFAULT_SEARCH_RADIUS);
				}
			}
		});

	}

	// ------------------------ 生命周期 ------------------------
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 自定义标题栏
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_user_defined_point);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.bar_title_user_defined_point);

		// 初始化布局组件
		initView();

		// 获得数据访问接口
		mGeoPointDao = DatabaseManager.getInstance(this).getGeoPointDao();
		mGeoPointList = mGeoPointDao.fetchAllGeoPoints();

		// 地图控制
		mMapController = new AMapController(this, mMapView);
		mMapController.onCreate(savedInstanceState);
		// 将地图视野移到当前所在位置
		mMapController.moveToLocation(mLocationPoint.getLatLng());
		mMapController.setLocationMarkerVisible(false);
		// 设置地图Infowindow样式
		mMapController.setInfowindow(minfowindow);
		// 监听地图状态
		mMapController.setAMapStatusLinstener(this);
		// 添加已标记的Poi层至地图
		mMapController.addUserDefinedPoiOverlay(mGeoPointList);

		// Poi搜索
		mPoiSearcher = new APoiSearcher(getApplicationContext());
		mPoiSearcher.setAPoiSearchListener(this);

		mQueryer = new AMapQueryer(this);
		mQueryer.setOnMapQueryListener(this);

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
		// 地图视图启动绘制
		mMapController.onResume();
		if (IS_DEBUG) {
			Log.d(TAG, "--OnResumed()--");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 地图视图停止绘制
		mMapController.onPause();
		mAffairLock.set(false);
		mAmapPoiLock.set(false);
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
		// 销毁地图
		mMapController.onDestroy();
		if (IS_DEBUG) {
			Log.d(TAG, "--OnDestroyed()--");
		}
	}

	// ------------------------ 业务逻辑 ------------------------
	/**
	 * MHandler:处理子线程分发的事件
	 * 
	 * @author Jinhu
	 * @date 2016/3/21
	 */
	private MHandler mHandler = new MHandler(this);

	static class MHandler extends Handler {

		private WeakReference<UserDefinedPointActivity> mActivity;

		public MHandler(UserDefinedPointActivity mActivity) {
			this.mActivity = new WeakReference<UserDefinedPointActivity>(
					mActivity);
		}

		/**
		 * 处理消息
		 * 
		 * @param msg
		 */
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case EVENT_ACTIVITY_FINISH_USER_DEFINED_POINT:
				mActivity.get().onBackPressed();
				break;

			case EVENT_SEARCH_POI:
				// 清空地图覆盖层
				mActivity.get().mMapController.removeOverlay();
				List<PoiItem> pois = mActivity.get().mPoiSearchData.getPois();
				if (null == pois || 0 == pois.size()) {
					ToastUtil.showShort(NO_RESULT);
					break;
				}

				if (mActivity.get().mAmapPoiLock.get()) {
					ToastUtil.showShort("高德兴趣点层已锁定");
					return;
				}

				// 再添加当前Poi结果至地图
				mActivity.get().mMapController
						.addPoiOverlay(mActivity.get().mCurrentPoiResult);
				break;

			case EVENT_USER_DEFINED_POINT_ADD:
				mActivity.get().mProgressDialog.dismiss();
				mActivity.get().mGeoPointDao
						.addGeoPoint(mActivity.get().mCurGeoPoint); // 向数据库提交当前信息点
				mActivity.get().mMapController.removeUserDefineMarkers(); // 获取数据库里的Id信息
				mActivity.get().mGeoPointList = mActivity.get().mGeoPointDao
						.fetchAllGeoPoints();
				mActivity.get().mAffairLock.set(false);
				mActivity.get().mMapController
						.addUserDefinedPoiOverlay(mActivity.get().mGeoPointList);// 更新地图层
				break;

			case EVENT_USER_DEFINED_POINT_DEL:
				mActivity.get().mProgressDialog.dismiss();
				mActivity.get().mGeoPointDao
						.deleteGeoPoint(mActivity.get().mCurGeoPointId);
				mActivity.get().mMapController
						.removeUserDefinedMarker(mActivity.get().mCurGeoPointId);
				mActivity.get().mAffairLock.set(false);
				break;
			}
		}
	}

	// ------------------------ 响应事件 ------------------------
	/**
	 * 主界面里按钮点击响应
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_rippleLayout_titleBar_backBtn:
			mBackBtn.showRipple();
			TimerUtil.schedule(mHandler,
					EVENT_ACTIVITY_FINISH_USER_DEFINED_POINT,
					mBackBtn.getAnimDuration()); // 结束Activity
			break;
		case R.id.id_imageView_select:
			ImageView iv = (ImageView) v;
			if (mSelectMode.compareAndSet(false, true)) {
				iv.setImageResource(R.drawable.ic_ar_del);
			} else {
				iv.setImageResource(R.drawable.ic_geopoint_select);
				mSelectMode.set(false);
			}
			break;
		case R.id.id_imageView_zoomIn_btn:
			mMapController.zoom(EVENT_MAP_ZOOM_IN);
			break;
		case R.id.id_imageView_zoomOut_btn:
			mMapController.zoom(EVENT_MAP_ZOOM_OUT);
			break;
		case R.id.id_imageView_layer:
			mLayerDialog.show();
			break;
		}
	}

	/**
	 * Poi点搜索信息更新回调
	 */
	@Override
	public void onPoiSearched(PoiResult result) {
		final PoiResult fresult = result;

		// 延迟500ms再隐藏进度对话框
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				mProgressDialog.dismiss();

				if (null == fresult) {
					return;
				}
				mCurrentPoiResult = fresult;
				mPoiSearchData.setPois(fresult.getPois());
				// 处理Poi搜索
				mHandler.sendEmptyMessage(EVENT_SEARCH_POI);
			}
		}, 500);
	}

	/**
	 * 地图状态监听
	 */
	@Override
	public void onMapStatusChanged(Marker mCurrentMarker,
			LatLng mCurrentLatLng, AMapStatus status) {
		switch (status) {
		case onMarkerClick:
			if (mCurrentMarker.getObject().toString()
					.startsWith(TAB_USER_DEFINED_POINT_MARKER))
				return;
		case onMapClick:
			if (mAffairLock.get())
				return;
			if (mSelectMode.get()) {
				mCurGeoPoint = new GeoPoint(mCurrentLatLng);
				mGeoPointInfoDialog.show();
			}
			break;
		case onRegeocodeSearched:
			// GeoPoint.poiMarkerToGeoPoint(mCurrentMarker, mDestinationPoint);
			break;
		}
	}

	/**
	 * 标记地点输入备注信息确认回调
	 */
	@Override
	public void onInputeConfirm(String inputTxt) {
		if (null == inputTxt) {
			mAffairLock.set(false);
			return;
		}

		mAffairLock.set(true);
		mProgressDialog.show();

		mQueryer.searchAddress(mCurGeoPoint.getLatLng(), 200);
		mCurGeoPoint.setName(inputTxt);

	}

	/**
	 * 逆地理搜索回调
	 */
	@Override
	public void onRegeocodeSearched(RegeocodeAddress address) {
		if (null != address) {
			mCurGeoPoint.setAddress(address.getFormatAddress());
			mCurGeoPoint.setCity(address.getCity());
			mCurGeoPoint.setCityCode(address.getCityCode());
			mCurGeoPoint.setAltitude(0.0d);
			mCurGeoPoint.setSnippet(address.getFormatAddress());
			mCurGeoPoint.setId(null);
		}
		TimerUtil.schedule(mHandler, EVENT_USER_DEFINED_POINT_ADD, 500);
	}

	/**
	 * 路径规划回调
	 */
	@Override
	public void onWalkRouteSearched(WalkRouteResult walkRouteResult) {

	}

	/**
	 * 删除Marker回调
	 */
	@Override
	public synchronized void deleteMarker(Marker marker) {
		if (mAffairLock.get())
			return;

		String strId = marker.getObject().toString()
				.substring(TAB_USER_DEFINED_POINT_MARKER.length());
		int id = 0;
		try {
			id = Integer.parseInt(strId);
		} catch (NumberFormatException e) {
			Log.w(TAG, e.getMessage());
			return;
		}
		mAffairLock.set(true);
		mProgressDialog.show();
		mCurGeoPointId = id;
		TimerUtil.schedule(mHandler, EVENT_USER_DEFINED_POINT_DEL, 500); // 删除自标记点
	}

	/**
	 * 兴趣点层改变时回调
	 */
	@Override
	public void onLayerChanged(Layer layer) {
		if (layer == mCurLayer)
			return;
		mCurLayer = layer;
		switch (layer) {
		case AMAP_POI:
			mLayerBtn.setImageResource(R.drawable.ic_map_layer_amap);
			mMapController.removeUserDefineMarkers();
			if (mMapController.isNullPoiOverlay())
				mMapController.addPoiOverlay(mCurrentPoiResult);
			mAmapPoiLock.set(false);
			break;
		case USER_POI:
			mLayerBtn.setImageResource(R.drawable.ic_map_layer_user);
			mMapController.removePoiOverlay();
			if (mMapController.isNullUserDefineMarkers())
				mMapController.addUserDefinedPoiOverlay(mGeoPointList);
			mAmapPoiLock.set(true);
			break;
		case MIX_POI:
			mLayerBtn.setImageResource(R.drawable.ic_map_layer_mix);
			if (mMapController.isNullPoiOverlay())
				mMapController.addPoiOverlay(mCurrentPoiResult);
			if (mMapController.isNullUserDefineMarkers())
				mMapController.addUserDefinedPoiOverlay(mGeoPointList);
			mAmapPoiLock.set(false);
			break;
		case NO_POI:
			mLayerBtn.setImageResource(R.drawable.ic_map_layer);
			mMapController.removePoiOverlay();
			mMapController.removeUserDefineMarkers();
			mAmapPoiLock.set(true);
			break;
		}
	}

}
