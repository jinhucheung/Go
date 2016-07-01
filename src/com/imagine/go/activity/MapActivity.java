package com.imagine.go.activity;

import static com.imagine.go.Constants.EVENT_ACTIVITY_START_AR;
import static com.imagine.go.Constants.EVENT_ACTIVITY_START_INDEX;
import static com.imagine.go.Constants.EVENT_ACTIVITY_START_MAP_NAVI;
import static com.imagine.go.Constants.EVENT_ACTIVITY_START_OFFLINEMAP;
import static com.imagine.go.Constants.EVENT_ACTIVITY_START_USER_DEFINED_POINT;
import static com.imagine.go.Constants.EVENT_ACTIVITY_START_WEATHER;
import static com.imagine.go.Constants.EVENT_BOTTOM_DIALOG_SHOW;
import static com.imagine.go.Constants.EVENT_MAP_ZOOM_IN;
import static com.imagine.go.Constants.EVENT_MAP_ZOOM_OUT;
import static com.imagine.go.Constants.EVENT_SEARCH_POI;
import static com.imagine.go.Constants.EVENT_SEEKDIALOG_PUSH;
import static com.imagine.go.Constants.IS_DEBUG;
import static com.imagine.go.Constants.NO_RESULT;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.imagine.go.AppManager;
import com.imagine.go.R;
import com.imagine.go.control.AMapController;
import com.imagine.go.control.AMapController.AMapStatus;
import com.imagine.go.control.AMapController.AMapStatusLinstener;
import com.imagine.go.control.AMapQueryer;
import com.imagine.go.control.AMapQueryer.OnInputTipsQueryListener;
import com.imagine.go.control.APoiSearcher;
import com.imagine.go.control.APoiSearcher.APoiSearchListener;
import com.imagine.go.data.DatabaseManager;
import com.imagine.go.data.GeoPointDao;
import com.imagine.go.model.GeoPoint;
import com.imagine.go.util.AnimationFactory;
import com.imagine.go.util.PoiTypeMatcher;
import com.imagine.go.util.TimerUtil;
import com.imagine.go.util.ToastUtil;
import com.imagine.go.view.ArcMenu;
import com.imagine.go.view.ArcMenu.OnMenuItemClickListener;
import com.imagine.go.view.BottomBtnDialog;
import com.imagine.go.view.GeoPointInfoDialog;
import com.imagine.go.view.GeoPointInfoDialog.OnInputConfirmListener;
import com.imagine.go.view.IconEditText;
import com.imagine.go.view.IconEditText.OnIconClickedListener;
import com.imagine.go.view.LayerDialog;
import com.imagine.go.view.LayerDialog.Layer;
import com.imagine.go.view.LayerDialog.OnLayerChangedListener;
import com.imagine.go.view.MInfowindow;
import com.imagine.go.view.MInfowindow.onInfowindowClickedLinstener;
import com.imagine.go.view.MaterialDrawerLayout;
import com.imagine.go.view.RippleLayout;

/**
 * MapActivity:地图层
 * 
 * @author Jinhu
 * @date 2016/3/21
 */
public class MapActivity extends OriginActivity implements OnClickListener,
		APoiSearchListener, OnSweetClickListener, onInfowindowClickedLinstener,
		AMapStatusLinstener, OnInputTipsQueryListener, OnLayerChangedListener,
		OnInputConfirmListener {
	private static final String TAG = MapActivity.class.getSimpleName();

	// -------- 界面相关 --------

	/* 地图层视图 . */
	private MapView mMapView;

	/* 搜索栏 . */
	private IconEditText mEditText;

	/* 侧滑标题栏 . */
	/* 已封装侧滑栏及标题栏逻辑 . */
	private MaterialDrawerLayout mDrawer;

	/* 工具栏 . */
	/* 搜索标签按钮 . */
	private ImageView mLabelBtn;
	/* 搜索标签菜单 . */
	private ArcMenu mArcMenu;
	/* 主页按钮 . */
	private RippleLayout mIndexBtn;
	/* 实景层按钮 . */
	private RippleLayout mCameraBtn;

	/* 定位按钮 . */
	private View mLocateBtn;
	/* 地图缩放按钮 . */
	private View mZoomOutBtn;
	private View mZoomInBtn;

	/* Poi图层选择 . */
	private ImageView mLayerBtn;

	/* 进度条对话框 . */
	private SweetAlertDialog mProgressDialog;

	/* Infowindow . */
	private MInfowindow minfowindow;

	/* 导航按钮 启动地图导航Activity . */
	private View mNavBtn;

	/* 底部对话框 . */
	private BottomBtnDialog mBottomBtnDialog;

	/* 地理信息点输入对空框 . */
	private GeoPointInfoDialog mGeoPointInfoDialog;

	/* Poi层选择对话框 . */
	private LayerDialog mLayerDialog;

	// -------- 业务相关 --------
	/* 地图控制 . */
	private AMapController mMapController;

	/* Poi点搜索 . */
	private APoiSearcher mPoiSearcher;
	/* Poi结果 . */
	private PoiResult mCurrentPoiResult;

	/* 高德搜索组件 . */
	private AMapQueryer mMapQueryer;

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
		mMapView = (MapView) findViewById(R.id.id_mapView);

		// 侧滑标题栏
		mDrawer = (MaterialDrawerLayout) findViewById(R.id.id_layout_drawer);
		mDrawer.bindTitleBar(findViewById(R.id.id_layout_titlebar));

		// 搜索栏
		mEditText = (IconEditText) findViewById(R.id.id_editText_search);

		// 工具栏
		// 搜索标签菜单
		mArcMenu = (ArcMenu) findViewById(R.id.id_arcmenu_map_labelBtn);
		// 搜索标签按钮
		mLabelBtn = (ImageView) findViewById(R.id.id_imageView_labelBtn);
		// 主页按钮
		mIndexBtn = (RippleLayout) findViewById(R.id.id_rippleLayout_toolBar_indexBtn);
		// 实景按钮
		mCameraBtn = (RippleLayout) findViewById(R.id.id_rippleLayout_toolBar_cameraBtn);

		// 定位按钮
		mLocateBtn = findViewById(R.id.id_imageView_locate_btn);
		// 地图缩放
		mZoomOutBtn = findViewById(R.id.id_imageView_zoomOut_btn);
		mZoomInBtn = findViewById(R.id.id_imageView_zoomIn_btn);
		// 导航按钮
		mNavBtn = findViewById(R.id.id_imageView_map_navBtn);
		// Poi层选
		mLayerBtn = (ImageView) findViewById(R.id.id_imageView_layer);

		// 进度对话框
		mProgressDialog = new SweetAlertDialog(MapActivity.this,
				SweetAlertDialog.PROGRESS_TYPE);
		// Poi层选对话框
		mLayerDialog = new LayerDialog(this);
		mLayerDialog.setLayer(Layer.AMAP_POI);

		mBottomBtnDialog = new BottomBtnDialog(this);
		mGeoPointInfoDialog = new GeoPointInfoDialog(this);

		// 初始化Infowindow
		minfowindow = new MInfowindow(this);

		// ---注册视图监听器---
		registerViewListener();

	}

	/**
	 * 初始化视图监听器
	 */
	private void registerViewListener() {
		// ------注册监听器-------
		// 工具栏
		mIndexBtn.setOnClickListener(this);
		mCameraBtn.setOnClickListener(this);
		// 标签主按钮弹出或隐藏标签按钮
		mLabelBtn.setOnClickListener(this);
		// 地图控制按钮
		mLocateBtn.setOnClickListener(this);
		mZoomOutBtn.setOnClickListener(this);
		mZoomInBtn.setOnClickListener(this);
		mNavBtn.setOnClickListener(this);
		mLayerBtn.setOnClickListener(this);
		mLayerDialog.setOnLayerChangedListener(this);
		mGeoPointInfoDialog.setOnInputConfirmListener(this);

		// 监听InfoWindow点击
		minfowindow.setOnInfowindowClickedLinstener(this);

		// 搜索标签点击响应
		mArcMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public void onMenuItemClicked(View v, int pos) {
				mLabelBtn.setImageResource(R.drawable.ic_toolbar_search);
				if (null == mALocation)
					return;
				String labelName = v.getTag() + "";
				String poiType = PoiTypeMatcher.getPoiType(labelName);

				mProgressDialog.show();
				// 清空之前PoiResult
				mCurrentPoiResult = null;
				mPoiSearchData.clearPois();
				// 搜索Poi
				if (null != poiType) {
					// 通过分类搜索Poi
					mPoiSearcher.searchNearbyType(mALocation, poiType,
							mPoiSearchData.getRadius());
				} else {
					// 通过关键字搜索Poi
					mPoiSearcher.searchNearbyKeyword(mALocation, labelName,
							mPoiSearchData.getRadius());
				}
			}
		});

		// ----监听搜索栏输入----
		mEditText.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (KeyEvent.KEYCODE_ENTER == keyCode) {
					// 用户输入完成
					String keyword = mEditText.getText().toString();
					mEditText.setText("");
					if (null == keyword || "".equals(keyword))
						return false;

					mProgressDialog.show();
					// 清空之前PoiResult
					mCurrentPoiResult = null;
					mPoiSearchData.clearPois();
					// 关键字搜索Poi
					mPoiSearcher.searchNearbyKeyword(mALocation, keyword,
							mPoiSearchData.getRadius());
					return true;
				}
				return false;
			}
		});

		// 注册搜索栏图标触发响应
		mEditText.setOnIconClickedListener(new OnIconClickedListener() {

			@Override
			public void onVoiceStart() {
				mVoiceController.startListeningByDialog(MapActivity.this);
			}

			@Override
			public void onSearchStart() {
				String keyword = mEditText.getText().toString();
				if (null == keyword || "".equals(keyword))
					return;

				mProgressDialog.show();
				// 清空之前PoiResult
				mCurrentPoiResult = null;
				mPoiSearchData.clearPois();
				// 关键字搜索Poi
				mPoiSearcher.searchNearbyKeyword(mALocation, keyword,
						mPoiSearchData.getRadius());
				mEditText.setText("");
			}
		});

		// 搜索栏文本改变监听
		mEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String mText = s.toString().trim();
				if (null == mText || 0 == mText.length())
					return;
				// 进行Poi提示字搜索
				mMapQueryer.searchPoiInputTips(mText, mLocationPoint.getCity());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		// ----监听软键盘隐藏与出现----
		mDrawer.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						int heightDiff = mDrawer.getRootView().getHeight()
								- mDrawer.getHeight();
						// -软键盘隐藏与出现-
						if (heightDiff > 100) {
							// 无论搜索栏是否有文本,都清空
							mEditText.setText("");
						}
					}
				});

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

	}

	// ------------------------ 生命周期 ------------------------
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 自定义标题栏
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_map);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.bar_title);

		// 初始化布局组件
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

		mMapQueryer = new AMapQueryer(getApplicationContext());
		mMapQueryer.setOnInputTipsQueryListener(this);

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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
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

		private WeakReference<MapActivity> mActivity;

		public MHandler(MapActivity mActivity) {
			this.mActivity = new WeakReference<MapActivity>(mActivity);
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
				// 搜索Poi结果处理
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

			case EVENT_SEEKDIALOG_PUSH:
				// 弹出拖拉条对话框 设置搜素半径
				SweetAlertDialog dialog = new SweetAlertDialog(mActivity.get(),
						SweetAlertDialog.SEEK_TYPE);
				dialog.setConfirmClickListener(mActivity.get());
				dialog.show();
				dialog.setProgress(mActivity.get().mPoiSearchData.getRadius());
				break;

			case EVENT_ACTIVITY_START_INDEX:
				// 启动MainActivity
				mActivity.get().startActivity(MainActivity.class);
				break;
			case EVENT_ACTIVITY_START_MAP_NAVI:
				// 启动MapNaviActivity
				Intent mapNavintent = new Intent(mActivity.get(),
						MapNaviActivity.class);
				mActivity.get().startActivity(mapNavintent);
				break;
			case EVENT_ACTIVITY_START_AR:
				mActivity.get().startActivity(MixActivity.class);
				break;

			case EVENT_ACTIVITY_START_WEATHER:
				// 启动WeatherActivity
				Intent weatherIntent = new Intent(mActivity.get()
						.getApplicationContext(), WeatherActivity.class);
				mActivity.get().startActivity(weatherIntent);
				break;

			case EVENT_ACTIVITY_START_OFFLINEMAP:
				// 启动OfflineMapActivity
				Intent offlineMapIntent = new Intent(mActivity.get()
						.getApplicationContext(), OfflineMapActivity.class);
				mActivity.get().startActivity(offlineMapIntent);
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
	 * 主界面里按钮点击响应
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_rippleLayout_toolBar_indexBtn:
			// 水波纹动画结束后跳转至MainActivity
			mIndexBtn.showRipple();
			TimerUtil.schedule(mHandler, EVENT_ACTIVITY_START_INDEX,
					mIndexBtn.getAnimDuration());
			break;

		case R.id.id_rippleLayout_toolBar_cameraBtn:
			mCameraBtn.showRipple();
			TimerUtil.schedule(mHandler, EVENT_ACTIVITY_START_AR,
					mIndexBtn.getAnimDuration());
			break;

		case R.id.id_imageView_labelBtn:
			mNavBtn.setVisibility(View.INVISIBLE);
			// 弹出搜索标签菜单
			mLabelBtn.startAnimation(AnimationFactory.rotateAnimation(0f, 360f,
					500));
			mArcMenu.toggle(500);
			if (mArcMenu.isOpen()) {
				mLabelBtn.setImageResource(R.drawable.ic_toolbar_del);
			} else {
				mLabelBtn.setImageResource(R.drawable.ic_toolbar_search);
			}
			break;

		case R.id.id_imageView_locate_btn:
			// 地图视图移动到当前位置
			mMapController.moveToLocation(mLocationPoint.getLatLng());
			break;
		case R.id.id_imageView_zoomIn_btn:
			mMapController.zoom(EVENT_MAP_ZOOM_IN);
			break;
		case R.id.id_imageView_zoomOut_btn:
			mMapController.zoom(EVENT_MAP_ZOOM_OUT);
			break;
		case R.id.id_imageView_map_navBtn:
			// 启动MapNavActivity
			mHandler.sendEmptyMessage(EVENT_ACTIVITY_START_MAP_NAVI);
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
					EVENT_ACTIVITY_START_USER_DEFINED_POINT,
					mIndexBtn.getAnimDuration());
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
			break;
		}
		sweetAlertDialog.dismiss();
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
	 * 地图状态监听
	 */
	@Override
	public void onMapStatusChanged(Marker mCurrentMarker,
			LatLng mCurrentLatLng, AMapStatus status) {
		switch (status) {
		case onMapClick:
			mNavBtn.setVisibility(View.INVISIBLE);
			break;
		case onMarkerClick:
			GeoPoint.poiMarkerToGeoPoint(mCurrentMarker, mDestinationPoint);
			mNavBtn.setVisibility(View.VISIBLE);
			break;
		case onRegeocodeSearched:
			GeoPoint.poiMarkerToGeoPoint(mCurrentMarker, mDestinationPoint);
			mNavBtn.setVisibility(View.VISIBLE);
			break;
		}
	}

	/**
	 * 语音识别回调结果
	 */
	@Override
	public void onResult(String result) {
		super.onResult(result);
		mEditText.setText(result);
	}

	/**
	 * Poi提示字搜索回调
	 */
	@Override
	public void onGetInputtips(List<String> nameList) {
		if (null == nameList || 0 == nameList.size())
			return;
		ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(this,
				R.layout.item_poi_tip, nameList);
		mEditText.setAdapter(aAdapter);
		aAdapter.notifyDataSetChanged();
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
			if (mMapController.isNullUserDefineMarkers()) {
				mGeoPointList = mGeoPointDao.fetchAllGeoPoints();
				mMapController.addUserDefinedPoiOverlay(mGeoPointList);
			}
			mAmapPoiLock.set(true);
			break;
		case MIX_POI:
			mLayerBtn.setImageResource(R.drawable.ic_map_layer_mix);
			if (mMapController.isNullPoiOverlay())
				mMapController.addPoiOverlay(mCurrentPoiResult);
			if (mMapController.isNullUserDefineMarkers()) {
				mGeoPointList = mGeoPointDao.fetchAllGeoPoints();
				mMapController.addUserDefinedPoiOverlay(mGeoPointList);
			}
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

	/**
	 * 标记地点输入备注信息确认回调
	 */
	@Override
	public synchronized void onInputeConfirm(String inputTxt) {
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
						MapActivity.this).getGeoPointDao(); // 数据库已在AppManager层打开
				mGeoPointDao.addGeoPoint(point); // 向数据库提交当前位置点

				mProgressDialog.dismiss();

				mAffairLock.set(false);
			}
		}, 500);
	}

}
