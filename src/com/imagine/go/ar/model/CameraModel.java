package com.imagine.go.ar.model;


/**
 * 摄像模型:焦距、视角、摄像视图投影
 * 
 * @author Jinhu
 * @date 2016/4/16
 */
public class CameraModel {

	private static final float[] tmp1 = new float[3];
	private static final float[] tmp2 = new float[3];

	private int width = 0;
	private int height = 0;

	private float distance = 0f;

	public CameraModel(int width, int height) {
		set(width, height);
	}

	public void set(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	/**
	 * 计算焦距
	 * 
	 * @param viewAngle
	 *            视角
	 */
	public void setViewAngle(float viewAngle) {
		this.distance = (this.width / 2) / (float) Math.tan(viewAngle / 2);
	}

	/**
	 * 计算摄像坐标到屏幕坐标的投影
	 * 
	 * @param orgPoint
	 *            摄像坐标
	 * @param prjPoint
	 *           屏幕坐标
	 * @param addX
	 *            差值
	 * @param addY
	 *            差值
	 */
	public void projectPoint(Vector orgPoint, Vector prjPoint, float addX,
			float addY) {
		orgPoint.get(tmp1);
		tmp2[0] = distance * tmp1[0] / -tmp1[2];
		tmp2[1] = distance * tmp1[1] / -tmp1[2];
		tmp2[2] = tmp1[2];
		tmp2[0] = tmp2[0] + addX + width / 2;
		tmp2[1] = -tmp2[1] + addY + height / 2;
		prjPoint.set(tmp2);
	}
}
