package com.imagine.go.control;

import java.util.ArrayList;

import android.content.Context;

import com.amap.api.maps.AMapException;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.amap.api.maps.offlinemap.OfflineMapManager.OfflineMapDownloadListener;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import com.amap.api.maps.offlinemap.OfflineMapStatus;

/**
 * 高德离线管理单例
 * 
 * @author Jinhu
 * @date 2016/5/20
 */
public class AOfflineMapManager implements OfflineMapDownloadListener {

	private static AOfflineMapManager instance;

	/* 高德离线地图组件 . */
	private OfflineMapManager mOfflineMapManger;

	private AOfflineMapDownloadListener mOfflineMapDownloadListener;

	public interface AOfflineMapDownloadListener {
		/**
		 * 离线地图下载回调
		 * 
		 * @param status
		 * @param completeCode
		 * @param downName
		 */
		void onDownload(int status, int completeCode, String downName);

		/**
		 * 离线地图检测更新回调
		 * 
		 * @param hasNew
		 * @param name
		 */
		void onCheckUpdate(boolean hasNew, String name);

		/**
		 * 离线地图删除回调
		 * 
		 * @param success
		 * @param name
		 * @param describe
		 */
		void onRemove(boolean success, String name, String describe);
	}

	private AOfflineMapManager(Context context) {
		mOfflineMapManger = new OfflineMapManager(context, this);
	}

	public static AOfflineMapManager getInstance(Context context) {
		if (null == instance) {
			instance = new AOfflineMapManager(context);
		}
		return instance;
	}

	public static AOfflineMapManager getInstance() {
		if (null == instance) {
			throw new NullPointerException(
					AOfflineMapManager.class.getSimpleName() + " is null");
		}
		return instance;
	}

	/**
	 * 销毁
	 */
	public void destroy() {
		mOfflineMapManger.destroy();
		mOfflineMapManger = null;
		instance = null;
	}

	/**
	 * 暂停下载队列中的任务
	 */
	public void pause() {
		mOfflineMapManger.pause();
	}

	/**
	 * 重新启动下载队列中的任务
	 */
	public void restart() {
		mOfflineMapManger.restart();
	}

	/**
	 * 停止下载任务
	 */
	public void stop() {
		mOfflineMapManger.stop();
	}

	/**
	 * 根据给定的城市名称删除该城市的离线地图包
	 * 
	 * @param cityname
	 */
	public void remove(String cityname) {
		mOfflineMapManger.remove(cityname);
	}

	/**
	 * 根据给定的城市编码下载该城市的离线地图包<br/>
	 * 异步方法
	 * 
	 * @param cityCode
	 * @throws AMapException
	 */
	public void downloadByCityCode(String cityCode) throws AMapException {
		mOfflineMapManger.downloadByCityCode(cityCode);
	}

	/**
	 * 根据给定的城市名称下载该城市的离线地图包 <br/>
	 * 异步方法
	 * 
	 * @param cityName
	 * @throws AMapException
	 */
	public void downloadByCityName(String cityName) throws AMapException {
		mOfflineMapManger.downloadByCityName(cityName);
	}

	/**
	 * 根据给定的城市名称下载该城市的离线地图包 <br/>
	 * 异步方法
	 * 
	 * @param provinceName
	 * @throws AMapException
	 */
	public void downloadByProvinceName(String provinceName)
			throws AMapException {
		mOfflineMapManger.downloadByProvinceName(provinceName);
	}

	/**
	 * @return 所有正在下载或等待下载离线地图的城市列表
	 */
	public ArrayList<OfflineMapCity> getDownloadingCityList() {
		return mOfflineMapManger.getDownloadingCityList();
	}

	/**
	 * 
	 * @return 所有正在下载或等待下载离线地图的省份列表
	 */
	public ArrayList<OfflineMapProvince> getDownloadingProvinceList() {
		return mOfflineMapManger.getDownloadingProvinceList();
	}

	/**
	 * @return 返回已经下载完成离线地图的城市列表。
	 */
	public ArrayList<OfflineMapCity> getDownloadOfflineMapCityList() {
		return mOfflineMapManger.getDownloadOfflineMapCityList();
	}

	/**
	 * 
	 * @return 返回已经下载完成离线地图的省份列表
	 */
	public ArrayList<OfflineMapProvince> getDownloadOfflineMapProvinceList() {
		return mOfflineMapManger.getDownloadOfflineMapProvinceList();
	}

	/**
	 * 根据城市编码获取OfflineMapCity对象
	 * 
	 * @param cityCode
	 * @return
	 */
	public OfflineMapCity getItemByCityCode(String cityCode) {
		return mOfflineMapManger.getItemByCityCode(cityCode);
	}

	/**
	 * 根据城市名称获取OfflneMapCity对象
	 * 
	 * @param cityName
	 * @return
	 */
	public OfflineMapCity getItemByCityName(String cityName) {
		return mOfflineMapManger.getItemByCityName(cityName);
	}

	/**
	 * 根据省份名称获取OfflineMapProvince对象
	 * 
	 * @param provineName
	 * @return
	 */
	public OfflineMapProvince getItemByProvinceName(String provineName) {
		return mOfflineMapManger.getItemByProvinceName(provineName);
	}

	/**
	 * 
	 * @return 获取所有存在有离线地图的城市列表
	 */
	public ArrayList<OfflineMapCity> getOfflineMapCityList() {
		return mOfflineMapManger.getOfflineMapCityList();
	}

	/**
	 * 获取所有存在有离线地图的省的列表。
	 * 
	 * @return
	 */
	public ArrayList<OfflineMapProvince> getOfflineMapProvinceList() {
		return mOfflineMapManger.getOfflineMapProvinceList();
	}

	/**
	 * 判断传入的城市（城市编码）是否有更新的离线数据包
	 * 
	 * @param cityCode
	 * @throws AMapException
	 */
	public void updateOfflineCityByCode(String cityCode) throws AMapException {
		mOfflineMapManger.updateOfflineCityByCode(cityCode);
	}

	/**
	 * 判断传入的城市（城市名称）是否有更新的离线数据包
	 * 
	 * @param cityName
	 * @throws AMapException
	 */
	public void updateOfflineCityByName(String cityName) throws AMapException {
		mOfflineMapManger.updateOfflineCityByName(cityName);
	}

	/**
	 * 判断传入的省份名称是否有更新的离线数据包
	 * 
	 * @param provinceName
	 * @throws AMapException
	 */
	public void updateOfflineMapProvinceByName(String provinceName)
			throws AMapException {
		mOfflineMapManger.updateOfflineMapProvinceByName(provinceName);
	}

	/**
	 * 启动所有暂停中的下载任务
	 * 
	 * @throws AMapException
	 */
	public void startDownloadInAllPause() throws AMapException {
		for (OfflineMapCity city : mOfflineMapManger.getDownloadingCityList()) {
			if (OfflineMapStatus.PAUSE == city.getState()) {
				downloadByCityCode(city.getCode());
			}
		}
	}

	/**
	 * 撤销所有正在下载和等待下载的任务
	 */
	public void cancelDownloading() {
		for (OfflineMapCity city : mOfflineMapManger.getDownloadingCityList()) {
			if (OfflineMapStatus.LOADING == city.getState()
					|| OfflineMapStatus.WAITING == city.getState()) {
				remove(city.getCity());
			}
		}
	}

	/**
	 * 检测已下载城市是否有更新
	 * 
	 * @throws AMapException
	 */
	public void checkUpdateDowloadedCity() throws AMapException {
		for (OfflineMapCity city : mOfflineMapManger
				.getDownloadOfflineMapCityList()) {
			updateOfflineCityByCode(city.getCode());
		}
	}

	/**
	 * 离线地图状态回调接口
	 * 
	 * @param aOfflineMapDownloadListener
	 */
	public void setAOfflineMapDownloadListener(
			AOfflineMapDownloadListener aOfflineMapDownloadListener) {
		mOfflineMapDownloadListener = aOfflineMapDownloadListener;
	}

	/**
	 * 离线地图下载回调
	 */
	@Override
	public void onDownload(int status, int completeCode, String downName) {
		if (null != mOfflineMapDownloadListener) {
			mOfflineMapDownloadListener.onDownload(status, completeCode,
					downName);
		}
	}

	/**
	 * 离线地图检测更新回调
	 */
	@Override
	public void onCheckUpdate(boolean hasNew, String name) {
		if (null != mOfflineMapDownloadListener) {
			mOfflineMapDownloadListener.onCheckUpdate(hasNew, name);
		}
	}

	/**
	 * 离线地图删除回调
	 */
	@Override
	public void onRemove(boolean success, String name, String describe) {
		if (null != mOfflineMapDownloadListener) {
			mOfflineMapDownloadListener.onRemove(success, name, describe);
		}
	}
}
