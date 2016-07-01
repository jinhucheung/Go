package com.imagine.go.ar.paintable;

import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;

/**
 * 绘制箭头容器对象<br/>
 * 控制箭头方位变换
 * 
 * @author Jinhu
 * @date 2016/5/4
 */
public class PaintableArrowContainer extends PaintableObject {

	/* 声明相机视角控件 . */
	private Camera mCamera = new Camera();

	/* 声明变换矩阵 . */
	private Matrix mMatrix = new Matrix();

	/* 绘制箭头 . */
	private PaintableObject drawObj;
	/* 中点 . */
	private float centerX;
	private float centerY;

	/* 固定绕Z轴偏转角 . */
	private float fixAngle;

	/* 绕Z轴偏转角. */
	private float rotationX;
	/* 绕X轴偏转角 . */
	private float rotationZ;

	private float width;
	private float height;

	public PaintableArrowContainer(PaintableObject drawObj, float centerX,
			float centerY, float fixAngle, float rotationX, float rotationZ) {
		set(drawObj, centerX, centerY, fixAngle, rotationX, rotationZ);
	}

	public void set(PaintableObject drawObj, float centerX, float centerY,
			float fixAngle, float rotationX, float rotationZ) {
		this.drawObj = drawObj;

		this.centerX = centerX;
		this.centerY = centerY;

		this.fixAngle = fixAngle;

		this.rotationX = rotationX;
		this.rotationZ = rotationZ;

		this.width = drawObj.getWidth();
		this.height = drawObj.getHeight();
	}

	@Override
	public void paint(Canvas canvas) {
		if (null == canvas || null == drawObj)
			throw new NullPointerException();
		canvas.save();

		// 进行视角变换
		mCamera.save();
		mCamera.rotateX(rotationX);
		mCamera.rotateZ(fixAngle);

		mCamera.rotateZ(rotationZ);

		mCamera.getMatrix(mMatrix); // 获取旋转矩阵

		mMatrix.preTranslate(-centerX, -centerY);
		mMatrix.postTranslate(centerX, centerY);
		mCamera.restore();

		// 应用矩阵变换
		canvas.setMatrix(mMatrix);
		drawObj.paint(canvas);

		canvas.restore();
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public float getHeight() {
		return height;
	}

	public float getCenterX() {
		return centerX;
	}

	public float getCenterY() {
		return centerY;
	}
}
