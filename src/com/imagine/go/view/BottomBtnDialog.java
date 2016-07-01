package com.imagine.go.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.imagine.go.R;

/**
 * 离线地图操作对话框
 * 
 * @author Jinhu
 * @date 2016/5/20
 */
public class BottomBtnDialog extends Dialog implements
		android.view.View.OnClickListener {

	private View mDialogView;

	private TextView mItem1Btn;

	private TextView mItem2Btn;

	private View mCancelBtn;

	// ------------------------ 初始化视图 ------------------------
	/**
	 * 初始化视图组件
	 */
	private void initView() {

		mDialogView = getWindow().getDecorView().findViewById(
				android.R.id.content);

		mItem1Btn = (TextView) findViewById(R.id.id_textView_item1);
		mItem2Btn = (TextView) findViewById(R.id.id_textView_item2);
		mCancelBtn = findViewById(R.id.id_textView_cancel);

		getWindow().setGravity(Gravity.BOTTOM);
		getWindow().setLayout(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);

		mCancelBtn.setOnClickListener(this);
	}

	public BottomBtnDialog(Context context) {
		super(context, R.style.bottom_dialog);
	}

	// ------------------------ 生命周期 ------------------------
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dialog_bottom);

		initView();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_textView_cancel:
			dismiss();
			break;
		}
	}

	public void setItem1Txt(String txt) {
		if (null == mItem1Btn)
			throw new NullPointerException();
		mItem1Btn.setText(txt);
	}

	public void setItem2Txt(String txt) {
		if (null == mItem1Btn)
			throw new NullPointerException();
		mItem2Btn.setText(txt);
	}

	public void setOnClickListener(
			android.view.View.OnClickListener onClickListener) {
		if (null == mItem1Btn || null == mItem2Btn)
			throw new NullPointerException();
		mItem1Btn.setOnClickListener(onClickListener);
		mItem2Btn.setOnClickListener(onClickListener);
	}

}
