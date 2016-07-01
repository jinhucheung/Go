package com.imagine.go.ar;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.imagine.go.ar.paintable.PaintableArrow;
import com.imagine.go.ar.paintable.PaintableArrowContainer;

/**
 * AR导航控件
 * 
 * @author Jinhu
 * @date 2016/4/21
 */
public class AugmentedNaviView extends View {

	/* 绘制锁 . */
	private final AtomicBoolean drawing = new AtomicBoolean(false);

	/* 当前路径段 . */
	private volatile int mCurStep = 0;

	/* 当前箭头类型 . */
	private volatile int mArrowType = 0;

	/* 屏幕尺寸 . */
	private int mScreenWidth;
	private int mScreenHeight;

	/* 导航箭头对象 . */
	private PaintableArrow mNaviArrow;
	/* 导航箭头容器对象 . */
	private PaintableArrowContainer mNaviArrowContainer;

	/* 当前导航端剩余距离 . */
	private double mCurrentStepDistance;

	public AugmentedNaviView(Context context) {
		this(context, null);

	}

	public AugmentedNaviView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AugmentedNaviView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setScreenWidth(int width) {
		mScreenWidth = width;
	}

	public void setScreenHeight(int height) {
		mScreenHeight = height;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (null == canvas)
			return;
		if (drawing.compareAndSet(false, true)) {
			// 更新Marker状态
			List<ARMarker> markers = ARData.getInstance().getNaviMarkers();

			float angle = 0;

			// 指示下一段路径节点
			int mNextStep = mCurStep + 1;
			if (mNextStep < markers.size()) {
				ARMarker m = markers.get(mNextStep);
				m.update(canvas, 0, 0);

				angle = m.calculShiftedAngle();

				// 偏航调节
				double offDistance = m.getDistance() - mCurrentStepDistance;
				if (offDistance <= 0) {
					mCurrentStepDistance = m.getDistance();
				} else if (offDistance > 30) {
					setCurrentStep(mNextStep);
					setArrowType(((ARNaviMarker) m).getNaviType());
				}
			}

			// 绘制箭头
			if (null == mNaviArrow) {
				mNaviArrow = new PaintableArrow(mScreenWidth / 2,
						mScreenHeight / 2, mArrowType);
			} else {
				mNaviArrow.set(mScreenWidth / 2, mScreenHeight / 2, mArrowType);
			}

			if (null == mNaviArrowContainer) {
				mNaviArrowContainer = new PaintableArrowContainer(mNaviArrow,
						mScreenWidth / 2, mScreenHeight / 2, angle, 70, ARData
								.getInstance().getAzimuth());
			} else {
				mNaviArrowContainer.set(mNaviArrow, mScreenWidth / 2,
						mScreenHeight / 2, angle, 70, ARData.getInstance()
								.getAzimuth());
			}
			mNaviArrowContainer.paint(canvas);
			drawing.set(false);
		}
	}

	public synchronized void setCurrentStep(int step) {
		this.mCurStep = step;
		calculCurStepDistance(step);
	}

	public synchronized void setArrowType(int type) {
		this.mArrowType = type;
	}

	private synchronized void calculCurStepDistance(int step) {
		List<ARMarker> markers = ARData.getInstance().getNaviMarkers();

		int mNextStep = mCurStep + 1;
		if (mNextStep < markers.size()) {
			ARMarker m = markers.get(mNextStep);
			mCurrentStepDistance = m.getDistance();
		}
	}

}
