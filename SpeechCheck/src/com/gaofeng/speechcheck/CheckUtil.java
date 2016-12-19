package com.gaofeng.speechcheck;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class CheckUtil {
	private Toast mToast;
	private static String TAG = "Check";
	
	private int TEXT = 1;
	private int FREE = 2;
	private int NUMBER = 3;
	// 数字声纹密码
	public String mNumPwd;// = "12345678-12345671-12345672-12345673-12345674";
	// 请使用英文字母或者字母和数字的组合，勿使用中文字符
	public String mAuthId = "gao123";
	// 当前声纹密码类型，1、2、3分别为文本、自由说和数字密码
	public int mPwdType = NUMBER;
	// 数字声纹密码段，默认有5段
	public String[] mNumPwdSegs;// = {"12345678","12345678","12345678","12345678","12345678"};
	
	public CheckUtil(Context context){
		mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
//		mToast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
		
	}
	
	public void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}
	
}
