package com.imagine.go.ar.paintable;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * 绘制图片对象
 * 
 * @author Jinhu
 * @date 2016/4/16
 */
public class PaintableIcon extends PaintableObject {

	private Bitmap bitmap = null;

	public PaintableIcon(Bitmap bitmap) {
		set(bitmap, bitmap.getWidth(), bitmap.getHeight());
	}

	public PaintableIcon(Bitmap bitmap, int width, int height) {
		set(bitmap, width, height);
	}

	public void set(Bitmap bitmap, int width, int height) {
		if (null == bitmap)
			throw new NullPointerException();
		this.bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
	}

	@Override
	public float getWidth() {
		return bitmap.getWidth();
	}

	@Override
	public float getHeight() {
		return bitmap.getHeight();
	}

	@Override
	public void paint(Canvas canvas) {
		if (canvas == null || bitmap == null)
			throw new NullPointerException();
		paintBitmap(canvas, bitmap, -(bitmap.getWidth() / 2),
				-(bitmap.getHeight() / 2));
	}

}
