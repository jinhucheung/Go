package com.imagine.go.util;

import android.app.Application;
import android.widget.Toast;

/**
 * ToastUtil:Toast统一调用
 * 
 * @author Jinhu
 * @date 2016/3/19
 */
public class ToastUtil {

	private static Application context;
	public static boolean isShow = true;

	/**
	 * 不支持实例化
	 */
	private ToastUtil() {
		throw new UnsupportedOperationException("cannot be instantiated");
	}

	/**
	 * 初始化环境
	 * 
	 * @param context
	 */
	public static void initContext(Application context) {
		ToastUtil.context = context;
	}

	/**
	 * 短时间显示Toast
	 * 
	 * @param message
	 */
	public static void showShort(CharSequence message) {
		if (context != null && isShow) {
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 长时间显示Toast
	 * 
	 * @param message
	 */
	public static void showLong(CharSequence message) {
		if (context != null && isShow) {
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 自定义显示Toast时间
	 * 
	 * @param message
	 * @param duration
	 */
	public static void show(CharSequence message, int duration) {
		if (context != null && isShow) {
			Toast.makeText(context, message, duration).show();
		}
	}
}
