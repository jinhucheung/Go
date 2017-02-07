package com.imagine.go.control;

import static com.imagine.go.Constants.EVENT_MAP_ZOOM_OUT;
import static com.imagine.go.Constants.NO_HIDE;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMap.OnPOIClickListener;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Poi;
import com.amap.api.maps.overlay.PoiOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.imagine.go.Constants;
import com.imagine.go.R;
import com.imagine.go.control.AMapQueryer.OnMapQueryListener;
import com.imagine.go.model.GeoPoint;
import com.imagine.go.view.MInfowindow;
import com.imagine.go.view.MWalkRouteOverlay;

/**
 * AMapController: 高德地图控制
 * 
 * @author Jinhu
 * @date 2016/3/26
 */
public class AMapController implements OnCameraChangeListener,
		OnMarkerClickListener, OnMapClickListener, OnPOIClickListener,
		OnMapQueryListener {

	private Context mContext;

	/* 地图层视图 . */
	private MapView mMapView;
	/* 地图控制 . */
	private AMap mMap;

	/* 定位图标 . */
	private Marker mLocationMarker;
	/* Poi覆盖层 . */
	private PoiOverlay mPoiOverlay;

	/* 当前缩放比例 . */
	private float zoom = 16;

	/* 标记点击的标签 . */
	private Marker mCurrentMarker;

	/* Infowindow . */
	private MInfowindow mInfowindow;

	/* 步行路径规划层 . */
	private MWalkRouteOverlay mWalkRouteOverlay;

	/* 搜索组件 . */
	private AMapQueryer mQueryer;
	/* 逆地理搜索半径 . */
	private float mRegeoRadius = 200;
	/* 标记进行逆地理搜索Poi . */
	private Poi mQueryPoi;
	/* 生成搜索结果Marker . */
	private Marker mQueryMarker;

	/* 已标记点的Marker集合 . */
	private SparseArray<Marker> mUserDefinedPointMarkerMap;

	/**
	 * 监听AMap状态
	 */
	public AMapStatusLinstener mMapStatusLinstener;

	public interface AMapStatusLinstener {
		void onMapStatusChanged(Marker mCurrentMarker, LatLng mCurrentLatLng,
				AMapStatus status);
	}

	/**
	 * 标记AMap状态
	 */
	public static enum AMapStatus {
		onMapClick, onMarkerClick, onRegeocodeSearched
	}

	public AMapController(Context context, MapView mapView) {
		mContext = context;
		mMapView = mapView;
		mMap = mapView.getMap();
		mQueryer = new AMapQueryer(context);
	}

	// -------------------初始化-----------------------
	/**
	 * 初始化地图UI配置
	 */
	private void initSetting() {
		UiSettings mMapSetting = mMap.getUiSettings();
		// 设置地图Logo位置
		mMapSetting.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_CENTER);
		// 取消缩放按钮
		mMapSetting.setZoomControlsEnabled(false);
	}

	/**
	 * 初始化属性
	 */
	private void init() {
		// 初始化定位图标
		MarkerOptions mlocationMarkerOpt = new MarkerOptions().icon(
				BitmapDescriptorFactory
						.fromResource(R.drawable.ic_map_location))
				.title("当前位置");

		mLocationMarker = mMap.addMarker(mlocationMarkerOpt);

		mUserDefinedPointMarkerMap = new SparseArray<Marker>();

	}

	/**
	 * 注册监听器
	 */
	private void registerListener() {
		mMap.setOnCameraChangeListener(this);
		mMap.setOnMarkerClickListener(this);
		mMap.setOnMapClickListener(this);
		mMap.setOnPOIClickListener(this);
		//
		mQueryer.setOnMapQueryListener(this);
	}

	// ------------------生命周期------------------------
	/**
	 * 地图视图创建
	 * 
	 * @param savedInstanceState
	 */
	public void onCreate(Bundle savedInstanceState) {
		mMapView.onCreate(savedInstanceState);
		initSetting();
		init();
		registerListener();
	}

	/**
	 * 地图视图启动绘制
	 */
	public void onResume() {
		mMapView.onResume();
	}

	/**
	 * 地图视图停止绘制
	 */
	public void onPause() {
		mMapView.onPause();
	}

	/**
	 * 销毁地图
	 */
	public void onDestroy() {
		mMapView.onDestroy();
	}

	// -------------------业务逻辑-----------------------
	/**
	 * 将地图移动到当前所在的位置
	 */
	public void moveToLocation(LatLng mLatLng) {
		if (null == mLatLng)
			return;
		mLocationMarker.setPosition(mLatLng);
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, zoom));
	}

	/**
	 * 设置定位信息
	 * 
	 * @param mLocation
	 */
	public void setLocationInfo(AMapLocation mLocation) {
		// 去掉省会
		String address = mLocation.getAddress().substring(3);
		mLocationMarker.setSnippet(address);
	}

	/**
	 * 设置定位标记可见性
	 * 
	 * @param visible
	 */
	public void setLocationMarkerVisible(boolean visible) {
		mLocationMarker.setVisible(visible);
	}

	/**
	 * 地图缩放
	 * 
	 * @param type
	 */
	public void zoom(int type) {
		if (EVENT_MAP_ZOOM_OUT == type) {
			// 缩小
			zoom--;
		} else {
			// 放大
			zoom++;
		}
		mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
	}

	/**
	 * 地图添加Poi层
	 * 
	 * @param result
	 */
	public void addPoiOverlay(PoiResult result) {
		if (null == result)
			return;
		this.addPoiOverlay(result.getPois());
	}

	/**
	 * 地图添加Poi层
	 * 
	 * @param pois
	 */
	public void addPoiOverlay(List<PoiItem> pois) {
		if (null == pois || 0 == pois.size())
			return;
		mPoiOverlay = new PoiOverlay(mMap, pois);
		// 添加Marker到地图中
		mPoiOverlay.addToMap();
		// 移动镜头到当前的视角
		mPoiOverlay.zoomToSpan();

	}

	/**
	 * 地图添加已标记的Poi
	 * 
	 * @param pois
	 */
	public void addUserDefinedPoiOverlay(List<GeoPoint> pois) {
		if (null == pois || 0 == pois.size())
			return;
		for (GeoPoint poi : pois) {
			addUserDefinedMarker(poi);
		}
	}

	/**
	 * 添加已标记的Poi
	 * 
	 * @param poi
	 */
	public void addUserDefinedMarker(GeoPoint poi) {
		if (null == poi)
			return;
		final BitmapDescriptor bitmap = BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_RED);
		MarkerOptions opt = new MarkerOptions().//
				position(poi.getLatLng()).//
				snippet(poi.getAddress()).//
				title(poi.getName()).//
				icon(bitmap);
		Marker signedMarker = mMap.addMarker(opt);
		int id = Integer.valueOf(poi.getId());
		signedMarker.setObject(Constants.TAB_USER_DEFINED_POINT_MARKER + id);
		mUserDefinedPointMarkerMap.put(id, signedMarker);
	}

	/**
	 * 移除自标记
	 * 
	 * @param poiId
	 */
	public void removeUserDefinedMarker(int poiId) {
		Marker m = mUserDefinedPointMarkerMap.get(poiId);
		if (null == m)
			return;
		m.remove();
		mUserDefinedPointMarkerMap.remove(poiId);
	}

	public void removeUserDefineMarkers() {
		for (int i = 0; i < mUserDefinedPointMarkerMap.size(); i++) {
			Marker m = mUserDefinedPointMarkerMap.valueAt(i);
			m.remove();
		}
		mUserDefinedPointMarkerMap.clear();
	}

	/**
	 * 移除地图覆盖层
	 */
	public void removeOverlay() {
		this.removePoiOverlay();
		this.removeWalkRouteOverlay();
		this.removeQueryPoiMarker();
	}

	/**
	 * 地图移除Poi层
	 */
	public void removePoiOverlay() {
		if (null == mPoiOverlay)
			return;
		mPoiOverlay.removeFromMap();
		mPoiOverlay = null;
	}

	/**
	 * Poi层是否为空
	 * 
	 * @return
	 */
	public boolean isNullPoiOverlay() {
		return null == mPoiOverlay;
	}

	/**
	 * 用户自标记层是否为空
	 * 
	 * @return
	 */
	public boolean isNullUserDefineMarkers() {
		return null == mUserDefinedPointMarkerMap
				|| mUserDefinedPointMarkerMap.size() == 0;
	}

	/**
	 * 地图添加步行路径规划层
	 * 
	 * @param mWalkPath
	 *            步行路径方案
	 * @param startPos
	 *            起点
	 * @param targetPos
	 *            终点
	 */
	public void addWalkRouteOverlay(WalkPath mWalkPath, LatLonPoint startPos,
			LatLonPoint targetPos) {
		if (null == mWalkPath || null == startPos || null == targetPos)
			return;
		mWalkRouteOverlay = new MWalkRouteOverlay(mContext, mMap, mWalkPath,
				startPos, targetPos);
		mWalkRouteOverlay.addToMap();
		mWalkRouteOverlay.zoomToSpan();
		mWalkRouteOverlay.setEndMarker(mCurrentMarker);
		mWalkRouteOverlay.setStartMarker(mLocationMarker);
	}

	/**
	 * 地图移除路径规划层
	 */
	public void removeWalkRouteOverlay() {
		if (null == mWalkRouteOverlay)
			return;
		mWalkRouteOverlay.removeFromMap();
		mWalkRouteOverlay = null;
		mLocationMarker.setObject("");
	}

	public void removeQueryPoiMarker() {
		if (null == mQueryMarker)
			return;
		mQueryMarker.setVisible(false);
		mQueryMarker = null;

	}

	/**
	 * Marker点击时必须先设置infowindow样式
	 * 
	 * @param mInfoWindowAdapter
	 */
	public void setInfowindow(MInfowindow mInfoWindow) {
		mMap.setInfoWindowAdapter(mInfoWindow);
		this.mInfowindow = mInfoWindow;
	}

	/**
	 * 监听地图状态
	 * 
	 * @param mMapStatusLinstener
	 */
	public void setAMapStatusLinstener(AMapStatusLinstener mMapStatusLinstener) {
		this.mMapStatusLinstener = mMapStatusLinstener;
	}

	/**
	 * 获得AMap搜索组件
	 * 
	 * @return mQueryer
	 */
	public AMapQueryer getMapQueryer() {
		return mQueryer;
	}

	// -------------------响应事件-----------------------
	/**
	 * 地图视野改变回调
	 */
	@Override
	public void onCameraChange(CameraPosition paramCameraPosition) {
		zoom = paramCameraPosition.zoom;
	}

	@Override
	public void onCameraChangeFinish(CameraPosition paramCameraPosition) {
		zoom = paramCameraPosition.zoom;
	}

	/**
	 * 地图Marker点击回调
	 */
	@Override
	public boolean onMarkerClick(Marker paramMarker) {
		// 当点击Poi搜索的Marker时,QueryMarker隐藏
		if (null != mQueryMarker && !NO_HIDE.equals(paramMarker.getObject())) {
			mQueryMarker.setVisible(false);
		}
		// 显示搜索出来Poi点的Marker
		paramMarker.showInfoWindow();

		mCurrentMarker = paramMarker;
		// 传出数据
		if (null != mMapStatusLinstener) {
			mMapStatusLinstener.onMapStatusChanged(mCurrentMarker,
					mCurrentMarker.getPosition(), AMapStatus.onMarkerClick);
		}
		return true;
	}

	@Override
	public void onMapClick(LatLng paramLatLng) {
		// 隐藏InfoWindow
		if (mInfowindow.IsShow()) {
			mCurrentMarker.hideInfoWindow();
		}
		// 隐藏Marker
		if (null != mQueryMarker) {
			mQueryMarker.setVisible(false);
			mMapView.invalidate();
		}
		// 移除路径规划层
		removeWalkRouteOverlay();
		// 传出数据
		if (null != mMapStatusLinstener) {
			mMapStatusLinstener.onMapStatusChanged(mCurrentMarker, paramLatLng,
					AMapStatus.onMapClick);
		}
	}

	// --------------------逆地理搜索--------------------
	/**
	 * 地图Poi点击 进行逆地理搜索
	 */
	@Override
	public synchronized void onPOIClick(Poi paramPoi) {
		mQueryer.searchAddress(paramPoi.getCoordinate(), mRegeoRadius);
		mQueryPoi = paramPoi;
	}

	/**
	 * 逆地理搜索回调
	 */
	@Override
	public synchronized void onRegeocodeSearched(RegeocodeAddress address) {
		if (null == address)
			return;

		if (null == mQueryMarker) {
			MarkerOptions mOptions = new MarkerOptions();
			mQueryMarker = mMap.addMarker(mOptions);
		}
		// 设置标记
		mQueryMarker.setTitle(mQueryPoi.getName());
		mQueryMarker.setSnippet(address.getFormatAddress().substring(3));
		mQueryMarker.setPosition(mQueryPoi.getCoordinate());
		mQueryMarker.setObject(NO_HIDE);
		mQueryMarker.setVisible(true);

		mQueryMarker.showInfoWindow();

		// 标记当前标记
		mCurrentMarker = mQueryMarker;
		// 传出数据
		if (null != mMapStatusLinstener) {
			mMapStatusLinstener.onMapStatusChanged(mCurrentMarker,
					mCurrentMarker.getPosition(),
					AMapStatus.onRegeocodeSearched);
		}
	}

	// -------------------- 步行规划--------------------
	/**
	 * 步行规划回调
	 */
	@Override
	public void onWalkRouteSearched(WalkRouteResult walkRouteResult) {
		if (null == walkRouteResult)
			return;
		if (null != mCurrentMarker) {
			mCurrentMarker.hideInfoWindow();
		}
		removeWalkRouteOverlay();
		// 获得最优选的步行规划方案
		final WalkPath mWalkPath = walkRouteResult.getPaths().get(0);
		// 生成步行规划层
		this.addWalkRouteOverlay(mWalkPath, walkRouteResult.getStartPos(),
				walkRouteResult.getTargetPos());
	}

}
