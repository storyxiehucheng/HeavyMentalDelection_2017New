package com.example.heavymentaldelection.global;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Application;
import android.util.Log;

public class MyApplication extends Application {
	protected String tag="story";
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				// TODO Auto-generated method stub
				Log.e(tag,"捕获到异常--------------");
				ex.printStackTrace();
				Log.e(tag,ex.toString());
				Log.e(tag,"捕获到异常--------------");
			}
		});
	}
}
