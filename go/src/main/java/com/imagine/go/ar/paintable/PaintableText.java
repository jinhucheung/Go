package com.imagine.go.ar.paintable;

import android.graphics.Canvas;
import android.graphics.Color;

/**
 * PaintableText:可绘制的文本对象
 * 
 * @author Jinhu
 * @date 2016/4/10
 */
public class PaintableText extends PaintableObject {

	private static final float WIDTH_PAD = 4;
	private static final float HEIGHT_PAD = 2;

	private String text = null;
	private int color = 0;
	private int size = 0;
	private float width = 0;
	private float height = 0;
	private boolean paintBackground = false;

	public PaintableText(String text, int color, int size,
			boolean paintBackground) {
		set(text, color, size, paintBackground);
	}

	public void set(String text, int color, int size, boolean paintBackground) {
		if (null == text)
			throw new NullPointerException();
		this.text = text;
		this.color = color;
		this.size = size;
		this.width = getTextWidth(text) + WIDTH_PAD * 2;
		this.height = getTextAsc() + getTextDesc() + HEIGHT_PAD * 2;
		this.paintBackground = paintBackground;
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
		if (null == canvas || null == text)
			throw new NullPointerException();

		setColor(color);
		setFontSize(size);
		if (paintBackground) {
			setColor(Color.rgb(0, 0, 0));
			setFill(true);
			paintRect(canvas, -(width / 2), -(height / 2), width, height);
			setColor(Color.rgb(255, 255, 255));
			setFill(false);
			paintRect(canvas, -(width / 2), -(height / 2), width, height);
		}
		paintText(canvas, (WIDTH_PAD - width / 2),
				(HEIGHT_PAD + getTextAsc() - height / 2), text);
	}
}
