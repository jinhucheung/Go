package com.imagine.go.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.imagine.go.R;

/**
 * 带文字的进度条
 * 
 * @author Jinhu
 * @date 2016/5/17
 */
public class TextProgressBar extends View {

	/* 进度条属性 . */
	private int mProgressColor;
	private int mProgressNormalColor;
	private int mProgressBarHeight;

	/* 当前进度值 . */
	private volatile int mProgress;
	private int mMaxProgress;

	/* 描述进度的文字 . */
	private String mTxt;
	private int mTxtColor;

	/* 画笔. */
	private Paint mPaint;
	private Rect mRect;

	public TextProgressBar(Context context) {
		this(context, null);
	}

	public TextProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TextProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		initAttrs(context, attrs);

		initPaint();

		initProgressBar();
	}

	/**
	 * 获取布局属性
	 */
	private void initAttrs(Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs,
				R.styleable.TextProgressBar);

		mProgressBarHeight = (int) ta.getDimension(
				R.styleable.TextProgressBar_progressBarHeight, 0);

		mProgressColor = ta.getColor(R.styleable.TextProgressBar_progressColor,
				Color.BLUE);
		mProgressNormalColor = ta.getColor(
				R.styleable.TextProgressBar_progressNormalColor, Color.BLACK);

		mMaxProgress = ta.getInt(R.styleable.TextProgressBar_progressMax, 100);
		mProgress = ta.getInt(R.styleable.TextProgressBar_progress, 0);

		mTxtColor = ta.getColor(R.styleable.TextProgressBar_textColor,
				Color.BLUE);

		ta.recycle();
	}

	/**
	 * 初始化画笔
	 */
	private void initPaint() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.FILL);
		mPaint.setTextSize(20f);
		mRect = new Rect();
	}

	/**
	 * 初始化进度条
	 */
	private void initProgressBar() {
		setProgress(mProgress);
	}

	/**
	 * 设置文字内容
	 * 
	 * @param progress
	 */
	private void setText(int progress) {
		int i = (int) ((progress * 1.0f / mMaxProgress) * 100);
		mTxt = String.valueOf(i) + "%";
	}

	/**
	 * 设置进度值
	 */
	public synchronized void setProgress(int progress) {
		mProgress = progress;
		setText(progress);
		postInvalidate();
	}

	/**
	 * 返回当前进度值
	 * 
	 * @return
	 */
	public synchronized int getProgress() {
		return mProgress;
	}

	/**
	 * 返回最大进度值
	 * 
	 * @return
	 */
	public int getMaxProgress() {
		return mMaxProgress;
	}

	/**
	 * 返回当前进度宽度
	 * 
	 * @return
	 */
	private synchronized int getProgressWidth() {
		return (int) (1.0f * mProgress / mMaxProgress * getWidth());
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// 绘制默认进度线
		mPaint.setColor(mProgressNormalColor);
		canvas.drawRect(0, getHeight() - mProgressBarHeight, getWidth(),
				getHeight(), mPaint);

		if (0 == mProgress || mMaxProgress <= mProgress) {
			return;
		}

		// 绘制进度文字
		mPaint.getTextBounds(mTxt, 0, mTxt.length(), mRect);

		int x = getProgressWidth() - mRect.centerX(); // 显示文字在进度条之上
		int y = (getHeight() - mProgressBarHeight) / 2 - mRect.centerY();
		mPaint.setColor(mTxtColor);
		canvas.drawText(mTxt, x, y, mPaint);

		// 绘制进度线
		mPaint.setColor(mProgressColor);
		canvas.drawRect(0, getHeight() - mProgressBarHeight,
				getProgressWidth(), getHeight(), mPaint);

	}

}
