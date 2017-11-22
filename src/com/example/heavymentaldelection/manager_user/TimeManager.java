package com.example.heavymentaldelection.manager_user;

import android.os.Handler;

public class TimeManager {
	private int totalSeconds;
	private StringBuffer TimeBuffer=new StringBuffer() ;

	public TimeManager(int totalTime) {
		super();
		this.totalSeconds = totalTime;
	}
	public void ShowTimer(final Handler handler)
	{						
		Timeprocess(totalSeconds);
		Runnable runnable=new Runnable()
		{
			@Override
			public void run() {
				// TODO Auto-generated method stub
				totalSeconds--;
				System.out.println("totalSeconds----"+totalSeconds);
				if(totalSeconds>0)
				{
				  handler.postDelayed(this, 1000);
				  Timeprocess(totalSeconds);
				  
				}
				else
				{
					Timeprocess(totalSeconds);
					handler.removeCallbacks(this);
				}
			}		
		};
		handler.postDelayed(runnable, 1000);			
	}
	
	private StringBuffer Timeprocess(int timedata)
	{
		int h=0;
		int m=0;
		int s=0;			
		h=(timedata/3600);
		m=(timedata%3600)/60;
		s=(timedata%3600)%60;
				
		TimeBuffer.append("富集时间剩余：").append(h).append("时").append(m).append("分").append(s).append("秒");
		return TimeBuffer;
					
	}
}
