package com.imagine.go.util;

import static com.imagine.go.Constants.FILE_COLLECTED_LABEL;

import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * CollectedLabelManager:本地管理收藏的搜索标签
 * 
 * @author Jinhu
 * @date 2016/3/21
 */
public class CollectedLabelManager {

	private static final String KEY_LABEL_NAME = "Label_Name";
	private static final String KEY_LABEL_NUM = "Laabel_Num";

	private static CollectedLabelManager instance;
	private Context context;
	private SharedPreferences mShared;
	private int labelNums;

	private CollectedLabelManager(Context context) {
		this.context = context;
		mShared = context.getSharedPreferences(FILE_COLLECTED_LABEL,
				Context.MODE_MULTI_PROCESS);
		labelNums = mShared.getInt(KEY_LABEL_NUM, 0);
	}

	public static CollectedLabelManager getInstance(Context context) {
		if (null == instance) {
			instance = new CollectedLabelManager(context);
		}
		return instance;
	}

	/**
	 * 读取已收藏的标签集合
	 * 
	 * @return List<String>
	 */
	public Set<String> load() {
		Set<String> data = mShared.getStringSet(KEY_LABEL_NAME, null);
		return data;
	}

	/**
	 * 读取已收藏第i个标签
	 * 
	 * @param i
	 * @return
	 */
	public String load(int i) {
		return mShared.getString(KEY_LABEL_NAME + i, "");
	}

	/**
	 * 保存已收藏的标签
	 * 
	 * @param data
	 */
	public void store(Set<String> data) {
		if (null == data || 0 == data.size())
			return;
		SharedPreferences.Editor mEditor = mShared.edit();
		mEditor.putStringSet(KEY_LABEL_NAME, data);
		mEditor.commit();
	}

	/**
	 * 保存一个已收藏的标签
	 * 
	 * @param data
	 */
	public void store(String data) {
		if (null == data || "".equals(data))
			return;
		labelNums++;
		SharedPreferences.Editor mEditor = mShared.edit();
		mEditor.putString(KEY_LABEL_NAME + labelNums, data);
		mEditor.putInt(KEY_LABEL_NUM, labelNums);
		mEditor.commit();
	}

	public int getLabelNums() {
		return labelNums;
	}
}
