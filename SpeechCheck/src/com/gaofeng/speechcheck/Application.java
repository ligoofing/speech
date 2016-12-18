package com.gaofeng.speechcheck;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;


public class Application extends android.app.Application {

	@Override
	public void onCreate() {
		SpeechUtility.createUtility(this, SpeechConstant.APPID +getString(R.string.app_id)); 
	}

}
