package com.imagine.go.control;

import static com.imagine.go.Constants.IS_DEBUG;
import static com.imagine.go.Constants.NAVI_MODE_GPS;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.enums.PathPlanningStrategy;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviGuide;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.imagine.go.util.ToastUtil;

/**
 * AMapNaviController
 * 
 * @地图语音导航管理
 * @author Jinhu
 * @date 2016/3/30
 */
public class AMapNaviController implements AMapNaviListener {
	private static final String TAG = AMapNaviController.class.getSimpleName();

	private Context mContext;

	/* 标记可启动导航 . */
	private boolean isNaviReady = false;
	/* 标记正在导航 . */
	private AtomicBoolean isNaving = new AtomicBoolean(false);
	/* 标记初次导航 . */
	private boolean fristNav = true;

	/* 导航视图 . */
	private AMapNaviView mMapNaviView = null;
	/* 导航控件 . */
	private AMapNavi mMapNavi;
	/* 导航模式 . */
	private int mNaviMode;

	/* 起点 . */
	private NaviLatLng mLocation;
	/* 终点 . */
	private NaviLatLng mDestination;

	/* 语音组件 . */
	private XFVoiceController mVoiceController;

	/* 导航路径信息 . */
	private AMapNaviPath mNaviPath;
	private List<AMapNaviGuide> mNaviGuides;

	/**
	 * 监听正在导航事件
	 */
	private OnNavingListener mOnNavingListener;

	public interface OnNavingListener {
		// 导航信息更新
		void onNaviInfoUpdated(NaviInfo info);

		// 到达目的地
		void onArriveDestination();

		// 路径规划成功
		void onCalculateRouteSuccess(List<AMapNaviGuide> mNaviGuides,
				AMapNaviPath mNaviPath);

		// 路径规划失败
		void onCalculateRouteFailure();

		// 重新规划路径
		void onReCalculateRoute();

		// 导航指引信息
		void onGetNavigationText(String text);

	}

	/**
	 * 使用导航视图
	 * 
	 * @param context
	 * @param aMapNaviView
	 */
	public AMapNaviController(Context context, AMapNaviView aMapNaviView) {
		mContext = context;
		mMapNaviView = aMapNaviView;
		// 生成语音组件
		mVoiceController = new XFVoiceController(mContext);
	}

	/**
	 * 不使用导航视图
	 * 
	 * @param context
	 */
	public AMapNaviController(Context context) {
		mContext = context;
		mVoiceController = new XFVoiceController(mContext);
	}

	// -------------------生命周期-----------------------
	public void onCreate(Bundle bundle) {
		if (null != mMapNaviView) {
			mMapNaviView.onCreate(bundle);
		}
		if (IS_DEBUG) {
			Log.d(TAG, "--onCreated()--");
		}
	}

	public void onResume() {
		if (null != mMapNaviView) {
			mMapNaviView.onResume();
		}
		if (IS_DEBUG) {
			Log.d(TAG, "--onResumed()--");
		}
	}

	public void onPasue() {
		if (null != mMapNaviView) {
			mMapNaviView.onPause();
		}
		mVoiceController.stopSeaking();
		if (IS_DEBUG) {
			Log.d(TAG, "--onPasued()--");
		}
	}

	public void onDestroy() {
		if (null != mMapNaviView) {
			mMapNaviView.onDestroy();
		}
		mVoiceController.onDestroy();
		if (null != mMapNavi) {
			mMapNavi.stopGPS();
			mMapNavi.stopNavi();
			mMapNavi.destroy();
		}
		if (IS_DEBUG) {
			Log.d(TAG, "--onDestroied()--");
		}
		isNaviReady = false;
	}

	public void onSaveInstanceState(Bundle bundle) {
		if (null != mMapNaviView) {
			mMapNaviView.onSaveInstanceState(bundle);
		}
		if (IS_DEBUG) {
			Log.d(TAG, "--onSaved()--");
		}
	}

	// -------------------业务逻辑-----------------------
	/**
	 * 设置起点、终点
	 * 
	 * @param location
	 * @param destination
	 */
	public void setUpLatLng(LatLng location, LatLng destination) {
		mLocation = latlng2NaviLatlng(location);
		mDestination = latlng2NaviLatlng(destination);
	}

	/**
	 * 设置终点
	 * 
	 * @param destination
	 */
	public void setUpLatLng(LatLng destination) {
		mDestination = latlng2NaviLatlng(destination);
	}

	/**
	 * 准备导航
	 * 
	 * @param context
	 */
	public void preNavi(Context context, int naviMode) {
		mMapNavi = AMapNavi.getInstance(context);
		mNaviMode = naviMode;
		// 一般人步行速度 5km/h
		mMapNavi.setEmulatorNaviSpeed(5);
		if (NAVI_MODE_GPS == naviMode)
			mMapNavi.startGPS();

		// 导航状态监听
		mMapNavi.addAMapNaviListener(this);
	}

	/**
	 * 开始导航
	 */
	public void startNavi() {
		if (null != mMapNavi && isNaviReady) {
			// mVoiceController.startSpeaking("导航开始");
			if (isNaving.compareAndSet(false, true) && !fristNav) {
				// 继续暂停的导航
				mMapNavi.resumeNavi();
			} else {
				// 重新导航
				mMapNavi.startNavi(mNaviMode);
				fristNav = false;
			}
		} else {
			mVoiceController.startSpeaking("导航准备失败");
		}
	}

	/**
	 * 暂停导航
	 */
	public void pauseNavi() {
		if (null != mMapNavi && isNaviReady) {
			if (isNaving.compareAndSet(true, false)) {
				mMapNavi.pauseNavi();
			}
		}
	}

	/**
	 * 计算路径
	 */
	public void calculRoute() {
		mMapNavi.calculateWalkRoute(mLocation, mDestination);
	}

	/**
	 * 重新计算路径
	 */
	public void recalculRoute() {
		// 重新规划
		mMapNavi.reCalculateRoute(PathPlanningStrategy.DRIVING_SHORT_DISTANCE);
		if (null != mOnNavingListener) {
			mOnNavingListener.onReCalculateRoute();
		}
	}

	/**
	 * 返回导航标记
	 * 
	 * @return
	 */
	public boolean IsNaving() {
		return isNaving.get();
	}

	/**
	 * 更新导航信息回调
	 * 
	 * @param mOnNavingListener
	 */
	public void setOnNavingListener(OnNavingListener mOnNavingListener) {
		this.mOnNavingListener = mOnNavingListener;
	}

	/**
	 * LatLng转NaviLatLng
	 * 
	 * @param latlng
	 * @return
	 */
	private NaviLatLng latlng2NaviLatlng(LatLng latlng) {
		if (null == latlng)
			return null;
		return new NaviLatLng(latlng.latitude, latlng.longitude);
	}

	// -------------------导航事件回调-----------------------
	/**
	 * 已经过时,使用OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo
	 * trafficFacilityInfo)
	 */
	@Override
	public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

	}

	/**
	 * 摄像头信息更新回调
	 */
	@Override
	public void OnUpdateTrafficFacility(
			AMapNaviTrafficFacilityInfo trafficFacilityInfo) {

	}

	/**
	 * 关闭路口放大图回调
	 */
	@Override
	public void hideCross() {

	}

	/**
	 * 关闭道路信息回调
	 */
	@Override
	public void hideLaneInfo() {

	}

	/**
	 * 通知当前是否显示平行路切换
	 */
	@Override
	public void notifyParallelRoad(int parallelRoadType) {

	}

	/**
	 * 到达目的地后回调
	 */
	@Override
	public void onArriveDestination() {
		mVoiceController.startSpeaking("导航结束");
		if (null != mOnNavingListener)
			mOnNavingListener.onArriveDestination();
		isNaving.set(false);
		fristNav = true;
	}

	/**
	 * 驾车路径导航到达某个途经点的回调
	 */
	@Override
	public void onArrivedWayPoint(int wayID) {

	}

	/**
	 * 多路线算路成功回调
	 */
	@Override
	public void onCalculateMultipleRoutesSuccess(int[] routeIds) {

	}

	/**
	 * 步行或者驾车路径规划失败后的回调
	 */
	@Override
	public void onCalculateRouteFailure(int errorInfo) {
		mVoiceController.startSpeaking("路径计算失败，请检查网络或输入参数");
		ToastUtil.showShort("路径计算失败，请检查网络或输入参数");
		isNaviReady = false;
		if (null != mOnNavingListener) {
			mOnNavingListener.onCalculateRouteFailure();
		}
	}

	/**
	 * 步行或者驾车路径规划成功后的回调
	 */
	@Override
	public void onCalculateRouteSuccess() {
		String calculateResult = "路径计算就绪";
		mVoiceController.startSpeaking(calculateResult);
		// 初始化状态
		isNaviReady = true;
		isNaving.set(false);
		fristNav = true;

		// 获得规划好的路径 概览
		mNaviGuides = mMapNavi.getNaviGuideList();
		// 详情
		mNaviPath = mMapNavi.getNaviPath();

		if (null != mOnNavingListener && null != mNaviPath) {
			mOnNavingListener.onCalculateRouteSuccess(mNaviGuides, mNaviPath);
		}
	}

	/**
	 * 模拟导航停止后回调
	 */
	@Override
	public void onEndEmulatorNavi() {
		mVoiceController.startSpeaking("导航结束");
		if (null != mOnNavingListener)
			mOnNavingListener.onArriveDestination();
		isNaving.set(false);
		fristNav = true;
	}

	/**
	 * 导航播报信息回调
	 */
	@Override
	public void onGetNavigationText(int type, String text) {
		mVoiceController.startSpeaking(text);
		isNaving.set(true);

		if (null != mOnNavingListener) {
			mOnNavingListener.onGetNavigationText(text);
		}
	}

	/**
	 * 用户手机GPS设置是否开启的回调
	 */
	@Override
	public void onGpsOpenStatus(boolean enabled) {
		if (!enabled)
			mVoiceController.startSpeaking("请打开GPS");
	}

	/**
	 * 导航创建失败时的回调
	 */
	@Override
	public void onInitNaviFailure() {
		isNaviReady = false;
	}

	/**
	 * 导航创建成功时的回调
	 */
	@Override
	public void onInitNaviSuccess() {
		// 路径规划
		mMapNavi.calculateWalkRoute(mLocation, mDestination);
	}

	/**
	 * 当GPS位置有更新时的回调
	 */
	@Override
	public void onLocationChange(AMapNaviLocation location) {
	}

	/**
	 * 导航引导信息回调 naviinfo 是导航信息类
	 */
	@Override
	public void onNaviInfoUpdate(NaviInfo naviinfo) {
		if (null == naviinfo)
			return;
		if (null != mOnNavingListener) {
			mOnNavingListener.onNaviInfoUpdated(naviinfo);
		}
	}

	/**
	 * 已过时
	 */
	@Override
	public void onNaviInfoUpdated(AMapNaviInfo naviinfo) {

	}

	/**
	 * 驾车导航时，如果前方遇到拥堵时需要重新计算路径的回调
	 */
	@Override
	public void onReCalculateRouteForTrafficJam() {
		mVoiceController.startSpeaking("前方路线拥堵,路线重新规划");
		// 重新规划
		recalculRoute();
	}

	/**
	 * 步行或驾车导航时,出现偏航后需要重新计算路径的回调
	 */
	@Override
	public void onReCalculateRouteForYaw() {
		mVoiceController.startSpeaking("您已偏航,路线重新规划");
		// 重新规划
		mMapNavi.reCalculateRoute(PathPlanningStrategy.DRIVING_SHORT_DISTANCE);
		if (null != mOnNavingListener) {
			mOnNavingListener.onReCalculateRoute();
		}
	}

	/**
	 * 启动导航后回调
	 */
	@Override
	public void onStartNavi(int type) {
		mVoiceController.startSpeaking("导航开始");
	}

	/**
	 * 当前方路况光柱信息有更新时回调
	 */
	@Override
	public void onTrafficStatusUpdate() {

	}

	/**
	 * 显示路口放大图回调
	 */
	@Override
	public void showCross(AMapNaviCross aMapNaviCross) {

	}

	/**
	 * 显示道路信息回调
	 */
	@Override
	public void showLaneInfo(AMapLaneInfo[] laneInfos,
			byte[] laneBackgroundInfo, byte[] laneRecommendedInfo) {

	}

	@Override
	public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] info) {

	}

	/**
	 * 巡航模式（无路线规划）下，统计信息更新回调
	 */
	@Override
	public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo info) {

	}

	/**
	 * 巡航模式（无路线规划）下，统计信息更新回调
	 */
	@Override
	public void updateAimlessModeStatistics(AimLessModeStat stat) {

	}

	// -------------------导航事件回调-----------------------
}
