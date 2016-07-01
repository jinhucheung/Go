package com.imagine.go.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * MarqueeTextView:自动水平滚动文本(当容器容纳不下文本长度时,文本滚动)
 * 
 * @author nickzhangdianxinba
 * @https://github.com/nickzhangdianxinba/MarqueeTextView
 * @date 2016/3/27
 */
public class MarqueeTextView extends TextView {

	public MarqueeTextView(Context context) {
		super(context);
	}

	public MarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean isFocused() {
		return true;
	}

}