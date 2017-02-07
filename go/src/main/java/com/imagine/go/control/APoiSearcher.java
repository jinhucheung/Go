package com.imagine.go.control;

import static com.imagine.go.Constants.CODE_AMAP_SEARCH_SUCCESS_RETURN;
import static com.imagine.go.Constants.IS_DEBUG;
import static com.imagine.go.Constants.NO_RESULT;
import android.content.Context;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;
import com.imagine.go.model.PoiType;
import com.imagine.go.util.ToastUtil;

/**
 * APoiSearcher:高德Poi搜索服务管理
 * 
 * @author Jinhu
 * @date 2016/3/22
 */
public class APoiSearcher implements OnPoiSearchListener {
	private static final String TAG = APoiSearcher.class.getSimpleName();

	private Context context;

	private PoiSearch.Query query;

	/* Poi搜索每页最多放回20条Poiitem . */
	private int pageSize = 50;
	/* 查询第1页 . */
	private int currentPage = 1;

	/**
	 * 对外回调接口
	 */
	private APoiSearchListener aPoiSearchListener;

	public interface APoiSearchListener {
		// 成功搜索
		void onPoiSearched(PoiResult result);
	}

	public APoiSearcher(Context context) {
		this.context = context;
	}

	/**
	 * 周边搜索Poi
	 * 
	 * @param mLocation
	 *            当前位置
	 * @param keyword
	 *            关键字
	 * @param type
	 *            Poi分类
	 * @param radius
	 *            搜索半径
	 * 
	 */
	public void searchNearby(AMapLocation mLocation, String keyword,
			String type, int radius) {
		query = new PoiSearch.Query(keyword, type, mLocation.getCityCode());
		query.setPageSize(pageSize);
		query.setPageNum(currentPage);
		PoiSearch search = new PoiSearch(context, query);
		// 设置周边搜索的中心点以及区域
		search.setBound(new SearchBound(new LatLonPoint(
				mLocation.getLatitude(), mLocation.getLongitude()), radius));
		// 设置数据返回的监听器
		search.setOnPoiSearchListener(this);
		// 开始搜索
		search.searchPOIAsyn();

		if (IS_DEBUG) {
			Log.d(TAG, "---searchNearby()--- searching");
		}

	}

	/**
	 * 通过Poi分类搜索Poi
	 * 
	 * @param mLocation
	 *            当前位置
	 * @param type
	 *            Poi分类
	 * @param radius
	 *            搜索半径
	 */
	public void searchNearbyType(AMapLocation mLocation, String type, int radius) {
		this.searchNearby(mLocation, type, type, radius);
		if (IS_DEBUG) {
			Log.d(TAG, "---searchNearbyType()--- searching");
		}
	}

	/**
	 * 通过关键字搜索Poi
	 * 
	 * @param mLocation
	 *            当前位置
	 * @param keyword
	 *            Poi分类
	 * @param radius
	 *            搜索半径
	 */
	public void searchNearbyKeyword(AMapLocation mLocation, String keyword,
			int radius) {
		this.searchNearby(mLocation, keyword, PoiType.Default.getValue(),
				radius);
		if (IS_DEBUG) {
			Log.d(TAG, "---searchNearbyKeyword()--- searching");
		}
	}

	/**
	 * 搜索信息回调接口
	 */
	@Override
	public void onPoiSearched(PoiResult result, int rCode) {
		if (CODE_AMAP_SEARCH_SUCCESS_RETURN == rCode) {
			if (null != result && null != result.getQuery()) {
				// 是否是同一条
				if (result.getQuery().equals(query)) {

					// 对外回调
					if (null != aPoiSearchListener) {
						aPoiSearchListener.onPoiSearched(result);
					}

					// 成功返回搜索信息
					if (IS_DEBUG) {
						Log.d(TAG, "---onPoiSearched()--- success");
					}
				}
			}
		} else {
			if (null != aPoiSearchListener) {
				aPoiSearchListener.onPoiSearched(null);
			}
			ToastUtil.showShort(NO_RESULT);
			// 搜索失败
			Log.e(TAG, "PoiSearch Error, ErrCode:" + rCode);
		}
	}

	@Override
	public void onPoiItemSearched(PoiItem item, int rCode) {

	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public void setAPoiSearchListener(APoiSearchListener aPoiSearchListener) {
		this.aPoiSearchListener = aPoiSearchListener;
	}

}
