package com.imagine.go.view;

import static com.imagine.go.Constants.IS_DEBUG;
import static com.imagine.go.Constants.KEY_ITEM_LABEL_IMG;
import static com.imagine.go.Constants.KEY_ITEM_LABLE_NAME;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.balysv.materialmenu.MaterialMenuDrawable.AnimationState;
import com.balysv.materialmenu.MaterialMenuDrawable.IconState;
import com.balysv.materialmenu.MaterialMenuView;
import com.imagine.go.R;

/**
 * MaterialDrawerLayout:绑定MaterialMenu的DrawerLayout 并实现DrawerLayout中Item的业务逻辑
 * 
 * @author Jinhu
 * @date 2016/3/21
 */
public class MaterialDrawerLayout extends DrawerLayout implements
		DrawerListener, OnClickListener, OnItemClickListener {
	private static final String TAG = MaterialDrawerLayout.class
			.getSimpleName();

	/* 环境 . */
	private Context context;

	/* 标记侧滑栏状态 . */
	private boolean isDrawerOpened;
	/* 侧滑栏标签组件 . */
	private ListView mDrawerListView;

	/* Material标题栏 . */
	private View mtitlebar;

	/* 侧滑栏按钮 . */
	/* 水波纹效果 . */
	private RippleLayout mMoreBtn;
	/* 渐变效果 . */
	private MaterialMenuView mMaterialBtn;

	/**
	 * 列表视图点击事件回调接口
	 */
	private OnItemClickListener onItemClickListener;

	public interface OnItemClickListener {
		void onItemClick(AdapterView<?> parent, View view, int position, long id);

	}

	public MaterialDrawerLayout(Context context) {
		this(context, null);
	}

	public MaterialDrawerLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MaterialDrawerLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	/**
	 * 绑定标题栏
	 * 
	 * @param titlebar
	 */
	public void bindTitleBar(View titlebar) {
		this.mtitlebar = titlebar;
		initView();
		registerViewListener();

		if (IS_DEBUG) {
			Log.d(TAG, "---bindTitleBar()---");
		}
	}

	/**
	 * 初始化视图组件
	 */
	private void initView() {
		mMaterialBtn = (MaterialMenuView) mtitlebar
				.findViewById(R.id.id_materialmenu_btn);
		mMoreBtn = (RippleLayout) mtitlebar
				.findViewById(R.id.id_rippleLayout_titleBar_moreBtn);
		mDrawerListView = (ListView) findViewById(R.id.id_listView_drawer);

		// ----初始化侧滑栏标签----
		// 获取数据
		String[] items = getResources().getStringArray(
				R.array.slidebar_menu_array);
		int[] imgs = { R.drawable.ic_drawer_radius,
				R.drawable.ic_drawer_weather, R.drawable.ic_drawer_offlinemap,
				R.drawable.ic_drawer_sign };

		//
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < items.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(KEY_ITEM_LABLE_NAME, items[i]);
			map.put(KEY_ITEM_LABEL_IMG, imgs[i]);
			list.add(map);
		}

		// 生成适配器
		SimpleAdapter madpater = new SimpleAdapter(context, list,
				R.layout.item_drawer, new String[] { KEY_ITEM_LABLE_NAME,
						KEY_ITEM_LABEL_IMG }, new int[] {
						R.id.id_textView_drawer_item,
						R.id.id_imageView_drawer_item });
		mDrawerListView.setAdapter(madpater);

		if (IS_DEBUG) {
			Log.d(TAG, "---initView()---");
		}
	}

	/**
	 * 注册监听器
	 */
	private void registerViewListener() {
		this.setDrawerListener(this);
		mMoreBtn.setOnClickListener(this);
		mDrawerListView.setOnItemClickListener(this);
	}

	/**
	 * 回调接口
	 * 
	 * @param onItemClickListener
	 */
	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	// --------------- 响应事件---------------
	// ------注册侧滑栏滑动监听------
	@Override
	public void onDrawerStateChanged(int newState) {
		// 当侧滑栏完成关闭或打开时切换按钮形状
		if (DrawerLayout.STATE_IDLE == newState) {
			if (isDrawerOpened)
				mMaterialBtn.setState(IconState.ARROW);
			else
				mMaterialBtn.setState(IconState.BURGER);

		}
	}

	@Override
	public void onDrawerSlide(View paramView, float slideOffset) {
		// 动态切换按钮形状
		mMaterialBtn.setTransformationOffset(AnimationState.BURGER_ARROW,
				isDrawerOpened ? 2 - slideOffset : slideOffset);
		// 显示水波纹
		if (!mMoreBtn.isRippleAnimationRunning()) {
			mMoreBtn.showRipple();
		}
	}

	@Override
	public void onDrawerOpened(View paramView) {
		isDrawerOpened = true;
	}

	@Override
	public void onDrawerClosed(View paramView) {
		isDrawerOpened = false;
	}

	// ------注册侧滑栏滑动监听------

	/**
	 * 侧滑栏按钮 调出侧滑栏
	 */
	@Override
	public void onClick(View v) {
		mMoreBtn.showRipple();
		if (isDrawerOpened) {
			// 隐藏侧滑栏
			this.closeDrawer(mDrawerListView);
			isDrawerOpened = false;
		} else {
			// 显示侧滑栏
			this.openDrawer(mDrawerListView);
			isDrawerOpened = true;
		}
	}

	/**
	 * 列表视图点击响应 回调回Context
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (null == onItemClickListener)
			return;
		this.closeDrawer(mDrawerListView);
		onItemClickListener.onItemClick(parent, view, position, id);
	}

	/**
	 * 持续时间
	 * 
	 * @return
	 */
	public int getDuration() {
		return mMoreBtn.getAnimDuration();
	}
}
