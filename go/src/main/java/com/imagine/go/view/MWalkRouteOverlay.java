package com.imagine.go.view;

import static com.imagine.go.Constants.NO_HIDE;
import android.content.Context;
import android.graphics.Color;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.overlay.WalkRouteOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.WalkPath;
import com.imagine.go.R;

/**
 * MWalkRouteOverlay: 高德路径规划图层
 * 
 * @自定义图层样式
 * @author Jinhu
 * @date 2016年3月28日
 */
public class MWalkRouteOverlay extends WalkRouteOverlay {

	private static final float RouteWidth = 10f;

	public MWalkRouteOverlay(Context context, AMap amap, WalkPath walkpath,
			LatLonPoint startPos, LatLonPoint targetPos) {
		super(context, amap, walkpath, startPos, targetPos);
		this.nodeIconVisible = false;

	}

	@Override
	protected BitmapDescriptor getStartBitmapDescriptor() {
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.ic_map_start);
		return bitmap;
	}

	@Override
	protected BitmapDescriptor getEndBitmapDescriptor() {
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.ic_map_target);
		return bitmap;
	}

	@Override
	protected int getWalkColor() {
		return Color.parseColor("#4990fa");
	}

	@Override
	protected float getRouteWidth() {
		return RouteWidth;
	}

	public void setEndMarker(Marker m) {
		this.endMarker.setTitle(m.getTitle());
		this.endMarker.setSnippet(m.getSnippet());
		this.endMarker.setObject(NO_HIDE);
	}

	public void setStartMarker(Marker m) {
		this.startMarker.setTitle(m.getTitle());
		this.startMarker.setSnippet(m.getSnippet());
		this.startMarker.setObject(NO_HIDE);
		m.setObject(NO_HIDE);
	}

}
