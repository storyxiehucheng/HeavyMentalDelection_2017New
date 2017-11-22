package com.example.heavymentaldelection.fragment;

import java.util.ArrayList;
import java.util.List;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.example.heavymentaldelection.R;
import com.example.heavymentaldelection.Info.HMDataBaseInfo;
import com.example.heavymentaldelection.dbDao.CitiesDao;
import com.example.heavymentaldelection.dbDao.HeavyMentalDataBaseDao;
import com.example.heavymentaldelection.manager_user.BaiduMapManager;
import com.example.heavymentaldelection.my_utils.MySortFilterArrayList;
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
import android.webkit.WebView;
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
	private static final String TABLE_NAME="heavymental01";
	private View historyView;
	private Spinner sp_history_pollution;
	private Context mContext;
	private TextView tv_history_choice_date;
	private Spinner sp_history_choice_city;
	private ArrayList<String> citieslist;
	private ListView lv_history_listview;
	private HeavyMentalDataBaseDao heavyMentalDataBaseDao;
	private Handler mHandler;
	private MyListViewAdapter myListViewAdapter;
	private ArrayList<HMDataBaseInfo> hMDataBaseInfoList;
	private ArrayList<HMDataBaseInfo> hMDataBaseInfoListAll;
	private TextView tv_history_time_order;
	private TextView tv_history_pollution_order;
	private MapView historyMapView;
	private BaiduMapManager hBaiduMapManager;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		historyView = inflater.inflate(R.layout.fragment_history, container, false);
		initId();
		return historyView;
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
				case 1:
					 tv_history_pollution_order.setText("污染：默认");
					 drawable = mContext.getDrawable(R.drawable.history_order_admin);
					 drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					 tv_history_pollution_order.setCompoundDrawables(null, null, drawable, null);
					 sortDate=tv_history_time_order.getText().toString();
					 sortPollution=tv_history_pollution_order.getText().toString();
					 
					 filterListData();
					 hMDataBaseInfoList=MySortFilterArrayList.sortList(sortDate, hMDataBaseInfoList);
					 updateListAdapter();
					
					break;
				case 2:
					tv_history_time_order.setText("时间：默认");
					drawable = mContext.getDrawable(R.drawable.history_order_admin);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_history_time_order.setCompoundDrawables(null, null, drawable, null);
					sortPollution=tv_history_pollution_order.getText().toString();
					
					filterListData();
					hMDataBaseInfoList=MySortFilterArrayList.sortList(sortPollution, hMDataBaseInfoList);
					updateListAdapter();
					break;
				case 3:
					 HMDataBaseInfo hmDataBaseInfo= (HMDataBaseInfo) msg.obj;
					 Message obtainMessage = mHandler.obtainMessage(3, hmDataBaseInfo);
					 hBaiduMapManager.clearCircelOvrelay();
					 hBaiduMapManager.setCircleOverlay(hmDataBaseInfo.getLatitude(), hmDataBaseInfo.getLongitude(), time*60);
					 time++;
					 if(time==10) time=0;
					 mHandler.sendMessageDelayed(obtainMessage, 120);
					break;
				default:
					break;
				}
			}
			/**
			 * 更新数据适配器
			 */
			private void updateListAdapter() {
			 showMarkerAll(hMDataBaseInfoList);
			 if(myListViewAdapter==null)
			   {
				   myListViewAdapter = new MyListViewAdapter();
				   lv_history_listview.setAdapter(myListViewAdapter);
			   }
			   else
			   {
				   myListViewAdapter.notifyDataSetChanged();
			   }
			}
			/**
			 * 按条件过滤检测数据
			 */
			protected void filterListData() {
				filterDate=tv_history_choice_date.getText().toString();
				filterCity=(String) sp_history_choice_city.getSelectedItem();
				filterCity=filterCity.substring(1, filterCity.length());
				filterPollution=(String) sp_history_pollution.getSelectedItem();
				hMDataBaseInfoList=MySortFilterArrayList.filterProcess(filterDate,filterCity, filterPollution,hMDataBaseInfoListAll);
				updateListAdapter();
			}
			
		};
		//初始化数据
		initData();
	}
	

	/**动态显示地图的圆形动画。
	 * @param hmDataBaseInfo
	 */
	protected void setCircleAnimation(HMDataBaseInfo hmDataBaseInfo) {
		
	}
	/**在地图上显示listview中对应的标记
	 * @param hMDataBaseInfoList2
	 */
	protected void showMarkerAll(ArrayList<HMDataBaseInfo> hMDataBaseInfoList2) {
		View markreView=View.inflate(mContext, R.layout.history_map_marker, null);
		TextView tv_history_map_marker=(TextView) markreView.findViewById(R.id.tv_history_map_marker);
		//停止发送Message（3），即停止circle overlay 动画
		mHandler.removeMessages(3);
		//先清除地图上的所有覆盖物
		hBaiduMapManager.clearMapOverLay();
		if(hMDataBaseInfoList2.isEmpty())
		{
			return;
		}
		//根据列表中的数据，对应在地图上显示标注
		for (HMDataBaseInfo hmDataBaseInfo : hMDataBaseInfoList2) {
			tv_history_map_marker.setText(hmDataBaseInfo.getDetails());
			hBaiduMapManager.setOverMark(hmDataBaseInfo,hmDataBaseInfo.getLatitude(),
					hmDataBaseInfo.getLongitude(),
					hmDataBaseInfo.getCity(), markreView, new BaiduMapManager.MyInfoWindowClickListener() {
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
		//污染情况的spinner的处理
		String[] spinner_text_pollution=new String[]{"全部","无污染","轻度污染","中度污染","重度污染"};
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(mContext,R.layout.spinner_text_style ,spinner_text_pollution);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_text_style);
		sp_history_pollution.setAdapter(adapter);
		sp_history_pollution.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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
		citieslist = citiesDao.getCities();
		citieslist.add(0, " 全部");
		String[] spinner_text_address=new String[citieslist.size()];
		for(int i=0;i<citieslist.size();i++)
		{
			spinner_text_address[i]=citieslist.get(i);
		}
		CitiessAdapter citiessAdapter = new CitiessAdapter(mContext, R.layout.spinner_text_style, citieslist);
		sp_history_choice_city.setAdapter(citiessAdapter);
		sp_history_choice_city.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mHandler.sendEmptyMessage(0);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Log.e("story", "-----触发了onNothingSelected监听器");
			}
		});
		
		//listView的处理
		HeavyMentalDataBaseDao hMDataBaseDao = HeavyMentalDataBaseDao.getInstance(mContext,TABLE_NAME); 
		int count = hMDataBaseDao.getCount();
		Log.e("myBluetooth", "---------总共查询到的数据数为："+count);
		heavyMentalDataBaseDao = HeavyMentalDataBaseDao.getInstance(mContext,TABLE_NAME);
		hMDataBaseDao.deleteAll();
//		heavyMentalDataBaseDao.insert("2017-04-11", "长沙市", "长沙市海淀区", "中度污染", "长沙的测试数据",39.9942,  116.343315);
//		heavyMentalDataBaseDao.insert("2017-06-29", "武汉市", "武汉市港口区", "轻度污染",  "武汉的测试数据",39.9842,  116.353315);
//		heavyMentalDataBaseDao.insert("2017-10-19", "桂林市", "桂林市山水区", "无污染", "桂林的测试数据",39.9742,  116.323315);
//		heavyMentalDataBaseDao.insert("2017-08-14", "成都市", "成都市武侯祠区", "中度污染", "成都的测试数据",39.9642,  116.363315);
		heavyMentalDataBaseDao.insert("2017-01-20", "北京市", "北京市海淀区", "轻度污染", "北京的测试数据",39.99470,  116.3332933);
		getDataBaseData();
		
		
		lv_history_listview.setOnItemClickListener(new OnItemClickListener() {
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
				//判断textview是否已经显示,未显示则将标志位设置为false
				if(!tv_history_item_details.isShown())    isShow=false;
				//如果已经显示则，标志位TRUE
				else    isShow=true;
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
					tv_history_item_details.setText(hMDataBaseInfoList.get(position).getDetails());
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
				HMDataBaseInfo hmDataBaseInfo = hMDataBaseInfoList.get(position);
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
	 * 初始化百度地图
	 */
	private void initBaiduMap() {
		// TODO Auto-generated method stub
		hBaiduMapManager=new BaiduMapManager(historyMapView, mContext);
		hBaiduMapManager.InitMapView();
		
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
				heavyMentalDataBaseDao = HeavyMentalDataBaseDao.getInstance(mContext,TABLE_NAME);
				hMDataBaseInfoListAll = heavyMentalDataBaseDao.getAll();
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
		final TextView tv_startdate=(TextView) view.findViewById(R.id.tv_dialog_datepicker_startdate);
		final TextView tv_enddate=(TextView) view.findViewById(R.id.tv_dialog_datepicker_enddate);
		final TextView tv_dialog_enddate_warning=(TextView) view.findViewById(R.id.tv_dialog_enddate_warning);
		Button bt_ok=(Button) view.findViewById(R.id.bt_dialog_date_picker_ok);
		Button bt_cancel=(Button) view.findViewById(R.id.bt_dialog_date_picker_cancel);
		Button bt_dialog_date_pick_all=(Button) view.findViewById(R.id.bt_dialog_date_pick_all);
		
		final DatePicker dp_startdate=(DatePicker) view.findViewById(R.id.dp_dialog_datepicker_startdate);
		final DatePicker dp_enddate=(DatePicker) view.findViewById(R.id.dp_dialog_datepicker_enddate);		
		dp_startdate.init(2017, 2, 9, new OnDateChangedListener() {
			
			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				String month=intFormatHandle(monthOfYear+1);
				String day=intFormatHandle(dayOfMonth);
				tv_startdate.setText(year+"年"+month+"月"+day+"日");
			}
		});
		dp_enddate.init(2017, 2, 9, new OnDateChangedListener() {
			
			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				String month=intFormatHandle(monthOfYear+1);
				String day=intFormatHandle(dayOfMonth);
				tv_enddate.setText(year+"年"+month+"月"+day+"日");
				tv_dialog_enddate_warning.setTextColor(Color.WHITE);
				tv_dialog_enddate_warning.setText("请选择结束日期");
			}
		});
		String month=intFormatHandle(dp_startdate.getMonth()+1);
		String day=intFormatHandle(dp_startdate.getDayOfMonth());
		tv_startdate.setText(dp_startdate.getYear()+"年"+month+"月"+day+"日");
		
		month=intFormatHandle(dp_enddate.getMonth()+1);
		day=intFormatHandle(dp_enddate.getDayOfMonth());
		tv_enddate.setText(dp_enddate.getYear()+"年"+month+"月"+day+"日");		
		bt_ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String startdate=tv_startdate.getText().toString();
				String enddate=tv_enddate.getText().toString();
				startdate=startdate.replaceAll("日","");
				enddate=enddate.replaceAll("日", "");
				startdate=startdate.replaceAll("[\u4E00-\u9FA5]", "-");
				enddate=enddate.replaceAll("[\u4E00-\u9FA5]", "-");
				if(enddate.compareTo(startdate)>0)
				{
					tv_history_choice_date.setTextSize(9);
					tv_history_choice_date.setText(startdate+"\n~\n"+enddate);
					mHandler.sendEmptyMessage(0);
					dateDialog.dismiss();
				}
				else 
				{
					tv_dialog_enddate_warning.setTextColor(Color.RED);
					tv_dialog_enddate_warning.setText("结束日期必须大于开始日期");
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
				// TODO Auto-generated method stub
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
		String datastr=""+data;
		if(datastr.length()==1) 
		{
			datastr="0"+datastr;
			return datastr;
		}	
		return datastr;
		
	}
	/**
	 *  初始化控件的ID
	 */
	private void initId() {
		historyMapView=(MapView)historyView.findViewById(R.id.history_BmapView);
		sp_history_pollution = (Spinner) historyView.findViewById(R.id.sp_history_choice_pollution);
		tv_history_choice_date = (TextView) historyView.findViewById(R.id.tv_history_choice_date);
		sp_history_choice_city=(Spinner)historyView.findViewById(R.id.sp_history_choice_address);
		lv_history_listview = (ListView) historyView.findViewById(R.id.lv_history_listview);
		tv_history_time_order = (TextView) historyView.findViewById(R.id.tv_history_time_order);
		tv_history_pollution_order = (TextView) historyView.findViewById(R.id.tv_history_pollution_order);
	}
	
	/**给城市spinner列表设置数据适配器
	 * @author story
	 *
	 */
	public class CitiessAdapter extends ArrayAdapter<String>
	{
		
		public CitiessAdapter(Context context, int resource, List<String> objects) {
			super(context, resource, objects);
		}
		public int getCount() {
			return citieslist.size();
		}

		@Override
		public String getItem(int position) {
			return citieslist.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			View view = View.inflate(mContext, R.layout.spinner_city_dropdown, null);
			TextView tv_spinner_city_dropdown=(TextView) view.findViewById(R.id.tv_spinner_city_dropdown);
			TextView tv_spinner_cityfirst_dropdown=(TextView) view.findViewById(R.id.tv_spinner_cityfirst_dropdown);
			String city = citieslist.get(position);
			String cityFirst = city.substring(0, 1);
			String cityName = city.substring(1, city.length());
			tv_spinner_city_dropdown.setText(cityName);
			tv_spinner_cityfirst_dropdown.setText(cityFirst);
			return view;
		}

		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = View.inflate(mContext, R.layout.spinner_text_style, null);
			TextView tv_spinner_text=(TextView) view.findViewById(R.id.tv_spinner_text);
			String city = citieslist.get(position);
			String cityName = city.substring(1, city.length());
			tv_spinner_text.setText(cityName);
			return view;
		}
	}
	
	public class MyListViewAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return hMDataBaseInfoList.size();
		}

		@Override
		public Object getItem(int position) {
			return hMDataBaseInfoList.get(position);
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
			HMDataBaseInfo hmDataBaseInfo = hMDataBaseInfoList.get(position);
			tv_history_item_date.setText(hmDataBaseInfo.getDate());
			tv_history_item_city.setText(hmDataBaseInfo.getCity());
			tv_history_item_address.setText(hmDataBaseInfo.getAddress());
			String pollutionstr = hmDataBaseInfo.getPollution();
			if(pollutionstr.equals("无污染")) 
			{
				//直接利用数字表示颜色的时候，必须加上透明度的两位数，不然默认为00即全透明。所以一共必须为8位
				tv_history_item_pollute.setTextColor(0xFF43CD80);
			}
			if(pollutionstr.equals("轻度污染")) 
			{
				tv_history_item_pollute.setTextColor(0xFF8E8E38);
			}
			if(pollutionstr.equals("中度污染")) 
			{
				tv_history_item_pollute.setTextColor(0xFF8B4726);
			}
			if(pollutionstr.equals("重度污染")) 
			{
				tv_history_item_pollute.setTextColor(0xFF8B0000);
			}
			tv_history_item_pollute.setText(pollutionstr);
			return convertView;
		}

	}
}
