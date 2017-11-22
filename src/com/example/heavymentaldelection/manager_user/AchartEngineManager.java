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
	private String Xtitle="x";
	private String Ytitle="y";
	private XYMultipleSeriesDataset mDataset;
	private Context mContext;
	/**创建一个AChartEngineManager的对象,用以创建曲线图
	 * @param context 上下文环境
	 */
	public AchartEngineManager(Context context) {
		this.mContext=context;
	}
	
	/**设置图形的总标题，以及横坐标和纵坐标的标题
	 * @param chartTitle  图形的标题
	 * @param xtitle      x轴的名称
	 * @param ytitle	  y轴的名称
	 */
	public void setChartTitle(String chartTitle,String xtitle,String ytitle) {
		ChartTitle = chartTitle;
		Xtitle=xtitle;
		Ytitle=ytitle;
	}
	
	/**创建两条曲线的图形
	 * @param DataFirst 第一条曲线的数据
	 * @param title_line_first  第一条曲线的名称
	 * @param DataSecond       第二条曲线的数据
	 * @param title_line_second 第二条曲线的名称
	 * @return 返回一个view对象
	 */
	public GraphicalView DataSet(List<Double> DataFirst,List<Double> DataFirst_x,String title_line_first,List<Double> DataSecond,List<Double> DataSecond_x,String title_line_second)
	{
		int i;
		if((!DataFirst.isEmpty())&&(!DataSecond.isEmpty()))
		{
			mDataset = new XYMultipleSeriesDataset();
			XYSeries seriesOne=new XYSeries(title_line_first);
			XYSeries seriesTwo=new XYSeries(title_line_second);
			Log.v("story", "DataFirst---"+DataFirst.size());
			Log.v("story", "DataFirst_x---"+DataFirst_x.size());
			Log.v("story", "DataSecond---"+DataSecond.size());
			Log.v("story", "DataSecond_x---"+DataSecond_x.size());
			for(i=0;i<DataFirst.size();i++)
			{
				seriesOne.add(DataFirst_x.get(i),DataFirst.get(i));
			}

			for(i=1;i<DataSecond.size();i++)
			{
				seriesTwo.add(DataSecond_x.get(i),DataSecond.get(i));
			}
			Log.e("story", "数据填充完毕");
			mDataset.addSeries(seriesOne);
			mDataset.addSeries(seriesTwo);
			XYMultipleSeriesRenderer renderer=setRenderer();
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(Color.CYAN);
			r.setPointStyle(PointStyle.CIRCLE);//设置点的样式
			r.setFillPoints(true);//填充点（显示的点是空心还是实心）
			r.setDisplayChartValues(true);//将点的值显示出来
			r.setChartValuesSpacing(10);//显示的点的值与图的距离
			r.setChartValuesTextSize(20);//点的值的文字大小
			r.setLineWidth(3);//设置线宽
			renderer.addSeriesRenderer(r);

			XYSeriesRenderer r1 = new XYSeriesRenderer();
			r1.setColor(Color.YELLOW);
			r1.setPointStyle(PointStyle.CIRCLE);//设置点的样式
			r1.setFillPoints(true);//填充点（显示的点是空心还是实心）
			r1.setDisplayChartValues(true);//将点的值显示出来
			r1.setChartValuesSpacing(10);//显示的点的值与图的距离
			r1.setChartValuesTextSize(20);//点的值的文字大小
			r1.setLineWidth(3);//设置线宽
			renderer.addSeriesRenderer(r1);
			GraphicalView mmview=ChartFactory.getCubeLineChartView(mContext,mDataset,renderer,0.2F);
			mmview.setBackgroundColor(Color.BLACK);
			return mmview;
		}
		else if(!DataFirst.isEmpty())
		{
			return DataSet(DataFirst,DataFirst_x,title_line_first);
		}
		else if(!DataSecond.isEmpty())
		{
			return DataSet(DataSecond,DataSecond_x,title_line_second);
		}
		else
		{
			return null;
		}
	}
	/**创建一个一条曲线的图形
	 * @param Data 曲线的数据
	 * @param title_line 曲线的名称
	 * @return
	 */
	public GraphicalView DataSet(List<Double> Data,List<Double> Data_x,String title_line)
	{
		if(Data!=null&&title_line!=null)
		{
			if(!Data.isEmpty())
			{
				mDataset = new XYMultipleSeriesDataset();
				XYSeries series=new XYSeries(title_line);
				for(int i=0;i<Data.size();i++)
				{
					series.add(Data_x.get(i),(Data.get(i)));
				}
				mDataset.addSeries(series);
				XYMultipleSeriesRenderer renderer=setRenderer();
				XYSeriesRenderer r = new XYSeriesRenderer();
		        r.setColor(Color.CYAN);
		        r.setPointStyle(PointStyle.CIRCLE);//设置点的样式  
		        r.setFillPoints(true);//填充点（显示的点是空心还是实心）  
		        r.setDisplayChartValues(true);//将点的值显示出来  
		        r.setChartValuesSpacing(10);//显示的点的值与图的距离  
		        r.setChartValuesTextSize(20);//点的值的文字大小  
		        r.setLineWidth(3);//设置线宽  
		        renderer.addSeriesRenderer(r);
		        GraphicalView mmview=ChartFactory.getCubeLineChartView(mContext,mDataset,renderer,0.2F);
		        mmview.setBackgroundColor(Color.BLACK); 
				return mmview;
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}
	
	/**XYMultipleSeriesRenderer的参数设置
	 * @return 返回设置后的XYMultipleSeriesRenderer对象
	 */
	private XYMultipleSeriesRenderer setRenderer()
	{
		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		mRenderer.setOrientation(XYMultipleSeriesRenderer.Orientation.HORIZONTAL);
		mRenderer.setXTitle(Xtitle);
		mRenderer.setYTitle(Ytitle);
		mRenderer.setAxisTitleTextSize(20);
		mRenderer.setXLabelsPadding(2);
		mRenderer.setYLabelsPadding(0);
		mRenderer.setLegendHeight(50);
		mRenderer.setYLabelsVerticalPadding(0);
		mRenderer.setChartTitle(ChartTitle);
		mRenderer.setLabelsColor(Color.MAGENTA);
		mRenderer.setChartTitleTextSize(40);//设置图表标题文字的大小  
        mRenderer.setLabelsTextSize(25);//设置标签的文字大小  
        mRenderer.setLegendTextSize(20);//设置图例文本大小  
        mRenderer.setPointSize(5f);//设置点的大小  
        mRenderer.setYAxisMin(-15);//设置y轴最小值是0
        mRenderer.setYAxisMax(15);
        mRenderer.setXAxisMax(700);  
        mRenderer.setXAxisMin(-300);     
        mRenderer.setShowGrid(true);//显示网格  
        //将x标签栏目显示如：1,2,3,4替换为显示1月，2月，3月，4月  
//        mRenderer.addXTextLabel(1, "1月");  
//        mRenderer.addXTextLabel(2, "2月");  
//        mRenderer.addXTextLabel(3, "3月");  
//        mRenderer.addXTextLabel(4, "4月");  
//        mRenderer.setXLabels(0);//设置只显示如1月，2月等替换后的东西，
        mRenderer.setMargins(new int[]{20,30,15,20});
		return mRenderer;
		
	}
	
}
