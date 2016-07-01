package com.imagine.go.ar.paintable;

import android.graphics.Canvas;

/**
 * PaintableCircle :可绘制的圆对象
 * 
 * @author Jinhu
 * @date 2016/4/10
 */
public class PaintableCircle extends PaintableObject {

	private int color = 0;
	private float radius = 0;
	private boolean fill = false;

	public PaintableCircle(int color, float radius, boolean fill) {
		set(color, radius, fill);
	}

	private void set(int color, float radius, boolean fill) {
		this.color = color;
		this.radius = radius;
		this.fill = fill;
	}

	@Override
	public float getWidth() {
		return radius * 2;
	}

	@Override
	public float getHeight() {
		return radius * 2;
	}

	@Override
	public void paint(Canvas canvas) {
		if (null == canvas)
			throw new NullPointerException();

		setFill(fill);
		setColor(color);
		paintCircle(canvas, 0, 0, radius);
	}

}
