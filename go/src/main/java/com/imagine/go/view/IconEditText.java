package com.imagine.go.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AutoCompleteTextView;

import com.imagine.go.R;

/**
 * IconEditText:
 * 
 * @语音搜索 输入文本组件
 * @author Jinhu
 * @date 2016/3/29
 */
public class IconEditText extends AutoCompleteTextView {

	/* 语音按钮引用. */
	private Drawable mVoiceDrawable;

	/* 搜索按钮引用. */
	private Drawable mSearchDrawble;

	/* 搜索栏图标点击回调. */
	private OnIconClickedListener mOnIconClickedListener;

	public interface OnIconClickedListener {
		// 搜索服务
		void onSearchStart();

		// 语音服务
		void onVoiceStart();
	}

	public IconEditText(Context context) {
		super(context);
		init();
	}

	public IconEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public IconEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@SuppressWarnings("deprecation")
	private void init() {
		mSearchDrawble = getCompoundDrawables()[0];
		mVoiceDrawable = getCompoundDrawables()[2];
		if (null == mSearchDrawble) {
			mSearchDrawble = getResources().getDrawable(
					R.drawable.ic_searchbar_magnifier);
		}
		if (null == mVoiceDrawable) {
			mVoiceDrawable = getResources().getDrawable(
					R.drawable.ic_searchbar_voice);
		}
		setIconVisible();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.performClick();
		if (MotionEvent.ACTION_DOWN == event.getAction()) {
			int xDown = (int) event.getX();
			// 点击了搜索按钮
			if (xDown > 0 && xDown < getCompoundPaddingLeft()) {
				if (null != mOnIconClickedListener)
					mOnIconClickedListener.onSearchStart();
			}
			// 点击了语音按钮
			if (xDown >= (getWidth() - getCompoundPaddingRight())
					&& xDown < getWidth()) {
				if (null != mOnIconClickedListener) {
					mOnIconClickedListener.onVoiceStart();
				}
			}
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 设置图标显示
	 */
	protected void setIconVisible() {
		setCompoundDrawables(mSearchDrawble, getCompoundDrawables()[1],
				mVoiceDrawable, getCompoundDrawables()[3]);
	}

	/**
	 * 回调
	 * 
	 * @param onVoiceStartListener
	 */
	public void setOnIconClickedListener(
			OnIconClickedListener onIconClickedListener) {
		mOnIconClickedListener = onIconClickedListener;
	}

}
