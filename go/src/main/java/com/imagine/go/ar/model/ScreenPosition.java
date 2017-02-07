package com.imagine.go.ar.model;

/**
 * ScreenPosition 封装屏幕一点的位置信息
 * 
 * @author Jinhu
 * @date 2016/4/10
 */
public class ScreenPosition {

	/* 点向量 . */
	private float x = 0f;
	private float y = 0f;

	public ScreenPosition() {
		set(0, 0);
	}

	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	/*
	 * 向量旋转
	 */
	public void rotate(double degrees) {
		float xp = (float) Math.cos(degrees) * x - (float) Math.sin(degrees)
				* y;
		float yp = (float) Math.sin(degrees) * x + (float) Math.cos(degrees)
				* y;

		x = xp;
		y = yp;
	}

	/**
	 * 向量平移
	 * 
	 * @param offx
	 * @param offy
	 */
	public void add(float offx, float offy) {
		this.x += offx;
		this.y += offy;
	}

	@Override
	public String toString() {
		return "screenPosition(x,y):" + "x=" + x + " y=" + y;
	}
}
