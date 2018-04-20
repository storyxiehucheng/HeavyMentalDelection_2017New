package com.example.heavymentaldelection.manager_user;

import java.util.List;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

public class AchartEngineManager {
	private String ChartTitle="曲线图";
	private String XTitle="x";
	private String YTitle="y";
	private Context mContext;
    private GraphicalView mGraphicalView;
	private XYMultipleSeriesDataset mDataSet;//数据集容器
    private XYMultipleSeriesRenderer mMultipleSeriesRenderer;// 渲染器容器
    private XYSeries mSeries;// 单条曲线数据集

	/**创建一个AChartEngineManager的对象,用以创建曲线图
	 * @param context 上下文环境
	 */
	public AchartEngineManager(Context context) {
		this.mContext=context;
	}

	/**
	 * 获取图表
	 * @return 返回图标view
	 */
	public GraphicalView getGraphicalView()
	{
		mGraphicalView=ChartFactory.getLineChartView(mContext,mDataSet,mMultipleSeriesRenderer);
		mGraphicalView.setBackgroundColor(Color.BLACK);
		mGraphicalView.invalidate();
		return mGraphicalView;
	}

	/**获取数据集，即xy的坐标集合
	 * @param curveTitle 曲线的名称
	 */
	public void setXYMultipleSersiesDataSet(String curveTitle)
	{
		mDataSet=new XYMultipleSeriesDataset();
		mSeries=new XYSeries(curveTitle);
		mDataSet.addSeries(mSeries);
	}


	/**添加曲线
	 * @param curveTitle 添加的曲线的名称
 	 * @param color 添加的曲线的颜色
	 * @param XList 需要添加的X值的集合
	 * @param YList 需要添加的Y值的集合
	 */
	public void AddXYseries(String curveTitle, int color, List<Double> XList, List<Double> YList)
	{
		XYSeries mSeries=new XYSeries(curveTitle);
		for(int i=0;i<XList.size();i++)
		{
			mSeries.add(XList.get(i),YList.get(i));
		}
		mDataSet.addSeries(mSeries);
		XYSeriesRenderer mRenderer = new XYSeriesRenderer();
		mRenderer.setColor(color);
		mRenderer.setPointStyle(PointStyle.CIRCLE);//设置点的样式
		mRenderer.setFillPoints(true);//填充点（显示的点是空心还是实心）
		mRenderer.setDisplayChartValues(true);//将点的值显示出来
		mRenderer.setChartValuesSpacing(10);//显示的点的值与图的距离
		mRenderer.setChartValuesTextSize(20);//点的值的文字大小
		mRenderer.setLineWidth(3);//设置线宽
		mMultipleSeriesRenderer.addSeriesRenderer(mRenderer);
		mGraphicalView.repaint();
	}
	/**设置图形的总标题，以及横坐标和纵坐标的标题
	 * @param chartTitle  图形的标题
	 * @param xTitle      x轴的名称
	 * @param yTitle	  y轴的名称
	 */
	public void setChartTitle(String chartTitle,String xTitle,String yTitle)
	{
		ChartTitle = chartTitle;
		XTitle=xTitle;
		YTitle=yTitle;
		if(mMultipleSeriesRenderer==null)
		{
			setXYMultipleSeriesRenderer();
		}
		else
		{
			mMultipleSeriesRenderer.setXTitle(XTitle);//设置X轴标题
			mMultipleSeriesRenderer.setYTitle(YTitle);//设置Y轴标题
			mMultipleSeriesRenderer.setChartTitle(ChartTitle);//设置总的图表名称
			mGraphicalView.repaint();
		}

	}

	/**
	 * 设置数据集渲染器容器
	 */
	public void setXYMultipleSeriesRenderer()
	{
		mMultipleSeriesRenderer = setRenderer();
		XYSeriesRenderer mRenderer = new XYSeriesRenderer();
		mRenderer.setColor(Color.CYAN);
		mRenderer.setPointStyle(PointStyle.CIRCLE);//设置点的样式
		mRenderer.setFillPoints(true);//填充点（显示的点是空心还是实心）
		mRenderer.setDisplayChartValues(true);//将点的值显示出来
		mRenderer.setChartValuesSpacing(10);//显示的点的值与图的距离
		mRenderer.setChartValuesTextSize(20);//点的值的文字大小
		mRenderer.setLineWidth(3);//设置线宽
		mMultipleSeriesRenderer.addSeriesRenderer(mRenderer);
	}

	/**更新数据点，注意，只能在主线程中运行
	 * @param x x的值
	 * @param y y的值
	 */
	public void updateCharPoint(double x,double y)
	{
		mSeries.add(x,y);
		mGraphicalView.repaint();
	}

	/**更新窗口X坐标
	 * @param minX x轴的最小值
	 * @param maxX x轴的最大值
	 */
	public void updateCharWindowX(double minX,double maxX)
	{
		if(maxX<=minX+10) maxX=minX+10;
		mMultipleSeriesRenderer.setXAxisMin(minX);
		mMultipleSeriesRenderer.setXAxisMax(maxX);
		mGraphicalView.repaint();
	}
	/**更新窗口Y坐标
	 * @param minY y轴的最小值
	 * @param maxY y轴的最大值
	 */
	public void updateCharWindowY(double minY,double maxY)
	{
//		if(maxY<=minY+10) maxY=minY+10;
		mMultipleSeriesRenderer.setYAxisMin(minY);
		mMultipleSeriesRenderer.setYAxisMax(maxY);
		mGraphicalView.repaint();
	}

	/**设置图标是否可以点击
	 * @param flag true表示可以点击，则不能实现图标的缩放，滑动等。false表示不可以点击，当可以实现图形的滑动等
	 */
	public void setChartClickable(boolean flag)
	{
		mMultipleSeriesRenderer.setClickEnabled(flag);
	}
	/**
	 * 添加一条新的曲线，可添加多组，注意只能运行在主线程中
	 * @param xList x值的集合
	 * @param yList y值的集合
	 */
	public void updateChart(List<Double> xList, List<Double> yList)
	{
		mSeries.clear();
		Log.e("story","开始添加数据xList:"+xList.size()+" ylist:"+yList.size());
		for(int i=0;i<xList.size();i++)
		{
			mSeries.add(xList.get(i),yList.get(i));
//			Log.e("story","添加的数据x:"+xList.get(i)+" y:"+yList.get(i));
		}
		Log.e("story","数据添加完成");
		mGraphicalView.repaint();
	}

	/**
	 * 清楚屏幕上的曲线点
	 */
	public void ClearCurve()
	{
		mDataSet.clear();

		mGraphicalView.repaint();
	}

	/**XYMultipleSeriesRenderer的参数设置
	 * @return 返回设置后的XYMultipleSeriesRenderer对象
	 */
	private XYMultipleSeriesRenderer setRenderer()
	{
		mMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
		//设置图表方向
		mMultipleSeriesRenderer.setOrientation(XYMultipleSeriesRenderer.Orientation.HORIZONTAL);
		mMultipleSeriesRenderer.setXTitle(XTitle);//设置X轴标题
		mMultipleSeriesRenderer.setYTitle(YTitle);//设置Y轴标题
		mMultipleSeriesRenderer.setAxisTitleTextSize(40);//设置坐标轴标题文本大小
		mMultipleSeriesRenderer.setXLabelsPadding(0);//设置X轴标签离轴的距离
		mMultipleSeriesRenderer.setYLabelsPadding(15);//设置Y轴标签离轴的距离
		mMultipleSeriesRenderer.setLegendHeight(0);
		mMultipleSeriesRenderer.setYLabelsVerticalPadding(0);
		mMultipleSeriesRenderer.setChartTitle(ChartTitle);//设置总的图表名称
		mMultipleSeriesRenderer.setLabelsColor(Color.MAGENTA);//设置标签颜色
		mMultipleSeriesRenderer.setChartTitleTextSize(50);//设置图表标题文字的大小
		mMultipleSeriesRenderer.setLabelsTextSize(30);//设置轴标签的文字大小
		mMultipleSeriesRenderer.setLegendTextSize(30);//设置左下角曲线名称的大小
		mMultipleSeriesRenderer.setPointSize(5f);//设置点的大小

		mMultipleSeriesRenderer.setYAxisMin(-2000);//设置y轴最小值
		mMultipleSeriesRenderer.setYAxisMax(1000);//设置y轴的最大值
		mMultipleSeriesRenderer.setXAxisMin(-650);//设置x轴的最小值
		mMultipleSeriesRenderer.setXAxisMax(-150);//设置x轴的最大值

		mMultipleSeriesRenderer.setShowGrid(true);//显示网格
		mMultipleSeriesRenderer.setFitLegend(true);// 调整合适的位置
		mMultipleSeriesRenderer.setClickEnabled(false);//设置是否可以点击
		mMultipleSeriesRenderer.setMargins(new int[]{20,80,30,20});//设置外边框距离
		return mMultipleSeriesRenderer;

	}
	
}
