package com.example.heavymentaldelection.view;

import com.example.heavymentaldelection.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class myGifView extends View {
	private Movie mMovie;
	private long movieStart;
	public myGifView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	
	}

	public myGifView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	
	}

	public myGifView(Context context, AttributeSet attrs) {
		super(context, attrs);
//		mMovie=Movie.decodeStream(getResources().openRawResource(R.drawable.accumaltion));
		mMovie=Movie.decodeStream(getResources().openRawResource(R.raw.timg));
	}

	public myGifView(Context context) {
		super(context);
//		mMovie=Movie.decodeStream(getResources().openRawResource(R.drawable.accumaltion));
		mMovie=Movie.decodeStream(getResources().openRawResource(R.raw.timg));
	}
	@Override
	protected void onDraw(Canvas canvas) {
		 long curTime=android.os.SystemClock.uptimeMillis();  
	        //第一次播放  
	        if (movieStart == 0) { 
	        	
	        movieStart = curTime;  
	        }  
	        if (mMovie != null) {  
	            int duraction = mMovie.duration(); 
	            if(duraction==0) duraction=1000;
	            int relTime = (int) ((curTime-movieStart)%duraction);  
	            mMovie.setTime(relTime);  
	            canvas.scale(0.7f, 0.7f);
	            mMovie.draw(canvas, 0, 0);
	            //强制重绘  
	            this.invalidate();  
	        }  
	}
	
}
