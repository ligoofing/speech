package com.gaofeng.speechcheck;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeakerVerifier;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.VerifierListener;
import com.iflytek.cloud.VerifierResult;


public class MainActivity extends Activity implements OnClickListener {

	private static String TAG = "MainActivity";
	CheckUtil mCheck;
	/*View*/
	TextView mUserView;
	TextView mNumberView;
	TextView mTipsView;
	Button mRegButton;
	Button mCheckButton;
	
	SpeakerVerifier mVerifier;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initview();
        mCheck = new CheckUtil(this);
        
		// 首先创建SpeakerVerifier对象  
		mVerifier = SpeakerVerifier.createVerifier(this, new InitListener() {
			
			@Override
			public void onInit(int errorCode) {
				if (ErrorCode.SUCCESS == errorCode) {
					mCheck.showTip("引擎初始化成功");
					Log.i(TAG, "引擎初始化成功");
				} else {
					mCheck.showTip("引擎初始化失败，错误码：" + errorCode);
					Log.i(TAG, "引擎初始化失败，错误码：" + errorCode);
				}
			}
		}); 
		// 通过setParameter设置密码类型，pwdType的取值为1、2、3，分别表示文本密码、自由说和数字密码  
		mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mCheck.mPwdType);   
    }

    
	private void initview(){
		mUserView = (TextView) findViewById(R.id.user);
		mNumberView = (TextView) findViewById(R.id.number);
		mTipsView = (TextView) findViewById(R.id.tips);
		mRegButton = (Button) findViewById(R.id.reg);
		mCheckButton = (Button) findViewById(R.id.check);
		
		mRegButton.setOnClickListener(this);
		mCheckButton.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		switch(v.getId()){
		
		    case R.id.reg:
		    	Message msgReg = new Message();
		    	msgReg.what = REGMSG;
		    	mHandle.sendMessage(msgReg);
		    	break;
		    case R.id.check:
		    	Message msgCheck = new Message();
		    	msgCheck.what = CHECKMSG;
		    	mHandle.sendMessage(msgCheck);
		    	break;
		    default:
			break;
		}
		
	}
	
	private static final int REGMSG = 1;
	private static final int CHECKMSG = 2;
	private Handler mHandle = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			
			switch(msg.what){
			case REGMSG:
				reg();
				break;
			case CHECKMSG:
				check();
				break;
			}
			
		}
		
	};
	
	private void reg(){
		// 清空参数
		mVerifier.setParameter(SpeechConstant.PARAMS, null);
		mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH,
				Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/test.pcm");
		// 对于某些麦克风非常灵敏的机器，如nexus、samsung i9300等，建议加上以下设置对录音进行消噪处理
//					mVerify.setParameter(SpeechConstant.AUDIO_SOURCE, "" + MediaRecorder.AudioSource.VOICE_RECOGNITION);

		// 数字密码注册需要传入密码
		if (TextUtils.isEmpty(mCheck.mNumPwd)) {
			mCheck.showTip("获取随机密码失败！");
			return;
		}
		mVerifier.setParameter(SpeechConstant.ISV_PWD, mCheck.mNumPwd);
		mNumberView.setText("请读出："
				+ mCheck.mNumPwd.substring(0, 8));
		mTipsView.setText("训练 第" + 1 + "遍，剩余4遍");
		
		// 设置auth_id，不能设置为空
		mVerifier.setParameter(SpeechConstant.AUTH_ID, mCheck.mAuthId);
		// 设置业务类型为注册
		mVerifier.setParameter(SpeechConstant.ISV_SST, "train");
		// 设置声纹密码类型
		mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mCheck.mPwdType);
		// 开始注册
		mVerifier.startListening(mRegisterListener);
	}
	
	private void check(){
		mTipsView.setText("");
		// 清空参数
		mVerifier.setParameter(SpeechConstant.PARAMS, null);
		mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH,
				Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/verify.pcm");
		mVerifier = SpeakerVerifier.getVerifier();
		// 设置业务类型为验证
		mVerifier.setParameter(SpeechConstant.ISV_SST, "verify");
		// 对于某些麦克风非常灵敏的机器，如nexus、samsung i9300等，建议加上以下设置对录音进行消噪处理
	//					mVerify.setParameter(SpeechConstant.AUDIO_SOURCE, "" + MediaRecorder.AudioSource.VOICE_RECOGNITION);
		
		// 数字密码注册需要传入密码
		String verifyPwd = mVerifier.generatePassword(8);
		mVerifier.setParameter(SpeechConstant.ISV_PWD, verifyPwd);
		mTipsView.setText("请读出："
				+ verifyPwd);
		
		// 设置auth_id，不能设置为空
		mVerifier.setParameter(SpeechConstant.AUTH_ID, mCheck.mAuthId);
		mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mCheck.mPwdType);
		// 开始验证
		mVerifier.startListening(mVerifyListener);
	}
	
	private VerifierListener mVerifyListener = new VerifierListener() {

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			mCheck.showTip("当前正在说话，音量大小：" + volume);
			Log.d(TAG, "返回音频数据："+data.length);
		}

		@Override
		public void onResult(VerifierResult result) {
			mCheck.showTip(result.source);
			
			if (result.ret == 0) {
				// 验证通过
				mTipsView.setText("验证通过");
			}
			else{
				// 验证不通过
				switch (result.err) {
				case VerifierResult.MSS_ERROR_IVP_GENERAL:
					mTipsView.setText("内核异常");
					break;
				case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
					mTipsView.setText("出现截幅");
					break;
				case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
					mTipsView.setText("太多噪音");
					break;
				case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
					mTipsView.setText("录音太短");
					break;
				case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
					mTipsView.setText("验证不通过，您所读的文本不一致");
					break;
				case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
					mTipsView.setText("音量太低");
					break;
				case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
					mTipsView.setText("音频长达不到自由说的要求");
					break;
				default:
					mTipsView.setText("验证不通过");
					break;
				}
			}
		}
		// 保留方法，暂不用
		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle arg3) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}

		@Override
		public void onError(SpeechError error) {
			
			switch (error.getErrorCode()) {
			case ErrorCode.MSP_ERROR_NOT_FOUND:
				mTipsView.setText("模型不存在，请先注册");
				break;

			default:
				mCheck.showTip("onError Code："	+ error.getPlainDescription(true));
				break;
			}
		}

		@Override
		public void onEndOfSpeech() {
			// 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
			mCheck.showTip("结束说话");
		}

		@Override
		public void onBeginOfSpeech() {
			// 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
			mCheck.showTip("开始说话");
		}
	};
	
	private VerifierListener mRegisterListener = new VerifierListener() {

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			mCheck.showTip("当前正在说话，音量大小：" + volume);
			Log.d(TAG, "返回音频数据："+data.length);
		}

		@Override
		public void onResult(VerifierResult result) {
			mCheck.showTip(result.source);
			
			if (result.ret == ErrorCode.SUCCESS) {
				switch (result.err) {
				case VerifierResult.MSS_ERROR_IVP_GENERAL:
					mTipsView.setText("内核异常");
					break;
				case VerifierResult.MSS_ERROR_IVP_EXTRA_RGN_SOPPORT:
					mTipsView.setText("训练达到最大次数");
					break;
				case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
					mTipsView.setText("出现截幅");
					break;
				case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
					mTipsView.setText("太多噪音");
					break;
				case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
					mTipsView.setText("录音太短");
					break;
				case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
					mTipsView.setText("训练失败，您所读的文本不一致");
					break;
				case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
					mTipsView.setText("音量太低");
					break;
				case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
					mTipsView.setText("音频长达不到自由说的要求");
				default:
					mTipsView.setText("");
					break;
				}
				
				if (result.suc == result.rgn) {
					mCheck.showTip("注册成功");
					
//					if (PWD_TYPE_TEXT == mPwdType) {
//						mResultEditText.setText("您的文本密码声纹ID：\n" + result.vid);
//					} else if (PWD_TYPE_NUM == mPwdType) {
//						mResultEditText.setText("您的数字密码声纹ID：\n" + result.vid);
//					}
					
				} else {
					int nowTimes = result.suc + 1;
					int leftTimes = result.rgn - nowTimes;
				    mTipsView.setText("请读出：" + mCheck.mNumPwdSegs[nowTimes - 1]);
					mTipsView.setText("训练 第" + nowTimes + "遍，剩余" + leftTimes + "遍");
				}

			}else {
				mCheck.showTip("注册失败，请重新开始。");
			}
		}
		// 保留方法，暂不用
		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle arg3) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}

		@Override
		public void onError(SpeechError error) {
			
			if (error.getErrorCode() == ErrorCode.MSP_ERROR_ALREADY_EXIST) {
				mCheck.showTip("模型已存在");
			} else {
				mCheck.showTip("onError Code：" + error.getPlainDescription(true));
			}
		}

		@Override
		public void onEndOfSpeech() {
			mCheck.showTip("结束说话");
		}

		@Override
		public void onBeginOfSpeech() {
			mCheck.showTip("开始说话");
		}
	};
}
