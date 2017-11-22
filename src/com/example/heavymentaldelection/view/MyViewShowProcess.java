package com.example.heavymentaldelection.view;

import com.example.heavymentaldelection.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyViewShowProcess extends RelativeLayout {

	private View mView;
	private TextView tv_showview;
	private ProgressBar pb_showview;
	public MyViewShowProcess(Context context) {
		this(context,null,0,0);
	}

	public MyViewShowProcess(Context context, AttributeSet attrs) {
		this(context, attrs,0,0);
	}

	public MyViewShowProcess(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr,0);
	}

	public MyViewShowProcess(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		mView = View.inflate(context, R.layout.show_process, this);
		tv_showview = (TextView) mView.findViewById(R.id.tv_showview);
		pb_showview = (ProgressBar) mView.findViewById(R.id.pb_showview);
		pb_showview.setInterpolator(new LinearInterpolator());
	}
	
	/**设置进度条中间的文字
	 * @param text 需要设置的文字
	 */
	public void MySetShowText(String text)
	{
		tv_showview.setText(text);
	}
	/**设置进度条中间文字的颜色
	 * @param color
	 */
	public void MySetShowTextColor(int color)
	{
		tv_showview.setTextColor(color);
	}
	/**设置进度条的图案
	 * @param drawable 需要设置的图案
	 */
	public void MySetProgressBarDrawable(Drawable drawable)
	{
		//在设置setIndeterminateDrawable之前必须设置drawable的边界，不然会不能显示
		drawable.setBounds(pb_showview.getIndeterminateDrawable().getBounds());
		pb_showview.setIndeterminateDrawable(drawable);
	}
}
