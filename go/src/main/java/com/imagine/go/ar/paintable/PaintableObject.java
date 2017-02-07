package com.imagine.go.ar.paintable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;

/**
 * PaintableObject <br/>
 * 绘制AR视图组件的基类 <br/>
 * 封装着一个Paint对象
 * 
 * @author Jinhu
 * @date 2016/4/10
 */
public abstract class PaintableObject {

	/* 声明画笔 . */
	private Paint mPaint = null;

	public PaintableObject() {
		if (null == mPaint) {
			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaint.setTextSize(16);
			mPaint.setColor(Color.BLUE);
			mPaint.setStyle(Paint.Style.STROKE);
		}
	}

	public abstract float getWidth();

	public abstract float getHeight();

	public abstract void paint(Canvas canvas);

	/**
	 * 设置画笔填充
	 * 
	 * @param fill
	 */
	public void setFill(boolean fill) {
		if (fill) {
			mPaint.setStyle(Paint.Style.FILL);
		} else {
			mPaint.setStyle(Paint.Style.STROKE);
		}
	}

	/**
	 * 设置画笔颜色
	 * 
	 * @param c
	 */
	public void setColor(int c) {
		mPaint.setColor(c);
	}

	/**
	 * 设置描边宽度
	 * 
	 * @param w
	 */
	public void setStrokeWidth(float w) {
		mPaint.setStrokeWidth(w);
	}

	/**
	 * 设置透明度
	 * 
	 * @param a
	 */
	public void setAlpha(int a) {
		mPaint.setAlpha(a);
	}

	/**
	 * 设置圆点
	 * 
	 * @param cap
	 */
	public void setStrokeCap(Cap cap) {
		mPaint.setStrokeCap(cap);
	}

	/**
	 * 设置抗锯齿
	 * 
	 * @param value
	 */
	public void setAntiAlias(boolean aa) {
		mPaint.setAntiAlias(aa);
	}

	/**
	 * 设置路径效果
	 * 
	 * @param effect
	 */
	public void setPathEffect(PathEffect effect) {
		mPaint.setPathEffect(effect);
	}

	/**
	 * 测量文本尺寸
	 * 
	 * @param txt
	 * @return 文本尺寸
	 */
	public float getTextWidth(String txt) {
		if (null == txt)
			throw new NullPointerException();
		return mPaint.measureText(txt);
	}

	/**
	 * 文本上界限距离
	 * 
	 * @return
	 */
	public float getTextAsc() {
		return -mPaint.ascent();
	}

	/**
	 * 文本下界限距离
	 * 
	 * @return
	 */
	public float getTextDesc() {
		return mPaint.descent();
	}

	/**
	 * 设置字体Size
	 * 
	 * @param size
	 */
	public void setFontSize(float size) {
		mPaint.setTextSize(size);
	}

	/**
	 * 绘制点
	 * 
	 * @param canvas
	 * @param x
	 * @param y
	 */
	public void paintPoint(Canvas canvas, float x, float y) {
		if (null == canvas)
			throw new NullPointerException();
		canvas.drawPoint(x, y, mPaint);
	}

	/**
	 * 绘制直线
	 * 
	 * @param canvas
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public void paintLine(Canvas canvas, float x1, float y1, float x2, float y2) {
		if (null == canvas)
			throw new NullPointerException();
		canvas.drawLine(x1, y1, x2, y2, mPaint);
	}

	/**
	 * 绘制圆
	 * 
	 * @param canvas
	 * @param x
	 * @param y
	 * @param radius
	 */
	public void paintCircle(Canvas canvas, float x, float y, float radius) {
		if (null == canvas)
			throw new NullPointerException();
		canvas.drawCircle(x, x, radius, mPaint);
	}

	/**
	 * 绘制矩形
	 * 
	 * @param canvas
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void paintRect(Canvas canvas, float x, float y, float width,
			float height) {
		if (null == canvas)
			throw new NullPointerException();
		canvas.drawRect(x, y, x + width, y + height, mPaint);
	}

	/**
	 * 绘制圆角矩形
	 * 
	 * @param canvas
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void paintRoundedRect(Canvas canvas, float x, float y, float width,
			float height) {
		if (null == canvas)
			throw new NullPointerException();

		RectF rect = new RectF(x, y, x + width, y + height);
		canvas.drawRoundRect(rect, 15F, 15F, mPaint);
	}

	/**
	 * 绘制图片
	 * 
	 * @param canvas
	 * @param bitmap
	 * @param left
	 * @param top
	 */
	public void paintBitmap(Canvas canvas, Bitmap bitmap, float left, float top) {
		if (null == canvas || null == bitmap)
			throw new NullPointerException();

		canvas.drawBitmap(bitmap, left, top, mPaint);
	}

	/**
	 * 绘制文本
	 * 
	 * @param canvas
	 * @param x
	 * @param y
	 * @param text
	 */
	public void paintText(Canvas canvas, float x, float y, String text) {
		if (null == canvas || null == text)
			throw new NullPointerException();
		canvas.drawText(text, x, y, mPaint);
	}

	/**
	 * 绘制路线
	 * 
	 * @param canvas
	 * @param path
	 */
	public void paintPath(Canvas canvas, Path path) {
		if (null == canvas || null == path)
			throw new NullPointerException();

		canvas.drawPath(path, mPaint);
	}

	/**
	 * 绘制PaintableObj对象
	 * 
	 * @param canvas
	 * @param obj
	 * @param x
	 * @param y
	 * @param rotaion
	 * @param scale
	 */
	public void paintObj(Canvas canvas, PaintableObject obj, float x, float y,
			float rotaion, float scale) {
		if (null == canvas || null == obj)
			throw new NullPointerException();

		canvas.save(); // 保存画布当前状态

		canvas.translate(x + obj.getWidth() / 2, y + obj.getHeight() / 2); // 以Obj中心开始做变换
		canvas.rotate(rotaion);
		canvas.scale(scale, scale);
		canvas.translate(-obj.getWidth() / 2, -obj.getHeight() / 2); // 以Container中心开始做绘图
		obj.paint(canvas);

		canvas.restore(); // 恢复画布之前状态
	}

}
