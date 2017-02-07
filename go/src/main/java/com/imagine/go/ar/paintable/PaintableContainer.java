package com.imagine.go.ar.paintable;

import android.graphics.Canvas;

/**
 * PaintableContainer:Paintable对象的容器类
 * 
 * @author Jinhu
 * @date 2016/4/10
 */
public class PaintableContainer extends PaintableObject {

	/* 容器尺寸 . */
	private float width = 0;
	private float height = 0;

	/* 可绘制对象 . */
	private PaintableObject obj = null;

	/* 可绘制对象属性. */
	private float objX = 0;
	private float objY = 0;
	private float objRotation = 0;
	private float objScale = 0;

	public PaintableContainer(PaintableObject drawObj, float centerX,
			float centerY, float rotation, float scale) {
		set(drawObj, centerX, centerY, rotation, scale);
	}

	public void set(PaintableObject drawObj, float centerX, float centerY,
			float rotation, float scale) {
		if (null == drawObj)
			throw new NullPointerException();

		this.obj = drawObj;
		this.objX = centerX;
		this.objY = centerY;
		this.objRotation = rotation;
		this.objScale = scale;
		this.width = obj.getWidth();
		this.height = obj.getHeight();
	}

	public void move(float x, float y) {
		objX = x;
		objY = y;
	}

	public float getObjX() {
		return objX;
	}

	public float getObjY() {
		return objY;
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
		if (null == canvas || null == obj)
			throw new NullPointerException();
		paintObj(canvas, obj, objX, objY, objRotation, objScale);
	}

	@Override
	public String toString() {
		return "objX=" + objX + " objY=" + objY + " width=" + width
				+ " height=" + height;
	}

}
