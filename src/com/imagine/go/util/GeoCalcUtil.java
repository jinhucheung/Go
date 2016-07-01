package com.imagine.go.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 空间时间计算类
 * 
 * @author swansword
 * 
 */
public class GeoCalcUtil {
	// 地球长半轴
	public static final double EARTH_RADIUS = 6378.137;

	/**
	 * 计算弧度
	 * 
	 * @param d
	 *            以度为单位的经纬度数值
	 * @return 以弧度为单位的经纬度数值
	 */
	public static double CalcRad(double d) {
		return d * Math.PI / 180.0;
	}

	/**
	 * 经纬度转换成以米为单位的平面直角坐标
	 * 
	 * @param lon
	 *            经度
	 * @param lat
	 *            纬度
	 * @return 平面直角坐标double型数组，以米为单位
	 * 
	 */
	public static double[] WGS2flat(double lon, double lat) {
		double L = CalcRad(lon);
		double l = L - CalcRad(120);
		double B = CalcRad(lat);
		double cosb = Math.cos(B);
		double sinb = Math.sin(B);

		double a = EARTH_RADIUS * 1000;
		// 地球短半轴
		double b = 6356752.3142451793;
		double t = Math.tan(B);
		// double r = 3600 * 180 / Math.PI;
		double e2 = (Math.pow(a, 2) - Math.pow(b, 2)) / Math.pow(a, 2);
		double e12 = (Math.pow(a, 2) - Math.pow(b, 2)) / Math.pow(b, 2);
		double n2 = e12 * Math.pow(cosb, 2);
		double N = a / Math.sqrt(1 - e2 * Math.pow(sinb, 2));

		double x = 6367449.1458 * B - 32009.8185 * cosb * sinb - 133.9975
				* cosb * Math.pow(sinb, 3) - 0.6975 * cosb * Math.pow(sinb, 5);
		double X = x + N / 2 * t * Math.pow(cosb, 2) * Math.pow(l, 2) + N / 24
				* t * Math.pow(cosb, 4)
				* (5 - Math.pow(t, 2) + 9 * n2 + 4 * Math.pow(n2, 2))
				* Math.pow(l, 4);
		double Y = N * cosb * l + N / 6 * Math.pow(cosb, 3)
				* (1 - Math.pow(t, 2) + n2) * Math.pow(l, 3);

		double[] coord = { X, Y };
		return coord;
	}

	// --------------------单位转换-----------------------
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
			builder.append(" 米");
		} else if (distance < 10000) {
			DecimalFormat format = new DecimalFormat("#.#");
			builder.append(format.format(distance / 1000));
			builder.append(" 公里");
		} else {
			builder.append((int) (distance / 1000));
			builder.append(" 公里");
		}
		return builder.toString();
	}

	/**
	 * 将时间格式化为字符串
	 * 
	 * @param time
	 *            时间(单位为秒)
	 * @return 格式化的字符串 (例如：120 格式化2.0分钟)
	 */
	public static String formatTime(double time) {
		StringBuilder builder = new StringBuilder();
		if (time < 60) {
			builder.append((int) time);
			builder.append(" 秒");
		} else if (time < 3600) {
			DecimalFormat format = new DecimalFormat("#.#");
			builder.append(format.format(time / 60));
			builder.append(" 分钟");
		} else {
			builder.append((int) (time / 3600));
			builder.append(" 小时");
		}
		return builder.toString();
	}

	/**
	 * 获取当前系统时间
	 * 
	 * @param offTime
	 *            差值
	 * @return
	 */
	public static String getCurrentSystemTime(long offTime) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm",
				Locale.getDefault());
		Date curDate = new Date(System.currentTimeMillis() + offTime * 1000);
		String time = dateFormat.format(curDate);
		return time;
	}

	// --------------------计算角度-----------------------
	/**
	 * 计算两点间角度
	 * 
	 * @param center_x
	 * @param center_y
	 * @param post_x
	 * @param post_y
	 * @return 两点夹角
	 */
	public static final float getAngle(float center_x, float center_y,
			float post_x, float post_y) {
		float tmpv_x = post_x - center_x;
		float tmpv_y = post_y - center_y;
		float d = (float) Math.sqrt(tmpv_x * tmpv_x + tmpv_y * tmpv_y);
		float cos = tmpv_x / d;
		float angle = (float) Math.toDegrees(Math.acos(cos));

		angle = (tmpv_y < 0) ? angle * -1 : angle;
		return angle;
	}
}
