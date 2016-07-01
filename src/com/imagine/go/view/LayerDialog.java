package com.imagine.go.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationSet;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.imagine.go.R;
import com.imagine.go.util.AnimationFactory;

/**
 * Poi图层选择对话框
 * 
 * @author Jinhu
 * @date 2016/5/23
 */
public class LayerDialog extends Dialog implements OnCheckedChangeListener {
	/* 对话框主体 . */
	private View mDialogView;

	/* Poi复选框 . */
	private CheckBox mAMapPoiBox;

	private CheckBox mUserPoiBox;

	/* 弹入动画 . */
	private AnimationSet mModalInAnim;

	/* 标记当前图层状态 . */
	private Layer mCurLayer = Layer.AMAP_POI;

	public static enum Layer {
		AMAP_POI, USER_POI, MIX_POI, NO_POI;
	}

	/* 图层改变回调接口 . */
	private OnLayerChangedListener mOnLayerChangedListener;

	public interface OnLayerChangedListener {
		void onLayerChanged(Layer layer);
	}

	// ------------------------ 初始化视图 ------------------------
	/**
	 * 初始化视图组件
	 */
	private void initView() {
		// ---初始化视图组件---
		mDialogView = getWindow().getDecorView().findViewById(
				android.R.id.content);
		mAMapPoiBox = (CheckBox) findViewById(R.id.id_checkBox_amap_poi);
		mUserPoiBox = (CheckBox) findViewById(R.id.id_checkBox_user_poi);

		mAMapPoiBox.setOnCheckedChangeListener(this);
		mUserPoiBox.setOnCheckedChangeListener(this);
	}

	/**
	 * 加载动画
	 */
	private void loadAnim() {
		mModalInAnim = (AnimationSet) AnimationFactory.loadAnimation(
				getContext(), R.anim.dialog_modal_in);
	}

	public LayerDialog(Context context) {
		super(context, R.style.alert_dialog);
	}

	// ------------------------ 生命周期 ------------------------
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dialog_layers_selection);

		initView();
		loadAnim();
	}

	@Override
	protected void onStart() {
		mDialogView.startAnimation(mModalInAnim);
	}

	@Override
	public void show() {
		super.show();
		switch (mCurLayer) {
		case AMAP_POI:
			mAMapPoiBox.setChecked(true);
			break;
		case USER_POI:
			mUserPoiBox.setChecked(true);
			break;
		case MIX_POI:
			mAMapPoiBox.setChecked(true);
			mUserPoiBox.setChecked(true);
			break;
		case NO_POI:
			mAMapPoiBox.setChecked(false);
			mUserPoiBox.setChecked(false);
			break;
		}
	}

	public void setLayer(Layer layer) {
		mCurLayer = layer;
	}

	public void setOnLayerChangedListener(
			OnLayerChangedListener onLayerChangedListener) {
		mOnLayerChangedListener = onLayerChangedListener;
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean isChecked) {
		if (mAMapPoiBox.isChecked() && mUserPoiBox.isChecked())
			mCurLayer = Layer.MIX_POI;
		else if (!mAMapPoiBox.isChecked() && !mUserPoiBox.isChecked())
			mCurLayer = Layer.NO_POI;
		else if (mAMapPoiBox.isChecked())
			mCurLayer = Layer.AMAP_POI;
		else
			mCurLayer = Layer.USER_POI;

		if (null != mOnLayerChangedListener) {
			mOnLayerChangedListener.onLayerChanged(mCurLayer);
		}
	}
}
