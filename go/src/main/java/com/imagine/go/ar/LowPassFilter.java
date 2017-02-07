package com.imagine.go.ar;

/**
 * LowPassFilter: 传感器低波过滤<br/>
 * 降噪,Alpha值越小,传感器越不灵敏
 * 
 * @author Jinhu
 * @date 2016年4月12日
 */
public class LowPassFilter {

	/* 描述过滤程度 */
	private static final float ALPHA_DEFAULT = 0.35f;
	private static final float ALPHA_STEADY = 0.001f;
	private static final float ALPHA_START_MOVING = 0.6f;
	private static final float ALPHA_MOVING = 0.9f;

	private LowPassFilter() {

	}

	/**
	 * 降噪
	 * 
	 * @param low
	 *            下限
	 * @param high
	 *            上限
	 * @param input
	 *            输入数组
	 * @param output
	 *            输出数组
	 * @return
	 */
	public static float[] filter(float low, float high, float[] input,
			float[] output) {
		if (null == input || null == output)
			throw new NullPointerException(
					"Input and prev float arrays must be non-NULL");
		if (input.length != output.length)
			throw new IllegalArgumentException(
					"Input and prev must be the same length");
		float alpha = computeAlpha(low, high, input, output);
		for (int i = 0; i < input.length; i++) {
			output[i] = output[i] + alpha * (input[i] - output[i]);
		}
		return output;
	}

	/**
	 * 测量Alpha
	 * 
	 * @param low
	 * @param high
	 * @param current
	 * @param previous
	 * @return
	 */
	private static final float computeAlpha(float low, float high,
			float[] input, float[] output) {
		if (3 != input.length || 3 != output.length)
			return ALPHA_DEFAULT;

		float x1 = input[0], y1 = input[1], z1 = input[2];
		float x2 = output[0], y2 = output[1], z2 = output[2];
		// 差值系数
		float distance = (float) (Math.sqrt(Math.pow(x2 - x1, 2d)
				+ Math.pow(y2 - y1, 2d) + Math.pow(z2 - z1, 2d)));

		if (distance < low)
			return ALPHA_STEADY;

		else if (distance >= low && distance <= high)
			return ALPHA_START_MOVING;

		else
			return ALPHA_MOVING;

	}
}
