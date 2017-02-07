package com.imagine.go.activity;

import static com.imagine.go.Constants.EVENT_ACTIVITY_START_AR;
import static com.imagine.go.Constants.EVENT_ACTIVITY_START_MAP;
import static com.imagine.go.Constants.EVENT_ACTIVITY_START_OFFLINEMAP;
import static com.imagine.go.Constants.EVENT_ACTIVITY_START_USER_DEFINED_POINT;
import static com.imagine.go.Constants.EVENT_ACTIVITY_START_WEATHER;
import static com.imagine.go.Constants.EVENT_BOTTOM_DIALOG_SHOW;
import static com.imagine.go.Constants.EVENT_MGRIDVIEVW_ARISE;
import static com.imagine.go.Constants.EVENT_SCROLL_DOWN;
import static com.imagine.go.Constants.EVENT_SEARCH_POI;
import static com.imagine.go.Constants.EVENT_SEEKDIALOG_PUSH;
import static com.imagine.go.Constants.IS_DEBUG;
import static com.imagine.go.Constants.ITEM_LABEL_BANK;
import static com.imagine.go.Constants.ITEM_LABEL_BUS;
import static com.imagine.go.Constants.ITEM_LABEL_MARKET;
import static com.imagine.go.Constants.ITEM_LABEL_STORE;
import static com.imagine.go.Constants.ITEM_LABEL_VIEWSPOT;
import static com.imagine.go.Constants.ITEM_LABEL_WC;
import static com.imagine.go.Constants.ITEM_LABLE_ENTERTAINMENT;
import static com.imagine.go.Constants.ITEM_LABLE_FOOD;
import static com.imagine.go.Constants.ITEM_LABLE_HOTEL;
import static com.imagine.go.Constants.NO_RESULT;
import static com.imagine.go.Constants.NUM_ITEM_LABEL;
import static com.imagine.go.Constants.TIME_WAIT_EXIT;
import static com.imagine.go.Constants.TIME_WAIT_SCROLL_DOWN;
import static com.imagine.go.Constants.VALUE_ALPHA_INIT_MGRIDVIEW;
import static com.imagine.go.Constants.VALUE_POSITION_DRAWERITEM_OFFLINEMAP;
import static com.imagine.go.Constants.VALUE_POSITION_DRAWERITEM_RADIUS;
import static com.imagine.go.Constants.VALUE_POSITION_DRAWERITEM_USERPOINT;
import static com.imagine.go.Constants.VALUE_POSITION_DRAWERITEM_WEATHER;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ScrollView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener;

import com.amap.api.location.AMapLocation;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.imagine.go.AppManager;
import com.imagine.go.R;
import com.imagine.go.adapter.LabelAdapter;
import com.imagine.go.control.ALocationController;
import com.imagine.go.control.AMapQueryer;
import com.imagine.go.control.AMapQueryer.OnInputTipsQueryListener;
import com.imagine.go.control.APoiSearcher;
import com.imagine.go.control.APoiSearcher.APoiSearchListener;
import com.imagine.go.data.DatabaseManager;
import com.imagine.go.data.GeoPointDao;
import com.imagine.go.model.GeoPoint;
import com.imagine.go.model.LabelModel;
import com.imagine.go.util.CollectedLabelManager;
import com.imagine.go.util.PoiTypeMatcher;
import com.imagine.go.util.TimerUtil;
import com.imagine.go.util.ToastUtil;
import com.imagine.go.view.BottomBtnDialog;
import com.imagine.go.view.GeoPointInfoDialog;
import com.imagine.go.view.GeoPointInfoDialog.OnInputConfirmListener;
import com.imagine.go.view.IconEditText;
import com.imagine.go.view.IconEditText.OnIconClickedListener;
import com.imagine.go.view.MGridView;
import com.imagine.go.view.MaterialDrawerLayout;
import com.imagine.go.view.RippleLayout;

/**
 * MainActivity:首页
 * 
 * @author Jinhu
 * @date 2016/3/17 .
 */
public class MainActivity extends OriginActivity implements OnClickListener,
		OnSweetClickListener, APoiSearchListener, OnInputTipsQueryListener,
		OnInputConfirmListener {
	private static final String TAG = MainActivity.class.getSimpleName();

	// -------- 界面相关 --------
	/* 搜索标签网格.. */
	private MGridView mGridView;
	/* 搜索标签适配器.. */
	private LabelAdapter mLabelApdater;
	/* 标签集合.. */
	private List<LabelModel> mLabelList;
	/* 搜索标签网格 滚动视图 . */
	private ScrollView mScrollView;

	/* 搜索栏.. */
	private IconEditText mEditText;

	/* 工具栏 . */
	/* 地图层按钮.. */
	private RippleLayout mMapBtn;
	/* 实景层按钮 . */
	private RippleLayout mCameraBtn;
	/* 添加搜索标签 . */
	private ImageView mAddBtn;

	/* 侧滑标题栏 . */
	/* 已封装侧滑栏及标题栏逻辑 . */
	private MaterialDrawerLayout mDrawer;

	/* 进度条对话框 . */
	private SweetAlertDialog mProgressDialog;

	/* 底部对话框 . */
	private BottomBtnDialog mBottomBtnDialog;

	/* 地理信息点输入对空框 . */
	private GeoPointInfoDialog mGeoPointInfoDialog;

	// -------- 业务相关 --------

	/* 退出程序判定. */
	private AtomicBoolean isExit = new AtomicBoolean(false);

	/* Poi点搜索. */
	private APoiSearcher mPoiSearcher;

	/* 高德搜索组件 . */
	private AMapQueryer mMapQueryer;

	/* 数据操作锁 . */
	private AtomicBoolean mAffairLock = new AtomicBoolean(false);

	// ------------------------ 初始化视图 ------------------------
	/**
	 * 初始化视图组件
	 */
	private void initView() {
		// ---初始化视图组件---
		// 搜索标签
		mGridView = (MGridView) findViewById(R.id.id_mgridView_nineGridView);
		mScrollView = (ScrollView) findViewById(R.id.id_scrollView_nineGridView);

		// 搜索栏
		mEditText = (IconEditText) findViewById(R.id.id_editText_search);

		// 工具栏
		mMapBtn = (RippleLayout) findViewById(R.id.id_rippleLayout_toolBar_mapBtn);
		mCameraBtn = (RippleLayout) findViewById(R.id.id_rippleLayout_toolBar_cameraBtn);
		mAddBtn = (ImageView) findViewById(R.id.id_imageView_addBtn);

		// 侧滑标题栏
		mDrawer = (MaterialDrawerLayout) findViewById(R.id.id_layout_drawer);
		mDrawer.bindTitleBar(findViewById(R.id.id_layout_titlebar));

		// 初始化对话框
		mProgressDialog = new SweetAlertDialog(this,
				SweetAlertDialog.PROGRESS_TYPE);

		mBottomBtnDialog = new BottomBtnDialog(this);
		mGeoPointInfoDialog = new GeoPointInfoDialog(this);

		// ---初始化视图适配器---
		initViewApdater();

		// ---注册视图监听器---
		registerViewListener();

	}

	/**
	 * 初始化视图适配器
	 */
	private void initViewApdater() {
		// ----初始化搜索标签----
		// 获取标签数据
		String[] item_name = { ITEM_LABLE_FOOD, ITEM_LABLE_HOTEL,
				ITEM_LABEL_BUS, ITEM_LABEL_BANK, ITEM_LABLE_ENTERTAINMENT,
				ITEM_LABEL_MARKET, ITEM_LABEL_VIEWSPOT, ITEM_LABEL_STORE,
				ITEM_LABEL_WC };
		int[] item_image = { R.drawable.ic_main_food, R.drawable.ic_main_hotel,
				R.drawable.ic_main_bus, R.drawable.ic_main_bank,
				R.drawable.ic_main_ktv, R.drawable.ic_main_market,
				R.drawable.ic_main_viewspot, R.drawable.ic_main_store,
				R.drawable.ic_main_wc };

		// 添加Label数据
		mLabelList = new ArrayList<LabelModel>();
		for (int i = 0; i < NUM_ITEM_LABEL; i++) {
			mLabelList.add(new LabelModel(item_image[i], item_name[i]));
		}
		// 获取已收藏的搜索标签
		int collectedLabelNums = CollectedLabelManager.getInstance(this)
				.getLabelNums();
		if (0 < collectedLabelNums) {
			for (int i = 1; i <= collectedLabelNums; i++) {
				String labelName = CollectedLabelManager.getInstance(this)
						.load(i);
				mLabelList.add(new LabelModel(R.drawable.ic_main_like,
						labelName));
			}
		}

		// 生成适配器
		mLabelApdater = new LabelAdapter(this, LabelAdapter.unpack(mLabelList));
		mGridView.setAdapter(mLabelApdater);

		// 无定位信息,标签网格不能使用
		if (null == mLocationPoint.getLatLng()) {
			mGridView.setAlpha(VALUE_ALPHA_INIT_MGRIDVIEW);
			mGridView.setEnabled(false);
			// 同时搜索栏失活
			mEditText.setEnabled(false);
			// 跳转按钮失活
			mMapBtn.setEnabled(false);
			mCameraBtn.setEnabled(false);
		}
	}

	/**
	 * 初始化视图监听器
	 */
	private void registerViewListener() {
		// ------注册监听器-------
		mMapBtn.setOnClickListener(this);
		mCameraBtn.setOnClickListener(this);
		mAddBtn.setOnClickListener(this);
		mGeoPointInfoDialog.setOnInputConfirmListener(this);

		// ----搜索标签视图触发响应----
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (null == mALocation)
					return;
				String labelName = mLabelList.get(position).getName().trim();
				String poiType = PoiTypeMatcher.getPoiType(labelName);

				mProgressDialog.show();
				// 清空之前PoiResult
				mPoiSearchData.clearPois();
				// 搜索Poi
				if (null != poiType) {
					// 通过分类搜索Poi
					mPoiSearcher.searchNearbyType(mALocation, poiType,
							mPoiSearchData.getRadius());
				} else {
					// 通过关键字搜索Poi
					mPoiSearcher.searchNearbyKeyword(mALocation, labelName,
							mPoiSearchData.getRadius());
				}
			}
		});

		// ----监听搜索栏输入----
		mEditText.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (KeyEvent.KEYCODE_ENTER == keyCode) {
					// 用户输入完成
					String keyword = mEditText.getText().toString();
					mEditText.setText("");
					if (null == keyword || "".equals(keyword))
						return false;

					mProgressDialog.show();
					// 清空之前PoiResult
					mPoiSearchData.clearPois();
					// 关键字搜索Poi
					mPoiSearcher.searchNearbyKeyword(mALocation, keyword,
							mPoiSearchData.getRadius());
					return true;
				}
				return false;
			}
		});

		// 注册搜索栏图标触发响应
		mEditText.setOnIconClickedListener(new OnIconClickedListener() {

			@Override
			public void onVoiceStart() {
				mVoiceController.startListeningByDialog(MainActivity.this);
			}

			@Override
			public void onSearchStart() {
				String keyword = mEditText.getText().toString();
				if (null == keyword || "".equals(keyword))
					return;

				mProgressDialog.show();
				// 清空之前PoiResult
				mPoiSearchData.clearPois();
				// 关键字搜索Poi
				mPoiSearcher.searchNearbyKeyword(mALocation, keyword,
						mPoiSearchData.getRadius());
				mEditText.setText("");
			}
		});

		// 搜索栏文本改变监听
		mEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String mText = s.toString().trim();
				if (null == mText || 0 == mText.length())
					return;
				// 进行Poi提示字搜索
				mMapQueryer.searchPoiInputTips(mText, mLocationPoint.getCity());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		// ----监听软键盘隐藏与出现----
		mDrawer.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						int heightDiff = mDrawer.getRootView().getHeight()
								- mDrawer.getHeight();
						// -软键盘隐藏与出现-
						if (heightDiff > 100) {
							// 无论搜索栏是否有文本,都清空
							mEditText.setText("");
						}
					}
				});

		// ----侧滑栏列表视图触发响应----
		mDrawer.setOnItemClickListener(new com.imagine.go.view.MaterialDrawerLayout.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				// 设置搜索半径
				case VALUE_POSITION_DRAWERITEM_RADIUS:
					// 延迟弹出拖动条对话框
					TimerUtil.schedule(mHandler, EVENT_SEEKDIALOG_PUSH,
							mDrawer.getDuration());
					break;
				case VALUE_POSITION_DRAWERITEM_WEATHER:
					// 启动WeatherActivity
					TimerUtil.schedule(mHandler, EVENT_ACTIVITY_START_WEATHER,
							mDrawer.getDuration());
					break;
				case VALUE_POSITION_DRAWERITEM_OFFLINEMAP:
					// 启动OfflineMapActivity
					TimerUtil.schedule(mHandler,
							EVENT_ACTIVITY_START_OFFLINEMAP,
							mDrawer.getDuration());
					break;
				case VALUE_POSITION_DRAWERITEM_USERPOINT:
					TimerUtil.schedule(mHandler, EVENT_BOTTOM_DIALOG_SHOW,
							mDrawer.getDuration());
					break;
				}
			}
		});
	}

	// ------------------------ 生命周期 ------------------------
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 自定义标题栏
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.bar_title);

		// 初始化布局组件
		initView();

		// Poi搜索
		mPoiSearcher = new APoiSearcher(getApplicationContext());
		mPoiSearcher.setAPoiSearchListener(this);

		// Poi提示字搜索
		mMapQueryer = new AMapQueryer(getApplicationContext());
		mMapQueryer.setOnInputTipsQueryListener(this);
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
		mAffairLock.set(false);
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
		if (isExit.compareAndSet(false, true)) {
			// 询问是否退出程序
			ToastUtil.showShort("再按一次退出");
			// 待2.5s后再按退出程序
			Timer exitTimer = new Timer(true);
			exitTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					isExit.set(false);
				}
			}, TIME_WAIT_EXIT);
		} else {
			// 退出程序
			AppManager.getInstance().exit();
		}

		if (IS_DEBUG) {
			Log.d(TAG, "--OnBackPressed()--");
		}
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

		private WeakReference<MainActivity> mActivity;

		public MHandler(MainActivity mActivity) {
			this.mActivity = new WeakReference<MainActivity>(mActivity);
		}

		/**
		 * 处理消息
		 * 
		 * @param msg
		 */
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_SCROLL_DOWN:
				// 滚动视图下拉
				mActivity.get().mScrollView.fullScroll(View.FOCUS_DOWN);
				break;

			case EVENT_MGRIDVIEVW_ARISE:
				// 标签网格视图显示
				Animation mAlphaAnim = new AlphaAnimation(
						VALUE_ALPHA_INIT_MGRIDVIEW, 1.0f);
				mAlphaAnim.setDuration(2000);
				mActivity.get().mGridView.startAnimation(mAlphaAnim);
				mActivity.get().mGridView.setAlpha(1.0f);
				mActivity.get().mGridView.setEnabled(true);
				// 同时激活搜索栏
				mActivity.get().mEditText.setEnabled(true);
				// 激活跳转Activity按钮
				mActivity.get().mMapBtn.setEnabled(true);
				mActivity.get().mCameraBtn.setEnabled(true);
				break;

			case EVENT_SEARCH_POI:
				// 搜索Poi结果处理
				List<PoiItem> pois = mActivity.get().mPoiSearchData.getPois();
				if (null == pois || 0 == pois.size()) {
					ToastUtil.showShort(NO_RESULT);
					break;
				}
				// 直接启动MixActivity
			case EVENT_ACTIVITY_START_AR:
				// 启动ARActivity
				Intent arIntent = new Intent(mActivity.get()
						.getApplicationContext(), MixActivity.class);
				mActivity.get().startActivity(arIntent);
				break;

			case EVENT_ACTIVITY_START_MAP:
				// 启动MapActivity
				mActivity.get().mPoiSearchData.clearPois();
				Intent mapIntent = new Intent(mActivity.get()
						.getApplicationContext(), MapActivity.class);
				mActivity.get().startActivity(mapIntent);
				break;

			case EVENT_ACTIVITY_START_WEATHER:
				// 启动WeatherActivity
				Intent weatherIntent = new Intent(mActivity.get()
						.getApplicationContext(), WeatherActivity.class);
				mActivity.get().startActivity(weatherIntent);
				break;

			case EVENT_ACTIVITY_START_OFFLINEMAP:
				// 启动OfflineMapActivity
				Intent offlineMapIntent = new Intent(mActivity.get()
						.getApplicationContext(), OfflineMapActivity.class);
				mActivity.get().startActivity(offlineMapIntent);
				break;

			case EVENT_SEEKDIALOG_PUSH:
				// 弹出拖拉条对话框 设置搜素半径
				SweetAlertDialog dialog = new SweetAlertDialog(mActivity.get(),
						SweetAlertDialog.SEEK_TYPE);
				dialog.setConfirmClickListener(mActivity.get());
				dialog.show();
				dialog.setProgress(mActivity.get().mPoiSearchData.getRadius());
				break;

			case EVENT_BOTTOM_DIALOG_SHOW:
				mActivity.get().mBottomBtnDialog.show();
				mActivity.get().mBottomBtnDialog.setItem1Txt("当前位置");
				mActivity.get().mBottomBtnDialog.setItem2Txt("地图选点");
				mActivity.get().mBottomBtnDialog.setOnClickListener(mActivity
						.get());
				break;
			case EVENT_ACTIVITY_START_USER_DEFINED_POINT:
				Intent geoPointSignIntent = new Intent(mActivity.get()
						.getApplicationContext(),
						UserDefinedPointActivity.class);
				mActivity.get().startActivity(geoPointSignIntent);
				break;
			}
		}
	}

	// ------------------------ 响应事件 ------------------------
	/**
	 * 主界面里按钮点击响应
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_rippleLayout_toolBar_mapBtn:
			// 水波纹动画结束后跳转至MapActivity
			mMapBtn.showRipple();
			TimerUtil.schedule(mHandler, EVENT_ACTIVITY_START_MAP,
					mMapBtn.getAnimDuration());
			break;
		case R.id.id_rippleLayout_toolBar_cameraBtn:
			mCameraBtn.showRipple();
			TimerUtil.schedule(mHandler, EVENT_ACTIVITY_START_AR,
					mMapBtn.getAnimDuration());
			break;

		case R.id.id_imageView_addBtn:
			// 添加搜索标签 弹出输入对话框
			SweetAlertDialog mInputDialog = new SweetAlertDialog(this,
					SweetAlertDialog.INPUT_TYPE);
			mInputDialog.setConfirmClickListener(this);
			mInputDialog.show();
			break;
		case R.id.id_textView_item1:
			// 标记当前位置信息
			mBottomBtnDialog.dismiss();
			mGeoPointInfoDialog.show();
			break;
		case R.id.id_textView_item2:
			mBottomBtnDialog.dismiss();
			TimerUtil.schedule(mHandler,
					EVENT_ACTIVITY_START_USER_DEFINED_POINT,
					mMapBtn.getAnimDuration());
			break;
		}
	}

	/**
	 * 对话框确认按钮响应
	 */
	@Override
	public void onClick(SweetAlertDialog sweetAlertDialog) {
		int dialogType = sweetAlertDialog.getAlerType();
		switch (dialogType) {
		case SweetAlertDialog.INPUT_TYPE:
			// --- 输入对话框 ---
			// 添加新搜索标签
			String inputText = sweetAlertDialog.getInputText();
			if (null != inputText && !"".equals(inputText)) {
				ToastUtil.showShort("已收藏标签");
				// 适配器添加新标签
				LabelModel newLabelModel = new LabelModel(
						R.drawable.ic_main_like, inputText);
				mLabelList.add(newLabelModel);
				mLabelApdater.addLabelModel(newLabelModel);
				mLabelApdater.notifyDataSetChanged();

				// 保存已收藏的搜索标签至本地
				CollectedLabelManager.getInstance(this).store(inputText);

				// 滚动视图下拉 显示新标签
				TimerUtil.schedule(mHandler, EVENT_SCROLL_DOWN,
						TIME_WAIT_SCROLL_DOWN);
			}
			break;
		case SweetAlertDialog.SEEK_TYPE:
			// 拖动条对话框
			// 设置搜索半径
			mPoiSearchData.setRadius(sweetAlertDialog.getRadius());
			break;
		}
		sweetAlertDialog.dismiss();
	}

	/**
	 * 定位信息更新回调
	 */
	@Override
	public void onLocationSucceeded(AMapLocation amapLocation) {
		super.onLocationSucceeded(amapLocation);
		// 去掉省会级的地址信息
		String address = mLocationPoint.getAddress();
		if (null != address) {
			if (address.length() >= 3)
				address = address.substring(3);
		} else {
			address = "";
		}

		if (ALocationController.Is_Frist_Locate
				&& AppManager.getInstance().getNetConnectedState()) {
			// 显示标签网格
			if (!mGridView.isEnabled())
				mHandler.sendEmptyMessage(EVENT_MGRIDVIEVW_ARISE);
			ToastUtil.showShort("当前位置:" + address);
			ALocationController.Is_Frist_Locate = false;
		}
	}

	/**
	 * Poi点搜索信息更新回调
	 */
	@Override
	public void onPoiSearched(PoiResult result) {
		final PoiResult fresult = result;

		// 延迟500ms再隐藏进度对话框
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				mProgressDialog.dismiss();

				if (null == fresult) {
					return;
				}
				mPoiSearchData.setPois(fresult.getPois());
				// 处理Poi搜索
				mHandler.sendEmptyMessage(EVENT_SEARCH_POI);
			}
		}, 500);
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
	 * Poi提示字搜索回调
	 */
	@Override
	public void onGetInputtips(List<String> nameList) {
		if (null == nameList || 0 == nameList.size())
			return;
		ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(this,
				R.layout.item_poi_tip, nameList);
		mEditText.setAdapter(aAdapter);
		aAdapter.notifyDataSetChanged();
	}

	/**
	 * 标记地点输入备注信息确认回调
	 */
	@Override
	public synchronized void onInputeConfirm(String inputTxt) {
		if (null == inputTxt)
			return;

		if (mAffairLock.get())
			return;
		mProgressDialog.show();
		mAffairLock.set(true);

		final GeoPoint point = mLocationPoint.clone();
		point.setName(inputTxt);

		new Timer(true).schedule(new TimerTask() {
			@Override
			public void run() {
				GeoPointDao mGeoPointDao = DatabaseManager.getInstance(
						MainActivity.this).getGeoPointDao(); // 数据库已在AppManager层打开
				mGeoPointDao.addGeoPoint(point); // 向数据库提交当前位置点

				mProgressDialog.dismiss();

				mAffairLock.set(false);
			}
		}, 500);
	}

}
