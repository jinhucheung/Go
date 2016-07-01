package com.imagine.go.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.imagine.go.R;
import com.imagine.go.util.AnimationFactory;

/**
 * ArcMenu:弧度菜单
 * 
 * @author Jinhu
 * @date 2015/9/16
 */
public class ArcMenu extends ViewGroup implements OnClickListener {

	/* 定位组件. */
	private static final int LEFT_TOP = 0;
	private static final int LEFT_BOTTOM = 1;
	private static final int RIGHT_TOP = 2;
	private static final int RIGHT_BOTTOM = 3;
	private static final int CENTER_BOTTOM = 4;

	/* 组件当前的位置. */
	private Position position;
	/* 当前菜单具体的位置. */
	private int is_leftmenu;
	private int is_topmenu;

	/* 弧度的半径. */
	private int radius;

	/* 组件当前状态. */
	private Status status;

	/* 主按钮. */
	private View mainButton;
	/* 主按钮位置. */
	private int mainButtonX;
	private int mainButtonY;
	/* 主按钮切换图片. */
	private Drawable mMButtonOpenIcon;
	private Drawable mMButtonCloseIcon;

	/* 子按钮位置. */
	private List<int[]> childButtonXY = new ArrayList<int[]>();

	/* 底部距离. */
	private int mainButton_layout_marginBottom;
	/* 子按钮对父控件的左右间距. */
	private int childButton_layout_padding_LeftAndRight;
	/* 子按钮对父控件的上下间距. */
	private int childButton_layout_padding_TopAndBottom;
	/* 子按钮间距. */
	private int childButton_gap;

	/* 回调接口. */
	private OnMenuItemClickListener onMenuItemClickListener;

	/**
	 * Status:控件状态
	 * 
	 * @date 2015/9/20
	 */
	public enum Status {
		OPEN, CLOSE;
	}

	/**
	 * OnMenuItemClickedListener:菜单项点击回调接口
	 * 
	 * @date 2015/9/20
	 */
	public interface OnMenuItemClickListener {
		void onMenuItemClicked(View v, int pos);
	}

	/**
	 * 位置相关
	 */
	public enum Position {
		LEFT_TOP, LEFT_BOTTOM, RIGHT_TOP, RIGHT_BOTTOM, CENTER_BOTTOM;
	}

	public ArcMenu(Context context) {
		this(context, null);
	}

	public ArcMenu(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ArcMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initData(context, attrs);
	}

	/**
	 * 获取组件的初始属性值
	 * 
	 * @param context
	 *            环境
	 * @param attrs
	 *            属性集
	 */
	private void initData(Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs,
				R.styleable.ArcMenu);

		radius = (int) ta.getDimension(R.styleable.ArcMenu_radius, 100);
		int pos = ta.getInt(R.styleable.ArcMenu_position, LEFT_BOTTOM);

		mainButton_layout_marginBottom = (int) ta.getDimension(
				R.styleable.ArcMenu_mainButton_layout_marginBottom, 0);
		childButton_layout_padding_LeftAndRight = (int) ta.getDimension(
				R.styleable.ArcMenu_childButton_padding_LeftAndRight, 0);
		childButton_layout_padding_TopAndBottom = (int) ta.getDimension(
				R.styleable.ArcMenu_childButton_padding_TopAndBottom, 0);
		childButton_gap = ta.getDimensionPixelOffset(
				R.styleable.ArcMenu_childButton_gap, 0);

		mMButtonOpenIcon = ta
				.getDrawable(R.styleable.ArcMenu_mainButton_open_img);
		mMButtonCloseIcon = ta
				.getDrawable(R.styleable.ArcMenu_mainButton_close_img);

		ta.recycle();

		switch (pos) {
		case LEFT_TOP:
			position = Position.LEFT_TOP;
			is_leftmenu = -1;
			is_topmenu = -1;
			break;
		case LEFT_BOTTOM:
			position = Position.LEFT_BOTTOM;
			is_leftmenu = -1;
			is_topmenu = 1;
			break;
		case RIGHT_TOP:
			position = Position.RIGHT_TOP;
			is_leftmenu = 1;
			is_topmenu = -1;
			break;
		case RIGHT_BOTTOM:
			position = Position.RIGHT_BOTTOM;
			is_leftmenu = 1;
			is_topmenu = 1;
			break;
		case CENTER_BOTTOM:
			position = Position.CENTER_BOTTOM;
			break;
		}
		status = Status.CLOSE;

	}

	/**
	 * 初始事件
	 */
	private void initEvent() {
		if (mainButton != null)
			mainButton.setOnClickListener(this);
	}

	/**
	 * 测量ArcMenu及其子控件的宽高,并将值保持至相应控件中
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// 测量ArcMenu控件
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			// 测量子控件
			measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
		}
	}

	/**
	 * 设置子控件的大小和位置
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (getChildCount() == 0) {
			throw new RuntimeException("No childView in the ArcMenu");
		}

		if (changed) {
			locateMainButton();
			locateChildButton();
			initEvent();
		}
	}

	/**
	 * 定位主按钮
	 */
	private void locateMainButton() {
		mainButton = getChildAt(getChildCount() - 1);

		int l = 0;
		int t = 0;

		int width = mainButton.getMeasuredWidth();
		int height = mainButton.getMeasuredHeight();

		switch (position) {
		case LEFT_TOP:
			l = 0;
			t = 0;
			break;
		case LEFT_BOTTOM:
			l = 0;
			t = getMeasuredHeight() - height;
			break;
		case RIGHT_TOP:
			l = getMeasuredWidth() - width;
			t = 0;
			break;
		case RIGHT_BOTTOM:
			l = getMeasuredWidth() - width;
			t = getMeasuredHeight() - height;
			break;
		case CENTER_BOTTOM:
			l = (getMeasuredWidth() - width) / 2;
			t = getMeasuredHeight() - height - mainButton_layout_marginBottom;
			break;
		}
		mainButtonX = l;
		mainButtonY = t;
		mainButton.layout(l, t, l + width, t + height);
	}

	/**
	 * 定位子按钮
	 */
	private void locateChildButton() {

		int count = getChildCount();
		View childView;
		// 子控件的左，上位置
		int cl = 0, ct = 0;
		// 子控件的宽高
		int cWidth = 0, cHeight = 0;
		for (int i = 1; i < count; i++) {
			childView = getChildAt(i - 1);

			childView.setVisibility(View.GONE);
			cWidth = childView.getMeasuredWidth();
			cHeight = childView.getMeasuredHeight();

			if (position == Position.CENTER_BOTTOM) {
				// 无弧度菜单 水平平铺
				cl = childButton_layout_padding_LeftAndRight + (i - 1)
						* (cWidth + childButton_gap);
				ct = getMeasuredHeight()
						- childButton_layout_padding_TopAndBottom * 2 - cHeight;

			} else {
				// 弧度菜单
				cl = (int) (radius * Math.sin(Math.PI / 2 / (count - 2)
						* (i - 1)));
				ct = (int) (radius * Math.cos(Math.PI / 2 / (count - 2)
						* (i - 1)));

				// 如果菜单在下方
				if (position == Position.LEFT_BOTTOM
						|| position == Position.RIGHT_BOTTOM) {
					ct = getMeasuredHeight() - cHeight - ct;
				}

				// 如果菜单在右方
				if (position == Position.RIGHT_TOP
						|| position == Position.RIGHT_BOTTOM) {
					cl = getMeasuredWidth() - cWidth - cl;
				}
			}
			childButtonXY.add(new int[] { cl, ct });
			childView.layout(cl, ct, cl + cWidth, ct + cHeight);
		}

	}

	@Override
	public void onClick(View v) {
		v.startAnimation(AnimationFactory.rotateAnimation(0f, 360f, 300));
		toggle(300);

		// 设置切换图片
		if (null == mMButtonCloseIcon || null == mMButtonOpenIcon)
			return;

		if (v instanceof ImageView) {
			ImageView iV = (ImageView) v;
			if (isOpen()) {
				iV.setImageDrawable(mMButtonCloseIcon);
			} else {
				iV.setImageDrawable(mMButtonOpenIcon);
			}
		}
	}

	/**
	 * 切换菜单状态
	 * 
	 * @param duration
	 */
	public void toggle(final int duration) {
		// 为子按钮添加平移旋转动画
		int count = getChildCount();

		for (int i = 1; i < count; i++) {
			final View childView = getChildAt(i - 1);

			childView.setVisibility(View.VISIBLE);

			int cl = childButtonXY.get(i - 1)[0];
			int ct = childButtonXY.get(i - 1)[1];

			TranslateAnimation tranAnim = null;
			// 菜单展开
			if (status == Status.CLOSE) {
				tranAnim = AnimationFactory.translateAnimation(
						mainButtonX - cl, 0, mainButtonY - ct, 0, duration,
						(i - 1), count);
				childView.setClickable(true);
				childView.setFocusable(true);
			} else {
				// 菜单折叠
				tranAnim = AnimationFactory.translateAnimation(0, mainButtonX
						- cl, 0, mainButtonY - ct, duration, (i - 1), count);
				childView.setClickable(false);
				childView.setFocusable(false);
			}

			tranAnim.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					if (status == Status.CLOSE) {
						childView.setVisibility(View.GONE);
					}
				}
			});

			RotateAnimation rotateAnim = AnimationFactory.rotateAnimation(0f,
					720f, duration);

			AnimationSet animset = new AnimationSet(true);
			animset.addAnimation(rotateAnim);
			animset.addAnimation(tranAnim);

			childView.startAnimation(animset);

			// 设置子按钮的点击事件
			final int index = i;
			childView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (onMenuItemClickListener != null) {
						onMenuItemClickListener.onMenuItemClicked(v, index);
					}
					menuItemAnim(index, duration);
					changeStatus();
				}
			});
		}

		// 切换菜单状态
		changeStatus();
	}

	/**
	 * 为子按钮添加淡出或增强动画
	 * 
	 * @param index
	 *            选中按钮的位置
	 */
	private void menuItemAnim(int index, int duration) {

		View childView;
		for (int i = 1; i < getChildCount(); i++) {
			childView = getChildAt(i - 1);
			// 放大子按钮
			if (i == index) {
				childView.startAnimation(AnimationFactory.scaleAlphaAnimation(
						1.0f, 2.5f, 1.0f, 2.5f, duration));
			} else {
				// 缩小子按钮
				childView.startAnimation(AnimationFactory.scaleAlphaAnimation(
						1.0f, 0.0f, 1.0f, 0.0f, duration));
			}
			childView.setFocusable(false);
			childView.setClickable(false);
		}
	}

	private void changeStatus() {
		status = (status == Status.CLOSE ? Status.OPEN : Status.CLOSE);
	}

	public boolean isOpen() {
		return status == Status.OPEN;
	}

	public void setUpOpenStateIcon() {
		// 设置切换图片
		if (null != mMButtonOpenIcon && mainButton instanceof ImageView) {
			ImageView iV = (ImageView) mainButton;
			iV.setImageDrawable(mMButtonOpenIcon);
		}
	}

	public void setUpCloseStateIcon() {
		if (null != mMButtonCloseIcon && mainButton instanceof ImageView) {
			ImageView iV = (ImageView) mainButton;
			iV.setImageDrawable(mMButtonCloseIcon);
		}
	}

	public void close() {
		status = Status.CLOSE;
		setUpOpenStateIcon();
		View childView;
		for (int i = 1; i < getChildCount(); i++) {
			childView = getChildAt(i - 1);
			childView.setVisibility(View.GONE);
		}

	}

	public void setOnMenuItemClickListener(
			OnMenuItemClickListener onMenuItemClickListener) {
		this.onMenuItemClickListener = onMenuItemClickListener;
	}

}
