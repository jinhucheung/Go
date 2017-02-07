package com.imagine.go.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.imagine.go.R;
import com.imagine.go.util.AnimationFactory;
import com.imagine.go.util.PoiTypeMatcher.Icon;

/**
 * AR信息提示框
 * 
 * @author Jinhu
 * @date 2016/5/15
 */
public class ARMarkerDialog extends Dialog implements OnClickListener {

	/* 对话框主体 . */
	private View mDialogView;

	/* Poi类型图 . */
	private ImageView mPoiTypeImg;

	/* Poi名 . */
	private TextView mPoiNameTxt;

	/* Poi地址 . */
	private TextView mPoiAddressTxt;

	/* 导航按钮 . */
	private View mNaviBtn;

	/* 弹入动画 . */
	private AnimationSet mModalInAnim;

	/* 启动导航接口 . */
	private onNaviListener mOnNaviListener;

	public interface onNaviListener {
		void onStartNavi();
	}

	public ARMarkerDialog(Context context) {
		super(context, R.style.alert_dialog);
	}

	// ------------------------ 初始化视图 ------------------------
	/**
	 * 初始化视图组件
	 */
	private void initView() {
		// ---初始化视图组件---
		mDialogView = getWindow().getDecorView().findViewById(
				android.R.id.content);
		mPoiTypeImg = (ImageView) findViewById(R.id.id_imageView_poiType);
		mPoiNameTxt = (TextView) findViewById(R.id.id_textView_poiName);
		mPoiAddressTxt = (TextView) findViewById(R.id.id_textView_poiAddress);
		mNaviBtn = findViewById(R.id.id_imageView_ar_navi);
		mNaviBtn.setOnClickListener(this);
	}

	/**
	 * 加载动画
	 */
	private void loadAnim() {
		mModalInAnim = (AnimationSet) AnimationFactory.loadAnimation(
				getContext(), R.anim.dialog_modal_in);
	}

	// ------------------------ 生命周期 ------------------------
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dialog_marker_ar);

		initView();
		loadAnim();
	}

	@Override
	protected void onStart() {
		mDialogView.startAnimation(mModalInAnim);
	}

	/**
	 * 更新Poi类型图
	 * 
	 * @param icon
	 */
	public void updatePoiTypeImg(Icon icon) {
		if (null == icon)
			return;
		mPoiTypeImg.setImageResource(icon.getType());
		mPoiTypeImg.setBackgroundResource(icon.getBackground());
		mPoiTypeImg.postInvalidate();
	}

	/**
	 * 更新Poi名
	 * 
	 * @param name
	 */
	public void updatePoiName(String name) {
		mPoiNameTxt.setText(name);
		mPoiNameTxt.postInvalidate();
	}

	/**
	 * 更新Poi地址
	 * 
	 * @param address
	 */
	public void updatePoiAddress(String address) {
		mPoiAddressTxt.setText(address);
		mPoiAddressTxt.postInvalidate();
	}

	@Override
	public void onClick(View v) {
		if (R.id.id_imageView_ar_navi == v.getId()) {
			if (null != mOnNaviListener) {
				mOnNaviListener.onStartNavi();
			}
		}
	}

	/**
	 * 导航启动回调
	 * 
	 * @param onNaviListener
	 */
	public void setOnNaviListener(onNaviListener onNaviListener) {
		mOnNaviListener = onNaviListener;
	}
}
