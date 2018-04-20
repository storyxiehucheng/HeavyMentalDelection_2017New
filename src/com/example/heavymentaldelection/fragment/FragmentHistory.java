package com.example.heavymentaldelection.fragment;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.example.heavymentaldelection.Info.HeavyMentalDataInfo;
import com.example.heavymentaldelection.dbDao.CitiesDao;
import com.example.heavymentaldelection.manager_user.BaiduMapManager;
import com.example.heavymentaldelection.my_utils.HttpUtils;
import com.example.heavymentaldelection.my_utils.MyConstantValue;
import com.example.heavymentaldelection.my_utils.MySortFilterArrayList;
import com.example.heavymentaldelection.my_utils.MyUsingUtils;
import com.example.heavymentaldelection.my_utils.QueryFromNet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.example.heavymentaldelection.R;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class FragmentHistory extends Fragment {
	private static final String TABLE_NAME="heavyMental01";
	private View mHistoryView;
	private Spinner sp_history_pollution;
	private Context mContext;
	private TextView tv_history_choice_date;
	private Spinner sp_history_choice_city;
	private ListView lv_history_listView;
	private Handler mHandler;
	private MyListViewAdapter myListViewAdapter;
	private ArrayList<HeavyMentalDataInfo> mHMInfoList=new ArrayList<>();
	private TextView tv_history_time_order;
	private TextView tv_history_pollution_order;
	private MapView historyMapView;
	private BaiduMapManager hBaiduMapManager;
	private Gson mGson=new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
	private ArrayList<HeavyMentalDataInfo> mHMInfoListAll;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mHistoryView = inflater.inflate(R.layout.fragment_history, container, false);
		initId();
		return mHistoryView;
	}
	@SuppressLint("HandlerLeak")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mContext = getActivity().getApplicationContext();
		super.onActivityCreated(savedInstanceState);
		mHandler=new Handler()
		{
			String filterDate;
			String filterCity;
			String filterPollution;
			String sortDate;
			String sortPollution;
			int time=0;
			@Override
			public void handleMessage(Message msg) {
				Drawable drawable;
				switch (msg.what) {
				case 0:
						filterListData();
					break;
				case 1://按时间排序
					 tv_history_pollution_order.setText("污染：默认");
					 drawable = mContext.getDrawable(R.drawable.history_order_admin);
					 drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					 tv_history_pollution_order.setCompoundDrawables(null, null, drawable, null);
					 sortDate=tv_history_time_order.getText().toString();
					 sortPollution=tv_history_pollution_order.getText().toString();
					 
					 filterListData();
					 mHMInfoList=MySortFilterArrayList.sortList(sortDate, mHMInfoList);
					 updateListAdapter();
					
					break;
				case 2://按污染程度排序
					tv_history_time_order.setText("时间：默认");
					drawable = mContext.getDrawable(R.drawable.history_order_admin);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_history_time_order.setCompoundDrawables(null, null, drawable, null);
					sortPollution=tv_history_pollution_order.getText().toString();
					
					filterListData();
					mHMInfoList=MySortFilterArrayList.sortList(sortPollution, mHMInfoList);
					updateListAdapter();
					break;
				case 3:
					 HeavyMentalDataInfo hmDataBaseInfo= (HeavyMentalDataInfo) msg.obj;
					 Message obtainMessage = mHandler.obtainMessage(3, hmDataBaseInfo);
					 hBaiduMapManager.clearCircelOvrelay();
					 hBaiduMapManager.setCircleOverlay(hmDataBaseInfo.getLatitude(), hmDataBaseInfo.getLongitude(), time*60);
					 time++;
					 if(time==10) time=0;
					 mHandler.sendMessageDelayed(obtainMessage, 120);
					 break;

					case 4://获取数据成功的返回值，更新数据显示
						updateListAdapter();
						break;
				default:
					break;
				}
			}
			/**
			 * 更新数据适配器
			 */
			private void updateListAdapter() {
			 showMarkerAll(mHMInfoList);
			 if(myListViewAdapter==null)
			   {
				   myListViewAdapter = new MyListViewAdapter();
				   lv_history_listView.setAdapter(myListViewAdapter);
			   }
			   else
			   {
				   myListViewAdapter.notifyDataSetChanged();
			   }
			}
			/**
			 * 按条件过滤检测数据
			 */
			void filterListData() {
				filterDate=tv_history_choice_date.getText().toString();
				filterCity=(String) sp_history_choice_city.getSelectedItem();
				filterCity=filterCity.substring(1, filterCity.length());
				filterPollution=(String) sp_history_pollution.getSelectedItem();
				mHMInfoList=MySortFilterArrayList.filterProcess(filterDate,filterCity, filterPollution,mHMInfoListAll);
				updateListAdapter();
			}
			
		};
		//初始化数据
		initData();
	}
	

	/**动态显示地图的圆形动画。
	 * @param hmDataBaseInfo
	 */
	protected void setCircleAnimation(HeavyMentalDataInfo hmDataBaseInfo) {
		
	}
	/**在地图上显示listview中对应的标记
	 * @param hMDataBaseInfoList2
	 */
	protected void showMarkerAll(ArrayList<HeavyMentalDataInfo> hMDataBaseInfoList2) {
		View markerView=View.inflate(mContext, R.layout.history_map_marker, null);
		TextView tv_history_map_marker=(TextView) markerView.findViewById(R.id.tv_history_map_marker);
		//停止发送Message（3），即停止circle overlay 动画
		mHandler.removeMessages(3);
		//先清除地图上的所有覆盖物
		hBaiduMapManager.clearMapOverLay();
		if(hMDataBaseInfoList2==null || hMDataBaseInfoList2.isEmpty())
		{
			Log.e("story","hMDataBaseInfoList2的数据为null");
			return;
		}
		//根据列表中的数据，对应在地图上显示标注
		for (HeavyMentalDataInfo hmDataBaseInfo : hMDataBaseInfoList2) {
			tv_history_map_marker.setText(hmDataBaseInfo.getDetail());
			hBaiduMapManager.setOverMark(hmDataBaseInfo,hmDataBaseInfo.getLatitude(),
					hmDataBaseInfo.getLongitude(),
					hmDataBaseInfo.getCity(), markerView, new BaiduMapManager.MyInfoWindowClickListener() {
						@Override
						public void MyBaiduInfoWindowClickListener(BaiduMap baidumap, Marker marker) {
							baidumap.hideInfoWindow();
						}
					});
		}
		hBaiduMapManager.setNewCenter(hMDataBaseInfoList2.get(0).getLatitude(), hMDataBaseInfoList2.get(0).getLongitude(), 12f);
	}
	/**
	 * 初始化控件的监听器，以及控件的adapter
	 */
	private void initData() {
		initBaiduMap();
		getAllDateFromNetServer();//从服务器上获取最新的数据
		//污染情况的spinner的处理
		String[] spinner_text_pollution=new String[]{
				MyConstantValue.ALL_POLLUTION,
				MyConstantValue.NO_POLLUTION,
				MyConstantValue.SLIGHT_POLLUTION,
				MyConstantValue.MIDDLE_POLLUTION,
				MyConstantValue.HIGH_POLLUTION};
		ArrayAdapter<String> adapter=new ArrayAdapter<>(mContext,R.layout.spinner_text_style ,spinner_text_pollution);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_text_style);
		sp_history_pollution.setAdapter(adapter);

		//spinner下拉选项会默认执行一次，第一个触发选项的触发，这里需要将第一次的触发关闭
		sp_history_pollution.setSelection(0,false);

		sp_history_pollution.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				Log.e("story","sp_history_pollution选择的：position="+position+" id="+id);
				//发送消息，显示过滤信息
				mHandler.sendEmptyMessage(0);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		//时间选择对话框的处理
		tv_history_choice_date.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDateDialog();
			}
		});

		//地点的spinner的处理
		CitiesDao citiesDao = new CitiesDao(mContext);
		ArrayList<String> mCitiesList = citiesDao.getCities();
		mCitiesList.add(0, " 全部");
		CitiesAdapter citiesAdapter = new CitiesAdapter(mContext, R.layout.spinner_text_style, mCitiesList);
		sp_history_choice_city.setAdapter(citiesAdapter);
		//spinner下拉选项会默认执行一次，第一个触发选项的触发，这里需要将第一次的触发关闭
		sp_history_choice_city.setSelection(0,false);

		sp_history_choice_city.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Log.e("story","sp_history_choice_city选择的：position="+position+" id="+id);
				mHandler.sendEmptyMessage(0);//发送到handler，进行统一过滤处理
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Log.e("story", "-----触发了onNothingSelected监听器");
			}
		});

		//listView的处理
		lv_history_listView.setOnItemClickListener(new OnItemClickListener() {
			//记录ID号，以便判断再次点击的是否是同一个ID，
			private int showId=-1;
			//判断是否显示的标志
			private boolean isShow=false;
			//记录上一个TextView的控件，当下一个view且不是同一个ID的时候，将这一个view隐藏
			private TextView previousView;
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView tv_history_item_details=(TextView) view.findViewById(R.id.tv_history_item_details);
				ImageView iv_history_item_arrow=(ImageView) view.findViewById(R.id.iv_history_item_arrow);
				//判断textView是否已经显示,未显示则将标志位设置为false
				isShow = tv_history_item_details.isShown();
				//判断是否同一个ID，如果不是则保存该ID，并设置previousView为现在的TextView	
				if(showId!=position)
				{
					showId=position;
					//判断是否为空，为空则直接将现在TextView赋值给previousView,表示第一次进入监听器
					if(previousView==null)
					{
						previousView=tv_history_item_details;
					}
					//如果不为空，则表示不是第一次进入监听器，则需要将上一次的view隐藏掉,再将本次的TextView进行保存
					else
					{
						previousView.setVisibility(View.GONE);
						previousView=tv_history_item_details;
					}
					tv_history_item_details.setText(mHMInfoList.get(position).getDetail());
				}
				if(isShow)
				{
					isShow=false;
					tv_history_item_details.setVisibility(View.GONE);
					iv_history_item_arrow.setBackgroundResource(R.drawable.history_item_arrow_detail_down);
				}
				else
				{
					isShow=true;
					tv_history_item_details.setVisibility(View.VISIBLE);
					iv_history_item_arrow.setBackgroundResource(R.drawable.history_item_arrow_detail_up);
				}

				//地图上显示相应的信息
				HeavyMentalDataInfo hmDataBaseInfo = mHMInfoList.get(position);
				hBaiduMapManager.setNewCenter(hmDataBaseInfo.getLatitude(), hmDataBaseInfo.getLongitude(), 15f);
				mHandler.removeMessages(3);
				Message message=new Message();
				message.what=3;
				message.obj=hmDataBaseInfo;
				mHandler.sendMessage(message);
			}
		});

		//时间排序的处理
		tv_history_time_order.setOnClickListener(new OnClickListener() {
			private int order=1;
			private Drawable drawable;
			@Override
			public void onClick(View v) {
				if(tv_history_time_order.getText().toString().contains("默认")) order=1;
				switch (order) {
				case 0:
					tv_history_time_order.setText("时间：默认");
					drawable = mContext.getDrawable(R.drawable.history_order_admin);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_history_time_order.setCompoundDrawables(null, null, drawable, null);
					mHandler.sendEmptyMessage(1);
					order=1;
					break;
				case 1:
					tv_history_time_order.setText("时间：由早到晚");
					drawable = mContext.getDrawable(R.drawable.history_arrow_up_pink);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_history_time_order.setCompoundDrawables(null, null, drawable, null);
					mHandler.sendEmptyMessage(1);
					order=2;
					break;
				case 2:
					tv_history_time_order.setText("时间：由晚到早");
					drawable = mContext.getDrawable(R.drawable.history_arrow_down_pink);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_history_time_order.setCompoundDrawables(null, null, drawable, null);
					mHandler.sendEmptyMessage(1);
					order=0;
					break;

				default:
					break;
				}
				
			}
		});
		
		//污染程度排序处理
		tv_history_pollution_order.setOnClickListener(new OnClickListener() {
			private int order=1;
			private Drawable drawable;
			@Override
			public void onClick(View v) {
				if(tv_history_pollution_order.getText().toString().contains("默认")) order=1;
				switch (order) {
				case 0:
					tv_history_pollution_order.setText("污染：默认");
					drawable = mContext.getDrawable(R.drawable.history_order_admin);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_history_pollution_order.setCompoundDrawables(null, null, drawable, null);
					mHandler.sendEmptyMessage(2);
					order=1;
					break;
				case 1:
					tv_history_pollution_order.setText("污染：由重到轻");
					drawable = mContext.getDrawable(R.drawable.history_arrow_up_pink);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_history_pollution_order.setCompoundDrawables(null, null, drawable, null);
					mHandler.sendEmptyMessage(2);
					order=2;
					break;
				case 2:
					tv_history_pollution_order.setText("污染：由轻到重");
					drawable = mContext.getDrawable(R.drawable.history_arrow_down_pink);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_history_pollution_order.setCompoundDrawables(null, null, drawable, null);
					mHandler.sendEmptyMessage(2);
					order=0;
					break;

				default:
					break;
				}
			}
		});
	}

	/**
	 * 开启一个线程来从网络上获取服务器中的数据
	 */
	private void getAllDateFromNetServer() {
		Thread thread=new Thread(new Runnable() {
			@Override
			public void run() {
				String queryAllStr = QueryFromNet.queryAll();
				if(!queryAllStr.equals(HttpUtils.URL_ERROR)  && !queryAllStr.equals(HttpUtils.NET_ERROR))
				{
					if(!queryAllStr.equals(HttpUtils.PARAMETR_ERROR))
					{
						Type type=new TypeToken<ArrayList<HeavyMentalDataInfo>>(){}.getType();
						ArrayList<HeavyMentalDataInfo> tempList=mGson.fromJson(queryAllStr,type);
						if(tempList!=null)
						{
							mHMInfoList=tempList;
							Log.e("story","接收到的mHMInfoList数据为："+mHMInfoList);
							mHMInfoListAll=mHMInfoList;
							mHandler.sendEmptyMessage(4);
						}
						else
						{
							Log.e("story","gson转换的数据有误");
						}

					}
					else
					{
						Log.e("story","从服务器获取数据失败-----请求参数错误");
					}

				}
				else
				{
					Log.e("story","从服务器获取数据失败------网络连接失败");
				}
			}
		});
		thread.start();

	}

	/**
	 * 初始化百度地图
	 */
	private void initBaiduMap() {
		hBaiduMapManager=new BaiduMapManager(historyMapView, mContext);
		boolean b = hBaiduMapManager.InitMapView();
		Log.e("story","地图初始化结果为："+b);
	}
	/**
	 * 获取数据库中数据，由于数据库中可能存在很多数据，因此需要开启一个新的线程来获取
	 */
	private void getDataBaseData() {
		new Thread()
		{
			@Override
			public void run() {
				super.run();

				mHandler.sendEmptyMessage(0);
			}
		}.start();
	}
	/**
	 * 显示日期选择对话框
	 */
	protected void showDateDialog() {
		final Dialog dateDialog = new Dialog(getActivity());
		dateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window window = dateDialog.getWindow();
		window.setBackgroundDrawableResource(R.drawable.history_date_background);
		dateDialog.show();
		View view = View.inflate(mContext, R.layout.dialog_date_picker, null);
		dateDialog.setContentView(view, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,890));
		final TextView tv_startDate=(TextView) view.findViewById(R.id.tv_dialog_datepicker_startdate);
		final TextView tv_endDate=(TextView) view.findViewById(R.id.tv_dialog_datepicker_enddate);
		final TextView tv_dialog_endDate_warning=(TextView) view.findViewById(R.id.tv_dialog_enddate_warning);
		Button bt_ok=(Button) view.findViewById(R.id.bt_dialog_date_picker_ok);
		Button bt_cancel=(Button) view.findViewById(R.id.bt_dialog_date_picker_cancel);
		Button bt_dialog_date_pick_all=(Button) view.findViewById(R.id.bt_dialog_date_pick_all);
		
		final DatePicker dp_startDate=(DatePicker) view.findViewById(R.id.dp_dialog_datepicker_startdate);
		final DatePicker dp_endDate=(DatePicker) view.findViewById(R.id.dp_dialog_datepicker_enddate);
		dp_startDate.init(2017, 2, 9, new OnDateChangedListener() {
			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				String month=intFormatHandle(monthOfYear+1);
				String day=intFormatHandle(dayOfMonth);
				tv_startDate.setText(year+"年"+month+"月"+day+"日");
			}
		});
		dp_endDate.init(2017, 2, 9, new OnDateChangedListener() {
			
			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				String month=intFormatHandle(monthOfYear+1);
				String day=intFormatHandle(dayOfMonth);
				tv_endDate.setText(year+"年"+month+"月"+day+"日");
				tv_dialog_endDate_warning.setTextColor(Color.WHITE);
				tv_dialog_endDate_warning.setText("请选择结束日期");
			}
		});
		String month=intFormatHandle(dp_startDate.getMonth()+1);
		String day=intFormatHandle(dp_startDate.getDayOfMonth());
		tv_startDate.setText(dp_startDate.getYear()+"年"+month+"月"+day+"日");
		
		month=intFormatHandle(dp_endDate.getMonth()+1);
		day=intFormatHandle(dp_endDate.getDayOfMonth());
		tv_endDate.setText(dp_endDate.getYear()+"年"+month+"月"+day+"日");
		bt_ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String startDate=tv_startDate.getText().toString();
				String endDate=tv_endDate.getText().toString();
				startDate=startDate.replaceAll("日","");
				endDate=endDate.replaceAll("日", "");
				startDate=startDate.replaceAll("[\u4E00-\u9FA5]", "-");//把中文换成-
				endDate=endDate.replaceAll("[\u4E00-\u9FA5]", "-");
				if(endDate.compareTo(startDate)>0)
				{
					tv_history_choice_date.setTextSize(9);
					tv_history_choice_date.setText(startDate+"\n~\n"+endDate);
					mHandler.sendEmptyMessage(0);
					dateDialog.dismiss();
				}
				else 
				{
					tv_dialog_endDate_warning.setTextColor(Color.RED);
					tv_dialog_endDate_warning.setText("结束日期必须大于开始日期");
				}
			}
		});
		bt_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dateDialog.dismiss();
			}
		});
		bt_dialog_date_pick_all.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tv_history_choice_date.setTextSize(14);
				tv_history_choice_date.setText("全部");
				mHandler.sendEmptyMessage(0);
				dateDialog.dismiss();
			}
		});
	}
	
	/** 整数处理，当一个数为一位数的时候，前面填0
	 * @param data  需要处理的整数
	 * @return 返回响应的字符串，如果整数位1位，则前面补0，如果为两位则直接返回相应的字符串
	 */
	private String intFormatHandle(int data) {
		String dataStr=""+data;
		if(dataStr.length()==1)
		{
			dataStr="0"+dataStr;
			return dataStr;
		}	
		return dataStr;
	}
	/**
	 *  初始化控件的ID
	 */
	private void initId() {
		historyMapView=(MapView)mHistoryView.findViewById(R.id.history_BmapView);
		sp_history_pollution = (Spinner) mHistoryView.findViewById(R.id.sp_history_choice_pollution);
		tv_history_choice_date = (TextView) mHistoryView.findViewById(R.id.tv_history_choice_date);
		sp_history_choice_city=(Spinner)mHistoryView.findViewById(R.id.sp_history_choice_address);
		lv_history_listView = (ListView) mHistoryView.findViewById(R.id.lv_history_listview);
		tv_history_time_order = (TextView) mHistoryView.findViewById(R.id.tv_history_time_order);
		tv_history_pollution_order = (TextView) mHistoryView.findViewById(R.id.tv_history_pollution_order);
	}
	
	/**给城市spinner列表设置数据适配器
	 * @author story
	 *
	 */
	private class CitiesAdapter extends ArrayAdapter<String>
	{
		private List<String> citiesList;
		CitiesAdapter(Context context, int resource, List<String> lists)
		{
			super(context, resource, lists);
			citiesList=lists;
		}
		public int getCount() {
			return citiesList==null? 0:citiesList.size();
		}
		@Override
		public String getItem(int position) {
			return citiesList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent)
		{
			if(convertView==null)
			{
				convertView=View.inflate(mContext, R.layout.spinner_city_dropdown, null);
			}
			TextView tv_spinner_city_dropdown=(TextView) convertView.findViewById(R.id.tv_spinner_city_dropdown);
			TextView tv_spinner_cityFirst_dropdown=(TextView) convertView.findViewById(R.id.tv_spinner_cityfirst_dropdown);
			String city = citiesList.get(position);
			String cityFirst = city.substring(0, 1);
			String cityName = city.substring(1, city.length());
			tv_spinner_city_dropdown.setText(cityName);
			tv_spinner_cityFirst_dropdown.setText(cityFirst);
			return convertView;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null)
			{
				convertView=View.inflate(mContext, R.layout.spinner_text_style, null);
			}
			TextView tv_spinner_text=(TextView) convertView.findViewById(R.id.tv_spinner_text);
			String city = citiesList.get(position);
			String cityName = city.substring(1, city.length());
			tv_spinner_text.setText(cityName);
			return convertView;
		}
	}
	
	private class MyListViewAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return mHMInfoList==null ? 0 : mHMInfoList.size();
		}
		@Override
		public Object getItem(int position) {
			return mHMInfoList.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null)
			{
				convertView=View.inflate(mContext, R.layout.history_listview_item, null);
			}
			TextView tv_history_item_date=(TextView) convertView.findViewById(R.id.tv_history_item_date);
			TextView tv_history_item_city=(TextView) convertView.findViewById(R.id.tv_history_item_city);
			TextView tv_history_item_address=(TextView) convertView.findViewById(R.id.tv_history_item_address);
			TextView tv_history_item_pollute=(TextView) convertView.findViewById(R.id.tv_history_item_pollute);
			TextView tv_history_item_details=(TextView) convertView.findViewById(R.id.tv_history_item_details);
			tv_history_item_details.setVisibility(View.GONE);
			ImageView iv_history_item_arrow=(ImageView) convertView.findViewById(R.id.iv_history_item_arrow);
			iv_history_item_arrow.setBackgroundResource(R.drawable.history_item_arrow_detail_down);
			HeavyMentalDataInfo hmDataBaseInfo = mHMInfoList.get(position);

			tv_history_item_date.setText(MyUsingUtils.DateToString(hmDataBaseInfo.getCreatetime()));
			tv_history_item_city.setText(hmDataBaseInfo.getCity());
			tv_history_item_address.setText(hmDataBaseInfo.getAddress());
			String pollutionStr = hmDataBaseInfo.getPollution();

			//根据不同的污染情况显示不同的字体颜色
			if(pollutionStr.equals(MyConstantValue.NO_POLLUTION))
			{
				tv_history_item_pollute.setTextColor(getResources().getColor(R.color.my_no_pollution));
			}
			if(pollutionStr.equals(MyConstantValue.SLIGHT_POLLUTION))
			{
				tv_history_item_pollute.setTextColor(getResources().getColor(R.color.my_slight_pollution));
			}
			if(pollutionStr.equals(MyConstantValue.MIDDLE_POLLUTION))
			{
				tv_history_item_pollute.setTextColor(getResources().getColor(R.color.my_middle_pollution));
			}
			if(pollutionStr.equals(MyConstantValue.HIGH_POLLUTION))
			{
				tv_history_item_pollute.setTextColor(getResources().getColor(R.color.my_high_pollution));
			}
			tv_history_item_pollute.setText(pollutionStr);
			return convertView;
		}

	}
}
