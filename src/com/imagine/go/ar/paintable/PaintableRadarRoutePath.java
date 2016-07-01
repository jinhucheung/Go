package com.imagine.go.ar.paintable;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;

import com.imagine.go.Constants;
import com.imagine.go.ar.ARData;
import com.imagine.go.ar.ARMarker;
import com.imagine.go.ar.Radar;
import com.imagine.go.ar.model.Point;

/**
 * 绘制雷达图导航路线
 * 
 * @author Jinhu
 * @date 2016/4/26
 */
public class PaintableRadarRoutePath extends PaintableObject {
	private static final int RADAR_ROUTE_COLOR = Color.WHITE;
	private static final int RADAR_ROUTE_NODE_COLOR = Color.argb(235, 84, 129,
			212);

	/* 记录下Marker的地理位置 . */
	private final float[] locationArray = new float[3];

	private final List<Point> mPoints = new ArrayList<Point>();

	public PaintableRadarRoutePath() {
		this.set();
	}

	public void set() {

	}

	@Override
	public float getWidth() {
		return Radar.RADIUS * 2;
	}

	@Override
	public float getHeight() {
		return Radar.RADIUS * 2;
	}

	@Override
	public void paint(Canvas canvas) {
		if (null == canvas)
			throw new NullPointerException();
		// 计算导航路线
		Path mPath = new Path();
		mPath.rewind();
		calculNaviPoint(mPath);

		// 设置裁剪区域
		Path mClipCircle = new Path();
		mClipCircle.addCircle(Radar.RADIUS, Radar.RADIUS, Radar.RADIUS - 2,
				Direction.CW);

		canvas.save();
		canvas.clipPath(mClipCircle); // 将超出雷达图范围的路径裁剪

		// 绘制导航路线
		setStrokeWidth(3f);
		setColor(RADAR_ROUTE_COLOR);
		paintPath(canvas, mPath);

		// 绘制导航路线节点
		setStrokeWidth(5f);
		setStrokeCap(Paint.Cap.ROUND);// 将点设置为圆点状
		setColor(RADAR_ROUTE_NODE_COLOR);
		for (Point p : mPoints) {
			paintPoint(canvas, p.getX(), p.getY());
		}

		canvas.restore();
	}

	/**
	 * 计算导航段节点
	 * 
	 * @param path
	 */
	private void calculNaviPoint(Path path) {
		mPoints.clear();

		float range = Constants.VALUE_DEFAULT_ROUTE_RADIUS; // 获取搜索半径
		float scale = range / Radar.RADIUS;

		int i = 0;
		for (ARMarker m : ARData.getInstance().getNaviMarkers()) {
			m.getLocation().get(locationArray); // locationArray保存了Marker的地理位置
			float x = locationArray[0] / scale; // 搜索半径range越大，雷达图显示的点越多
			float y = locationArray[2] / scale;
			if (0 == i) {
				path.moveTo(x + Radar.RADIUS, y + Radar.RADIUS); // 设置路径起点
			} else {
				path.lineTo(x + Radar.RADIUS, y + Radar.RADIUS);
			}
			i++;
			mPoints.add(new Point(x + Radar.RADIUS, y + Radar.RADIUS));

		}

	}

}
