package com.imagine.go.util;

import java.text.DecimalFormat;

/**
 * SeekBarUtil:计算拖动条缩放值和范围值
 * 
 * @author Jinhu
 * @date 2016/3/25
 */
public class SeekBarUtil {

	/**
	 * 根据缩放值计算范围（最小值为 300，0-12的步长为 50，13-22 的步长为 100，23-30 的步长为 200，最大值为 3400）。
	 * 
	 * @return 范围。
	 */
	public static int calcRadius(int progress) {
		int radius = 0;
		if (progress <= 12) {
			radius = progress * 50 + 300;
		} else if (progress > 12 && progress <= 22) {
			radius = progress * 100 - 300;
		} else {
			radius = progress * 200 - 2600;
		}
		return radius;
	}

	/**
	 * 根据半径范围计算缩放值
	 * 
	 * @return
	 */
	public static int calcProgress(int radius) {
		int progress = 0;
		if (radius <= 900) {
			progress = (radius - 300) / 50;
		} else if (radius >= 1000 && radius <= 1900) {
			progress = (radius + 300) / 100;
		} else {
			progress = (radius + 2600) / 200;
		}
		return progress;
	}

	/**
	 * 将距离格式化为字符串。
	 * 
	 * @param distance
	 *            距离（单位为米）。
	 * @return 格式化后的字符串（例如：960.69 格式化为 960 m，1230.321 格式化为 1.2 km，12321.123 格式化为
	 *         12 km）。
	 */
	public static String formatDistance(double distance) {
		StringBuilder builder = new StringBuilder();
		if (distance < 1000) {
			builder.append((int) distance);
			builder.append(" m");
		} else if (distance < 10000) {
			DecimalFormat format = new DecimalFormat("#.#");
			builder.append(format.format(distance / 1000));
			builder.append(" km");
		} else {
			builder.append((int) (distance / 1000));
			builder.append(" km");
		}
		return builder.toString();
	}
}
