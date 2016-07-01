package com.imagine.go.ar.paintable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Path;

import com.imagine.go.R.color;

/**
 * 绘制路线箭头
 * 
 * @author Jinhu
 * @date 2016/4/25
 */
public class PaintableArrow extends PaintableObject {

	private float width = 220;
	private float height = 240;

	private int centerX = 0;
	private int centerY = 0;

	/* 箭头颜色 . */
	private int mColor = COLOR_ARROW;

	private static final int COLOR_ARROW = Color.argb(235, 84, 129, 212);

	/* 箭头类型 . */
	private volatile int mType = 0;

	/**
	 * 箭头类型
	 */
	public enum ARROW {
		DEFAULT(0), LEFT(2), RIGHT(3), FRONT_LEFT(4), FRONT_RIGHT(5), FRONT_LEFT_BACK(
				6), FRONT_RIGHT_BACK(7), FRONT_LEFT_BACK2(8), FRONT(9), DESTATION(
				15);

		private int value;

		private ARROW(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

	}

	public PaintableArrow(int centerX, int centerY, int type) {
		set(centerX, centerY, type);
	}

	public void set(int centerX, int centerY, int type) {
		this.centerX = centerX;
		this.centerY = centerY;
		this.mType = type;
		initPaint();
	}

	private void initPaint() {
		setColor(mColor);
		setAntiAlias(true); // 抗锯齿
		setPathEffect(new CornerPathEffect(10)); // 路径效果
		setFill(true);
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public synchronized void paint(Canvas canvas) {
		if (null == canvas)
			throw new NullPointerException();
		// 绘制不同类型箭头
		switch (mType) {
		case 0:
			drawFrontArrow(canvas, centerX, centerY);
			break; // 缺省
		case 1:
			drawFrontArrow(canvas, centerX, centerY);
			break; // 缺省
		case 2:
			drawLeftArrow(canvas, centerX, centerY);
			break; // 左转箭头
		case 3:
			drawRightArrow(canvas, centerX, centerY);
			break; // 右转箭头
		case 4:
			drawFrontLeftArrow(canvas, centerX, centerY);
			break; // 直行左转箭头
		case 5:
			drawFrontRightArrow(canvas, centerX, centerY);
			break; // 直行右转箭头
		case 6:
			drawFrontLeftBackArrow(canvas, centerX, centerY);
			break;// 直行左转回退箭头
		case 7:
			drawFrontRightBackArrow(canvas, centerX, centerY);
			break;// 直行右转回退箭头
		case 8:
			drawFrontLeftBack2Arrow(canvas, centerX, centerY);
			break; // 直行左转回退箭头
		case 9:
			drawFrontArrow(canvas, centerX, centerY);
			break;// 直行箭头
		case 15:
			drawFrontArrow(canvas, centerX, centerY);
			break;// 到达目的地
		default:
			drawFrontArrow(canvas, centerX, centerY);
			break;// 缺省
		}

	}

	/**
	 * 绘制基本箭头
	 * 
	 * @param canvas
	 * @param centerX
	 * @param centerY
	 */
	private void drawBasicArrow(Canvas canvas, float centerX, float centerY) {
		Path mArrowPath = new Path();
		mArrowPath.rewind();

		mArrowPath.moveTo(centerX, centerY - height / 2);
		mArrowPath.lineTo(centerX - width / 2, centerY - height / 6);
		mArrowPath.lineTo(centerX - width / 2, centerY + height / 2);
		mArrowPath.lineTo(centerX, centerY + height * 2 / 6);
		mArrowPath.lineTo(centerX + width / 2, centerY + height / 2);
		mArrowPath.lineTo(centerX + width / 2, centerY - height / 6);
		mArrowPath.lineTo(centerX, centerY - height / 2);
		mArrowPath.close();

		setColor(mColor);
		setFill(true);
		paintPath(canvas, mArrowPath);

		setColor(color.lightgray);
		setStrokeWidth(5);
		setFill(false);
		paintPath(canvas, mArrowPath);
	}

	/**
	 * 绘制直行箭头
	 * 
	 * @param canvas
	 * @param centerX
	 * @param centerY
	 */
	private void drawFrontArrow(Canvas canvas, float centerX, float centerY) {
		drawBasicArrow(canvas, centerX, centerY);

		canvas.save();
		canvas.translate(0, -300);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();

		canvas.save();
		canvas.translate(0, -600);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();
	}

	/**
	 * 绘制左转箭头
	 * 
	 * @param canvas
	 * @param centerX
	 * @param centerY
	 */
	private void drawLeftArrow(Canvas canvas, float centerX, float centerY) {
		drawBasicArrow(canvas, centerX, centerY);

		canvas.save();
		canvas.translate(0, -300);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();

		canvas.save();
		canvas.translate(-300, -600);
		canvas.rotate(-90, centerX, centerY);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();

		canvas.save();
		canvas.translate(-600, -600);
		canvas.rotate(-90, centerX, centerY);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();
	}

	/**
	 * 绘制右转箭头
	 * 
	 * @param canvas
	 * @param centerX
	 * @param centerY
	 */
	private void drawRightArrow(Canvas canvas, float centerX, float centerY) {
		drawBasicArrow(canvas, centerX, centerY);

		canvas.save();
		canvas.translate(0, -300);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();

		canvas.save();
		canvas.translate(300, -600);
		canvas.rotate(90, centerX, centerY);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();

		canvas.save();
		canvas.translate(600, -600);
		canvas.rotate(90, centerX, centerY);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();
	}

	/**
	 * 绘制直行左转箭头
	 * 
	 * @param canvas
	 * @param centerX
	 * @param centerY
	 */
	private void drawFrontLeftArrow(Canvas canvas, float centerX, float centerY) {
		drawBasicArrow(canvas, centerX, centerY);

		canvas.save();
		canvas.translate(0, -300);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();

		canvas.save();
		canvas.translate(-100, -600);
		canvas.rotate(-45, centerX, centerY);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();

		canvas.save();
		canvas.translate(-350, -900);
		canvas.rotate(-45, centerX, centerY);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();
	}

	/**
	 * 绘制直行右转箭头
	 * 
	 * @param canvas
	 * @param centerX
	 * @param centerY
	 */
	private void drawFrontRightArrow(Canvas canvas, float centerX, float centerY) {
		drawBasicArrow(canvas, centerX, centerY);

		canvas.save();
		canvas.translate(0, -300);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();

		canvas.save();
		canvas.translate(100, -600);
		canvas.rotate(45, centerX, centerY);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();

		canvas.save();
		canvas.translate(350, -900);
		canvas.rotate(45, centerX, centerY);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();
	}

	/**
	 * 绘制直行左转回退箭头
	 * 
	 * @param canvas
	 * @param centerX
	 * @param centerY
	 */
	private void drawFrontLeftBackArrow(Canvas canvas, float centerX,
			float centerY) {
		drawBasicArrow(canvas, centerX, centerY);

		canvas.save();
		canvas.translate(0, -300);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();

		canvas.save();
		canvas.translate(-300, -600);
		canvas.rotate(-90, centerX, centerY);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();

		canvas.save();
		canvas.translate(-500, -300);
		canvas.rotate(-180, centerX, centerY);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();
	}

	/**
	 * 绘制直行右转回退箭头
	 * 
	 * @param canvas
	 * @param centerX
	 * @param centerY
	 */
	private void drawFrontRightBackArrow(Canvas canvas, float centerX,
			float centerY) {
		drawBasicArrow(canvas, centerX, centerY);

		canvas.save();
		canvas.translate(0, -300);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();

		canvas.save();
		canvas.translate(300, -600);
		canvas.rotate(90, centerX, centerY);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();

		canvas.save();
		canvas.translate(500, -300);
		canvas.rotate(180, centerX, centerY);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();
	}

	/**
	 * 绘制直行左转回退箭头
	 * 
	 * @param canvas
	 * @param centerX
	 * @param centerY
	 */
	private void drawFrontLeftBack2Arrow(Canvas canvas, float centerX,
			float centerY) {
		drawBasicArrow(canvas, centerX, centerY);

		canvas.save();
		canvas.translate(0, -300);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();

		canvas.save();
		canvas.translate(0, -600);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();

		canvas.save();
		canvas.translate(-350, -500);
		canvas.rotate(-150, centerX, centerY);
		drawBasicArrow(canvas, centerX, centerY);
		canvas.restore();
	}

}
