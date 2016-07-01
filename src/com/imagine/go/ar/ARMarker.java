package com.imagine.go.ar;

import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.PoiItem;
import com.imagine.go.Constants;
import com.imagine.go.ar.model.CameraModel;
import com.imagine.go.ar.model.PhysicalLocation;
import com.imagine.go.ar.model.Vector;
import com.imagine.go.ar.paintable.PaintableBox;
import com.imagine.go.ar.paintable.PaintableBoxedText;
import com.imagine.go.ar.paintable.PaintableCircle;
import com.imagine.go.ar.paintable.PaintableContainer;
import com.imagine.go.ar.paintable.PaintableObject;
import com.imagine.go.model.GeoPoint;
import com.imagine.go.util.GeoCalcUtil;

/**
 * AR兴趣点信息图标
 * 
 * @author Jinhu
 * @date 2016/4/15
 */
public class ARMarker implements Comparable<ARMarker> {

	/* 兴趣点的基本信息 . */
	protected String id = null;
	protected String name = null;
	protected String address = null;
	protected String info = null;
	protected String type = null;
	protected LatLng latlng = null;
	protected double altitude = 0.0d;
	protected PoiItem poiItem = null;
	protected GeoPoint geoPoint = null;

	/* Marker绘制属性 . */
	public static final int DEFAULT_ALPHA_OFF = -20;
	protected int color = Color.WHITE;
	protected int alpha = 255;
	protected int alpha_off = DEFAULT_ALPHA_OFF;
	protected int maxWidth = 500;
	protected float scale = 1;

	/* Marker的地理坐标 . */
	protected volatile PhysicalLocation physicalLocation = new PhysicalLocation();
	/* Marker相对与当前用户所在的向量. */
	protected final Vector locationXYZRelative2PhysicalLocation = new Vector();
	protected final float[] locationArray = new float[3];
	/* 相对当前用户所在的高程(屏幕Y轴) . */
	private float initialY = 0.0f;

	/* 当前所在地与Marker的距离 . */
	protected volatile double distance = 0.0d;
	private final float[] distanceArray = new float[1];

	/* Marker相对的摄像视图坐标. */
	protected final Vector symbolXYZRelative2CameraView = new Vector();
	protected final Vector textXYZRelative2CameraView = new Vector();
	protected final float[] symbolArray = new float[3];
	protected final float[] textArray = new float[3];

	private static final Vector symbolVector = new Vector(0, 0, 0);
	private static final Vector textVector = new Vector(0, 1, 0);

	/* 过渡向量 . */
	private final Vector tmpVector = new Vector();
	private final Vector tmpSymbolVector = new Vector();
	private final Vector tmpTextVector = new Vector();

	/* 屏幕向量 . */
	protected final Vector screenPositionVector = new Vector();
	protected final float[] screenPositionArray = new float[3];

	/* Marker状态 . */
	protected volatile boolean isOnRadar = false;
	protected volatile boolean isInView = false;
	protected volatile boolean isUpdated = false;

	/* 摄像投影变换 . */
	private volatile static CameraModel cam = null;

	/* 文本框 . */
	private volatile PaintableBoxedText textBox = null;
	private volatile PaintableContainer textBoxContainer = null;

	/* 图标. */
	protected volatile boolean isNeedIcon = false;
	protected volatile PaintableObject iconSymbol = null;
	protected volatile PaintableContainer iconSymbolContainer = null;

	/* 检测冲突 . */
	private static boolean debugCollisionZone = false;
	private static PaintableBox collisionBox = null;
	private static PaintableContainer collisionBoxContainer = null;

	/* 检测接触. */
	private static boolean debugTouchZone = false;
	private static PaintableBox touchBox = null;
	private static PaintableContainer touchBoxContainer = null;

	public ARMarker(PoiItem poi) {
		this(poi.getPoiId(), //
				poi.getTitle(), //
				poi.getSnippet(), //
				poi.getSnippet(),//
				poi.getTypeDes(), //
				poi.getLatLonPoint().getLatitude(), //
				poi.getLatLonPoint().getLongitude(),//
				0.0d);
		poiItem = poi;
	}

	public ARMarker(GeoPoint poi) {
		this(poi.getId(), //
				poi.getName(), //
				poi.getAddress(), //
				poi.getSnippet(),//
				poi.getURL(), //
				poi.getlatitude(), //
				poi.getlongitude(),//
				0.0d);
		this.geoPoint = poi;
	}

	public ARMarker(String id, String name, String address, String info,
			String type, double latitude, double longitude, double altitude) {
		set(id, name, address, info, type, latitude, longitude, altitude);
	}

	public synchronized void set(String id, String name, String address,
			String info, String type, double latitude, double longitude,
			double altitude) {
		if (null == id || "".equals(id))
			throw new NullPointerException();

		this.id = id;
		this.name = name;
		this.address = address;
		this.info = info;
		this.type = type;

		this.physicalLocation.set(latitude, longitude, altitude);
		this.locationXYZRelative2PhysicalLocation.set(0, 0, 0);
		this.initialY = 0.0f;

		this.symbolXYZRelative2CameraView.set(0, 0, 0);
		this.textXYZRelative2CameraView.set(0, 0, 0);

		this.isOnRadar = false;
		this.isInView = false;

		this.latlng = new LatLng(latitude, longitude);
		this.altitude = altitude;
	}

	public synchronized void update(Canvas canvas, float addX, float addY) {
		if (null == canvas)
			throw new NullPointerException();

		if (null == cam)
			cam = new CameraModel(canvas.getWidth(), canvas.getHeight());
		else
			cam.set(canvas.getWidth(), canvas.getHeight());
		cam.setViewAngle(Constants.DEFAULT_CAMERA_VIEW_ANGLE);
		// 摄像投影变换 大地坐标变换到屏幕坐标
		populateMatrices(cam, addX, addY);
		updateRadar();
		updateView();
	}

	/**
	 * 更新雷达图状态
	 */
	private synchronized void updateRadar() {
		isOnRadar = false;

		float range = ARData.getInstance().getRadius();
		float scale = range / Radar.RADIUS;
		locationXYZRelative2PhysicalLocation.get(locationArray);
		float x = locationArray[0] / scale;
		float y = locationArray[2] / scale;

		float[] tmpArrays = new float[3];
		if (isNeedIcon) {
			symbolXYZRelative2CameraView.get(tmpArrays);
		} else {
			textXYZRelative2CameraView.get(tmpArrays);
		}
		if ((tmpArrays[2] < -1f)
				&& (x * x + y * y) < (Radar.RADIUS * Radar.RADIUS)) {
			isOnRadar = true;
		}
	}

	/**
	 * 更新视图状态
	 */
	private synchronized void updateView() {
		isInView = false;

		float[] tmpArrays = new float[3];
		if (isNeedIcon) {
			symbolXYZRelative2CameraView.get(tmpArrays);
		} else {
			textXYZRelative2CameraView.get(tmpArrays);
		}

		float x1 = tmpArrays[0] + (getWidth() / 2);
		float y1 = tmpArrays[1] + (getHeight() / 2);
		float x2 = tmpArrays[0] - (getWidth() / 2);
		float y2 = tmpArrays[1] - (getHeight() / 2);
		if (x1 >= -1 && x2 <= (cam.getWidth()) && y1 >= -1
				&& y2 <= (cam.getHeight())) {
			isInView = true;
		}
	}

	/**
	 * 摄像投影变换 大地坐标变换到屏幕坐标
	 * 
	 * @param cam
	 * @param addX
	 * @param addY
	 */
	private synchronized void populateMatrices(CameraModel cam, float addX,
			float addY) {
		if (null == cam)
			throw new NullPointerException();

		tmpSymbolVector.set(symbolVector);
		tmpSymbolVector.add(locationXYZRelative2PhysicalLocation);
		tmpSymbolVector.prod(ARData.getInstance().getRotationMatrix()); // 大地坐标向摄像机坐标变换

		tmpTextVector.set(textVector);
		tmpTextVector.add(locationXYZRelative2PhysicalLocation);
		tmpTextVector.prod(ARData.getInstance().getRotationMatrix()); // 大地坐标向摄像机坐标变换

		cam.projectPoint(tmpSymbolVector, tmpVector, addX, addY); // 摄像机坐标向屏幕坐标变换
		symbolXYZRelative2CameraView.set(tmpVector);
		cam.projectPoint(tmpTextVector, tmpVector, addX, addY); // 摄像机坐标向屏幕坐标变换
		textXYZRelative2CameraView.set(tmpVector);
	}

	/**
	 * 计算Marker的大地坐标
	 * 
	 * @param mylocation
	 *            用户位置
	 */
	public synchronized void calcRelativePosition(AMapLocation mylocation) {
		if (null == mylocation)
			throw new NullPointerException();

		updateDistance(mylocation);

		if (physicalLocation.getAltitude() == 0.0)
			physicalLocation.setAltitude(mylocation.getAltitude());

		// 计算相对当前用户坐标的方向向量
		physicalLocation.convLocationToVector(mylocation,
				locationXYZRelative2PhysicalLocation); // 地理坐标向大地坐标变换

		this.initialY = locationXYZRelative2PhysicalLocation.getY();

		updateRadar();
	}

	public synchronized float getWidth() {
		if (!isNeedIcon) {
			if (null != textBoxContainer)
				return textBoxContainer.getWidth();
		}
		if (iconSymbolContainer == null || textBoxContainer == null)
			return 0f;
		float w1 = textBoxContainer.getWidth();
		float w2 = iconSymbolContainer.getWidth();
		return (w1 > w2) ? w1 : w2;
	}

	public synchronized float getHeight() {
		if (!isNeedIcon) {
			if (null != textBoxContainer)
				return textBoxContainer.getHeight();
		}
		if (iconSymbolContainer == null || textBoxContainer == null)
			return 0f;
		return iconSymbolContainer.getHeight() + textBoxContainer.getHeight();
	}

	// ------------------------------访问属性---------------------------
	// ----兴趣点信息----
	public synchronized String getId() {
		return id;
	}

	public synchronized String getName() {
		return name;
	}

	public synchronized void setName(String name) {
		this.name = name;
	}

	public synchronized String getAddress() {
		return address;
	}

	public synchronized void setAddress(String address) {
		this.address = address;
	}

	public synchronized String getInfo() {
		return info;
	}

	public synchronized void setInfo(String info) {
		this.info = info;
	}

	public synchronized String getType() {
		return type;
	}

	public synchronized void setType(String type) {
		this.type = type;
	}

	public synchronized PhysicalLocation getPhysicalLocation() {
		return physicalLocation;
	}

	public synchronized void setPhysicalLocation(double latitude,
			double longitude, double altitude) {
		physicalLocation.set(latitude, longitude, altitude);
	}

	public synchronized double getDistance() {
		return distance;
	}

	public synchronized void setDistance(double distance) {
		this.distance = distance;
	}

	public synchronized void updateDistance(AMapLocation mylocation) {
		if (null == mylocation)
			throw new NullPointerException();

		Location.distanceBetween(physicalLocation.getLatitude(),
				physicalLocation.getLongitude(), mylocation.getLatitude(),
				mylocation.getLongitude(), distanceArray);
		distance = distanceArray[0];
	}

	public synchronized LatLng getLatLng() {
		return this.latlng;
	}

	public synchronized double getAltitude() {
		return this.altitude;
	}

	// -----------------------Marker图标信息-----------------------
	public synchronized void setAlpha(int a) {
		this.alpha = a;
	}

	public synchronized void setColor(int c) {
		this.color = c;
	}

	public synchronized int getColor() {
		return this.color;
	}

	public synchronized void setMaxWidth(int width) {
		this.maxWidth = width;
	}

	public synchronized void setScale(float scale) {
		this.scale = scale;
	}

	public synchronized float getScale() {
		return this.scale;
	}

	public synchronized void setAlphaOff(int off) {
		this.alpha_off = off;
	}

	// -----------------------Marker事件处理-----------------------
	/**
	 * 判断Marker是否接触
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public synchronized boolean handleClick(float x, float y) {
		if (!isOnRadar || !isInView)
			return false;
		return isPointOnMarker(x, y, this);
	}

	/**
	 * 判断Marker是否重叠
	 * 
	 * @param marker
	 * @return
	 */
	public synchronized boolean isMarkerOnMarker(ARMarker marker) {
		return isMarkerOnMarker(marker, true);
	}

	/**
	 * 判断接触点是否在Marker
	 * 
	 * @param x
	 * @param y
	 * @param marker
	 * @return
	 */
	protected synchronized boolean isPointOnMarker(float x, float y,
			ARMarker marker) {
		marker.getScreenPosition().get(screenPositionArray);
		float myX = screenPositionArray[0];
		float myY = screenPositionArray[1];
		float adjWidth = marker.getWidth() / 2;
		float adjHeight = marker.getHeight() / 2;

		float x1 = 0, y1 = 0, x2 = 0, y2 = 0;
		float off = 2;
		if (!isNeedIcon) {
			x1 = myX - adjWidth - off;
			y1 = myY - off;
			x2 = myX + adjWidth + off;
			y2 = myY + adjHeight * 2 + off;
		} else {
			x1 = myX - adjWidth;
			y1 = myY - adjHeight;
			x2 = myX + adjWidth;
			y2 = myY + adjHeight;
		}
		if (x >= x1 && x <= x2 && y >= y1 && y <= y2)
			return true;

		return false;
	}

	/**
	 * 判断Marker是否重叠
	 * 
	 * @param marker
	 * @param reflect
	 * @return
	 */
	private synchronized boolean isMarkerOnMarker(ARMarker marker,
			boolean reflect) {
		marker.getScreenPosition().get(screenPositionArray);
		float x = screenPositionArray[0];
		float y = screenPositionArray[1];
		boolean middleOfMarker = isPointOnMarker(x, y, this);
		if (middleOfMarker)
			return true;

		float halfWidth = marker.getWidth() / 2;
		float halfHeight = marker.getHeight() / 2;

		float x1 = x - halfWidth;
		float y1 = y - halfHeight;
		boolean upperLeftOfMarker = isPointOnMarker(x1, y1, this);
		if (upperLeftOfMarker)
			return true;

		float x2 = x + halfWidth;
		float y2 = y1;
		boolean upperRightOfMarker = isPointOnMarker(x2, y2, this);
		if (upperRightOfMarker)
			return true;

		float x3 = x1;
		float y3 = y + halfHeight;
		boolean lowerLeftOfMarker = isPointOnMarker(x3, y3, this);
		if (lowerLeftOfMarker)
			return true;

		float x4 = x2;
		float y4 = y3;
		boolean lowerRightOfMarker = isPointOnMarker(x4, y4, this);
		if (lowerRightOfMarker)
			return true;

		return (reflect) ? marker.isMarkerOnMarker(this, false) : false;
	}

	// -----------------------绘制Marker-----------------------

	public synchronized void draw(Canvas canvas) {
		if (null == canvas)
			throw new NullPointerException();

		if (!isOnRadar || !isInView)
			return;

		if (debugCollisionZone)
			drawCollisionZone(canvas);
		if (debugTouchZone)
			drawTouchZone(canvas);
		if (isNeedIcon)
			drawIcon(canvas);
		drawText(canvas);
	}

	/**
	 * 绘制文本框
	 * 
	 * @param canvas
	 */
	protected synchronized void drawText(Canvas canvas) {
		if (null == canvas)
			throw new NullPointerException();

		textXYZRelative2CameraView.get(textArray);
		symbolXYZRelative2CameraView.get(symbolArray);

		float maxHeight = Math.round(canvas.getHeight() / 12f) + 1;
		if (null == textBox) {
			textBox = new PaintableBoxedText(name,
					Math.round(maxHeight / 2f) + 1, maxWidth);
		} else {
			textBox.set(name, Math.round(maxHeight / 2f) + 1, maxWidth);
		}
		textBox.adjAlpha(alpha_off);

		// float currentAngle = GeoCalcUtil.getAngle(symbolArray[0],
		// symbolArray[1], textArray[0], textArray[1]);
		// float angle = currentAngle + 90;

		float x = textArray[0] - (textBox.getWidth() / 2);
		float y = textArray[1] + maxHeight;

		if (null == textBoxContainer) {
			textBoxContainer = new PaintableContainer(textBox, x, y, 0, scale);
		} else {
			textBoxContainer.set(textBox, x, y, 0, scale);
		}

		textBoxContainer.paint(canvas);
	}

	/**
	 * 绘制图标
	 * 
	 * @param canvas
	 */
	protected synchronized void drawIcon(Canvas canvas) {
		if (null == canvas)
			throw new NullPointerException();

		if (null == iconSymbol) {
			iconSymbol = new PaintableCircle(Color.TRANSPARENT, 36, true);
			iconSymbol.setAlpha(this.alpha);
		}

		textXYZRelative2CameraView.get(textArray);
		symbolXYZRelative2CameraView.get(symbolArray);

		// float currentAngle = GeoCalcUtil.getAngle(symbolArray[0],
		// symbolArray[1], textArray[0], textArray[1]);
		// float angle = currentAngle + 90;

		if (null == iconSymbolContainer) {
			iconSymbolContainer = new PaintableContainer(iconSymbol,
					symbolArray[0], symbolArray[1], 0, scale);
		} else {
			iconSymbolContainer.set(iconSymbol, symbolArray[0], symbolArray[1],
					0, scale);
		}

		iconSymbolContainer.paint(canvas);
	}

	/**
	 * 绘制冲突域
	 * 
	 * @param canvas
	 */
	protected synchronized void drawCollisionZone(Canvas canvas) {
		if (null == canvas)
			throw new NullPointerException();

		getScreenPosition().get(screenPositionArray);
		float x = screenPositionArray[0];
		float y = screenPositionArray[1];

		float width = getWidth();
		float height = getHeight();
		float halfWidth = width / 2;
		float halfHeight = height / 2;

		float x1 = x - halfWidth;
		float y1 = y - halfHeight;

		float x2 = x + halfWidth;
		float y2 = y1;

		float x3 = x1;
		float y3 = y + halfHeight;

		float x4 = x2;
		float y4 = y3;

		if (Constants.IS_DEBUG) {
			Log.d("collisionBox", "ul (x=" + x1 + " y=" + y1 + ")");
			Log.d("collisionBox", "ur (x=" + x2 + " y=" + y2 + ")");
			Log.d("collisionBox", "ll (x=" + x3 + " y=" + y3 + ")");
			Log.d("collisionBox", "lr (x=" + x4 + " y=" + y4 + ")");
		}

		if (null == collisionBox) {
			collisionBox = new PaintableBox(width, height, Color.WHITE,
					Color.RED);
		} else {
			collisionBox.set(width, height);
		}

		float currentAngle = GeoCalcUtil.getAngle(symbolArray[0],
				symbolArray[1], textArray[0], textArray[1]) + 90;

		if (null == collisionBoxContainer) {
			collisionBoxContainer = new PaintableContainer(collisionBox, x1,
					y1, currentAngle, scale);
		} else {
			collisionBoxContainer
					.set(collisionBox, x1, y1, currentAngle, scale);
		}
		collisionBoxContainer.paint(canvas);

	}

	/**
	 * 绘制接触域
	 * 
	 * @param canvas
	 */
	protected synchronized void drawTouchZone(Canvas canvas) {
		if (null == canvas)
			throw new NullPointerException();

		if (null == iconSymbol)
			return;

		symbolXYZRelative2CameraView.get(symbolArray);
		textXYZRelative2CameraView.get(textArray);
		float x1 = symbolArray[0];
		float y1 = symbolArray[1];
		float x2 = textArray[0];
		float y2 = textArray[1];
		float width = getWidth();
		float height = getHeight();
		float adjX = (x1 + x2) / 2;
		float adjY = (y1 + y2) / 2;
		float currentAngle = GeoCalcUtil.getAngle(symbolArray[0],
				symbolArray[1], textArray[0], textArray[1]) + 90;
		adjX -= (width / 2);
		adjY -= (iconSymbol.getHeight() / 2);

		if (Constants.IS_DEBUG) {
			Log.d("touchBox", "ul (x=" + (adjX) + " y=" + (adjY) + ")");
			Log.d("touchBox", "ur (x=" + (adjX + width) + " y=" + (adjY) + ")");
			Log.d("touchBox", "ll (x=" + (adjX) + " y=" + (adjY + height) + ")");
			Log.d("touchBox", "lr (x=" + (adjX + width) + " y="
					+ (adjY + height) + ")");
		}

		if (null == touchBox)
			touchBox = new PaintableBox(width, height, Color.WHITE, Color.GREEN);
		else
			touchBox.set(width, height);

		if (null == touchBoxContainer)
			touchBoxContainer = new PaintableContainer(touchBox, adjX, adjY,
					currentAngle, 1);
		else
			touchBoxContainer.set(touchBox, adjX, adjY, currentAngle, 1);
		touchBoxContainer.paint(canvas);
	}

	// -----------------------Marker状态信息-----------------------
	/**
	 * 定位信息
	 * 
	 * @return
	 */
	public synchronized Vector getLocation() {
		return this.locationXYZRelative2PhysicalLocation;
	}

	public synchronized float getInitialY() {
		return this.initialY;
	}

	/**
	 * 获得屏幕坐标向量
	 * 
	 * @return
	 */
	public synchronized Vector getScreenPosition() {
		symbolXYZRelative2CameraView.get(symbolArray);
		textXYZRelative2CameraView.get(textArray);
		float x = 0, y = 0, z = 0;
		if (!isNeedIcon) {
			x = textArray[0];
			y = textArray[1];
			z = textArray[2];
		} else {
			x = (symbolArray[0] + textArray[0]) / 2;
			y = (symbolArray[1] + textArray[1]) / 2;
			z = (symbolArray[2] + textArray[2]) / 2;
		}
		if (null != textBox) {
			y += (textBox.getHeight() / 2);
		}
		screenPositionVector.set(x, y, z);
		return screenPositionVector;
	}

	public synchronized boolean isOnRadar() {
		return this.isOnRadar;
	}

	public synchronized boolean isInView() {
		return this.isInView;
	}

	public synchronized boolean isUpdated() {
		return this.isUpdated;
	}

	public synchronized void updateStatus() {
		isUpdated = true;
	}

	@Override
	public synchronized int compareTo(ARMarker another) {
		if (null == another)
			throw new NullPointerException();
		return id.compareTo(another.getId());
	}

	@Override
	public synchronized boolean equals(Object marker) {
		if (null == marker || null == id)
			throw new NullPointerException();
		if (marker instanceof ARMarker) {
			ARMarker another = (ARMarker) marker;
			return id.equals(another.getId());
		}
		return false;
	}

	/**
	 * 计算此POI与当前位置的偏移角
	 * 
	 * @return 正值偏移角
	 */
	public synchronized float calculShiftedAngle() {
		getLocation().get(locationArray);
		float x = locationArray[0];
		float y = locationArray[2];
		float angle = (float) Math.toDegrees(Math.atan(x / y));

		if (angle < 0) {
			angle = -angle;
		}

		// 修正
		if (x > 0 && y > 0) {
			angle = 180 + angle;
		} else if (x < 0 && y > 0) {
			angle = 180 - angle;
		} else if (x > 0 && y < 0) {
			angle = -angle;
		}

		return angle;
	}

	@Override
	protected ARMarker clone() {
		ARMarker m = null;
		if (null != poiItem) {
			m = new ARMarker(poiItem);
		} else if (null != geoPoint) {
			m = new ARMarker(geoPoint);
		} else {
			m = new ARMarker(id, name, address, info, type, latlng.latitude,
					latlng.longitude, altitude);
		}
		return m;
	}
}
