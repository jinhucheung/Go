package com.imagine.go.ar;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.imagine.go.Constants;

/**
 * AugmentedView:增强信息控件
 * 
 * @author Jinhu
 * @date 2016/4/10
 */
public class AugmentedView extends View {

	/* 绘制锁 . */
	private final AtomicBoolean drawing = new AtomicBoolean(false);

	/* 正在显示的Markers . */
	private final List<ARMarker> cacheMarkers = new ArrayList<ARMarker>();

	/* 已更新状态的Markers . */
	private final TreeSet<ARMarker> updatedMarkers = new TreeSet<ARMarker>();

	/* 检测冲突 . */
	private boolean useCollisionDetection = true;

	/* 冲突常量 . */
	private static final int COLLISION_ADJUSTMENT_NEAR = 45;
	private static final int COLLISION_ADJUSTMENT_MIDDLE = 80;
	private static final int COLLISION_ADJUSTMENT_FAR = 250;
	private static final int DISTANCE_FAR = 1500;
	private static final int DISTANCE_MIDDLE = 500;

	/* 获取Marker位置信息 . */
	private final float[] locationArray = new float[3];

	public AugmentedView(Context context) {
		this(context, null);
	}

	public AugmentedView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AugmentedView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (null == canvas)
			return;

		if (drawing.compareAndSet(false, true)) {

			// 筛选Marker
			cacheMarkers.clear();
			for (ARMarker m : ARData.getInstance().getMarkers()) {
				m.update(canvas, 0, 0);
				if (m.isOnRadar())
					cacheMarkers.add(m);
			}
			// 检测冲突
			if (useCollisionDetection) {
				adjustForCollisions(canvas, cacheMarkers);
			}

			// 绘制Marker
			ListIterator<ARMarker> iter = cacheMarkers
					.listIterator(cacheMarkers.size());
			while (iter.hasPrevious()) {
				ARMarker m = iter.previous();
				m.draw(canvas);
			}

			drawing.set(false);
		}
	}

	/**
	 * 解决Marker的重叠问题
	 * 
	 * @param canvas
	 * @param markers
	 */
	private void adjustForCollisions(Canvas canvas, List<ARMarker> markers) {
		updatedMarkers.clear();
		int off = 0;
		for (ARMarker m1 : markers) {
			if (updatedMarkers.contains(m1) || !m1.isInView()) {
				continue;
			}

			float scale = 1 - (float) (m1.getDistance() / Constants.VALUE_DEFAULT_SEARCH_RADIUS);
			if (scale <= 0.4f) {
				scale = 0.4f;
			}
			m1.setScale(scale);

			if (m1 instanceof ARIconMarker) {
				ARIconMarker im = (ARIconMarker) m1;
				if (m1.getDistance() > DISTANCE_FAR) {
					im.showContentView(false);
				} else {
					im.showContentView(true);
				}
			}

			int collisions = 1;
			for (ARMarker m2 : markers) {
				if (m1.equals(m2) || updatedMarkers.contains(m2)
						|| !m2.isInView()) {
					continue;
				}

				if (m1.isMarkerOnMarker(m2)) {
					if (m2.getDistance() > DISTANCE_FAR) {
						off = COLLISION_ADJUSTMENT_FAR;
					} else if (m2.getDistance() > DISTANCE_MIDDLE) {
						off = COLLISION_ADJUSTMENT_MIDDLE;
						m2.setScale(0.7f);
					} else {
						off = COLLISION_ADJUSTMENT_NEAR;
						m2.setScale(0.9f);
					}

					m1.getLocation().get(locationArray);
					float y = locationArray[1];
					float h = collisions * off;
					locationArray[1] = y + h;
					m2.getLocation().set(locationArray);
					m2.update(canvas, 0, 0);
					collisions++;
					updatedMarkers.add(m2);
				}
			}
			updatedMarkers.add(m1);
		}
	}

	/**
	 * 设置冲突检测
	 * 
	 * @param use
	 */
	public void setCollisionDetection(boolean use) {
		useCollisionDetection = use;
	}

	@Override
	public boolean performClick() {
		return super.performClick();
	}

}
