package com.imagine.go;

import static com.imagine.go.Constants.VALUE_DEFAULT_SEARCH_RADIUS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.app.Application;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.imagine.go.control.AOfflineMapManager;
import com.imagine.go.data.DatabaseManager;
import com.imagine.go.model.GeoPoint;
import com.imagine.go.model.PoiSearchData;
import com.imagine.go.util.ToastUtil;

/**
 * AppManager:管理程序的Activity集合
 * 
 * @author Jinhu
 * @date 2016/3/19
 */
public class AppManager extends Application {

	/* AppManager单例。 */
	private static AppManager instatnce;

	/* Activity集合。 */
	private List<Activity> activitys = new ArrayList<Activity>();

	/* 记录当前所在地信息。 */
	private GeoPoint mLocationPoint = new GeoPoint();
	/* 记录当前目的地信息。 */
	private GeoPoint mDestinationPoint = new GeoPoint();

	/* 记录屏幕宽高. */
	private int screenWidth = -1;
	private int screenHeight = -1;

	/* 网络状态监听 . */
	private AtomicBoolean mNetConnected = new AtomicBoolean(true);
	private AtomicBoolean mWifiConnected = new AtomicBoolean(false);
	private NetConnectionReceiver mConnectReceiver;

	/* 离线地图下载管理 . */
	private AOfflineMapManager mOfflineMapManager;

	/* 数据库管理 . */
	private DatabaseManager mDbMgr;

	/* Poi搜索数据 。 */
	private PoiSearchData mPoiSearchData = new PoiSearchData();
	{
		mPoiSearchData.setRadius(VALUE_DEFAULT_SEARCH_RADIUS);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// 初始化工具
		ToastUtil.initContext(this);

		// 初始化监听器
		registerReceiver();

		mOfflineMapManager = AOfflineMapManager.getInstance(this);
		
		mDbMgr = DatabaseManager.getInstance(this);
		mDbMgr.open();
	}
	
	/**
	 * 获得唯一的AppManager
	 * 
	 * @return AppManager
	 */
	public static AppManager getInstance() {
		if (null == instatnce) {
			instatnce = new AppManager();
		}
		return instatnce;
	}

	/**
	 * 添加Activity
	 * 
	 * @param activity
	 */
	public void addActivity(Activity activity) {
		activitys.add(activity);
	}

	/**
	 * 移除Activity
	 * 
	 * @param activity
	 * @return isDeled
	 */
	public boolean delActivity(Activity activity) {
		boolean isDeled = activitys.remove(activity);
		return isDeled;
	}

	/**
	 * 结束程序
	 */
	public void exit() {
		unregisterReceiver();
		for (Activity activity : activitys) {
			activity.finish();
		}
		activitys.clear();
		mOfflineMapManager.destroy();
		mDbMgr.close();
		System.exit(0);
	}

	/**
	 * 注册状态接收器
	 */
	private void registerReceiver() {
		IntentFilter filter = new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION);
		mConnectReceiver = new NetConnectionReceiver();
		this.registerReceiver(mConnectReceiver, filter);
	}

	/**
	 * 取消状态接收器
	 */
	private void unregisterReceiver() {
		this.unregisterReceiver(mConnectReceiver);
	}

	public void setNetConnectedState(boolean isConnected) {
		this.mNetConnected.set(isConnected);
	}

	public boolean getNetConnectedState() {
		return this.mNetConnected.get();
	}

	public void setWifiConnectedState(boolean isConnected) {
		this.mWifiConnected.set(isConnected);
	}

	public boolean getWifiConnectedState() {
		return this.mWifiConnected.get();
	}

	public GeoPoint getLocationPoint() {
		return mLocationPoint;
	}

	public GeoPoint getDestinationPoint() {
		return mDestinationPoint;
	}

	public PoiSearchData getPoiSearchData() {
		return mPoiSearchData;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

}
