package com.imagine.go.ar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import com.imagine.go.Constants;
import com.imagine.go.ar.model.ScreenPosition;
import com.imagine.go.ar.paintable.PaintableCircle;
import com.imagine.go.ar.paintable.PaintableContainer;
import com.imagine.go.ar.paintable.PaintableLine;
import com.imagine.go.ar.paintable.PaintableRadarPoints;
import com.imagine.go.ar.paintable.PaintableText;

/**
 * 雷达控制
 * 
 * @author Jinhu
 * @date 2016年4月17日
 */
public class RadarView extends View {
	/* 雷达图半径 . */
	public static final float RADIUS = 70;
	/* 雷达图扇形线颜色 . */
	private static final int LINE_COLOR = Color.argb(100, 255, 255, 255);
	/* 雷达图环形颜色. */
	private static final int RING_COLOR = Color.argb(200, 255, 255, 255);
	/* 雷达图圈内颜色 . */
	private static final int RADRA_COLOR = Color.argb(100, 100, 100, 100);
	/* 雷达图文本颜色. */
	private static final int TEXT_COLOR = Color.rgb(255, 255, 255);
	/* 雷达图文本大小. */
	private static final int TEXT_SIZE = 12;

	/* 雷达图扇形线端点相对屏幕坐标 . */
	private static ScreenPosition leftRadarLine = null;
	private static ScreenPosition rightRadarLine = null;

	/* 扇形线绘图容器. */
	private static PaintableContainer leftLineContainer = null;
	private static PaintableContainer rightLineContainer = null;

	/* 圆环绘图容器 . */
	private static PaintableContainer ringContainer = null;
	/* 圆形绘图容器 . */
	private static PaintableContainer circleContainer = null;

	/* 雷达图文本. */
	private static PaintableText radarText;
	/* 雷达图文本容器 . */
	private static PaintableContainer textContainer = null;

	/* 雷达图点 . */
	private static PaintableRadarPoints radarPoints = null;
	/* 雷达图点容器. */
	private static PaintableContainer radarPointsContainer = null;

	/* 雷达图坐标 . */
	protected float mX = 0;
	protected float mY = 0;

	/* 雷达图导航模式 . */
	protected boolean onNaviMode = false;

	public RadarView(Context context) {
		this(context, null);
	}

	public RadarView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RadarView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		if (null == leftRadarLine) {
			leftRadarLine = new ScreenPosition();
		}
		if (null == rightRadarLine) {
			rightRadarLine = new ScreenPosition();
		}
		if (mX <= 0) {
			mX = Constants.DEFAULT_RADAR_X;
		}
		if (mY <= 0) {
			mY = Constants.DEFAULT_RADAR_Y;
		}
	}

	/**
	 * 设置雷达位置
	 * 
	 * @param x
	 * @param y
	 */
	public void setLocation(float x, float y) {
		mX = x;
		mY = y;
		if (mX <= 0) {
			mX = Constants.DEFAULT_RADAR_X;
		}
		if (mY <= 0) {
			mY = Constants.DEFAULT_RADAR_Y;
		}
	}

	/**
	 * 设置导航模式
	 * 
	 * @param mode
	 */
	public void setOnNaviMode(boolean mode) {
		onNaviMode = mode;
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
		// 绘制雷达点
		drawRadarPoints(canvas);
		// 绘制扇形线
		drawRadarLines(canvas);
		// 绘制方位描述文本
		drawRadarText(canvas);

	}

	/**
	 * 绘制扇形线
	 * 
	 * @param canvas
	 */
	protected void drawRadarLines(Canvas canvas) {
		if (null == canvas)
			throw new NullPointerException();
		// 绘制左扇形线
		if (null == leftLineContainer) {
			leftRadarLine.set(0, -RADIUS + 10);
			leftRadarLine.rotate(-Constants.DEFAULT_CAMERA_VIEW_ANGLE / 2);
			leftRadarLine.add(mX + RADIUS, mY + RADIUS); // 左扇形线端点相对屏幕原点的坐标

			float leftX = leftRadarLine.getX() - (mX + RADIUS);
			float leftY = leftRadarLine.getY() - (mY + RADIUS); // 左扇形线端点相对与雷达中点的坐标

			PaintableLine leftLine = new PaintableLine(LINE_COLOR, leftX, leftY);
			leftLine.setStrokeWidth(2);
			// 生成绘图容器
			leftLineContainer = new PaintableContainer(leftLine, mX + RADIUS,
					mY + RADIUS, 0, 1);
		}
		leftLineContainer.paint(canvas);

		// 绘制右扇形线
		if (null == rightLineContainer) {
			rightRadarLine.set(0, -RADIUS + 10);
			rightRadarLine.rotate(Constants.DEFAULT_CAMERA_VIEW_ANGLE / 2);
			rightRadarLine.add(mX + RADIUS, mY + RADIUS); // 左扇形线端点相对屏幕原点的坐标

			float rightX = rightRadarLine.getX() - (mX + RADIUS);
			float rightY = rightRadarLine.getY() - (mY + RADIUS); // 左扇形线端点相对与雷达中点的坐标

			PaintableLine rightLine = new PaintableLine(LINE_COLOR, rightX,
					rightY);
			rightLine.setStrokeWidth(2);
			// 生成绘图容器
			rightLineContainer = new PaintableContainer(rightLine, mX + RADIUS,
					mY + RADIUS, 0, 1);
		}
		rightLineContainer.paint(canvas);
	}

	/**
	 * 绘制雷达圆
	 * 
	 * @param canvas
	 */
	protected void drawRadarCircle(Canvas canvas) {
		if (null == canvas)
			throw new NullPointerException();

		// 绘制圆环
		if (null == ringContainer) {
			PaintableCircle mRing = new PaintableCircle(RING_COLOR, RADIUS,
					false);
			mRing.setStrokeWidth(3);
			ringContainer = new PaintableContainer(mRing, mX + RADIUS, mY
					+ RADIUS, 0, 1);
		}
		ringContainer.paint(canvas);

		if (null == circleContainer) {
			PaintableCircle mCircle = new PaintableCircle(RADRA_COLOR, RADIUS,
					true);
			circleContainer = new PaintableContainer(mCircle, mX + RADIUS, mY
					+ RADIUS, 0, 1);
		}
		circleContainer.paint(canvas);
	}

	protected void drawRadarText(Canvas canvas) {
		if (null == canvas)
			throw new NullPointerException();

		// 将正北方向偏角映射到[0,15]范围内
		int range = (int) (ARData.getInstance().getAzimuth() / (360f / 16f));
		String dirTxt = "";

		if (range == 15 || range == 0)
			dirTxt = "N";
		else if (range == 1 || range == 2)
			dirTxt = "NE";
		else if (range == 3 || range == 4)
			dirTxt = "E";
		else if (range == 5 || range == 6)
			dirTxt = "SE";
		else if (range == 7 || range == 8)
			dirTxt = "S";
		else if (range == 9 || range == 10)
			dirTxt = "SW";
		else if (range == 11 || range == 12)
			dirTxt = "W";
		else if (range == 13 || range == 14)
			dirTxt = "NW";

		int bearing = (int) ARData.getInstance().getAzimuth();
		String txt = "" + bearing + ((char) 176) + " " + dirTxt;// 构建方位描述文本
		paintText(canvas, txt, (mX + RADIUS), (mY - 5), true);
	}

	/**
	 * 绘制文本
	 * 
	 * @param canvas
	 * @param txt
	 * @param x
	 * @param y
	 * @param bg
	 */
	protected void paintText(Canvas canvas, String txt, float x, float y,
			boolean bg) {
		if (null == canvas || null == txt)
			throw new NullPointerException();

		if (null == radarText) {
			radarText = new PaintableText(txt, TEXT_COLOR, TEXT_SIZE, bg);
		} else {
			radarText.set(txt, TEXT_COLOR, TEXT_SIZE, bg);
		}

		if (null == textContainer) {
			textContainer = new PaintableContainer(radarText, x, y, 0, 1);
		} else {
			textContainer.set(radarText, x, y, 0, 1);
		}
		textContainer.paint(canvas);
	}

	/**
	 * 绘制雷达点
	 * 
	 * @param canvas
	 */
	protected void drawRadarPoints(Canvas canvas) {
		if (null == canvas)
			throw new NullPointerException();
		if (null == radarPoints) {
			radarPoints = new PaintableRadarPoints();
		}
		radarPoints.setOnNaviMode(onNaviMode);

		if (null == radarPointsContainer) {
			radarPointsContainer = new PaintableContainer(radarPoints, mX, mY,
					-ARData.getInstance().getAzimuth(), 1);
		} else {
			radarPointsContainer.set(radarPoints, mX, mY, -ARData.getInstance()
					.getAzimuth(), 1);
		}
		radarPointsContainer.paint(canvas);
	}
}
