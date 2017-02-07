package com.imagine.go.ar;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.imagine.go.ar.paintable.PaintableContainer;
import com.imagine.go.ar.paintable.PaintableRadarRoutePath;

/**
 * 雷达路线导航图
 * 
 * @author Jinhu
 * @date 2016/4/26
 */
public class RadarRouteView extends RadarView {

	/* 雷达图路线 . */
	private static PaintableRadarRoutePath radarRoute = null;
	/* 雷达图路线容器. */
	private static PaintableContainer radarRouteContainer = null;

	public RadarRouteView(Context context) {
		this(context, null);
	}

	public RadarRouteView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RadarRouteView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	/**
	 * 绘制雷达图
	 * 
	 * @param canvas
	 */
	@Override
	public void draw(Canvas canvas) {
		if (null == canvas)
			throw new NullPointerException();

		AzimuthPitchRollCalculator.calc(ARData.getInstance()
				.getRotationMatrix());
		ARData.getInstance()
				.setAzimuth(AzimuthPitchRollCalculator.getAzimuth());
		ARData.getInstance().setPitch(AzimuthPitchRollCalculator.getPitch());

		// 绘制雷达圆
		drawRadarCircle(canvas);
		// 绘制扇形线
		drawRadarLines(canvas);
		// 绘制方位描述文本
		drawRadarText(canvas);
		// 绘制路线
		drawRoutePaths(canvas);
	}

	/**
	 * 绘制导航路线
	 * 
	 * @param canvas
	 */
	private void drawRoutePaths(Canvas canvas) {
		if (null == canvas)
			throw new NullPointerException();

		if (null == radarRoute) {
			radarRoute = new PaintableRadarRoutePath();
		}

		if (null == radarRouteContainer) {
			radarRouteContainer = new PaintableContainer(radarRoute, mX, mY,
					-ARData.getInstance().getAzimuth(), 1);
		} else {
			radarRouteContainer.set(radarRoute, mX, mY, -ARData.getInstance()
					.getAzimuth(), 1);
		}
		radarRouteContainer.paint(canvas);
	}
}
