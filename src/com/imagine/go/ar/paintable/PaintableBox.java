package com.imagine.go.ar.paintable;

import android.graphics.Canvas;
import android.graphics.Color;

/**
 * 绘制Marker检测框 <br/>
 * 用于检测接触事件
 * 
 * @author Jinhu
 * @date 2016/4/16
 */
public class PaintableBox extends PaintableObject {

	private float width = 0, height = 0;

	private int borderColor = COLOR_BORDER;
	private int backgroundColor = COLOR_BACKGROUND;

	private static final int COLOR_BORDER = Color.rgb(255, 255, 255);
	private static final int COLOR_BACKGROUND = Color.argb(128, 0, 0, 0);

	public PaintableBox(float width, float heihgt) {
		set(width, heihgt);
	}

	public PaintableBox(float width, float height, int borderColor, int bgColor) {
		set(width, height, borderColor, bgColor);
	}

	public void set(float width, float height) {
		set(width, height, COLOR_BORDER, COLOR_BACKGROUND);
	}

	public void set(float width, float height, int borderColor, int bgColor) {
		this.width = width;
		this.height = height;
		this.borderColor = borderColor;
		this.backgroundColor = bgColor;
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
	public void paint(Canvas canvas) {
		if (null == canvas)
			throw new NullPointerException();

		setFill(true);
		setColor(backgroundColor);
		paintRect(canvas, 0, 0, width, height);

		setFill(false);
		setColor(borderColor);
		paintRect(canvas, 0, 0, width, height);
	}
}
