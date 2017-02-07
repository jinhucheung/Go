package com.imagine.go.util;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;

/**
 * TimerUtil:计时器工厂类
 * 
 * @author Jinhu
 * @date 2016/3/25
 */
public class TimerUtil {

	/**
	 * Handler延迟发送消息
	 * 
	 * @param handler
	 *            处理器
	 * @param event
	 *            事件标记
	 * @param delay
	 *            时间
	 */
	public static void schedule(Handler handler, int event, long delay) {
		final Handler mHandler = handler;
		final int mEvent = event;
		Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				mHandler.sendEmptyMessage(mEvent);
			}
		}, delay);
	}

}
