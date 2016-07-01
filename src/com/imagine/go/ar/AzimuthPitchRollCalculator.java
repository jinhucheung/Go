package com.imagine.go.ar;

import com.imagine.go.ar.model.Matrix;
import com.imagine.go.ar.model.Vector;
import com.imagine.go.util.GeoCalcUtil;

/**
 * 计算欧拉角
 * 
 * @author Jinhu
 * @date 2016/4/15
 */
public class AzimuthPitchRollCalculator {

	private static final Vector looking = new Vector();
	private static final float[] lookingArray = new float[3];

	/* 欧拉角 . */
	private static volatile float azimuth = 0;
	private static volatile float pitch = 0;
	private static volatile float roll = 0;

	private AzimuthPitchRollCalculator() {
	}

	public static synchronized float getAzimuth() {
		return azimuth;
	}

	public static synchronized float getPitch() {
		return pitch;
	}

	public static synchronized float getRoll() {
		return roll;
	}

	/**
	 * 计算欧拉角
	 * 
	 * @param rotationM
	 */
	public static synchronized void calc(Matrix rotationM) {
		if (null == rotationM)
			return;

		// 获得旋转矩阵在XOZ平面上分量向量
		looking.set(0, 0, 0);
		rotationM.transpose();
		looking.set(1, 0, 0);
		looking.prod(rotationM);

		looking.get(lookingArray);
		AzimuthPitchRollCalculator.azimuth = ((GeoCalcUtil.getAngle(0, 0,
				lookingArray[0], lookingArray[2]) + 360) % 360); // 获得角度是[-180,180]做一个映射到[0,360]

		// 获得旋转矩阵在YOZ平面上分量向量
		rotationM.transpose();
		looking.set(0, 1, 0);
		looking.prod(rotationM);

		looking.get(lookingArray);
		AzimuthPitchRollCalculator.pitch = -GeoCalcUtil.getAngle(0, 0,
				lookingArray[0], lookingArray[2]);

		// 获得旋转矩阵在XOY平面上分量向量
		rotationM.transpose();
		looking.set(0, 0, 1);
		looking.prod(rotationM);

		looking.get(lookingArray);
		AzimuthPitchRollCalculator.roll = -GeoCalcUtil.getAngle(0, 0,
				lookingArray[0], lookingArray[1]) / 2.0f;
		rotationM.transpose();
	}

}
