package com.imagine.go.activity;

import static com.imagine.go.Constants.EVENT_ACTIVITY_FINISH_WEATHER;
import static com.imagine.go.Constants.IS_DEBUG;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.services.weather.LocalDayWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecast;
import com.amap.api.services.weather.LocalWeatherLive;
import com.balysv.materialmenu.MaterialMenuDrawable.IconState;
import com.balysv.materialmenu.MaterialMenuView;
import com.imagine.go.AppManager;
import com.imagine.go.R;
import com.imagine.go.adapter.WeatherPagerAdapter;
import com.imagine.go.control.AMapQueryer;
import com.imagine.go.control.AMapQueryer.OnWeatherQueryListener;
import com.imagine.go.util.AnimationFactory;
import com.imagine.go.util.TimerUtil;
import com.imagine.go.util.WeatherUtil;
import com.imagine.go.view.CirclePageIndicator;
import com.imagine.go.view.RippleLayout;
import com.imagine.go.view.WeatherFragment;

/**
 * WeatherActivity <br/>
 * 天气预报层
 * 
 * @author Jinhu
 * @date 2016/5/10
 */
public class WeatherActivity extends OriginActivity implements OnClickListener,
		OnWeatherQueryListener {
	private static final String TAG = WeatherActivity.class.getSimpleName();

	// -------- 界面相关 --------
	/* 标题栏 . */
	private View mtitlebar;
	/* 退出按钮 . */
	private RippleLayout mBackBtn;
	/* 箭头. */
	private MaterialMenuView mMaterialBtn;
	/* 更新状态按钮 . */
	private View mUpdateBtn;

	/* 当前城市 . */
	private TextView mCityText;
	/* 预报发布时间 . */
	private TextView mTimeText;
	/* 当前湿度 . */
	private TextView mHumidityText;

	/* 当日天气情况 . */
	private ImageView mWeatherImg;
	/* 当日周几. */
	private TextView mWeekText;
	/* 摄氏度 . */
	private TextView mTemperatureText;
	/* 气候 . */
	private TextView mClimateText;
	/* 风力 . */
	private TextView mWindText;

	/* 天气预测子页组件 . */
	private ViewPager mWForecastViewPager;

	/* 播放按钮 . */
	private RippleLayout mPlayBtn;

	// -------- 业务相关 --------

	/* 当前天气实况. */
	private StringBuffer mWeatherLive;

	/* 高德搜索组件 . */
	private AMapQueryer mMapQueryer;

	/* 天气预测子页适配器 . */
	private WeatherPagerAdapter mWForecastPagerAdapter;
	/* 天气预测子页内容 . */
	private List<WeatherFragment> mWForecastFragments;
	/* 子页 . */
	private WeatherFragment mFristWeatherFragment;
	private WeatherFragment mSecondWeatherFragment;

	// ------------------------ 初始化视图 ------------------------
	/**
	 * 初始化视图组件
	 */
	private void initView() {
		// 初始化标题栏
		mtitlebar = findViewById(R.id.id_layout_titlebar);
		mBackBtn = (RippleLayout) mtitlebar
				.findViewById(R.id.id_rippleLayout_titleBar_backBtn);
		mUpdateBtn = mtitlebar.findViewById(R.id.id_imageView_updateBtn);
		mMaterialBtn = (MaterialMenuView) mtitlebar
				.findViewById(R.id.id_materialmenu_btn);
		mMaterialBtn.setState(IconState.ARROW);

		// 初始主界面
		mCityText = (TextView) findViewById(R.id.id_textView_city);
		mCityText.setText(mLocationPoint.getCity());
		mTimeText = (TextView) findViewById(R.id.id_textView_time);
		mHumidityText = (TextView) findViewById(R.id.id_textView_humidity);

		// 实时天气组件
		mWeatherImg = (ImageView) findViewById(R.id.id_imageView_weatherImg);
		mWeekText = (TextView) findViewById(R.id.id_textView_week);
		mTemperatureText = (TextView) findViewById(R.id.id_textView_temperature);
		mClimateText = (TextView) findViewById(R.id.id_textView_climate);
		mWindText = (TextView) findViewById(R.id.id_textView_wind);

		// 预测天气组件
		mWForecastViewPager = (ViewPager) findViewById(R.id.id_viewpager_weather);
		mWForecastFragments = new ArrayList<WeatherFragment>();
		mFristWeatherFragment = new WeatherFragment();
		mSecondWeatherFragment = new WeatherFragment();
		mSecondWeatherFragment.setOffIndex(3);
		mWForecastFragments.add(mFristWeatherFragment);
		mWForecastFragments.add(mSecondWeatherFragment);
		mWForecastPagerAdapter = new WeatherPagerAdapter(
				getSupportFragmentManager(), mWForecastFragments);
		mWForecastViewPager.setAdapter(mWForecastPagerAdapter);
		((CirclePageIndicator) findViewById(R.id.id_indicator_weather))
				.setViewPager(mWForecastViewPager);

		mPlayBtn = (RippleLayout) findViewById(R.id.id_rippleLayout_playBtn);
		mPlayBtn.setEnabled(false);

		// ---注册视图监听器---
		registerViewListener();
	}

	/**
	 * 初始化视图监听器
	 */
	private void registerViewListener() {
		mBackBtn.setOnClickListener(this);
		mUpdateBtn.setOnClickListener(this);
		mPlayBtn.setOnClickListener(this);
	}

	/**
	 * 更新今天天气信息
	 */
	private void updateTodayWeatherInfo(LocalWeatherLive weatherLive,
			LocalDayWeatherForecast weatherForecast) {
		// 更新实况信息
		if (null != weatherLive) {
			// 解析信息预测时间
			String reportTime = weatherLive.getReportTime();
			Pattern pattern = Pattern.compile("\\d{1,2}:\\d{1,2}");
			Matcher matcher = pattern.matcher(reportTime);
			if (matcher.find()) {
				reportTime = matcher.group();
			}
			mTimeText.setText("今天" + reportTime + "发布");

			// 解析湿度
			String humidity = "湿度:" + weatherLive.getHumidity() + "%";
			mHumidityText.setText(humidity);

			String temp = "";
			String weather = "";
			String wind = "";
			String str = weatherLive.getTemperature();
			if (!isNull(str))
				temp = str + "°C";

			str = weatherLive.getWeather();
			if (!isNull(str)) {
				weather = str;
			}

			str = weatherLive.getWindDirection();
			if (!isNull(str)) {
				if (!"无风向".equals(str) && !"旋转不定".equals(str))
					str = str + "风";
				wind = str + weatherLive.getWindPower() + "级";
			}

			if (!isNull(temp))
				mTemperatureText.setText(temp);
			if (!isNull(wind))
				mWindText.setText(wind);
			if (!isNull(weather)) {
				mClimateText.setText(weather);
				mWeatherImg.setImageResource(WeatherUtil
						.getWeatherIcon(weather));
			} else {
				mWeatherImg.setImageResource(WeatherUtil.getWeatherIcon("晴"));
			}

			// 天气播报
			weather = weatherLive.getWeather();
			temp = weatherLive.getTemperature() + "摄氏度";
			wind = weatherLive.getWindDirection() + "风" + ",风力"
					+ weatherLive.getWindPower() + "级";

			mWeatherLive = new StringBuffer();
			mWeatherLive.append("实况天气播报:" + mLocationPoint.getCity() + "。"
					+ weather + "。" + temp + "." + wind + "。湿度"
					+ weatherLive.getHumidity() + "%");
			mPlayBtn.setEnabled(true);
		}

		// 天气预测
		if (null != weatherForecast) {
			String week = WeatherUtil.matchWeek(weatherForecast.getWeek());
			mWeekText.setText(week);
			mFristWeatherFragment.updateWeek(weatherForecast.getWeek());
			mSecondWeatherFragment.updateWeek(weatherForecast.getWeek());
		}
	}

	// ------------------------ 生命周期 ------------------------
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_weather);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.bar_title_weather);

		// 初始化布局组件
		initView();

		// 搜素组件
		mMapQueryer = new AMapQueryer(this);
		mMapQueryer.setOnWeatherQueryListener(this);
		mMapQueryer.searchWeather(mLocationPoint.getCity());

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
		if (IS_DEBUG) {
			Log.d(TAG, "--OnResumed()--");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
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
		if (IS_DEBUG) {
			Log.d(TAG, "--OnDestroyed()--");
		}
	}

	@Override
	public void onBackPressed() {
		AppManager.getInstance().delActivity(this);
		this.finish();
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

		private WeakReference<WeatherActivity> mActivity;

		public MHandler(WeatherActivity mActivity) {
			this.mActivity = new WeakReference<WeatherActivity>(mActivity);
		}

		/**
		 * 处理消息
		 * 
		 * @param msg
		 */
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_ACTIVITY_FINISH_WEATHER:
				mActivity.get().onBackPressed();
				break;
			}
		}
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

	// ------------------------ 响应事件 ------------------------
	/**
	 * 界面里按钮点击响应
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_rippleLayout_titleBar_backBtn:
			mBackBtn.showRipple();
			TimerUtil.schedule(mHandler, EVENT_ACTIVITY_FINISH_WEATHER,
					mBackBtn.getAnimDuration()); // 结束Activity
			break;

		case R.id.id_imageView_updateBtn:
			Animation anim = AnimationFactory.rotateAnimation(0f, 360f, 1000);
			anim.setFillAfter(false);
			mUpdateBtn.startAnimation(anim); // 更新天气信息
			mMapQueryer.searchWeather(mLocationPoint.getCity());
			break;

		case R.id.id_rippleLayout_playBtn:
			mPlayBtn.showRipple(); // 天气播报
			mVoiceController.startSpeaking(mWeatherLive.toString());
			break;
		}
	}

	/**
	 * 实况天气信息回调
	 */
	@Override
	public void onWeatherLiveSearched(LocalWeatherLive weatherLive) {
		if (null == weatherLive)
			return;
		updateTodayWeatherInfo(weatherLive, null);
	}

	/**
	 * 预报天气信息回调
	 */
	@Override
	public void onWeatherForecastSearched(LocalWeatherForecast weatherForecast) {
		if (null == weatherForecast)
			return;
		List<LocalDayWeatherForecast> weatherForecastList = weatherForecast
				.getWeatherForecast();
		updateTodayWeatherInfo(null, weatherForecastList.get(0));
		mFristWeatherFragment.updateWeather(weatherForecastList);
		mSecondWeatherFragment.updateWeather(weatherForecastList);
	}

}
