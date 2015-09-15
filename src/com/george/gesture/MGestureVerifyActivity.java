package com.george.gesture;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.george.gesture.widget.GestureContentView;
import com.george.gesture.widget.GestureDrawline.GestureCallBack;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * 手势绘制/校验界面
 * 
 */
public class MGestureVerifyActivity extends Activity implements android.view.View.OnClickListener {
	private TextView mTextCancel;
	private ImageView mImgUserLogo;
	private TextView mTextPhoneNumber;
	private TextView mTextTip;
	private FrameLayout mGestureContainer;
	private GestureContentView mGestureContentView;
	private TextView mTextForget;
	private TextView mTextOther;

	private Animation shakeAnimation;

	private SharedPreferences spf;
	private String name, head, pass;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gesture_activity_verify);
		spf = getSharedPreferences("GesturePassword", 4);
		name = getIntent().getStringExtra("name");
		head = getIntent().getStringExtra("head");
		if (name == null || name.equals("")) {
			Intent intent = getIntent();
			intent.putExtra("msg", "用户名错误");
			setResult(1, intent);
			this.finish();
			return;
		}
		pass = spf.getString(name, null);
		if (pass == null || pass.equals("")) {
			Intent intent = getIntent();
			intent.putExtra("msg", "密码为空");
			setResult(1, intent);
			this.finish();
			return;
		}
		setUpViews();
		setUpListeners();
	}

	private void setUpViews() {
		shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.gesture_shake);
		mTextCancel = (TextView) findViewById(R.id.text_cancel);
		mTextPhoneNumber = (TextView) findViewById(R.id.text_phone_number);
		mTextPhoneNumber.setText(getProtectedMobile(name));
		mImgUserLogo = (ImageView) findViewById(R.id.user_logo);
		mImgUserLogo.setImageBitmap(base64ToBitmap(head));
		mTextTip = (TextView) findViewById(R.id.text_tip);
		mGestureContainer = (FrameLayout) findViewById(R.id.gesture_container);
		mTextForget = (TextView) findViewById(R.id.text_forget_gesture);
		mTextOther = (TextView) findViewById(R.id.text_other_account);
		// 初始化一个显示各个点的viewGroup
		mGestureContentView = new GestureContentView(this, true, pass, new GestureCallBack() {

			@Override
			public void onGestureCodeInput(String inputCode) {

			}

			@Override
			public void checkedSuccess() {
				mTextTip.setVisibility(View.INVISIBLE);
				mGestureContentView.clearDrawlineState(0L);
				setResult(RESULT_OK);
				MGestureVerifyActivity.this.finish();
			}

			@Override
			public void checkedFail() {
				mTextTip.setVisibility(View.VISIBLE);
				mTextTip.setText(Html.fromHtml("<font color='#c70c1e'>密码错误</font>"));
				mTextTip.startAnimation(shakeAnimation);
				mGestureContentView.clearDrawlineState(1300L);
			}
		});
		// 设置手势解锁显示到哪个布局里面
		mGestureContentView.setParentView(mGestureContainer);
	}

	private void setUpListeners() {
		mTextCancel.setOnClickListener(this);
		mTextForget.setOnClickListener(this);
		mTextOther.setOnClickListener(this);
	}

	private String getProtectedMobile(String phoneNumber) {
		if (phoneNumber.length() > 8) {
			StringBuilder builder = new StringBuilder(phoneNumber);
			builder.replace(3, 7, "****");
			return builder.toString();
		}
		return phoneNumber;
	}

	public String bitmapToBase64(Bitmap bitmap) {
		String result = null;
		ByteArrayOutputStream baos = null;
		try {
			if (bitmap != null) {
				baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

				baos.flush();
				baos.close();

				byte[] bitmapBytes = baos.toByteArray();
				result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);

			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null) {
					baos.flush();
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	private Bitmap base64ToBitmap(String str) {
		Bitmap bitmap = null;
		try {
			if (head == null || head.equals(""))
				return bitmap;
			byte[] bitmapBytes = Base64.decode(str, Base64.DEFAULT);
			bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
			return bitmap;
		} catch (Exception e) {
			bitmap = null;
		}
		return bitmap;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		setResult(RESULT_CANCELED);
		this.finish();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.text_cancel) {
			setResult(RESULT_CANCELED);
			this.finish();
		} else if (v.getId() == R.id.text_forget_gesture) {
			setResult(2);
			this.finish();
		} else if (v.getId() == R.id.text_other_account) {
			setResult(3);
			this.finish();
		}
	}

}
