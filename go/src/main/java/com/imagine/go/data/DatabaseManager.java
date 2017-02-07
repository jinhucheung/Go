package com.imagine.go.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 数据库管理
 * 
 * @author Jinhu
 * @date 2016/5/21
 */
public class DatabaseManager {
	private static final String TAG = DatabaseManager.class.getSimpleName();

	/* 数据库文件 . */
	private static final String DATABSE_NAME = "godatabase.db3";

	/* 数据库文件版本号. */
	private static final int DATABASE_VERSION = 1;

	/* 单例模式 . */
	private static DatabaseManager mInstance;

	/* 环境 . */
	private final Context mContext;

	/* 操作工具类 . */
	private DatabaseHelper mDbHelper;

	/* GeoPoint数据访问接口 . */
	private GeoPointDao mGeoPointDao;

	private DatabaseManager(Context context) {
		mDbHelper = new DatabaseHelper(context, DATABSE_NAME, DATABASE_VERSION);
		mContext = context;
	}

	/**
	 * 获取单例
	 * 
	 * @param context
	 * @return
	 */
	public static DatabaseManager getInstance(Context context) {
		if (null == mInstance) {
			mInstance = new DatabaseManager(context);
		}
		return mInstance;
	}

	/**
	 * 返回GeoPoint数据访问接口
	 * 
	 * @return
	 */
	public GeoPointDao getGeoPointDao() {
		return mGeoPointDao;
	}

	/**
	 * 打开一个数据库
	 * 
	 * @throws SQLException
	 */
	public void open() throws SQLException {
		SQLiteDatabase mDb = mDbHelper.getWritableDatabase();

		mGeoPointDao = new GeoPointDao(mDb);
	}

	/**
	 * 关闭所有数据库
	 */
	public void close() {
		mDbHelper.close();
	}

	/**
	 * 数据库操作工具
	 * 
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context, String name, int version) {
			super(context, name, null, version);
		}

		/**
		 * 初次创建数据库时回调
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(IGeoPointSchema.GEO_POINTS_TABLE_CREATE);
		}

		/**
		 * 更新数据库版本时回调
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + " which destroys all old data");
			db.execSQL("DROP TABLE IF EXISTS " + IGeoPointSchema.GEOPOINT_TABLE);
			onCreate(db);
		}
	}

}
