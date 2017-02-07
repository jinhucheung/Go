package com.imagine.go.ar;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.TextView;
import com.amap.api.services.core.PoiItem;
import com.imagine.go.R;
import com.imagine.go.model.GeoPoint;
import com.imagine.go.util.GeoCalcUtil;
import com.imagine.go.util.PoiTypeMatcher.Icon;

/**
 * ARIconMarker: <br/>
 * 增强ARMarker
 * 
 * @author Jinhu
 * @date 2016/5/12
 */
public class ARIconMarker extends ARMarker {

	/* Marker视图 . */
	private View mMarkerView = null;
	/* 类型图 . */
	private ImageView mTypeImg = null;
	/* 内容 . */
	private View mContent = null;
	/* 名字 . */
	private TextView mNameText = null;
	/* 距离 . */
	private TextView mDistanceText = null;

	/* Poi类型图 . */
	private Icon mPoiIcon = null;

	private Paint mPaint = new Paint();

	public ARIconMarker(PoiItem poi) {
		super(poi);
		alpha_off = -30;
	}

	public ARIconMarker(GeoPoint poi) {
		super(poi);
		alpha_off = -30;
	}

	/**
	 * 初始化视图组件
	 */
	@SuppressLint("InflateParams")
	public void initView(LayoutInflater inflater) {
		if (null == inflater)
			throw new NullPointerException();
		mMarkerView = inflater.inflate(R.layout.item_ar_marker, null);
		if (null == mMarkerView)
			return;
		mTypeImg = (ImageView) mMarkerView
				.findViewById(R.id.id_imageView_poiType);
		mContent = mMarkerView.findViewById(R.id.id_layout_poiContent);
		mNameText = (TextView) mMarkerView
				.findViewById(R.id.id_textView_poiName);
		mDistanceText = (TextView) mMarkerView
				.findViewById(R.id.id_textView_poiDinstance);
	}

	/**
	 * 更新MarkerView
	 */
	private void updateMarkerView() {
		if (null == mMarkerView)
			return;

		if (null != mPoiIcon) {
			mTypeImg.setImageResource(mPoiIcon.getType());
			mTypeImg.setBackgroundResource(mPoiIcon.getBackground());
		}

		mNameText.setText(getName());
		mDistanceText.setText(GeoCalcUtil.formatDistance(getDistance()));
	}

	public void showContentView(boolean isShow) {
		if (null == mContent)
			return;
		mContent.setVisibility(isShow ? View.VISIBLE : View.GONE);
	}

	public void setPoiIcon(Icon icon) {
		mPoiIcon = icon;
	}

	/**
	 * 视图转换成位图
	 * 
	 * @param v
	 * @return
	 */
	private Bitmap convertViewToBitmap(View v) {
		v.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
		v.buildDrawingCache();
		Bitmap bitmap = v.getDrawingCache();
		return bitmap;
	}

	@Override
	public synchronized float getWidth() {
		if (null == mMarkerView)
			return super.getWidth();
		return mMarkerView.getWidth();
	}

	@Override
	public synchronized float getHeight() {
		if (null == mMarkerView)
			return super.getHeight();
		return mMarkerView.getHeight();
	}

	@Override
	public synchronized void draw(Canvas canvas) {
		if (null == canvas)
			throw new NullPointerException();

		if (!isOnRadar || !isInView)
			return;

		updateMarkerView();
		Bitmap mBtimap = convertViewToBitmap(mMarkerView);
		mBtimap = Bitmap.createScaledBitmap(mBtimap,
				(int) (getWidth() * scale), (int) (getHeight() * scale), true);

		getScreenPosition().get(screenPositionArray);
		float x = screenPositionArray[0] - (getWidth() / 2);
		float y = screenPositionArray[1] - (getHeight() / 2);

		if (255 < alpha + alpha_off) {
			mPaint.setAlpha(255);
		} else
			mPaint.setAlpha(alpha + alpha_off);
		canvas.save();
		canvas.drawBitmap(mBtimap, x, y, mPaint);
		canvas.restore();
	}

	/**
	 * 判断接触点是否在Marker
	 * 
	 * @param x
	 * @param y
	 * @param marker
	 * @return
	 */
	@Override
	protected synchronized boolean isPointOnMarker(float x, float y,
			ARMarker marker) {
		marker.getScreenPosition().get(screenPositionArray);
		float myX = screenPositionArray[0];
		float myY = screenPositionArray[1];
		float adjWidth = marker.getWidth() / 2;
		float adjHeight = marker.getHeight() / 2;

		float x1 = 0, y1 = 0, x2 = 0, y2 = 0;
		float off = 2;

		x1 = myX - adjWidth - off;
		y1 = myY - adjHeight - off;
		x2 = myX + adjWidth + off;
		y2 = myY + adjHeight + off;

		if (x >= x1 && x <= x2 && y >= y1 && y <= y2)
			return true;

		return false;
	}
}
