package com.george.gesture;

import com.george.gesture.widget.GestureContentView;
import com.george.gesture.widget.LockIndicator;
import com.george.gesture.widget.GestureDrawline.GestureCallBack;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * 
 */
public class MGestureEditActivity extends Activity implements OnClickListener {
	private TextView mTextCancel;
	private LockIndicator mLockIndicator;
	private TextView mTextTip;
	private FrameLayout mGestureContainer;
	private GestureContentView mGestureContentView;
	private TextView mText;
	private Animation shakeAnimation;

	private SharedPreferences spf;
	private String name, orgPass, newPass;

	private int state = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gesture_activity_edit);
		spf = getSharedPreferences("GesturePassword", 4);
		name = getIntent().getStringExtra("name");
		if (name == null || name.equals("")) {
			Intent intent = getIntent();
			intent.putExtra("msg", "用户名错误");
			setResult(1, intent);
			this.finish();
			return;
		}
		orgPass = spf.getString(name, null);
		if (orgPass == null || orgPass.equals("")) {
			state = 1;
		} else {
			state = 0;
		}
		setUpViews();
		setUpListeners();
	}

	private void setUpViews() {
		shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.gesture_shake);
		mTextCancel = (TextView) findViewById(R.id.text_cancel);
		mText = (TextView) findViewById(R.id.text);
		if (state == 0) {
			mText.setText(getString(R.string.gesture_set_pattern_org));
		} else {
			mText.setText(getString(R.string.gesture_set_pattern_reason));
		}
		mLockIndicator = (LockIndicator) findViewById(R.id.lock_indicator);
		mTextTip = (TextView) findViewById(R.id.text_tip);
		mGestureContainer = (FrameLayout) findViewById(R.id.gesture_container);
		mGestureContentView = new GestureContentView(this, false, "", new GestureCallBack() {
			@Override
			public void onGestureCodeInput(String inputCode) {
				if (!isInputPassValidate(inputCode)) {
					mTextTip.setText(Html.fromHtml("<font color='#c70c1e'>最少链接4个点, 请重新绘制</font>"));
					mGestureContentView.clearDrawlineState(0L);
					return;
				}
				if (state == 0) {
					if (!inputCode.equals(orgPass)) {
						mTextTip.setText(Html.fromHtml("<font color='#c70c1e'>原密码错误</font>"));
						mTextTip.startAnimation(shakeAnimation);
						mGestureContentView.clearDrawlineState(1300L);
					} else {
						mTextTip.setText(R.string.gesture_set_pattern);
						mGestureContentView.clearDrawlineState(0L);
						mText.setText(getString(R.string.gesture_set_pattern_reason));
						state = 1;
					}
				} else if (state == 1) {
					mTextTip.setText(R.string.gesture_set_pattern);
					newPass = inputCode;
					updateCodeList(inputCode);
					mGestureContentView.clearDrawlineState(0L);
					mText.setText(getString(R.string.gesture_set_pattern_again));
					state = 2;
				} else if (state == 2) {
					if (inputCode.equals(newPass)) {
						spf.edit().putString(name, newPass).commit();
						mTextTip.setText(R.string.gesture_set_pattern);
						mGestureContentView.clearDrawlineState(0L);
						setResult(RESULT_OK, getIntent());
						MGestureEditActivity.this.finish();
					} else {
						mTextTip.setText(Html.fromHtml("<font color='#c70c1e'>与上一次绘制不一致，请重新绘制</font>"));
						mTextTip.startAnimation(shakeAnimation);
						mGestureContentView.clearDrawlineState(1300L);
						mText.setText(getString(R.string.gesture_set_pattern_reason));
						state = 1;
					}
				}
			}

			@Override
			public void checkedSuccess() {

			}

			@Override
			public void checkedFail() {

			}
		});
		mGestureContentView.setParentView(mGestureContainer);
		updateCodeList("");
	}

	private void setUpListeners() {
		mTextCancel.setOnClickListener(this);
		mText.setOnClickListener(this);
	}

	private void updateCodeList(String inputCode) {
		mLockIndicator.setPath(inputCode);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.text_cancel) {
			setResult(RESULT_CANCELED);
			this.finish();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		setResult(RESULT_CANCELED);
		this.finish();
	}

	private boolean isInputPassValidate(String inputPassword) {
		if (TextUtils.isEmpty(inputPassword) || inputPassword.length() < 4) {
			return false;
		}
		return true;
	}

}
