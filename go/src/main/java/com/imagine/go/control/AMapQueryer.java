package com.imagine.go.control;

import static com.imagine.go.Constants.CODE_AMAP_SEARCH_SUCCESS_RETURN;
import static com.imagine.go.Constants.NO_RESULT;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.util.Log;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Inputtips.InputtipsListener;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.FromAndTo;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.weather.LocalWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearch.OnWeatherSearchListener;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.imagine.go.Constants;
import com.imagine.go.util.ToastUtil;

/**
 * AMapQueryer:高德搜索组件
 * 
 * @author Jinhu
 * @date 2016/3/27
 */
public class AMapQueryer implements OnGeocodeSearchListener,
		OnRouteSearchListener, OnWeatherSearchListener, InputtipsListener {
	private static final String TAG = AMapQueryer.class.getSimpleName();

	private Context mContext;

	/* 标记是否正在进行搜索 . */
	private final AtomicBoolean isSearching = new AtomicBoolean(false);

	/* 地图搜索回调 . */
	private OnMapQueryListener mOnMapQueryListener;

	public interface OnMapQueryListener {
		// 逆地理搜索结果回调
		void onRegeocodeSearched(RegeocodeAddress address);

		// 步行规划搜索结果回调
		void onWalkRouteSearched(WalkRouteResult walkRouteResult);

	}

	/* Poi提示字搜索回调 . */
	private OnInputTipsQueryListener mOnInputtipsQueryListener;

	public interface OnInputTipsQueryListener {
		// Poi提示字回调
		void onGetInputtips(List<String> nameList);
	}

	/* 天气搜索回调 . */
	private OnWeatherQueryListener mOnWeatherQueryListener;

	public interface OnWeatherQueryListener {
		// 实况天气
		void onWeatherLiveSearched(LocalWeatherLive weatherLive);

		// 预报天气
		void onWeatherForecastSearched(LocalWeatherForecast weatherForecast);
	}

	public AMapQueryer(Context context) {
		mContext = context;
	}

	/**
	 * 经纬度转换高德几何对象点
	 * 
	 * @param latLng
	 * @return
	 */
	public LatLonPoint latLng2latLonPoint(LatLng latLng) {
		return new LatLonPoint(latLng.latitude, latLng.longitude);
	}

	// ------------------------ 搜索-----------------------------
	/**
	 * 根据经纬度获取其地址信息
	 * 
	 * @param latlng
	 *            经纬度
	 */
	public synchronized void searchAddress(LatLng latlng, float radius) {
		// 如果不是正在搜索,则进行搜索
		if (isSearching.compareAndSet(false, true)) {
			RegeocodeQuery mquery = new RegeocodeQuery(new LatLonPoint(
					latlng.latitude, latlng.longitude), radius,
					GeocodeSearch.AMAP);
			GeocodeSearch msearch = new GeocodeSearch(mContext);
			msearch.setOnGeocodeSearchListener(this);
			msearch.getFromLocationAsyn(mquery);
		} else {
			ToastUtil.showShort("正在进行搜索,请稍等");
		}

		if (Constants.IS_DEBUG) {
			Log.d(TAG, "--searchAddress()--  searching");
		}
	}

	/**
	 * 天气搜索
	 * 
	 * @param mode
	 *            搜索模式
	 * @param city
	 *            模式城市
	 */
	public synchronized void searchWeather(String city, int mode) {
		if (WeatherSearchQuery.WEATHER_TYPE_LIVE != mode
				&& WeatherSearchQuery.WEATHER_TYPE_FORECAST != mode)
			throw new IllegalArgumentException();
		// 如果不是正在搜索,则进行搜索
		if (isSearching.compareAndSet(false, true)) {
			WeatherSearchQuery mquery = new WeatherSearchQuery(city, mode); // 实况天气搜索
			WeatherSearch msearch = new WeatherSearch(mContext);
			msearch.setOnWeatherSearchListener(this);
			msearch.setQuery(mquery);
			msearch.searchWeatherAsyn();
		} else {
			ToastUtil.showShort("正在进行搜索,请稍等");
		}
		if (Constants.IS_DEBUG) {
			Log.d(TAG, "--searchWeather()--  searching");
		}
	}

	/**
	 * 天气搜索
	 * 
	 * @param city
	 *            所在城市
	 */
	public synchronized void searchWeather(String city) {
		// 如果不是正在搜索,则进行搜索
		if (isSearching.compareAndSet(false, true)) {
			WeatherSearch mLiveSearch = new WeatherSearch(mContext);
			WeatherSearchQuery mLiveQuery = new WeatherSearchQuery(city,
					WeatherSearchQuery.WEATHER_TYPE_LIVE); // 实况天气搜索
			mLiveSearch.setOnWeatherSearchListener(this);
			mLiveSearch.setQuery(mLiveQuery);
			mLiveSearch.searchWeatherAsyn();

			WeatherSearch mForecastSearch = new WeatherSearch(mContext);
			WeatherSearchQuery mForecastQuery = new WeatherSearchQuery(city,
					WeatherSearchQuery.WEATHER_TYPE_FORECAST); // 预报天气搜索
			mForecastSearch.setOnWeatherSearchListener(this);
			mForecastSearch.setQuery(mForecastQuery);
			mForecastSearch.searchWeatherAsyn();
		} else {
			ToastUtil.showShort("正在进行搜索,请稍等");
		}
		if (Constants.IS_DEBUG) {
			Log.d(TAG, "--searchWeather()--  searching");
		}
	}

	/**
	 * Poi提示字搜索
	 * 
	 * @param keyword
	 * @param city
	 */
	public synchronized void searchPoiInputTips(String keyword, String city) {
		// 如果不是正在搜索,则进行搜索
		if (isSearching.compareAndSet(false, true)) {
			InputtipsQuery mquery = new InputtipsQuery(keyword, city);
			Inputtips msearch = new Inputtips(mContext, mquery);
			msearch.setInputtipsListener(this);
			msearch.requestInputtipsAsyn();
		}

		if (Constants.IS_DEBUG) {
			Log.d(TAG, "--searchPoiInputTips()--  searching");
		}
	}

	// -----------------------------搜索回调---------------------------------
	@Override
	public void onGeocodeSearched(GeocodeResult result, int rcode) {

	}

	/**
	 * 高德搜索经纬度相应的地址时回调结果
	 */
	@Override
	public synchronized void onRegeocodeSearched(RegeocodeResult result,
			int rcode) {
		isSearching.set(false);
		if (CODE_AMAP_SEARCH_SUCCESS_RETURN == rcode) {
			if (null != result && null != result.getRegeocodeAddress()
					&& null != result.getRegeocodeAddress().getFormatAddress()) {
				RegeocodeAddress address = result.getRegeocodeAddress();
				
				if (null != mOnMapQueryListener) {
					mOnMapQueryListener.onRegeocodeSearched(address);
				}
			}
		} else {
			if (null != mOnMapQueryListener) {
				mOnMapQueryListener.onRegeocodeSearched(null);
			}
			ToastUtil.showShort(NO_RESULT);
		}

		if (Constants.IS_DEBUG) {
			Log.d(TAG, "--onRegeocodeSearched() -- searched");
		}
	}

	/**
	 * 设置高德地图搜索监听
	 * 
	 * @param mRegeocodeSearched
	 */
	public void setOnMapQueryListener(OnMapQueryListener mQueryListener) {
		this.mOnMapQueryListener = mQueryListener;
	}

	/**
	 * 设置Poi提示字搜索回调监听
	 * 
	 * @param mQueryListener
	 */
	public void setOnInputTipsQueryListener(
			OnInputTipsQueryListener mQueryListener) {
		this.mOnInputtipsQueryListener = mQueryListener;
	}

	/**
	 * 设置天气搜索回调
	 * 
	 * @param mQueryListener
	 */
	public void setOnWeatherQueryListener(OnWeatherQueryListener mQueryListener) {
		this.mOnWeatherQueryListener = mQueryListener;
	}

	// --------------------- 步行路径规划-----------------------------
	/**
	 * 步行路径规划
	 */
	public synchronized void searchWalkRoute(LatLng mLocation,
			LatLng mDestination) {
		// 如果不是正在搜索,则进行搜索
		if (isSearching.compareAndSet(false, true)) {
			FromAndTo mFromAndTo = new FromAndTo(latLng2latLonPoint(mLocation),
					latLng2latLonPoint(mDestination));
			WalkRouteQuery mWalkQuery = new WalkRouteQuery(mFromAndTo,
					RouteSearch.WalkDefault);
			RouteSearch mSearch = new RouteSearch(mContext);
			mSearch.calculateWalkRouteAsyn(mWalkQuery);
			mSearch.setRouteSearchListener(this);
		} else {
			ToastUtil.showShort("正在进行搜索,请稍等");
		}
	}

	/**
	 * 高德步行规划结果回调
	 */
	@Override
	public synchronized void onWalkRouteSearched(
			WalkRouteResult walkRouteResult, int rCode) {
		isSearching.set(false);
		if (CODE_AMAP_SEARCH_SUCCESS_RETURN == rCode) {
			if (walkRouteResult != null && walkRouteResult.getPaths() != null) {
				if (walkRouteResult.getPaths().size() > 0) {
					// 回调结果
					if (null != mOnMapQueryListener) {
						mOnMapQueryListener
								.onWalkRouteSearched(walkRouteResult);
					}
				} else if (walkRouteResult != null
						&& walkRouteResult.getPaths() == null) {
					ToastUtil.showShort(NO_RESULT);
				}
			} else {
				ToastUtil.showShort(NO_RESULT);
			}
		} else {
			Log.w(TAG, "--onWalkRouteSearched()--  error " + rCode);
			ToastUtil.showShort(NO_RESULT);
			if (null != mOnMapQueryListener) {
				mOnMapQueryListener.onWalkRouteSearched(null);
			}
		}
	}

	@Override
	public void onBusRouteSearched(BusRouteResult paramBusRouteResult,
			int paramInt) {

	}

	@Override
	public void onDriveRouteSearched(DriveRouteResult paramDriveRouteResult,
			int paramInt) {

	}

	/**
	 * 预报天气搜索返回
	 * 
	 * @param result
	 * @param rCode
	 */
	@Override
	public void onWeatherForecastSearched(LocalWeatherForecastResult result,
			int rCode) {
		isSearching.set(false);
		if (CODE_AMAP_SEARCH_SUCCESS_RETURN == rCode) {
			if (null != result
					&& null != result.getForecastResult()
					&& null != result.getForecastResult().getWeatherForecast()
					&& result.getForecastResult().getWeatherForecast().size() > 0) {
				LocalWeatherForecast weatherForecast = result
						.getForecastResult();
				if (null != mOnWeatherQueryListener) {
					mOnWeatherQueryListener
							.onWeatherForecastSearched(weatherForecast);
				}
			}
		} else {
			Log.w(TAG, "--onWeatherForecastSearched()--  error " + rCode);
			if (null != mOnWeatherQueryListener) {
				mOnWeatherQueryListener.onWeatherForecastSearched(null);
			}
		}
	}

	/**
	 * 实况天气搜索返回
	 * 
	 * @param result
	 * @param rCode
	 */
	@Override
	public void onWeatherLiveSearched(LocalWeatherLiveResult result, int rCode) {
		isSearching.set(false);
		if (CODE_AMAP_SEARCH_SUCCESS_RETURN == rCode) {
			if (null != result && null != result.getLiveResult()) {
				LocalWeatherLive weatherlive = result.getLiveResult();
				if (null != mOnWeatherQueryListener) {
					mOnWeatherQueryListener.onWeatherLiveSearched(weatherlive);
				}
			}
		} else {
			Log.w(TAG, "--onWeatherLiveSearched()--  error " + rCode);
			if (null != mOnWeatherQueryListener) {
				mOnWeatherQueryListener.onWeatherLiveSearched(null);
			}
		}
	}

	/**
	 * Poi搜索提示字返回
	 * 
	 * @param paramList
	 * @param paramInt
	 */
	@Override
	public void onGetInputtips(List<Tip> tipList, int rCode) {
		isSearching.set(false);
		if (CODE_AMAP_SEARCH_SUCCESS_RETURN == rCode) {
			List<String> nameList = new ArrayList<String>();
			for (int i = 0; i < tipList.size(); i++) {
				nameList.add(tipList.get(i).getName());
			}

			if (null != mOnInputtipsQueryListener) {
				mOnInputtipsQueryListener.onGetInputtips(nameList);
			}
		} else {
			Log.w(TAG, "--onWeatherLiveSearched()--  error " + rCode);
			if (null != mOnInputtipsQueryListener) {
				mOnInputtipsQueryListener.onGetInputtips(null);
			}
		}
	}

}
