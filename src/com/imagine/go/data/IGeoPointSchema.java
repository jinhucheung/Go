package com.imagine.go.data;

/**
 * GeoPoint数据模式
 * 
 * @author Jinhu
 * @date 2016/5/21
 */
public interface IGeoPointSchema {

	String GEOPOINT_TABLE = "geopoints";
	String COLUMN_ID = "_id";
	String COLUMN_POINT_NAME = "point_name";
	String COLUMN_LAT = "latitude";
	String COLUMN_LON = "longitude";
	String COLUMN_ALT = "altitude";
	String COLUMN_ADDRESS = "address";
	String COLUMN_CITY = "city_name";
	String COLUMN_CITY_CODE = "city_code";
	String COLUMN_SNIPPET = "snippet";
	String COLUMN_URL = "url";

	String[] GEOPOINT_COLUMNS = new String[] { COLUMN_ID, COLUMN_POINT_NAME,
			COLUMN_LAT, COLUMN_LON, COLUMN_ALT, COLUMN_ADDRESS, COLUMN_CITY,
			COLUMN_CITY_CODE, COLUMN_SNIPPET, COLUMN_URL };

	String GEO_POINTS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "//
			+ GEOPOINT_TABLE//
			+ "("//
			+ COLUMN_ID //
			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"//
			+ COLUMN_POINT_NAME //
			+ " TEXT,"//
			+ COLUMN_LAT//
			+ " REAL,"//
			+ COLUMN_LON//
			+ " REAL,"//
			+ COLUMN_ALT//
			+ " REAL,"//
			+ COLUMN_ADDRESS//
			+ " TEXT,"//
			+ COLUMN_CITY//
			+ " TEXT,"//
			+ COLUMN_CITY_CODE//
			+ " TEXT,"//
			+ COLUMN_SNIPPET//
			+ " TEXT,"//
			+ COLUMN_URL//
			+ " TEXT"//
			+ " )";
}
