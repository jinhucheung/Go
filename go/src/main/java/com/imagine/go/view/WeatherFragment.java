package com.imagine.go.view;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.services.weather.LocalDayWeatherForecast;
import com.imagine.go.R;
import com.imagine.go.util.WeatherUtil;

/**
 * 设置Weather子页
 * 
 * @author Jinhu
 * @date 2016/5/11
 */
public class WeatherFragment extends Fragment {
	private static final int size = 3;

	private int offIndex = 0;

	private TextView weekTv1, weekTv2, weekTv3;
	private ImageView weather_imgIv1, weather_imgIv2, weather_imgIv3;
	private TextView temperatureTv1, temperatureTv2, temperatureTv3;
	private TextView climateTv1, climateTv2, climateTv3;
	private TextView windTv1, windTv2, windTv3;

	private TextView[] weekTvs = new TextView[size];
	private ImageView[] weather_imgIvs = new ImageView[size];
	private TextView[] temperatureTvs = new TextView[size];
	private TextView[] climateTvs = new TextView[size];
	private TextView[] windTvs = new TextView[size];

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.item_weather, container, false);
		View view1 = view.findViewById(R.id.id_layout_subitem1);
		View view2 = view.findViewById(R.id.id_layout_subitem2);
		View view3 = view.findViewById(R.id.id_layout_subitem3);

		weekTv1 = (TextView) view1.findViewById(R.id.id_textView_week);
		weekTv2 = (TextView) view2.findViewById(R.id.id_textView_week);
		weekTv3 = (TextView) view3.findViewById(R.id.id_textView_week);

		weekTv1.setText(WeatherUtil.matchWeek("1"));
		weekTv2.setText(WeatherUtil.matchWeek("2"));
		weekTv3.setText(WeatherUtil.matchWeek("3"));

		weather_imgIv1 = (ImageView) view1
				.findViewById(R.id.id_imageView_weatherImg);
		weather_imgIv2 = (ImageView) view2
				.findViewById(R.id.id_imageView_weatherImg);
		weather_imgIv3 = (ImageView) view3
				.findViewById(R.id.id_imageView_weatherImg);
		temperatureTv1 = (TextView) view1
				.findViewById(R.id.id_textView_temperature);
		temperatureTv2 = (TextView) view2
				.findViewById(R.id.id_textView_temperature);
		temperatureTv3 = (TextView) view3
				.findViewById(R.id.id_textView_temperature);

		climateTv1 = (TextView) view1.findViewById(R.id.id_textView_climate);
		climateTv2 = (TextView) view2.findViewById(R.id.id_textView_climate);
		climateTv3 = (TextView) view3.findViewById(R.id.id_textView_climate);

		windTv1 = (TextView) view1.findViewById(R.id.id_textView_wind);
		windTv2 = (TextView) view2.findViewById(R.id.id_textView_wind);
		windTv3 = (TextView) view3.findViewById(R.id.id_textView_wind);

		putTextViews(weekTvs, weekTv1, weekTv2, weekTv3);
		putTextViews(temperatureTvs, temperatureTv1, temperatureTv2,
				temperatureTv3);
		putTextViews(climateTvs, climateTv1, climateTv2, climateTv3);
		putTextViews(windTvs, windTv1, windTv2, windTv3);
		putImageViews(weather_imgIvs, weather_imgIv1, weather_imgIv2,
				weather_imgIv3);

		return view;
	}

	/**
	 * 更新天气
	 * 
	 * @param weatherForecastList
	 */
	public void updateWeather(List<LocalDayWeatherForecast> weatherForecastList) {
		int i = 1;
		if (null != weatherForecastList
				&& weatherForecastList.size() > (i + offIndex)) {
			for (i = 1; (i + offIndex) < weatherForecastList.size()
					&& i <= size; i++) {
				LocalDayWeatherForecast weatherForecast = weatherForecastList
						.get(i + offIndex);

				weekTvs[i - 1].setText(WeatherUtil.matchWeek(weatherForecast
						.getWeek()));

				String temp = "";
				String weather = "";
				String wind = "";
				String str = weatherForecast.getDayTemp();
				if (!isNull(str))
					temp = str + "°C";

				str = weatherForecast.getNightTemp();
				if (!isNull(str))
					temp += "/" + str + "°C";

				str = weatherForecast.getDayWeather();
				if (!isNull(str))
					weather = str;

				str = weatherForecast.getDayWindDirection();
				if (!isNull(str)) {
					if (!"无风向".equals(str) && !"旋转不定".equals(str))
						str = str + "风";
					wind = str + weatherForecast.getDayWindPower() + "级";
				}

				if (!isNull(temp))
					temperatureTvs[i - 1].setText(temp);
				if (!isNull(wind))
					windTvs[i - 1].setText(wind);
				if (!isNull(weather)) {
					climateTvs[i - 1].setText(weather);
					weather_imgIvs[i - 1].setImageResource(WeatherUtil
							.getWeatherIcon(weather));
				} else {
					weather_imgIvs[i - 1].setImageResource(WeatherUtil
							.getWeatherIcon("晴"));
				}
			}
		} else {
			for (i = 0; i < size; i++) {
				weather_imgIvs[i].setImageResource(R.drawable.ic_weather_na);
				climateTvs[i].setText("N/A");
				temperatureTvs[i].setText("N/A");
				windTvs[i].setText("N/A");
			}
		}
	}

	/**
	 * 更新星期
	 * 
	 * @param week
	 */
	public void updateWeek(String week) {
		int i = Integer.parseInt(week) + offIndex;
		weekTv1.setText(WeatherUtil.matchWeek(++i + ""));
		weekTv2.setText(WeatherUtil.matchWeek(++i + ""));
		weekTv3.setText(WeatherUtil.matchWeek(++i + ""));

	}

	/**
	 * 设置差值
	 * 
	 * @param off
	 */
	public void setOffIndex(int off) {
		this.offIndex = off;
	}

	/**
	 * 判断字符串是否为空串
	 * 
	 * @param str
	 * @return
	 */
	private boolean isNull(String str) {
		if (null != str && !"".equals(str.trim()) && !"N/A".equals(str.trim())) {
			return false;
		}
		return true;
	}

	/**
	 * 填充TextView数组
	 * 
	 * @param tvs
	 * @param tv
	 */
	private void putTextViews(TextView[] tvs, TextView... tv) {
		if (null == tvs)
			throw new NullPointerException();
		for (int i = 0; i < size; i++) {
			tvs[i] = tv[i];
		}
	}

	/**
	 * 填充ImageView数组
	 * 
	 * @param ivs
	 * @param iv
	 */
	private void putImageViews(ImageView[] ivs, ImageView... iv) {
		if (null == ivs)
			throw new NullPointerException();
		for (int i = 0; i < size; i++) {
			ivs[i] = iv[i];
		}
	}
}
