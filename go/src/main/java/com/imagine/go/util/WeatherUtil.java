package com.imagine.go.util;

import java.util.HashMap;
import java.util.Map;

import com.imagine.go.R;

/**
 * 天气预报工具类
 * 
 * @author Jinhu
 * @date 2016/5/11
 */
public class WeatherUtil {

	private static final Map<String, Integer> mWeatherIcons = new HashMap<String, Integer>();

	static {
		initWeatherIcons();
	}

	/**
	 * 初始化天气图标
	 */
	private static void initWeatherIcons() {
		mWeatherIcons.put("晴", R.drawable.ic_weather_qing);
		mWeatherIcons.put("多云", R.drawable.ic_weather_duoyun);
		mWeatherIcons.put("阴", R.drawable.ic_weather_yin);
		mWeatherIcons.put("阵雨", R.drawable.ic_weather_zhenyu);
		mWeatherIcons.put("雷阵雨", R.drawable.ic_weather_leizhenyu);

		mWeatherIcons.put("雷阵雨并伴有冰雹", R.drawable.ic_weather_leizhenyubingbao);
		mWeatherIcons.put("雨夹雪", R.drawable.ic_weather_yujiaxue);
		mWeatherIcons.put("小雨", R.drawable.ic_weather_xiaoyu);
		mWeatherIcons.put("中雨", R.drawable.ic_weather_zhongyu);
		mWeatherIcons.put("大雨", R.drawable.ic_weather_dayu);

		mWeatherIcons.put("暴雨", R.drawable.ic_weather_baoyu);
		mWeatherIcons.put("大暴雨", R.drawable.ic_weather_dabaoyu);
		mWeatherIcons.put("特大暴雨", R.drawable.ic_weather_tedabaoyu);
		mWeatherIcons.put("阵雪", R.drawable.ic_weather_zhenxue);
		mWeatherIcons.put("小雪", R.drawable.ic_weather_xiaoxue);

		mWeatherIcons.put("中雪", R.drawable.ic_weather_zhongxue);
		mWeatherIcons.put("大雪", R.drawable.ic_weather_daxue);
		mWeatherIcons.put("暴雪", R.drawable.ic_weather_baoxue);
		mWeatherIcons.put("雾", R.drawable.ic_weather_wu);
		mWeatherIcons.put("冻雨", R.drawable.ic_weather_dongyu);

		mWeatherIcons.put("沙尘暴", R.drawable.ic_weather_shachenbao);
		mWeatherIcons.put("小雨-中雨", R.drawable.ic_weather_xiaoyu);
		mWeatherIcons.put("中雨-大雨", R.drawable.ic_weather_zhongyu);
		mWeatherIcons.put("大雨-暴雨", R.drawable.ic_weather_dayu);
		mWeatherIcons.put("暴雨-大暴雨", R.drawable.ic_weather_baoyu);

		mWeatherIcons.put("大暴雨-特大暴雨", R.drawable.ic_weather_dabaoyu);
		mWeatherIcons.put("小雪-中雪", R.drawable.ic_weather_xiaoxue);
		mWeatherIcons.put("中雪-大雪", R.drawable.ic_weather_zhongxue);
		mWeatherIcons.put("大雪-暴雪", R.drawable.ic_weather_daxue);
		mWeatherIcons.put("浮尘", R.drawable.ic_weather_fuchen);

		mWeatherIcons.put("扬沙", R.drawable.ic_weather_fuchen);
		mWeatherIcons.put("强沙尘暴", R.drawable.ic_weather_shachenbao);
		mWeatherIcons.put("飑", R.drawable.ic_weather_longjuanfeng);
		mWeatherIcons.put("龙卷风", R.drawable.ic_weather_longjuanfeng);
		mWeatherIcons.put("弱高吹雪", R.drawable.ic_weather_chuixue);

		mWeatherIcons.put("轻霾", R.drawable.ic_weather_mai);
		mWeatherIcons.put("霾", R.drawable.ic_weather_mai);
	}

	/**
	 * 获取天气图标
	 * 
	 * @param weather
	 * @return
	 */
	public static int getWeatherIcon(String weather) {
		Integer i = mWeatherIcons.get(weather);
		return i.intValue();
	}

	/**
	 * 匹配星期
	 * 
	 * @param mweek
	 * @return
	 */
	public static String matchWeek(String mweek) {
		String week = null;
		int i = Integer.valueOf(mweek);
		i %= 7;
		switch (i) {
		case 0:
			week = "星期日";
			break;
		case 1:
			week = "星期一";
			break;
		case 2:
			week = "星期二";
			break;
		case 3:
			week = "星期三";
			break;
		case 4:
			week = "星期四";
			break;
		case 5:
			week = "星期五";
			break;
		case 6:
			week = "星期六";
			break;
		}
		return week;
	}

}
