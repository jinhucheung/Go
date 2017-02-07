package com.imagine.go.ar.paintable;

import android.graphics.Canvas;

/**
 * PaintableLine: 可绘制的直线对象
 * 
 * @author Jinhu
 * @date 2016/4/10
 */
public class PaintableLine extends PaintableObject {

	private int color = 0;
	private float x = 0;
	private float y = 0;

	public PaintableLine(int color, float x, float y) {
		set(color, x, y);
	}

	public void set(int color, float x, float y) {
		this.color = color;
		this.x = x;
		this.y = y;
	}

	@Override
	public float getWidth() {
		return x;
	}

	@Override
	public float getHeight() {
		return y;
	}

	@Override
	public void paint(Canvas canvas) {
		if (null == canvas)
			throw new NullPointerException();

		setFill(false);
		setColor(color);
		paintLine(canvas, 0, 0, x, y); // (0,0)相对与PaintableLine容器类
	}

}
