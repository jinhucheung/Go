package com.imagine.go.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.imagine.go.model.GeoPoint;

/**
 * 封装GeoPoint数据操作
 * 
 * @author Jinhu
 * @date 2016/5/21
 */
public class GeoPointDao extends DbContentProvider implements IGeoPointSchema,
		IGeoPointDao {

	private Cursor cursor;

	private ContentValues insertValues;

	public GeoPointDao(SQLiteDatabase db) {
		super(db);
	}

	@Override
	protected GeoPoint cursorToEntity(Cursor cursor) {
		GeoPoint gPoint = new GeoPoint();

		int idIndex;
		int pointNameIndex;
		int latIndex, lonIndex, altIndex;
		int addressIndex;
		int cityIndex, cityCodeIndex;
		int snippetIndex, urlIndex;

		if (null != cursor) {
			if (-1 != cursor.getColumnIndex(COLUMN_ID)) {
				idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
				gPoint.setId(cursor.getInt(idIndex) + "");
			}

			if (-1 != cursor.getColumnIndex(COLUMN_POINT_NAME)) {
				pointNameIndex = cursor
						.getColumnIndexOrThrow(COLUMN_POINT_NAME);
				gPoint.setName(cursor.getString(pointNameIndex));
			}

			if (-1 != cursor.getColumnIndex(COLUMN_LAT)) {
				latIndex = cursor.getColumnIndexOrThrow(COLUMN_LAT);
				gPoint.setlatitude(cursor.getDouble(latIndex));
			}

			if (-1 != cursor.getColumnIndex(COLUMN_LON)) {
				lonIndex = cursor.getColumnIndexOrThrow(COLUMN_LON);
				gPoint.setlongitude(cursor.getDouble(lonIndex));
			}

			if (-1 != cursor.getColumnIndex(COLUMN_ALT)) {
				altIndex = cursor.getColumnIndexOrThrow(COLUMN_ALT);
				gPoint.setAltitude(cursor.getDouble(altIndex));
			}

			if (-1 != cursor.getColumnIndex(COLUMN_ADDRESS)) {
				addressIndex = cursor.getColumnIndexOrThrow(COLUMN_ADDRESS);
				gPoint.setAddress(cursor.getString(addressIndex));
			}

			if (-1 != cursor.getColumnIndex(COLUMN_CITY)) {
				cityIndex = cursor.getColumnIndexOrThrow(COLUMN_CITY);
				gPoint.setCity(cursor.getString(cityIndex));
			}

			if (-1 != cursor.getColumnIndex(COLUMN_CITY_CODE)) {
				cityCodeIndex = cursor.getColumnIndexOrThrow(COLUMN_CITY_CODE);
				gPoint.setCityCode(cursor.getString(cityCodeIndex));
			}

			if (-1 != cursor.getColumnIndex(COLUMN_SNIPPET)) {
				snippetIndex = cursor.getColumnIndexOrThrow(COLUMN_SNIPPET);
				gPoint.setSnippet(cursor.getString(snippetIndex));
			}

			if (-1 != cursor.getColumnIndex(COLUMN_URL)) {
				urlIndex = cursor.getColumnIndexOrThrow(COLUMN_URL);
				gPoint.setURL(cursor.getString(urlIndex));
			}
		}
		return gPoint;
	}

	private void setContentValues(GeoPoint point) {
		insertValues = new ContentValues();
		insertValues.put(COLUMN_POINT_NAME, point.getName());
		insertValues.put(COLUMN_LON, point.getlongitude());
		insertValues.put(COLUMN_LAT, point.getlatitude());
		insertValues.put(COLUMN_ALT, point.getAltitude());
		insertValues.put(COLUMN_ADDRESS, point.getAddress());
		insertValues.put(COLUMN_CITY, point.getCity());
		insertValues.put(COLUMN_CITY_CODE, point.getCityCode());
		insertValues.put(COLUMN_SNIPPET, point.getSnippet());
		insertValues.put(COLUMN_URL, point.getURL());
		try {
			insertValues.put(COLUMN_ID, Integer.parseInt(point.getId()));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			insertValues.putNull(COLUMN_ID);
		}
	}

	public ContentValues getContentValues() {
		return insertValues;
	}

	@Override
	public GeoPoint fetchGeoPointById(int pointId) {
		final String selectionArgs[] = { String.valueOf(pointId) };
		final String selection = COLUMN_ID + "= ? ";
		GeoPoint gPoint = new GeoPoint();
		cursor = super.query(GEOPOINT_TABLE, GEOPOINT_COLUMNS, selection,
				selectionArgs, COLUMN_ID);
		if (null != cursor) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				gPoint = cursorToEntity(cursor);
				cursor.moveToNext();
			}
			cursor.close();
		}
		return gPoint;
	}

	@Override
	public List<GeoPoint> fetchAllGeoPoints() {
		List<GeoPoint> pointList = new ArrayList<GeoPoint>();
		cursor = super.query(GEOPOINT_TABLE, GEOPOINT_COLUMNS, null, null,
				COLUMN_ID);
		if (null != cursor) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				GeoPoint gPoint = cursorToEntity(cursor);
				pointList.add(gPoint);
				cursor.moveToNext();
			}
			cursor.close();
		}
		return pointList;
	}

	@Override
	public boolean addGeoPoint(GeoPoint point) {
		setContentValues(point);
		try {
			return super.insert(GEOPOINT_TABLE, getContentValues()) > 0;
		} catch (SQLiteConstraintException e) {
			Log.w("Database", e.getMessage());
			return false;
		}
	}

	@Override
	public boolean addGeoPoints(List<GeoPoint> points) {
		if (null == points || 0 == points.size())
			return false;
		super.beginTransaction();
		int insertNum = 0;
		try {
			for (GeoPoint point : points) {
				setContentValues(point);
				insertNum += super.insert(GEOPOINT_TABLE, getContentValues());
			}
			super.setTransactionSuccessful();
		} catch (SQLiteConstraintException e) {
			Log.w("Database", e.getMessage());
		} finally {
			super.endTransaction();
		}
		return insertNum > 0;
	}

	@Override
	public boolean deleteAllGeoPoints() {
		try {
			return super.delete(GEOPOINT_TABLE, null, null) > 0;
		} catch (SQLiteConstraintException e) {
			Log.w("Database", e.getMessage());
			return false;
		}
	}

	@Override
	public boolean deleteGeoPoint(int pointId) {
		try {
			final String selection = COLUMN_ID + "= ? ";
			final String selectionArgs[] = { String.valueOf(pointId) };
			return super.delete(GEOPOINT_TABLE, selection, selectionArgs) > 0;
		} catch (SQLiteConstraintException e) {
			Log.w("Database", e.getMessage());
			return false;
		}
	}

	@Override
	public boolean updateGeoPointName(int pointId, String pointName) {
		final ContentValues updateValue = new ContentValues();
		updateValue.put(COLUMN_POINT_NAME, pointName);

		final String selection = COLUMN_ID + "= ?";
		final String selectionArgs[] = { String.valueOf(pointId) };
		return super.update(GEOPOINT_TABLE, updateValue, selection,
				selectionArgs) > 0;
	}

	@Override
	public boolean updateGeoPointURL(int pointId, String url) {
		final ContentValues updateValue = new ContentValues();
		updateValue.put(COLUMN_URL, url);
		final String selection = COLUMN_ID + "= ?";
		final String selectionArgs[] = { String.valueOf(pointId) };
		return super.update(GEOPOINT_TABLE, updateValue, selection,
				selectionArgs) > 0;
	}

}
