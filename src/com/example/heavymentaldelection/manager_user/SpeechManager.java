package com.example.heavymentaldelection.manager_user;

import com.example.heavymentaldelection.my_utils.MyConstantValue;
import com.example.heavymentaldelection.my_utils.MySpUtils;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class SpeechManager {
	private SynthesizerListener mSynlistener=null;
	private SpeechSynthesizer mTts=null;
	private Context mContext;
	public SpeechManager(Context mContext) {
		super();
		this.mContext=mContext;
		VoiceInit();
	}		
	private void VoiceInit()
	{
		mSynlistener=new SynthesizerListener() {				
			@Override
			public void onSpeakResumed() {
			}				
			@Override
			public void onSpeakProgress(int arg0, int arg1, int arg2) {
			}				
			@Override
			public void onSpeakPaused() {
			}			
			@Override
			public void onSpeakBegin() {
			}			
			@Override
			public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
			}			
			@Override
			public void onCompleted(SpeechError error) {
			}				
			@Override
			public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {
			}
		};
		mTts=SpeechSynthesizer.createSynthesizer(mContext, null);
		Log.e("story", "语音准备设置");
		if(mTts==null)
		{
			return;
		}
		Log.e("story", "语音设置");
		//设置发音人
		mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
		//设置语速
		mTts.setParameter(SpeechConstant.SPEED, "45");
		//设置音量（0-100）
		mTts.setParameter(SpeechConstant.VOLUME, "60");
		boolean isNetworkAvailable = MySpUtils.getBoolean(mContext.getApplicationContext(), MyConstantValue.NETWORK_AVAILABLE, false);
		if(isNetworkAvailable)
		{
			//设置云端
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);			
		}
		else
		{
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);			
			//设置本地合成资源
			mTts.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath());			
		}
	}
	
	private String getResourcePath() {
		StringBuffer tempBuffer=new StringBuffer();
		//合成通用资源
		tempBuffer.append(ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, "tts/common.jet"));
		tempBuffer.append(";");
		//发音人资源
		tempBuffer.append(ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, "tts/xiaoyan.jet"));
		return tempBuffer.toString();
	}
	public void setSpeechString(String str)
	{

		if(mTts!=null)
		{
			Log.e("story", "语音开始准备合成");
			//开始合成
			mTts.startSpeaking(str, mSynlistener);
		}

	}
}
