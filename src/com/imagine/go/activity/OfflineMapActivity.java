package com.imagine.go.activity;

import static com.imagine.go.Constants.EVENT_ACTIVITY_FINISH_OFFLINEMAP;
import static com.imagine.go.Constants.EVENT_OFFLINEMAP_DOWNLOAD;
import static com.imagine.go.Constants.EVENT_OFFLINEMAP_INIT_ADPATERS;
import static com.imagine.go.Constants.EVENT_OFFLINEMAP_UPDATE_ADPATERS;
import static com.imagine.go.Constants.EVENT_OFFLINEMAP_UPDATE_RELATED_CITY_ADPATER;
import static com.imagine.go.Constants.IS_DEBUG;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.ListView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import com.balysv.materialmenu.MaterialMenuDrawable.IconState;
import com.balysv.materialmenu.MaterialMenuView;
import com.imagine.go.AppManager;
import com.imagine.go.R;
import com.imagine.go.adapter.OfflineMapAllCityListAdapter;
import com.imagine.go.adapter.OfflineMapCityListAdapter;
import com.imagine.go.adapter.OfflineMapDownloadCityListAdapter;
import com.imagine.go.adapter.OfflineMapHotRegionListAdapter;
import com.imagine.go.adapter.OfflineMapPagerAdapter;
import com.imagine.go.control.AOfflineMapManager;
import com.imagine.go.control.AOfflineMapManager.AOfflineMapDownloadListener;
import com.imagine.go.util.TimerUtil;
import com.imagine.go.view.IconEditText;
import com.imagine.go.view.IconEditText.OnIconClickedListener;
import com.imagine.go.view.RippleLayout;

/**
 * 离线地图层下载
 * 
 * @author Jinhu
 * @date 2016/5/16
 */
public class OfflineMapActivity extends OriginActivity implements
		OnClickListener, OnPageChangeListener, AOfflineMapDownloadListener,
		OnSweetClickListener {
	private static final String TAG = OfflineMapActivity.class.getSimpleName();

	// -------- 界面相关 --------
	/* 标题栏 . */
	private View mtitlebar;
	/* 退出按钮 . */
	private RippleLayout mBackBtn;
	/* 箭头. */
	private MaterialMenuView mMaterialBtn;
	/* 城市列表按钮 . */
	private View mCityListBtn;
	/* 下载管理按钮 . */
	private View mDownloadBtn;

	/* 内容分页 . */
	private ViewPager mContentPager;
	/* 离线地图分页适配器. */
	private PagerAdapter mPagerAdapter;

	/* 城市列表页 . */
	private View mCityView;
	/* 热门地区列表. */
	private ListView mHotRegionLView;
	/* 全部城市列表 . */
	private ExpandableListView mAllCityLView;
	/* 相关城市布局控件 . */
	private View mRelatedCityFrame;
	/* 相关城市列表 . */
	private ListView mReatedCityLView;

	/* 搜索栏. */
	private IconEditText mEditText;

	/* 下载管理页. */
	private View mDownloadView;
	/* 下载城市列表. */
	private ListView mDownloadCityLView;

	// -------- 业务相关 --------
	/* 离线地图管理组件 . */
	private AOfflineMapManager mOfflineMapMgr;

	/* 热门地区适配器. */
	private OfflineMapHotRegionListAdapter mHotRegionListAdapter;

	/* 全部城市适配器. */
	private OfflineMapAllCityListAdapter mAllCityListAdapter;

	/* 相关城市适配器 . */
	private OfflineMapCityListAdapter mRelatedCityListAdapter;

	/* 下载城市适配器. */
	private OfflineMapDownloadCityListAdapter mDownloadCityListApdater;

	// ------------------------ 初始化视图 ------------------------
	/**
	 * 初始化视图组件
	 */
	@SuppressLint("InflateParams")
	private void initView() {
		// ---初始化视图组件---
		// 初始化标题栏
		mtitlebar = findViewById(R.id.id_layout_titlebar);
		mBackBtn = (RippleLayout) mtitlebar
				.findViewById(R.id.id_rippleLayout_titleBar_backBtn);
		mMaterialBtn = (MaterialMenuView) mtitlebar
				.findViewById(R.id.id_materialmenu_btn);
		mMaterialBtn.setState(IconState.ARROW);
		mCityListBtn = mtitlebar.findViewById(R.id.id_textView_citylist);
		mDownloadBtn = mtitlebar.findViewById(R.id.id_textView_downloadM);

		// 初始化内容分页
		mContentPager = (ViewPager) findViewById(R.id.id_viewPager_content);
		mCityView = getLayoutInflater().inflate(
				R.layout.content_activity_offlinemap_allcity, null);
		mDownloadView = getLayoutInflater().inflate(
				R.layout.content_activity_offlinemap_download, null);
		mPagerAdapter = new OfflineMapPagerAdapter(mContentPager, mCityView,
				mDownloadView);
		mContentPager.setAdapter(mPagerAdapter);
		mContentPager.setCurrentItem(0);
		mContentPager.addOnPageChangeListener(this);

		mEditText = (IconEditText) mCityView
				.findViewById(R.id.id_editText_search);

		// 初始化城市列表页组件
		mHotRegionLView = (ListView) mCityView
				.findViewById(R.id.id_listView_hotRegion);
		mAllCityLView = (ExpandableListView) mCityView
				.findViewById(R.id.id_elistView_allCity);
		mAllCityLView.setChildDivider(ResourcesCompat.getDrawable(
				getResources(), R.drawable.divider_transparent, null));

		mRelatedCityFrame = mCityView.findViewById(R.id.id_layout_relatedCity);
		mReatedCityLView = (ListView) mRelatedCityFrame
				.findViewById(R.id.id_listView_relatedCity);

		// 初始化下载管理列表页组件
		mDownloadCityLView = (ListView) mDownloadView
				.findViewById(R.id.id_listView_downloadCity);

		registerViewListener();
	}

	/**
	 * 初始化视图监听器
	 */
	private void registerViewListener() {
		mBackBtn.setOnClickListener(this);
		mCityListBtn.setOnClickListener(this);
		mDownloadBtn.setOnClickListener(this);

		// ----监听搜索栏输入----
		mEditText.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (KeyEvent.KEYCODE_ENTER == keyCode) {
					// 用户输入完成
					String keyword = mEditText.getText().toString();
					if (null == keyword || "".equals(keyword))
						return false;

					mRelatedCityFrame.setVisibility(View.VISIBLE);
					mRelatedCityListAdapter
							.initCityList(searchOfflineMapCity(keyword));
					mHandler.sendEmptyMessage(EVENT_OFFLINEMAP_UPDATE_RELATED_CITY_ADPATER);
					return true;
				}
				return false;
			}
		});

		// 注册搜索栏图标触发响应
		mEditText.setOnIconClickedListener(new OnIconClickedListener() {

			@Override
			public void onVoiceStart() {
				mVoiceController
						.startListeningByDialog(OfflineMapActivity.this);
			}

			@Override
			public void onSearchStart() {
				String keyword = mEditText.getText().toString();
				if (null == keyword || "".equals(keyword))
					return;

				mRelatedCityFrame.setVisibility(View.VISIBLE);
				mRelatedCityListAdapter
						.initCityList(searchOfflineMapCity(keyword));
				mHandler.sendEmptyMessage(EVENT_OFFLINEMAP_UPDATE_RELATED_CITY_ADPATER);
			}
		});
	}

	/**
	 * 初始化适配器
	 */
	private void initApdater() {
		if (null == mOfflineMapMgr)
			return;
		// 初始化热门地区适配器
		mHotRegionListAdapter = new OfflineMapHotRegionListAdapter(this,
				mOfflineMapMgr);
		mHotRegionLView.setAdapter(mHotRegionListAdapter);

		// 初始化全部城市适配器
		mAllCityListAdapter = new OfflineMapAllCityListAdapter(this,
				mOfflineMapMgr, initProvinceAndCityList());
		mAllCityLView.setAdapter(mAllCityListAdapter);
		mAllCityLView.setOnGroupCollapseListener(mAllCityListAdapter);
		mAllCityLView.setOnGroupExpandListener(mAllCityListAdapter);
		mAllCityLView.setGroupIndicator(null);

		// 初始化相关城市适配器
		mRelatedCityListAdapter = new OfflineMapCityListAdapter(this,
				mOfflineMapMgr);
		mReatedCityLView.setAdapter(mRelatedCityListAdapter);

		// 初始化下载管理城市适配器
		mDownloadCityListApdater = new OfflineMapDownloadCityListAdapter(this,
				mOfflineMapMgr);
		mDownloadCityLView.setAdapter(mDownloadCityListApdater);

	}

	/**
	 * 初始化省份及城市数据
	 * 
	 * @return
	 */
	private List<OfflineMapProvince> initProvinceAndCityList() {
		if (null == mOfflineMapMgr)
			return null;

		List<OfflineMapProvince> proList = mOfflineMapMgr
				.getOfflineMapProvinceList();

		// 保存省份及城市数据
		List<OfflineMapProvince> mProvinces = new ArrayList<OfflineMapProvince>();

		// 保存直辖市
		ArrayList<OfflineMapCity> mMunicipalities = new ArrayList<OfflineMapCity>();
		// 保存港澳地区
		ArrayList<OfflineMapCity> mHKM = new ArrayList<OfflineMapCity>();
		mProvinces.add(null);
		mProvinces.add(null);

		for (int i = 0; i < proList.size(); i++) {
			OfflineMapProvince pro = proList.get(i);
			if (1 != pro.getCityList().size()) {
				// 普通省份
				mProvinces.add(i + 2, pro);
			} else {
				// 特殊地区
				String proName = pro.getProvinceName();
				if (proName.contains("香港")) {
					mHKM.addAll(pro.getCityList());
				} else if (proName.contains("澳门")) {
					mHKM.addAll(pro.getCityList());
				} else if (!proName.contains("全国概要图")) {
					mMunicipalities.addAll(pro.getCityList());
				}
			}
		}

		// 设置直辖市城市
		OfflineMapProvince mMuniPro = new OfflineMapProvince();
		mMuniPro.setProvinceName("直辖市");
		mMuniPro.setCityList(mMunicipalities);
		mProvinces.set(0, mMuniPro);

		// 设置港澳地区
		OfflineMapProvince mHKMPro = new OfflineMapProvince();
		mHKMPro.setProvinceName("港澳地区");
		mHKMPro.setCityList(mHKM);
		mProvinces.set(1, mHKMPro);

		return mProvinces;
	}

	/**
	 * 搜索关键字城市
	 * 
	 * @param keyWord
	 * @return
	 */
	private List<OfflineMapCity> searchOfflineMapCity(String keyWord) {
		if (null == keyWord || "".equals(keyWord.trim()))
			return null;
		ArrayList<OfflineMapCity> offlineCities = mOfflineMapMgr
				.getOfflineMapCityList();
		List<OfflineMapCity> mOfflineCities = new ArrayList<OfflineMapCity>();
		for (OfflineMapCity city : offlineCities) {
			if (city.getCity().contains(keyWord)) {
				mOfflineCities.add(city);
			}
		}

		return mOfflineCities;
	}

	// ------------------------ 生命周期 ------------------------
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 自定义标题栏
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_offlinemap);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.bar_title_offlinemap);

		// 初始化布局组件
		initView();

		// 初始化控制器
		mOfflineMapMgr = AOfflineMapManager.getInstance(this);
		mOfflineMapMgr.setAOfflineMapDownloadListener(this);

		// 初始化适配器
		mHandler.sendEmptyMessage(EVENT_OFFLINEMAP_INIT_ADPATERS);

		if (IS_DEBUG) {
			Log.d(TAG, "--OnCreated()--");
		}

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
		if (mRelatedCityFrame.isShown()) {
			// 隐藏相关城市列表
			mEditText.setText("");
			mRelatedCityFrame.setVisibility(View.GONE);
			return;
		}
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

		private WeakReference<OfflineMapActivity> mActivity;

		public MHandler(OfflineMapActivity mActivity) {
			this.mActivity = new WeakReference<OfflineMapActivity>(mActivity);
		}

		/**
		 * 处理消息
		 * 
		 * @param msg
		 */
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_ACTIVITY_FINISH_OFFLINEMAP:
				mActivity.get().onBackPressed();
				break;
			case EVENT_OFFLINEMAP_UPDATE_ADPATERS:
				mActivity.get().mHotRegionListAdapter.notifyDataSetChanged();
				mActivity.get().mAllCityListAdapter.notifyDataSetChanged();
				mActivity.get().mRelatedCityListAdapter.notifyDataSetChanged();
				mActivity.get().mDownloadCityListApdater.notifyDataChange();
				break;
			case EVENT_OFFLINEMAP_UPDATE_RELATED_CITY_ADPATER:
				mActivity.get().mRelatedCityListAdapter.notifyDataSetChanged();
				break;

			case EVENT_OFFLINEMAP_INIT_ADPATERS:
				mActivity.get().initApdater();
				break;

			}
		}
	}

	/**
	 * 改变分页内容
	 * 
	 * @param position
	 */
	private void changeContentPage(int position) {
		int paddingHorizontal = mCityListBtn.getPaddingLeft();
		int paddingVertical = mCityListBtn.getPaddingTop();

		switch (position) {
		case 0:
			mCityListBtn
					.setBackgroundResource(R.drawable.ic_offlinearrow_tab1_pressed);
			mDownloadBtn
					.setBackgroundResource(R.drawable.ic_offlinearrow_tab2_normal);
			break;
		case 1:
			mCityListBtn
					.setBackgroundResource(R.drawable.ic_offlinearrow_tab1_normal);
			mDownloadBtn
					.setBackgroundResource(R.drawable.ic_offlinearrow_tab2_pressed);
			break;
		}
		mCityListBtn.setPadding(paddingHorizontal, paddingVertical,
				paddingHorizontal, paddingVertical);
		mDownloadBtn.setPadding(paddingHorizontal, paddingVertical,
				paddingHorizontal, paddingVertical);

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
			TimerUtil.schedule(mHandler, EVENT_ACTIVITY_FINISH_OFFLINEMAP,
					mBackBtn.getAnimDuration()); // 结束Activity
			break;
		case R.id.id_textView_citylist:
			changeContentPage(0);
			mContentPager.setCurrentItem(0);
			break;
		case R.id.id_textView_downloadM:
			changeContentPage(1);
			mContentPager.setCurrentItem(1);
			break;

		case R.id.id_textView_update:
			try {
				mOfflineMapMgr.checkUpdateDowloadedCity();
			} catch (AMapException e1) {
				e1.printStackTrace();
			}
			break;
		case R.id.id_textView_download:
			// 非WIFI网络下进行下载判断
			if (!AppManager.getInstance().getWifiConnectedState()) {
				SweetAlertDialog dialog = SweetAlertDialog.buildConfirmDialog(
						this, EVENT_OFFLINEMAP_DOWNLOAD,
						"正处于非WIFI网络环境,请确认是否下载?", this);
				dialog.show();
				break;
			}

			try {
				mOfflineMapMgr.startDownloadInAllPause();
			} catch (AMapException e) {
				e.printStackTrace();
			}
			break;
		case R.id.id_textView_pause:
			mOfflineMapMgr.cancelDownloading();
			break;
		}
	}

	/**
	 * 对话框确认点击
	 */
	@Override
	public void onClick(SweetAlertDialog sweetAlertDialog) {
		sweetAlertDialog.dismiss();
		if (EVENT_OFFLINEMAP_DOWNLOAD == sweetAlertDialog.getId()) {
			try {
				mOfflineMapMgr.startDownloadInAllPause();
			} catch (AMapException e) {
				e.printStackTrace();

			}
		}

	}

	// ------------------------ 内容分页事件回调 ------------------------
	@Override
	public void onPageScrollStateChanged(int state) {

	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {
		changeContentPage(position);
	}

	/**
	 * 语音识别回调结果
	 */
	@Override
	public void onResult(String result) {
		super.onResult(result);
		mEditText.setText(result);
	}

	/**
	 * 离线地图下载回调
	 */
	@Override
	public void onDownload(int status, int completeCode, String downName) {
		mHandler.sendEmptyMessage(EVENT_OFFLINEMAP_UPDATE_ADPATERS);
	}

	/**
	 * 离线地图检测更新回调
	 */
	@Override
	public void onCheckUpdate(boolean hasNew, String name) {

	}

	/**
	 * 离线地图删除回调
	 */
	@Override
	public void onRemove(boolean success, String name, String describe) {

	}

}
