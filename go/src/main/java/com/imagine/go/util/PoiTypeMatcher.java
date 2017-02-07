package com.imagine.go.util;

import java.util.HashMap;
import java.util.Map;

import com.imagine.go.R;
import com.imagine.go.model.PoiType;

/**
 * PoiTypeMatcher:根据字符串硬编码匹配Poi类型及图片
 * 
 * @author Jinhu
 * @date 2016/3/22
 */
public class PoiTypeMatcher {

	private static String mLabelType;

	private static Map<String, PoiType> typeMatcher = new HashMap<String, PoiType>();
	private static Map<String, Icon> iconMatcher = new HashMap<String, Icon>();

	/**
	 * 图片类型
	 * 
	 * @author Jinhu
	 * @date 2016年5月12日
	 */
	public static class Icon {
		int mTypeIcon;
		int mBackgroundIcon;

		public Icon(int typeIc, int bgIc) {
			mTypeIcon = typeIc;
			mBackgroundIcon = bgIc;
		}

		public int getType() {
			return mTypeIcon;
		}

		public int getBackground() {
			return mBackgroundIcon;
		}
	}

	static {
		initTypeMatcher();
		initIconMatcher();
	}

	private static void initTypeMatcher() {
		typeMatcher.put("美食", PoiType.Catering);
		typeMatcher.put("酒店", PoiType.Accommodation);
		typeMatcher.put("银行", PoiType.Finance);
		typeMatcher.put("娱乐", PoiType.Entertainment);
		typeMatcher.put("商场", PoiType.ShoppingMarket);
		typeMatcher.put("便利店", PoiType.Store);
		typeMatcher.put("景点", PoiType.ViewSpot);
		typeMatcher.put("厕所", PoiType.WC);
		typeMatcher.put("公交站", PoiType.BusStation);
	}

	private static void initIconMatcher() {
		iconMatcher.put("美食", new Icon(R.drawable.ic_map_food,
				R.drawable.shaper_ar_marker_btn_rect));
		iconMatcher.put("酒店", new Icon(R.drawable.ic_map_hotel,
				R.drawable.shaper_ar_marker_btn_rect_b));
		iconMatcher.put("银行", new Icon(R.drawable.ic_map_bank,
				R.drawable.shaper_ar_marker_btn_rect_b));
		iconMatcher.put("娱乐", new Icon(R.drawable.ic_map_ktv,
				R.drawable.shaper_ar_marker_btn_rect));
		iconMatcher.put("商场", new Icon(R.drawable.ic_map_market,
				R.drawable.shaper_ar_marker_btn_rect));
		iconMatcher.put("景点", new Icon(R.drawable.ic_map_viewspot,
				R.drawable.shaper_ar_marker_btn_rect_b));
		iconMatcher.put("厕所", new Icon(R.drawable.ic_map_wc,
				R.drawable.shaper_ar_marker_btn_rect_b));
		iconMatcher.put("公交站", new Icon(R.drawable.ic_map_bus,
				R.drawable.shaper_ar_marker_btn_rect_b));
	}

	public static String getCurrentLableName() {
		return mLabelType;
	}

	/**
	 * 获取Poi类型
	 * 
	 * @param labelName
	 * @return
	 */
	public static String getPoiType(String labelName) {
		mLabelType = labelName;
		PoiType poiType = typeMatcher.get(labelName);
		if (null == poiType)
			return null;
		return poiType.getValue();
	}

	/**
	 * 获取Poi图片
	 * 
	 * @param lableName
	 * @return
	 */
	public static Icon getPoiIcon(String lableName) {
		Icon icon = iconMatcher.get(lableName);
		if (null == icon)
			return new Icon(R.drawable.ic_map_like,
					R.drawable.shaper_ar_marker_btn_rect);
		return icon;
	}
}