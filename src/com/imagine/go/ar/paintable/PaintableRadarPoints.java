package com.imagine.go.ar.paintable;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;

import com.imagine.go.ar.ARData;
import com.imagine.go.ar.ARMarker;
import com.imagine.go.ar.Radar;

/**
 * PaintableRadarPoints: 绘制雷达点对象
 * 
 * @author Jinhu
 * @date 2016/4/10
 * 
 */
public class PaintableRadarPoints extends PaintableObject {

	private static final int RADAR_POINTS_COLOR = Color.rgb(88, 162, 202);

	/* 记录下Marker的地理位置 . */
	private final float[] locationArray = new float[3];
	/* 声明雷达点对象 . */
	private PaintablePoint mPoint = null;
	/* 声明雷达点容器 . */
	private PaintableContainer mPointContainer = null;
	/* 导航模式：单雷达点 . */
	private boolean onNaviMode = false;

	@Override
	public float getWidth() {
		return Radar.RADIUS * 2;
	}

	@Override
	public float getHeight() {
		return Radar.RADIUS * 2;
	}

	public void setOnNaviMode(boolean mode) {
		onNaviMode = mode;
	}

	@Override
	public void paint(Canvas canvas) {
		if (null == canvas)
			throw new NullPointerException();

		float range = ARData.getInstance().getRadius(); // 获取搜索半径
		float scale = range / Radar.RADIUS;
		// 绘制雷达点
		List<ARMarker> markers = null;
		if (!onNaviMode) {
			markers = ARData.getInstance().getMarkers();
		} else {
			markers = ARData.getInstance().getNaviMarkers();
		}
		for (ARMarker m : markers) {
			paintRadarPoint(canvas, scale, m);
		}

	}

	private void paintRadarPoint(Canvas canvas, float scale, ARMarker m) {
		m.getLocation().get(locationArray); // locationArray保存了Marker的地理位置
		float x = locationArray[0] / scale; // 搜索半径range越大，雷达图显示的点越多
		float y = locationArray[2] / scale;

		if ((x * x + y * y) < (Radar.RADIUS * Radar.RADIUS)) { // 判断Marker是否在雷达内
			// 生成雷达点对象
			if (null == mPoint)
				mPoint = new PaintablePoint(RADAR_POINTS_COLOR, true);
			else
				mPoint.set(RADAR_POINTS_COLOR, true);

			// 生成雷达点容器
			if (null == mPointContainer)
				mPointContainer = new PaintableContainer(mPoint, (x
						+ Radar.RADIUS - 1), (y + Radar.RADIUS - 1), 0, 1);
			else
				mPointContainer.set(mPoint, (x + Radar.RADIUS - 1), (y
						+ Radar.RADIUS - 1), 0, 1);

			mPointContainer.paint(canvas);
		}
	}
}
