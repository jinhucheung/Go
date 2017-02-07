package com.imagine.go.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * MGridView:解决GridView嵌套在ScrollView中时，只显示第一行数据
 * 
 * @author Jinhu
 * @date 2016/3/17
 */
public class MGridView extends GridView {

	public MGridView(Context context) {
		super(context);
	}

	public MGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MGridView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// 子视图元素至多达到指定大小
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}

}
