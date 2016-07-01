package com.imagine.go.activity;

import static com.imagine.go.Constants.EVENT_ACTIVITY_START_AR_NAVI;
import static com.imagine.go.Constants.EVENT_ACTIVITY_START_OFFLINEMAP;
import static com.imagine.go.Constants.EVENT_ACTIVITY_START_USER_DEFINED_POINT;
import static com.imagine.go.Constants.EVENT_ACTIVITY_START_WEATHER;
import static com.imagine.go.Constants.EVENT_AR_INFOWINDOW_ARISE;
import static com.imagine.go.Constants.EVENT_BOTTOM_DIALOG_SHOW;
import static com.imagine.go.Constants.EVENT_MAP_ZOOM_IN;
import static com.imagine.go.Constants.EVENT_MAP_ZOOM_OUT;
import static com.imagine.go.Constants.EVENT_SEARCH_POI;
import static com.imagine.go.Constants.EVENT_SEEKDIALOG_PUSH;
import static com.imagine.go.Constants.IS_DEBUG;
import static com.imagine.go.Constants.NO_RESULT;
import static com.imagine.go.Constants.VALUE_DEFAULT_SEARCH_RADIUS;
import static com.imagine.go.Constants.VALUE_POSITION_DRAWERITEM_OFFLINEMAP;
import static com.imagine.go.Constants.VALUE_POSITION_DRAWERITEM_RADIUS;
import static com.imagine.go.Constants.VALUE_POSITION_DRAWERITEM_USERPOINT;
import static com.imagine.go.Constants.VALUE_POSITION_DRAWERITEM_WEATHER;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ImageView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener;
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetSeekBarChangeListener;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.imagine.go.AppManager;
import com.imagine.go.R;
import com.imagine.go.ar.ARData;
import com.imagine.go.ar.ARMarker;
import com.imagine.go.control.AMapController;
import com.imagine.go.control.AMapController.AMapStatus;
import com.imagine.go.control.AMapController.AMapStatusLinstener;
import com.imagine.go.control.APoiSearcher;
import com.imagine.go.control.APoiSearcher.APoiSearchListener;
import com.imagine.go.data.DatabaseManager;
import com.imagine.go.data.GeoPointDao;
import com.imagine.go.model.GeoPoint;
import com.imagine.go.util.AnimationFactory;
import com.imagine.go.util.PoiTypeMatcher;
import com.imagine.go.util.TimerUtil;
import com.imagine.go.util.ToastUtil;
import com.imagine.go.view.ARMarkerDialog;
import com.imagine.go.view.ARMarkerDialog.onNaviListener;
import com.imagine.go.view.ArcMenu;
import com.imagine.go.view.ArcMenu.OnMenuItemClickListener;
import com.imagine.go.view.BottomBtnDialog;
import com.imagine.go.view.GeoPointInfoDialog;
import com.imagine.go.view.GeoPointInfoDialog.OnInputConfirmListener;
import com.imagine.go.view.LayerDialog;
import com.imagine.go.view.LayerDialog.Layer;
import com.imagine.go.view.LayerDialog.OnLayerChangedListener;
import com.imagine.go.view.MInfowindow;
import com.imagine.go.view.MInfowindow.onInfowindowClickedLinstener;
import com.imagine.go.view.MaterialDrawerLayout;

/**
 * ARActivity:AR层
 * 
 * @author Jinhu
 * @date 2016/3/21
 */
public class MixActivity extends ARActivity implements OnClickListener,
		APoiSearchListener, OnSweetClickListener, OnSweetSeekBarChangeListener,
		AMapStatusLinstener, onInfowindowClickedLinstener, onNaviListener,
		OnLayerChangedListener, OnInputConfirmListener {
	private static final String TAG = MixActivity.class.getSimpleName();

	// -------- 界面相关 --------

	/* 侧滑标题栏 . */
	/* 已封装侧滑栏及标题栏逻辑 . */
	private MaterialDrawerLayout mDrawer;

	/* 地图层. */
	private View mMapFrame;
	/* 地图层视图 . */
	private MapView mMapView;
	/* 地图Infowindow . */
	private MInfowindow minfowindow;
	/* 地图缩放按钮 . */
	private View mZoomOutBtn;
	private View mZoomInBtn;
	/* 图层按钮. */
	private ImageView mLayerBtn;

	/* 搜索标签菜单. */
	private ArcMenu mArcMenu;

	/* 进度对话框 . */
	private SweetAlertDialog mProgressDialog;

	/* Marker信息对话框 . */
	private ARMarkerDialog mMarkerDialog;

	/* 导航按钮 . */
	private View mNaviBtn;

	/* 底部对话框 . */
	private BottomBtnDialog mBottomBtnDialog;

	/* 地理信息点输入对空框 . */
	private GeoPointInfoDialog mGeoPointInfoDialog;

	/* Poi层选择对话框 . */
	private LayerDialog mLayerDialog;

	// -------- 业务相关 --------

	/* Poi点搜索. */
	private APoiSearcher mPoiSearcher;

	/* Poi结果 . */
	private PoiResult mCurrentPoiResult;

	/* 地图控制 . */
	private AMapController mMapController;

	/* 地图移动锁 . */
	private AtomicBoolean mMapMoveToLock = new AtomicBoolean(true);

	/* 当前的兴趣点层 . */
	private Layer mCurLayer = Layer.AMAP_POI;
	/* 高德兴趣点显示锁 . */
	private AtomicBoolean mAmapPoiLock = new AtomicBoolean(false);

	/* 地理信息点数据访问接口 . */
	private GeoPointDao mGeoPointDao;

	/* 当前所有用户定义信息点 . */
	private List<GeoPoint> mGeoPointList;

	/* 数据操作锁 . */
	private AtomicBoolean mAffairLock = new AtomicBoolean(false);

	// ------------------------ 初始化视图 ------------------------
	/**
	 * 初始化视图组件
	 */
	private void initView() {
		// ---初始化视图组件---
		// 地图层
		mMapFrame = findViewById(R.id.id_frameLayout_map);
		// 地图视图
		mMapView = (MapView) findViewById(R.id.id_mapView);
		// 初始化Infowindow
		minfowindow = new MInfowindow(this);
		// 地图缩放
		mZoomOutBtn = findViewById(R.id.id_imageView_zoomOut_btn);
		mZoomInBtn = findViewById(R.id.id_imageView_zoomIn_btn);

		mLayerBtn = (ImageView) findViewById(R.id.id_imageView_layer);

		// 搜索标签菜单
		mArcMenu = (ArcMenu) findViewById(R.id.id_arcMenu);

		// 导航按钮
		mNaviBtn = findViewById(R.id.id_imageView_ar_navi);

		// 侧滑标题栏
		mDrawer = (MaterialDrawerLayout) findViewById(R.id.id_layout_drawer);
		mDrawer.bindTitleBar(findViewById(R.id.id_layout_titlebar));

		mProgressDialog = new SweetAlertDialog(this,
				SweetAlertDialog.PROGRESS_TYPE);

		mMarkerDialog = new ARMarkerDialog(this);
		mMarkerDialog.setOnNaviListener(this);

		// Poi层选对话框
		mLayerDialog = new LayerDialog(this);
		mLayerDialog.setLayer(Layer.AMAP_POI);

		mBottomBtnDialog = new BottomBtnDialog(this);
		mGeoPointInfoDialog = new GeoPointInfoDialog(this);

		// 注册视图监听器
		registerViewListener();
	}

	/**
	 * 初始化视图监听器
	 */
	private void registerViewListener() {
		// ------注册监听器-------
		// 监听InfoWindow点击
		minfowindow.setOnInfowindowClickedLinstener(this);
		mZoomOutBtn.setOnClickListener(this);
		mZoomInBtn.setOnClickListener(this);
		mLayerBtn.setOnClickListener(this);
		mLayerDialog.setOnLayerChangedListener(this);
		mGeoPointInfoDialog.setOnInputConfirmListener(this);

		// ----侧滑栏列表视图触发响应----
		mDrawer.setOnItemClickListener(new com.imagine.go.view.MaterialDrawerLayout.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				// 设置搜索半径
				case VALUE_POSITION_DRAWERITEM_RADIUS:
					// 延迟弹出拖动条对话框
					TimerUtil.schedule(mHandler, EVENT_SEEKDIALOG_PUSH,
							mDrawer.getDuration());
					break;
				case VALUE_POSITION_DRAWERITEM_WEATHER:
					// 启动WeatherActivity
					TimerUtil.schedule(mHandler, EVENT_ACTIVITY_START_WEATHER,
							mDrawer.getDuration());
					break;
				case VALUE_POSITION_DRAWERITEM_OFFLINEMAP:
					// 启动OfflineMapActivity
					TimerUtil.schedule(mHandler,
							EVENT_ACTIVITY_START_OFFLINEMAP,
							mDrawer.getDuration());
					break;
				case VALUE_POSITION_DRAWERITEM_USERPOINT:
					TimerUtil.schedule(mHandler, EVENT_BOTTOM_DIALOG_SHOW,
							mDrawer.getDuration());
					break;
				}
			}
		});

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

	/**
	 * 显示隐藏导航按钮
	 * 
	 * @param show
	 */
	private void showNaviBtn(boolean show) {
		if ((show && mNaviBtn.isShown()) || (!show && !mNaviBtn.isShown()))
			return;
		Animation anim = null;
		if (show) {
			anim = AnimationFactory.alphaAnimation(0f, 1f, 500);
		} else {
			anim = AnimationFactory.alphaAnimation(1f, 0f, 500);
		}
		mNaviBtn.startAnimation(anim);
		mNaviBtn.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
	}

	// ------------------------ 生命周期 ------------------------
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 初始化视图
		initView();

		// 地图控制
		mMapController = new AMapController(this, mMapView);
		mMapController.onCreate(savedInstanceState);
		// 将地图视野移到当前所在位置
		mMapController.moveToLocation(mLocationPoint.getLatLng());
		// 添加Poi层至地图
		mMapController.addPoiOverlay(mPoiSearchData.getPois());
		// 设置地图Infowindow样式
		mMapController.setInfowindow(minfowindow);
		// 监听地图状态
		mMapController.setAMapStatusLinstener(this);

		// Poi搜索
		mPoiSearcher = new APoiSearcher(getApplicationContext());
		mPoiSearcher.setAPoiSearchListener(this);

		// 获取搜索数据
		if (null != mPoiSearchData) {
			ARData.getInstance().setRadius(mPoiSearchData.getRadius());
			if (null != mPoiSearchData.getPois())
				updateARIconMarkers(mPoiSearchData.getPois());
		}

		// 获得数据访问接口
		mGeoPointDao = DatabaseManager.getInstance(this).getGeoPointDao();
		mGeoPointList = mGeoPointDao.fetchAllGeoPoints();

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
		mMapMoveToLock.set(true);
		mAmapPoiLock.set(false);
		if (IS_DEBUG) {
			Log.d(TAG, "--OnResumed()--");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 地图视图停止绘制
		mMapController.onPause();
		mAmapPoiLock.set(false);
		mAffairLock.set(false);
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

	@Override
	public void onBackPressed() {
		AppManager.getInstance().delActivity(this);
		this.finish();
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

		private WeakReference<MixActivity> mActivity;

		public MHandler(MixActivity mActivity) {
			this.mActivity = new WeakReference<MixActivity>(mActivity);
		}

		/**
		 * 处理消息
		 * 
		 * @param msg
		 */
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
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

				// 更新AR图层
				if (Layer.MIX_POI == mActivity.get().mCurLayer) {
					mActivity.get().updateARIconMarker(pois,
							mActivity.get().mGeoPointDao.fetchAllGeoPoints());
				} else {
					mActivity.get().updateARIconMarkers(pois);
				}

				// 再添加当前Poi结果至地图
				mActivity.get().mMapController
						.addPoiOverlay(mActivity.get().mCurrentPoiResult);
				break;
			case EVENT_ACTIVITY_START_AR_NAVI:
				Intent mixNavintent = new Intent(mActivity.get(),
						MixNaviActivity.class);
				mActivity.get().startActivity(mixNavintent);
				break;
			case EVENT_ACTIVITY_START_WEATHER:
				// 启动WeatherActivity
				Intent weatherIntent = new Intent(mActivity.get(),
						WeatherActivity.class);
				mActivity.get().startActivity(weatherIntent);
				break;

			case EVENT_ACTIVITY_START_OFFLINEMAP:
				// 启动OfflineMapActivity
				Intent offlineMapIntent = new Intent(mActivity.get()
						.getApplicationContext(), OfflineMapActivity.class);
				mActivity.get().startActivity(offlineMapIntent);
				break;

			case EVENT_SEEKDIALOG_PUSH:
				// 弹出拖拉条对话框 设置搜素半径
				SweetAlertDialog dialog = new SweetAlertDialog(mActivity.get(),
						SweetAlertDialog.SEEK_TYPE);
				dialog.setConfirmClickListener(mActivity.get());
				dialog.setOnSweetSeekBarChangeListener(mActivity.get());
				dialog.show();
				dialog.setProgress(mActivity.get().mPoiSearchData.getRadius());
				break;

			case EVENT_AR_INFOWINDOW_ARISE:
				// 弹出Marker信息对话框
				mActivity.get().mMarkerDialog.show();
				if (null != mActivity.get().mTouchedMarker) {
					ARMarker marker = mActivity.get().mTouchedMarker;
					mActivity.get().mMarkerDialog.updatePoiName(marker
							.getName());
					mActivity.get().mMarkerDialog.updatePoiAddress(marker
							.getAddress());
					mActivity.get().mMarkerDialog
							.updatePoiTypeImg(PoiTypeMatcher
									.getPoiIcon(PoiTypeMatcher
											.getCurrentLableName()));
				}
				break;

			case EVENT_BOTTOM_DIALOG_SHOW:
				mActivity.get().mBottomBtnDialog.show();
				mActivity.get().mBottomBtnDialog.setItem1Txt("当前位置");
				mActivity.get().mBottomBtnDialog.setItem2Txt("地图选点");
				mActivity.get().mBottomBtnDialog.setOnClickListener(mActivity
						.get());
				break;
			case EVENT_ACTIVITY_START_USER_DEFINED_POINT:
				Intent geoPointSignIntent = new Intent(mActivity.get()
						.getApplicationContext(),
						UserDefinedPointActivity.class);
				mActivity.get().startActivity(geoPointSignIntent);
				break;
			}
		}
	}

	// ------------------------ 响应事件 ------------------------
	/**
	 * 界面里按钮点击响应
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_imageView_ar_navi:
			mHandler.sendEmptyMessage(EVENT_ACTIVITY_START_AR_NAVI);
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
		case R.id.id_textView_item1:
			// 标记当前位置信息
			mBottomBtnDialog.dismiss();
			mGeoPointInfoDialog.show();
			break;
		case R.id.id_textView_item2:
			mBottomBtnDialog.dismiss();
			TimerUtil.schedule(mHandler,
					EVENT_ACTIVITY_START_USER_DEFINED_POINT, 500);
			break;
		}
	}

	/**
	 * 对话框确认按钮响应
	 */
	@Override
	public void onClick(SweetAlertDialog sweetAlertDialog) {
		int dialogType = sweetAlertDialog.getAlerType();
		switch (dialogType) {
		case SweetAlertDialog.SEEK_TYPE:
			// 拖动条对话框
			// 设置搜索半径
			mPoiSearchData.setRadius(sweetAlertDialog.getRadius());
			ARData.getInstance().setRadius(sweetAlertDialog.getRadius());
			break;
		}
		sweetAlertDialog.dismiss();
	}

	/**
	 * 对话框滑动条更新回调
	 */
	@Override
	public void onSeekBarChanged(int range) {
		mPoiSearchData.setRadius(range);
		ARData.getInstance().setRadius(range);
		updateView();
	}

	/**
	 * 定位信息更新回调
	 */
	@Override
	public void onLocationSucceeded(AMapLocation amapLocation) {
		super.onLocationSucceeded(amapLocation);
		mMapController.setLocationInfo(amapLocation);
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
	 * Marker点击回调
	 */
	@Override
	protected void onMarkerTouch(ARMarker marker) {
		if (!mMarkerDialog.isShowing()) {
			// 设置导航信息
			ARData.getInstance().setDestinationMarker(marker);
			mDestinationPoint.setLatLng(marker.getLatLng());
			TimerUtil.schedule(mHandler, EVENT_AR_INFOWINDOW_ARISE, 300);
		}
	}

	/**
	 * 设备水平放置回调
	 */
	@Override
	protected void inHorizontal(boolean isHorizontal) {
		if (isHorizontal) {
			// 显示地图
			mMapFrame.setVisibility(View.VISIBLE);
			// 将地图视野移到当前所在位置
			if (mMapMoveToLock.compareAndSet(false, true)) {
				mMapController.moveToLocation(mLocationPoint.getLatLng());
			}
			mZoomInBtn.setVisibility(View.VISIBLE);
			mZoomOutBtn.setVisibility(View.VISIBLE);
			mLayerBtn.setVisibility(View.VISIBLE);
			mMapView.postInvalidate();
		} else {
			// 不显示地图
			mMapFrame.setVisibility(View.INVISIBLE);
			mZoomInBtn.setVisibility(View.INVISIBLE);
			mZoomOutBtn.setVisibility(View.INVISIBLE);
			mLayerBtn.setVisibility(View.INVISIBLE);
			mMapMoveToLock.set(false);
			if (mNaviBtn.isShown()) {
				showNaviBtn(false);
			}
		}

	}

	@Override
	public void onMapStatusChanged(Marker mCurrentMarker,
			LatLng mCurrentLatLng, AMapStatus status) {
		switch (status) {
		case onMapClick:
			showNaviBtn(false);
			break;
		case onMarkerClick:
			GeoPoint.poiMarkerToGeoPoint(mCurrentMarker, mDestinationPoint);
			showNaviBtn(true);
			break;
		case onRegeocodeSearched:
			GeoPoint.poiMarkerToGeoPoint(mCurrentMarker, mDestinationPoint);
			showNaviBtn(true);
			break;
		}
	}

	/**
	 * Infowindow 点击路线按钮,步行路径规划
	 */
	@Override
	public void searchWalkRoute(Marker marker) {
		mDestinationPoint.setLatLng(marker.getPosition());
		String snippet = marker.getSnippet();
		if (null == snippet || "".equals(snippet)) {
			ToastUtil.showShort("目的地址信息不全");
			return;
		}
		mDestinationPoint.setAddress(snippet);
		mMapController.getMapQueryer().searchWalkRoute(
				mLocationPoint.getLatLng(), mDestinationPoint.getLatLng());

	}

	/**
	 * 启动导航
	 */
	@Override
	public void onStartNavi() {
		mHandler.sendEmptyMessage(EVENT_ACTIVITY_START_AR_NAVI);
	}

	/**
	 * 标记地点输入备注信息确认回调
	 */
	@Override
	public void onInputeConfirm(String inputTxt) {
		if (null == inputTxt)
			return;

		if (mAffairLock.get())
			return;
		mProgressDialog.show();
		mAffairLock.set(true);

		final GeoPoint point = mLocationPoint.clone();
		point.setName(inputTxt);

		new Timer(true).schedule(new TimerTask() {
			@Override
			public void run() {
				GeoPointDao mGeoPointDao = DatabaseManager.getInstance(
						MixActivity.this).getGeoPointDao(); // 数据库已在AppManager层打开
				mGeoPointDao.addGeoPoint(point); // 向数据库提交当前位置点

				mProgressDialog.dismiss();

				mAffairLock.set(false);
			}
		}, 500);
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
				mMapController.addPoiOverlay(mPoiSearchData.getPois());
			updateARIconMarkers(mPoiSearchData.getPois());
			mAmapPoiLock.set(false);
			break;
		case USER_POI:
			mLayerBtn.setImageResource(R.drawable.ic_map_layer_user);
			mMapController.removePoiOverlay();
			if (mMapController.isNullUserDefineMarkers()) {
				mGeoPointList = mGeoPointDao.fetchAllGeoPoints();
				mMapController.addUserDefinedPoiOverlay(mGeoPointList);
			}
			updateARIconMarkersByGeoPoint(mGeoPointList);
			mAmapPoiLock.set(true);
			break;
		case MIX_POI:
			mLayerBtn.setImageResource(R.drawable.ic_map_layer_mix);
			if (mMapController.isNullPoiOverlay())
				mMapController.addPoiOverlay(mPoiSearchData.getPois());
			if (mMapController.isNullUserDefineMarkers()) {
				mGeoPointList = mGeoPointDao.fetchAllGeoPoints();
				mMapController.addUserDefinedPoiOverlay(mGeoPointList);
			}
			updateARIconMarker(mPoiSearchData.getPois(), mGeoPointList);
			mAmapPoiLock.set(false);
			break;
		case NO_POI:
			mLayerBtn.setImageResource(R.drawable.ic_map_layer);
			mMapController.removePoiOverlay();
			mMapController.removeUserDefineMarkers();
			ARData.getInstance().clearMarkers();
			mAmapPoiLock.set(true);
			break;
		}
	}

}
