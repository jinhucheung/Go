package com.imagine.go.view;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.imagine.go.R;

/**
 * 地理坐标点信息输入对话框
 * 
 * @author Jinhu
 * @date 2016/5/21
 */
public class GeoPointInfoDialog extends Dialog implements
		android.view.View.OnKeyListener {
	private View mDialogView;

	private EditText mEditText;

	/* 软键盘 . */
	private InputMethodManager inputMgr;

	/* 输入完成回调接口 . */
	private OnInputConfirmListener mOnInputConfirmListener;

	public interface OnInputConfirmListener {
		void onInputeConfirm(String inputTxt);
	}

	// ------------------------ 初始化视图 ------------------------
	/**
	 * 初始化视图组件
	 */
	private void initView() {

		mDialogView = getWindow().getDecorView().findViewById(
				android.R.id.content);

		mEditText = (EditText) findViewById(R.id.id_editText_info);

		getWindow().setGravity(Gravity.BOTTOM);
		getWindow().setLayout(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);

		mEditText.setOnKeyListener(this);

	}

	public GeoPointInfoDialog(Context context) {
		super(context, R.style.bottom_dialog);
	}

	// ------------------------ 生命周期 ------------------------
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dialog_user_defined_point_info);

		initView();
	}

	@Override
	public void show() {
		super.show();
		mEditText.setText("");
		mEditText.requestFocus();

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				inputMgr = (InputMethodManager) getContext().getSystemService(
						Context.INPUT_METHOD_SERVICE);
				inputMgr.showSoftInput(mEditText, 0);
			}
		}, 200);
	}

	@Override
	public synchronized boolean onKey(View v, int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_ENTER == keyCode) {
			final String editTxt = mEditText.getText().toString();
			if (null != editTxt && !"".equals(editTxt.trim())) {
				if (null != mOnInputConfirmListener) {
					mOnInputConfirmListener.onInputeConfirm(editTxt);
				}
			}
			if (null != mOnInputConfirmListener) {
				mOnInputConfirmListener.onInputeConfirm(null);
			}
			// 输入完成
			dismiss();
			return true;
		}
		if (null != mOnInputConfirmListener) {
			mOnInputConfirmListener.onInputeConfirm(null);
		}
		return false;
	}

	public void setOnInputConfirmListener(
			OnInputConfirmListener onInputConfirmListener) {
		mOnInputConfirmListener = onInputConfirmListener;
	}

}
