package com.imagine.go.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 数据库操作集合
 * 
 * @author Jinhu
 * @date 2016/5/21
 */
public abstract class DbContentProvider {

	public SQLiteDatabase mDb;

	public DbContentProvider(SQLiteDatabase db) {
		mDb = db;
	}

	public int delete(String tableName, String selection, String[] selectionArgs) {
		return mDb.delete(tableName, selection, selectionArgs);
	}

	public long insert(String tableName, ContentValues values) {
		return mDb.insert(tableName, null, values);
	}

	public Cursor query(String tableName, String[] columns, String selection,
			String[] selectionArgs, String sortOrder) {
		final Cursor cursor = mDb.query(tableName, columns, selection,
				selectionArgs, null, null, sortOrder);
		return cursor;
	}

	public Cursor query(String tableName, String[] columns, String selection,
			String[] selectionArgs, String sortOrder, String limit) {
		final Cursor cursor = mDb.query(tableName, columns, selection,
				selectionArgs, null, null, sortOrder, limit);
		return cursor;
	}

	public Cursor query(String tableName, String[] columns, String selection,
			String[] selectionArgs, String group, String having,
			String sortOrder, String limit) {
		final Cursor cursor = mDb.query(tableName, columns, selection,
				selectionArgs, group, having, sortOrder, limit);
		return cursor;
	}

	public int update(String tableName, ContentValues values, String selection,
			String[] selectionArgs) {
		return mDb.update(tableName, values, selection, selectionArgs);
	}

	public Cursor rawQuery(String sql, String[] selectionArgs) {
		return mDb.rawQuery(sql, selectionArgs);
	}

	public void beginTransaction() {
		mDb.beginTransaction();
	}

	public void setTransactionSuccessful() {
		mDb.setTransactionSuccessful();
	}

	public void endTransaction() {
		mDb.endTransaction();
	}

	protected abstract <T> T cursorToEntity(Cursor cursor);

}
