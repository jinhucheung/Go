package com.imagine.go.ar.paintable;

import android.graphics.Canvas;

/**
 * PaintablePoint: 可绘制的点对象
 * 
 * @author Jinhu
 * @date 2016/4/10
 */
public class PaintablePoint extends PaintableObject {

	private static int width = 2;
	private static int height = 2;
	private int color = 0;
	private boolean fill = false;

	public PaintablePoint(int color, boolean fill) {
		set(color, fill);
	}

	public void set(int color, boolean fill) {
		this.color = color;
		this.fill = fill;
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
		setFill(fill);
		setColor(color);
		paintRect(canvas, -1, -1, width, height);
	}
}
