package com.example.heavymentaldelection.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.MapView;
import com.example.heavymentaldelection.Info.BaiduMapInfo;
import com.example.heavymentaldelection.R;
import com.example.heavymentaldelection.global.MyGlobalStaticVar;
import com.example.heavymentaldelection.manager_user.AchartEngineManager;
import com.example.heavymentaldelection.manager_user.BLEManager;
import com.example.heavymentaldelection.manager_user.BaiduMapManager;
import com.example.heavymentaldelection.manager_user.SpeechManager;
import com.example.heavymentaldelection.my_utils.MyAnimationUtils;
import com.example.heavymentaldelection.my_utils.MyConstantValue;
import com.example.heavymentaldelection.my_utils.MyDataAnalysis;
import com.example.heavymentaldelection.my_utils.MySortFilterArrayList;
import com.example.heavymentaldelection.my_utils.MySpUtils;
import com.example.heavymentaldelection.my_utils.MyUsingUtils;
import com.example.heavymentaldelection.service.BLEService;
import com.example.heavymentaldelection.view.MyViewShowProcess;
import org.achartengine.GraphicalView;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TreeMap;

/**
 * 主界面的fragment程序
 * 1、曲线图形的显示
 * 2、富集检测
 * 3、地图定位
 * 4、显示检测结果
 */
public class FragmentHome extends Fragment {
	private View mHomeView;//整个主界面框架  
	private LinearLayout mDisplayCurveLayout;//主界面显示检测图像的LinearLayout    
	private TextView mTv_Location;//主界面显示定位信息的view
	private Button mBtn_showMap; //控制地图是否显示的按钮
	private Button mBtn_uploadCloud;//上传云端按钮
	private MapView mMapView;//地图显示控件view
	private Button mBtn_showCurve;//控制图形是否显示的按钮
	private BaiduMapManager mBaiduMapManager;//百度地图管理器
	private Context mContext;
	private ImageButton mImgBtn_location_refresh;//定位信息刷新图像按钮  
	private Handler mHandler; 
	private int mMapTimeOut=10;//地图定位时间倒计时
	private Button mBtn_accumulation;//富集按钮
	private Button mBtn_detection;//检测按钮
	private TextView mTv_timeCountdown;//富集时间倒计时显示view
	private MyHandler mCountdownHandler;//富集时间倒计时Handler
	private BroadcastReceiver mBluetoothReceiver;//蓝牙广播接收器
	private TextView mTv_hint;//检测过程中的提示信息
	private ImageView mIv_reconnect_ble;//重新连接蓝牙
	private LinearLayout mLayout_process_animation;//检测过程中的动画显示 
	private AnimationDrawable mAnimLinearLayout;//动画显示layout
	private MyViewShowProcess mShow_process_start;//显示开始操作
	private MyViewShowProcess mShow_process_accumulation;//显示富集操作 
	private MyViewShowProcess mShow_process_detection;//显示检测操作
	private MyViewShowProcess mShow_process_end;//显示结束操作
	private ImageView mIv_start_to_accumulation;//开始富集
	private ImageView mIv_accumulation_to_detection;//富集到检测 
	private ImageView mIv_detection_to_end; //检测到结束
	private Button mBtn_end_accumulation;//结束富集按钮 
	private Handler mAccumulationHandler;//富集操作的handler
	private SpeechManager speechManager;//语音管理
	private ArrayList<String> BluetoothData_up=new ArrayList<>();//接收的蓝牙数据
    private ArrayList<String> BluetoothData_down=new ArrayList<>();
	private String mBluetoothCommand="";//蓝牙命令 
	private int mDetectionRepeatTimes=1;//检测的重复次数
	private BLEManager mBLEManager;//BLE蓝牙管理器

	private boolean isReceiveBLEData=false;
	private boolean isBLESendCommandSuccess=true;
	private static int mSample=-100;

	private Map<Integer,List<Double>> mDataSaveMap=new HashMap<>();
	private ArrayList<Double> mMaxCurList=new ArrayList<>();
	private Map<Integer,Double> mCurLineMap=new HashMap<>();

	private int mHeartChartPoint=0;
	private Handler viewHeartHandler;
	private AchartEngineManager mAchartEngineManager; //图标管理器
	private boolean mIsSetChartWindow=true;
	private ArrayList<Double> mHeartDataList_X=new ArrayList<>();
	private ArrayList<Double> mHeartDataList_Y=new ArrayList<>();
	private boolean isHeartShowing=false;
	private int connectedTimes=0;
	private ArrayList<String> mSaveDateList=new ArrayList<>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		mHomeView = inflater.inflate(R.layout.fragment_home, container, false);
		InitID();//从布局文件中初始化控件ID
		return mHomeView;
	}
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	mContext=getActivity().getApplicationContext();    	
    	initUIListener();//初始化控件监听器
    	startMainTask();//开启主要的一些任务
    }
	private void startMainTask() {
		BaiduMapInit();
		mBLEManager=new BLEManager(this.getActivity().getApplicationContext(),this.getActivity());
		startBluetoothBroadcastReceiver();
		speechManager = new SpeechManager(mContext);
		connectBluetooth();
		showChartInit("定标检测图形","电压(mv)","电流(uA)");
//		Test();
	}

	/**
	 * 曲线显示的初始化函数
	 * @param Title      图形名称
	 * @param X_Title    X轴标题
	 * @param Y_Title    Y轴标题
	 */
	private void showChartInit(String Title, String X_Title, String Y_Title)
	{
		mDisplayCurveLayout.removeAllViews();
		mAchartEngineManager = new AchartEngineManager(getActivity().getApplicationContext());
		mAchartEngineManager.setChartTitle(Title, X_Title, Y_Title);
		Log.e("story","-----开始初始化图形显示");
		mAchartEngineManager.setXYMultipleSersiesDataSet("曲线1");
		mAchartEngineManager.setXYMultipleSeriesRenderer();
		GraphicalView graphicalView = mAchartEngineManager.getGraphicalView();
		mDisplayCurveLayout.addView(graphicalView);
//		mAchartEngineManager.updateCharPoint(0,0);
		mAchartEngineManager.setChartClickable(false);
		mIsSetChartWindow=true;
		Log.e("story","曲线初始化完成");
	}

	/**
	 * 开启一个蓝牙接收广播用来接收，蓝牙状态的变化。
	 */
	@SuppressWarnings("unchecked")
	private void startBluetoothBroadcastReceiver() {
		mBluetoothReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action=intent.getAction();
				Log.e("story","接收到的action；"+action);
				if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
				{
					int state=intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
					if(state==BluetoothAdapter.STATE_ON)
					{
						mBLEManager.initBle();
					}
				}
				else if(BLEService.ACTION_GATT_CONNECTED.equals(action))
				{
					Log.e("story","BLE ACTION_GATT_CONNECTED----");
					BLEService.discoverService();
				}
				else if(BLEService.ACTION_GATT_DISCONNECTED.equals(action))
				{
					Log.e("story","BLE ACTION_GATT_DISCONNECTED");
					mTv_timeCountdown.setText("蓝牙已经断开，正在重新连接...");
					//将四个状态都恢复为初始化状态
					setProcessDeletionState(0);
					setProcessAccumlationState(0);
					setProcessStartState(0);
					setProcessEndState(0);
					//取消正在倒计时的富集时间
					if(mCountdownHandler!=null)
					{
						mCountdownHandler.sendEmptyMessage(1);
					}
					if(mHandler!=null)
					{
						mHandler.removeMessages(4);
					}
					if(connectedTimes<3)
					{
						connectedTimes++;
						Log.e("story","蓝牙连接次数: "+connectedTimes);
						connectBluetooth();
					}
					else
					{
						mIv_reconnect_ble.setVisibility(View.VISIBLE);
						mTv_timeCountdown.setTextColor(Color.RED);
						mTv_timeCountdown.setText("与检测设备连接失败，请检查后重新连接");
					}
				}
				else if(BLEService.ACTION_RECEIVED_AVAILABLE.equals(action))
				{
					Log.e("story","准备接收蓝牙数据ACTION_RECEIVED_AVAILABLE");
					Bundle bundle=intent.getBundleExtra("bluetoothData");
					try {
						String tempStr=bundle.getString("BLEData");
//						Log.e("story","广播接收到的蓝牙数据为："+tempStr);
						receiveBLEDataSuccess(tempStr,true);
					}
					catch (Exception e)
					{
						Log.e("story","！！！！！蓝牙数据接收错误"+e.toString());
						mTv_hint.append("!!!蓝牙数据接收错误\n");
						mBLEManager.disConnectDevice();
					}
				}
				else if(BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
				{
					BLEService.requestMtu(512);
					connectedTimes=0;
					Log.e("story","------BLE蓝牙服务连接成功");
					mIv_reconnect_ble.setVisibility(View.INVISIBLE);
					//连接前，先检测一下电池电量
					mTv_timeCountdown.setTextColor(Color.BLUE);
					mTv_timeCountdown.setText("蓝牙连接成功");
					if(!isBLESendCommandSuccess)
					{
						Log.e("story","开始重新发送命令..."+mBluetoothCommand);
						mHandler.sendEmptyMessageDelayed(5,5000);
					}
					else
					{
						Log.e("story","没有重新发送命令");
					}
				}
				else if(BLEService.ACTION_COMMAND_RECEIVE_OK.equals(action))
				{
					isBLESendCommandSuccess=true;
					if(mHandler!=null)
					{
						Log.e("story","------BLE蓝牙命令成功，已经取消再次发送");
						mHandler.removeMessages(4);
					}
				}
				else if(BLEService.ACTION_PRE_PROCESSING_RECEIVE_OK.equals(action))
				{
					receiveBLEDataSuccess("",false);
					Log.e("story","------第一次的预处理成功......");
				}
				else if(BLEService.ACTION_HEART_DETECTED_RECEIVE_OK.equals(action))
				{
					Log.e("story","准备接收蓝牙数据ACTION_HEART_DETECTED_RECEIVE_OK");
					Bundle bundle=intent.getBundleExtra("bluetoothData");
					try {
						String tempStr=bundle.getString("BLEData");
//						Log.e("story","广播接收到的蓝牙数据为："+tempStr);
						heartDataProcess(tempStr);
					}
					catch (Exception e)
					{
						Log.e("story","！！！！！蓝牙数据接收错误"+e.toString());
						mTv_hint.append("!!!蓝牙数据接收错误\n");
						mBLEManager.disConnectDevice();
					}
				}
			}
		};
		mContext.registerReceiver(mBluetoothReceiver,MySpUtils.makeGattUpdateIntentFilter());
	}

	/**心脏数据的处理与显示
	 * @param BLEData 蓝牙接收的数据
	 */
	private void heartDataProcess(String BLEData) {

		String[] bleStrings=BLEData.split("!");

		for (String bleString : bleStrings)
		{
			String[] strings = bleString.split("#");
			{
				if (strings[0].equals("T"))
				{
					if(strings[1].equals("D"))
					{
						speechManager.setSpeechString("沉积结束");
						SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy年MM月dd日-HH时mm分ss秒", Locale.getDefault());
						String date=dateFormat.format(new Date());
						String fileName="总磷沉积"+date;
						String fileDir="总磷检测数据";
						MyUsingUtils.DataSaveToFile(fileDir,fileName,mSaveDateList);
						mSaveDateList.clear();
					}
					else
					{
						double x=Double.parseDouble(strings[1]);
						double y=Double.parseDouble(strings[2]);
						Log.e("story","获取的显示坐标为：("+x+", "+y+")");
						mAchartEngineManager.updateCharPoint(x,y);
						mSaveDateList.add(x+"#"+y);
					}
				}
			}
		}
//		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy年MM月dd日-HH时mm分ss秒", Locale.getDefault());
//		String date=dateFormat.format(new Date());
//		DataSaveToFile(date,saveDateList);
//		Log.e("story","接收到的mHeartDataList_X数据总共为：----------------"+mHeartDataList_X.size());
//		Log.e("story","接收到的mHeartDataList_Y数据总共为：----------------"+mHeartDataList_Y.size());
////		Log.e("story","接收到的mHeartDataList_X数据总共为：----------------"+mHeartDataList_X);
//		showHeartChart();
	}

	/**
	 * 显示心脏图像曲线
	 */
	private void showHeartChart()
	{
		if(viewHeartHandler==null)
		{
			viewHeartHandler=new Handler()
			{
				@Override
				public void handleMessage(Message msg) {
//					Log.e("story","----------------mHeartDataList_X的大小为"+mHeartDataList_X.size());
//					Log.e("story","----------------mHeartChartPoint的值为："+mHeartChartPoint);
					if(mHeartChartPoint<=mHeartDataList_X.size()-1)
					{
						isHeartShowing=true;
						double x=mHeartDataList_X.get(mHeartChartPoint)*10;
						double y=mHeartDataList_Y.get(mHeartChartPoint);
						if(x>100 && mIsSetChartWindow)
						{
							mAchartEngineManager.updateCharWindowX(x-500,x+500);
//							mAchartEngineManager.updateCharWindowY(y-5,y+5);
						}
//						mAchartEngineManager.updateCharWindowY(y-10,y+10);
						mAchartEngineManager.updateCharPoint(x,y);
						mHeartChartPoint++;
						viewHeartHandler.sendEmptyMessageDelayed(0,200);
					}
					else
					{
						isHeartShowing=false;
						viewHeartHandler.removeMessages(0);
					}
				}
			};
		}
		if(!isHeartShowing)
		{
			viewHeartHandler.sendEmptyMessageDelayed(0,1000);
		}
	}

	/*
	* 接收成功后的，蓝牙数据处理
	* */
	private void receiveBLEDataSuccess(String BLEData,boolean isPreProcess) {
		if(isPreProcess)
		{
			//解析接收的蓝牙数据
			String[] bleStrings=BLEData.split("!");
//		Log.e("story","接收到的数据总共为："+bleStrings.length);
			BluetoothData_up.clear();
			BluetoothData_down.clear();
			for (String bleString : bleStrings)
			{
				String[] strings = bleString.split("#");
				{
					if (strings[0].equals("U"))
					{
						if(!BluetoothData_up.contains(strings[1] + "#" + strings[2]))
						{
							BluetoothData_up.add(strings[1] + "#" + strings[2]);
						}

					}
					else if (strings[0].equals("D"))
					{
						if(!BluetoothData_down.contains(strings[1] + "#" + strings[2]))
						{
							BluetoothData_down.add(strings[1] + "#" + strings[2]);
						}
					}
				}
			}
			Log.e("story","接收到的BluetoothData_up数据总共为："+BluetoothData_up.size());
			Log.e("story","接收到的BluetoothData_down数据总共为："+BluetoothData_down.size());

			//设置结束状态为完成状态
			setProcessEndState(2);
			//设置检测状态为完成状态
			setProcessDeletionState(2);
		    //Todo
			// 将检测结果存入本地文件
			SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy年MM月dd日-HH时mm分ss秒", Locale.getDefault());
			String date=dateFormat.format(new Date());
//			String fileName=date+"-"+mSample+"-"+"#"+mDetectionRepeatTimes;
			String fileName=date+"-"+"总磷检测"+"-"+"#"+mDetectionRepeatTimes;
			String fileDir="总磷检测数据";
		    MyUsingUtils.DataSaveToFile(fileDir,fileName,BluetoothData_up);
			//数据处理，得出结果
			bluetoothDataProcess(BluetoothData_up,BluetoothData_down);
		}
		if(mDetectionRepeatTimes<5)
		{
			++mDetectionRepeatTimes;
			isReceiveBLEData=true;
			BLECommandSend(mBluetoothCommand, true);
		}
		else
		{
			//设置语音提醒
			speechManager.setSpeechString("检测结束");
			mTv_timeCountdown.setText("检测结束");
		}
	}

	private void initUIListener() {
		mBaiduMapManager=new BaiduMapManager(mMapView,mContext);
		mBtn_showMap.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//隐藏图形显示控件，显示地图显示控件
				mDisplayCurveLayout.setVisibility(View.INVISIBLE);
				mMapView.setVisibility(View.VISIBLE);
				BaiduMapInit();	
			}
		});
		mDisplayCurveLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsSetChartWindow=false;
				mAchartEngineManager.setChartClickable(false);
			}
		});

		mBtn_showCurve.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//隐藏地图显示控件,显示图形显示控件
				mMapView.setVisibility(View.INVISIBLE);
				mDisplayCurveLayout.setVisibility(View.VISIBLE);
			}
		});	
		mTv_Location.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//当定位不准确，或者失败的时候，点击字体控件可跳转到设置界面，设置GPS或者网络等
				String text = (String)mTv_Location.getText();
				if(text.contains("点击跳转到设置"))
				{
					Intent intent=new Intent(Settings.ACTION_SETTINGS);
					startActivity(intent);
				}
			}
		});
		mImgBtn_location_refresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//定位刷新
				MyAnimationUtils.setAnimation(mImgBtn_location_refresh);
				BaiduMapInit();
			}
		});

		//富集操作按钮
		mBtn_accumulation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				showChartInit("检测图形","时间(ms)","电流(uA)");
//				mIsSetChartWindow=true;
//				mAchartEngineManager.setChartClickable(true);
//				mAchartEngineManager.updateCharWindowY(0,100);
//				mAchartEngineManager.updateCharWindowX(0,1000);
//				mBluetoothCommand="StartHeart";

//				mBluetoothCommand="000000, AT+NAME=USR-BLE3";
//				mBluetoothCommand="000000, AT+BATEN=ON";
//				mBluetoothCommand="fhaklfakl";

//				BLECommandSend(mBluetoothCommand, true);

//				showCalibrationSampleDialog();
				showScanningMethodDialog();
			}
		});
		//检测操作按钮
		mBtn_detection.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				mCurLineMap.put(0,2.5243);
//				mCurLineMap.put(50,3.1835);
//				mCurLineMap.put(125,4.0964);
//				mCurLineMap.put(250,5.1399);
//				mCurLineMap.put(500,7.5252);
//				mCurLineMap.put(750,9.8461);

				if(mCurLineMap.size()<4)
				{
					Toast.makeText(getActivity(),"标定未完成，请先标定后再检测",Toast.LENGTH_SHORT).show();
				}
				else
				{
					mSample=MyConstantValue.SAMPLE_ACTUAL;
					Set<Integer> keys = mCurLineMap.keySet();
					for(int key:keys)
					{
						Log.e("story","需要拟合的点为：("+key+","+mCurLineMap.get(key)+")");
					}
					showChartInit("实际水样检测图形","电压(mv)","电流(uA)");
					showCalibrationCurve();
					showSWVParameterSet(); //选择电化学检测方法,有CV，DPV，SWV
				}
			}
		});
		//结束富集按钮
		mBtn_end_accumulation.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mCountdownHandler!=null)
				{
					Message obtainMessage = mCountdownHandler.obtainMessage(0, 0);
					mCountdownHandler.sendMessage(obtainMessage);
				}
			}
		});
		//云端上传按钮
		mBtn_uploadCloud.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mAchartEngineManager.ClearCurve();
				showChartInit("溶解沉积曲线","时间(100ms)","电流(uA)");
				mTv_timeCountdown.setText("正在检测....");
				mTv_hint.setText("");

//				mIsSetChartWindow=true;
//				mAchartEngineManager.setChartClickable(true);

			}
		});
		//BLE蓝牙重连
		mIv_reconnect_ble.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MySpUtils.putBoolean(mContext, MyConstantValue.BLUETOOTH_FIRST_INIT, true);
				//清除本地保存的蓝牙设备，重新选择连接
				MySpUtils.putString(mContext, MyConstantValue.BLUETOOTH_ADDRESS, null);
				MySpUtils.putString(mContext, MyConstantValue.BLUETOOTH_NAME, null);
				connectBluetooth();
			}
		});
	}

	/**
	 * 显示标定曲线
	 */
	private void showCalibrationCurve()
	{
		MyDataAnalysis.calibrationCurve(mCurLineMap);

		ArrayList<Double> calibrationCurveList_X=new ArrayList<>();
		ArrayList<Double> calibrationCurveList_Y=new ArrayList<>();
		calibrationCurveList_X.add(0.0);
		calibrationCurveList_Y.add(MyGlobalStaticVar.Curve_Intercept);

		calibrationCurveList_X.add(20.0);
		calibrationCurveList_Y.add(MyGlobalStaticVar.Curve_Slope*20+MyGlobalStaticVar.Curve_Intercept);

		calibrationCurveList_X.add(40.0);
		calibrationCurveList_Y.add(MyGlobalStaticVar.Curve_Slope*40+MyGlobalStaticVar.Curve_Intercept);

		calibrationCurveList_X.add(60.0);
		calibrationCurveList_Y.add(MyGlobalStaticVar.Curve_Slope*60+MyGlobalStaticVar.Curve_Intercept);

		calibrationCurveList_X.add(100.0);
		calibrationCurveList_Y.add(MyGlobalStaticVar.Curve_Slope*100+MyGlobalStaticVar.Curve_Intercept);

		calibrationCurveList_X.add(200.0);
		calibrationCurveList_Y.add(MyGlobalStaticVar.Curve_Slope*200+MyGlobalStaticVar.Curve_Intercept);

		calibrationCurveList_X.add(400.0);
		calibrationCurveList_Y.add(MyGlobalStaticVar.Curve_Slope*400+MyGlobalStaticVar.Curve_Intercept);

		calibrationCurveList_X.add(600.0);
		calibrationCurveList_Y.add(MyGlobalStaticVar.Curve_Slope*600+MyGlobalStaticVar.Curve_Intercept);

		calibrationCurveList_X.add(800.0);
		calibrationCurveList_Y.add(MyGlobalStaticVar.Curve_Slope*800+MyGlobalStaticVar.Curve_Intercept);

		calibrationCurveList_X.add(1000.0);
		calibrationCurveList_Y.add(MyGlobalStaticVar.Curve_Slope*1000+MyGlobalStaticVar.Curve_Intercept);

		showChartInit("标定曲线","浓度(ug/L)","电流(uA)");

		mAchartEngineManager.AddXYseries("标定曲线",Color.WHITE,calibrationCurveList_X,calibrationCurveList_Y);
	}

	/**
	 * 显示标定对话框
	 */
	private void showCalibrationDialog() {
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		View view=View.inflate(getActivity(),R.layout.dialog_calibration,null);
		builder.setView(view);
		final AlertDialog alertDialog=builder.create();
		alertDialog.show();
		final Button btn_calibration_1000=(Button)view.findViewById(R.id.btn_calibration_1000);
		final Button btn_calibration_600=(Button)view.findViewById(R.id.btn_calibration_600);
		final Button btn_calibration_400=(Button)view.findViewById(R.id.btn_calibration_400);
		final Button btn_calibration_200=(Button)view.findViewById(R.id.btn_calibration_200);
		final Button btn_calibration_100=(Button)view.findViewById(R.id.btn_calibration_100);
		final Button btn_calibration_75=(Button)view.findViewById(R.id.btn_calibration_75);
		final Button btn_calibration_50=(Button)view.findViewById(R.id.btn_calibration_50);
		final Button btn_calibration_25=(Button)view.findViewById(R.id.btn_calibration_25);
		final Button btn_calibration_0=(Button)view.findViewById(R.id.btn_calibration_0);

		btn_calibration_1000.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSample=MyConstantValue.SAMPLE_1000;
				btn_calibration_1000.setBackgroundColor(Color.CYAN);
				showSWVParameterSet();
			}
		});
		btn_calibration_600.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSample=MyConstantValue.SAMPLE_600;
				btn_calibration_600.setBackgroundColor(Color.CYAN);
				showSWVParameterSet();
			}
		});
		btn_calibration_400.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				mSample=MyConstantValue.SAMPLE_400;
				btn_calibration_400.setBackgroundColor(Color.CYAN);
				showSWVParameterSet();
			}
		});
		btn_calibration_200.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				mSample=MyConstantValue.SAMPLE_200;
				showSWVParameterSet();
				btn_calibration_200.setBackgroundColor(Color.CYAN);
			}
		});
		btn_calibration_100.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSample=MyConstantValue.SAMPLE_100;
				showSWVParameterSet();
				btn_calibration_100.setBackgroundColor(Color.CYAN);
			}
		});
		btn_calibration_75.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSample=MyConstantValue.SAMPLE_75;
				showSWVParameterSet();
				btn_calibration_75.setBackgroundColor(Color.CYAN);
			}
		});
		btn_calibration_50.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSample=MyConstantValue.SAMPLE_50;
				btn_calibration_50.setBackgroundColor(Color.CYAN);
				showSWVParameterSet();
			}
		});
		btn_calibration_25.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSample=MyConstantValue.SAMPLE_25;
				btn_calibration_25.setBackgroundColor(Color.CYAN);
				showSWVParameterSet();
			}
		});
		btn_calibration_0.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSample=MyConstantValue.SAMPLE_0;
				btn_calibration_0.setBackgroundColor(Color.CYAN);
				showSWVParameterSet();
			}
		});
	}	/**
	 * 显示实际水样标定对话框
	 */
	private void showCalibrationSampleDialog() {
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		View view=View.inflate(getActivity(),R.layout.dialog_calibration_sample,null);
		builder.setView(view);
		final AlertDialog alertDialog=builder.create();
		alertDialog.show();
		final Button btn_calibration_1000=(Button)view.findViewById(R.id.btn_calibration_sample_1000);
		final Button btn_calibration_750=(Button)view.findViewById(R.id.btn_calibration_sample_750);
		final Button btn_calibration_500=(Button)view.findViewById(R.id.btn_calibration_sample_500);
		final Button btn_calibration_250=(Button)view.findViewById(R.id.btn_calibration_sample_250);
		final Button btn_calibration_125=(Button)view.findViewById(R.id.btn_calibration_sample_125);
		final Button btn_calibration_50=(Button)view.findViewById(R.id.btn_calibration_sample_50);
		final Button btn_calibration_0=(Button)view.findViewById(R.id.btn_calibration_sample_0);

		btn_calibration_1000.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSample=MyConstantValue.SAMPLE_1000;
				btn_calibration_1000.setBackgroundColor(Color.CYAN);
				showSWVParameterSet();
			}
		});
		btn_calibration_750.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSample=MyConstantValue.SAMPLE_750;
				btn_calibration_750.setBackgroundColor(Color.CYAN);
				showSWVParameterSet();
			}
		});
		btn_calibration_500.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				mSample=MyConstantValue.SAMPLE_500;
				btn_calibration_500.setBackgroundColor(Color.CYAN);
				showSWVParameterSet();
			}
		});
		btn_calibration_250.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				mSample=MyConstantValue.SAMPLE_250;
				showSWVParameterSet();
				btn_calibration_250.setBackgroundColor(Color.CYAN);
			}
		});
		btn_calibration_125.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSample=MyConstantValue.SAMPLE_125;
				showSWVParameterSet();
				btn_calibration_125.setBackgroundColor(Color.CYAN);
			}
		});
		btn_calibration_50.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSample=MyConstantValue.SAMPLE_50;
				btn_calibration_50.setBackgroundColor(Color.CYAN);
				showSWVParameterSet();
			}
		});
		btn_calibration_0.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSample=MyConstantValue.SAMPLE_0;
				btn_calibration_0.setBackgroundColor(Color.CYAN);
				showSWVParameterSet();
			}
		});
	}


	/**
	 * 扫描方式的选择
	 */
	private void showScanningMethodDialog() {
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		View view=View.inflate(getActivity(),R.layout.scanning_method_layout,null);
		builder.setView(view);
		final AlertDialog alertDialog=builder.create();
		alertDialog.show();
		Button swvButton=(Button)view.findViewById(R.id.bt_SWV);
		Button lsvButton=(Button)view.findViewById(R.id.bt_LSV);
		Button dpvButton=(Button)view.findViewById(R.id.bt_DPV);
		swvButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
				mBluetoothCommand="StartDiss";
				mDetectionRepeatTimes=5;
				isReceiveBLEData=true;
				BLECommandSend(mBluetoothCommand, true);
			}
		});
		lsvButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
				mAchartEngineManager.setChartTitle("检测曲线","电压(mv)","电流(uA)");
				mAchartEngineManager.updateCharWindowX(-625,-195);
				mAchartEngineManager.updateCharWindowY(-10,-2);
				mBluetoothCommand="StartDetection";
				mDetectionRepeatTimes=3;
				isReceiveBLEData=true;
				BLECommandSend(mBluetoothCommand, true);
			}
		});
		dpvButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
				mAchartEngineManager.setChartTitle("硝酸盐水质检测系统","时间(s)","电流(uA)");
				mAchartEngineManager.updateCharWindowX(-10,325);
				mBluetoothCommand="StartDep";
				mDetectionRepeatTimes=5;
				isReceiveBLEData=true;
				BLECommandSend(mBluetoothCommand, true);
			}
		});
	}

	private void showSWVParameterSet() {
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		View view=View.inflate(getActivity(),R.layout.swv_parameter_layout,null);
		builder.setView(view);
		final AlertDialog alertDialog=builder.create();
		alertDialog.show();
		final EditText et_dialog_InitV=(EditText)view.findViewById(R.id.et_dialog_InitV);
		et_dialog_InitV.setHint("150");
		final EditText et_dialog_EndV=(EditText)view.findViewById(R.id.et_dialog_EndV);
		et_dialog_EndV.setHint("400");
		final EditText et_dialog_Frequency=(EditText)view.findViewById(R.id.et_dialog_Frequency);
		et_dialog_Frequency.setHint("25");
		final EditText et_dialog_Amplitude=(EditText)view.findViewById(R.id.et_dialog_Amplitude);
		et_dialog_Amplitude.setHint("25");
		Button bt_dialog_ParameterSet_OK=(Button)view.findViewById(R.id.bt_dialog_ParameterSet_OK);
		bt_dialog_ParameterSet_OK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String InitV=et_dialog_InitV.getText().toString();
				String EndV=et_dialog_EndV.getText().toString();
				String Frequency=et_dialog_Frequency.getText().toString();
				String Amplitude=et_dialog_Amplitude.getText().toString();
				if(InitV.isEmpty())
				{
//					et_dialog_InitV.setError("初始值不能设置为空");
					InitV="150";
				}
				if(EndV.isEmpty())
				{
//					et_dialog_EndV.setError("结束值不能设置为空");
					EndV="400";
				}
				if(Frequency.isEmpty())
				{
//					et_dialog_Frequency.setError("频率不能设置为空");
					Frequency="25";
				}
				if(Amplitude.isEmpty())
				{
//					et_dialog_Amplitude.setError("脉冲高度不能设置为空");
					Amplitude="25";
				}
				if(Integer.parseInt(InitV)>=2000||Integer.parseInt(InitV)<=-2000)
				{
					et_dialog_InitV.setError("电压范围为-2000~2000");
				}
				else if(Integer.parseInt(EndV)<=Integer.parseInt(InitV))
				{
					et_dialog_EndV.setError("结束值必须大于开始值");
				}
				else if(Integer.parseInt(EndV)<=-2000||Integer.parseInt(EndV)>=2000)
				{
					et_dialog_EndV.setError("电压范围为-2000~2000");
				}
				else if(Integer.parseInt(Frequency)>25)
				{
					et_dialog_Frequency.setError("很抱歉，频率值范围为0~25");
				}
				else
				{
					alertDialog.dismiss();
					Log.e("story", "传输的蓝牙命令为："+"SWV"+InitV+"$"+EndV+"!"+Frequency+"&"+Amplitude);
					mBluetoothCommand="SWV!"+InitV+"!"+EndV+"!"+Frequency+"!"+Amplitude;
					mDetectionRepeatTimes=3;
					mMaxCurList.clear();
					isReceiveBLEData=true;
					speechManager.setSpeechString("开始检测");
					mTv_hint.setTextColor(Color.BLUE);
					if(mSample==MyConstantValue.SAMPLE_ACTUAL)
					{
						mTv_hint.append("\n正在检测待测液....请稍后");
					}
					else
					{
						mTv_hint.append("标样："+mSample+"μg/L\n");
					}

					BLECommandSend(mBluetoothCommand, true);
				}
			}
		});
	}

	/**
	 * 发送蓝牙命令
	 */
	protected void BLECommandSend(String bluetoothCommand, boolean isReceiveBleData) {
		if(mBLEManager.isConnected())
		{
			//设置文字提示"正在检测,请稍后"
			mTv_timeCountdown.setText("总氮: 正在进行第"+mDetectionRepeatTimes+"检测....");
			Log.e("story","总氮"+": 正在进行第--- "+mDetectionRepeatTimes+" ---检测,检测命令为："+bluetoothCommand);
//			if(mSample==MyConstantValue.SAMPLE_ACTUAL)
//			{
//				mTv_timeCountdown.setText("待测液: 正在进行第"+mDetectionRepeatTimes+"检测....");
//				Log.e("story","实际待测液: 正在进行第--- "+mDetectionRepeatTimes+" ---检测,检测命令为："+bluetoothCommand);
//			}
//			else
//			{
//				mTv_timeCountdown.setText("标样"+mSample+": 正在进行第"+mDetectionRepeatTimes+"检测....");
//				Log.e("story","标样"+mSample+": 正在进行第--- "+mDetectionRepeatTimes+" ---检测,检测命令为："+bluetoothCommand);
//			}
			//发送数据，提示检测设备可以开始检测
			isBLESendCommandSuccess=false;
			BLEService.writeData(bluetoothCommand,false);
			mHandler.sendEmptyMessageDelayed(4,10000);
		}
		else
		{
			//连接蓝牙
			connectBluetooth();
		}
	}
	/**
	 * 开启富集的动画
	 */
	private void setAccumulationAnimation()
	{
		mLayout_process_animation.setVisibility(View.INVISIBLE);
		mLayout_process_animation.setBackgroundResource(R.drawable.accumlation_animation);
		//--------------------------------------------------------取消富集动画
		mLayout_process_animation.setVisibility(View.INVISIBLE);
		mAnimLinearLayout = (AnimationDrawable) mLayout_process_animation.getBackground();
		mAnimLinearLayout.start();
	}
	/**
	 * 开启一个检测的动画
	 */
	private void setDetectionAnimation()
	{
		mLayout_process_animation.setVisibility(View.INVISIBLE);
		mLayout_process_animation.setBackgroundResource(R.drawable.delection_animation);
		//--------------------------------------------------------取消检测动画
		mLayout_process_animation.setVisibility(View.INVISIBLE);
		mAnimLinearLayout = (AnimationDrawable) mLayout_process_animation.getBackground();
		mAnimLinearLayout.start();
	}
	
	/**
	 * 断开蓝牙连接，用于在退出时断开蓝牙连接
	 */
	public void cancelConnectBluetooth()
	{
		if(mBLEManager!=null) mBLEManager.disConnectDevice();
		else Log.e("story","mBLEManger未初始化!");
	}
	/**
	 * 连接蓝牙，并在蓝牙重新开启的时候再次通过此函数连接蓝牙
	 */
	private void connectBluetooth() {

		//首先判断蓝牙是否打开,如果打开了，则进行连接
		if(MyGlobalStaticVar.isBleOpen)
		{
			mTv_timeCountdown.setTextColor(Color.RED);
			mIv_reconnect_ble.setVisibility(View.INVISIBLE);
			mTv_timeCountdown.setText("正在连接蓝牙...请稍后");
			if(!mBLEManager.startBleDeviceConnect())
			{
				bleShouldOpen();
			}
		}
		//如果没有打开则重新提醒用户，打开蓝牙
		else
		{
			bleShouldOpen();
		}
	}

	private void bleShouldOpen() {
		mTv_timeCountdown.setTextColor(Color.RED);
		mIv_reconnect_ble.setVisibility(View.VISIBLE);
		mTv_timeCountdown.setText("蓝牙设备已经关闭，请打开后重试");
		Intent intent=new Intent(MyConstantValue.ACTION_SHOULD_OPEN_BLE);
		mContext.sendBroadcast(intent);
	}

	/**处理数据，计算出数据的结果
	 * @param bluetoothData_up 接收的第一条数据
	 * @param bluetoothData_down 接收的第二条数据
	 */
	public void bluetoothDataProcess(ArrayList<String> bluetoothData_up, ArrayList<String> bluetoothData_down) {
		String tempVolt;
		String tempCur;
		//定义四个集合来装分离出来的电压电流
		ArrayList<Double> bluetoothData_up_Volt=new ArrayList<>();
		ArrayList<Double> bluetoothData_up_Cur=new ArrayList<>();
//		ArrayList<Double> bluetoothData_down_Volt=new ArrayList<>();
//		ArrayList<Double> bluetoothData_down_Cur=new ArrayList<>();

		//定义一个treeMap来保留按电压排序后的电压-电流对
		TreeMap<Integer,Double> voltCurMap=new TreeMap<>();
		//设置显示格式
		DecimalFormat df=new DecimalFormat("#.0000");//保留4位小数
		//按电压的大小排序
		Log.e("story", "进入图形处理函数");
		if(!bluetoothData_up.isEmpty()) {
			Log.e("story", "开始排序");
			ArrayList<String> bluetoothData_upSortVolt = MySortFilterArrayList.sortString(bluetoothData_up, true);
			ArrayList<String> bluetoothData_upSortCur = MySortFilterArrayList.sortString(bluetoothData_up, false);
//			mTv_hint.append("\n--UpSize:" + bluetoothData_upSortCur.size());
			//遍历集合取出电压，电流的值
			for (String str : bluetoothData_upSortVolt) {
				String[] strVolt = str.split("#");
				Log.e("story", "电压：" + strVolt[0] + "  电流：" + strVolt[1]);
				try {
					tempVolt = df.format(Double.parseDouble(strVolt[0]));
					bluetoothData_up_Volt.add(Double.valueOf(tempVolt));
					tempCur = df.format(Double.valueOf(strVolt[1]));
					bluetoothData_up_Cur.add(Double.parseDouble(tempCur));
					//将电流电压键值对，放入treeMap当中
					voltCurMap.put(Integer.valueOf(strVolt[0]), Double.parseDouble(tempCur));
				} catch (Exception e) {
					Log.e("story", "电流电压转换错误---" + "电压：" + strVolt[0] + "  电流：" + strVolt[1]);
				}
			}
			ArrayList<Integer> maxPointList = MyDataAnalysis.findExtremeMaxPoint_2(voltCurMap);//寻找到极大值点,返回对应的电压值集合
			Log.e("story", "maxPointList：" + maxPointList);
			if (!maxPointList.isEmpty())
			{
				for (int i = 0; i < maxPointList.size(); i++) {
//					if(maxPointList.get(i)<200) continue;
					int tempMaxPoint = maxPointList.get(i);
					double sum = voltCurMap.get(tempMaxPoint);
					Log.e("story", "峰电位---：" + tempMaxPoint + " 峰电流---：" + voltCurMap.get(tempMaxPoint));
					int k;
					for (k = 1; k < 5; k++) {
						if(voltCurMap.containsKey(tempMaxPoint + k * 4) && voltCurMap.containsKey(tempMaxPoint - k * 4))
						{
							sum += voltCurMap.get(tempMaxPoint + k * 4);
							Log.e("story", "增大方向：" + (tempMaxPoint + k * 4) + " 电流：" + voltCurMap.get(tempMaxPoint + k * 4));
							sum += voltCurMap.get(tempMaxPoint - k * 4);
							Log.e("story", "减小方向：" +(tempMaxPoint - k * 4) + " 电流：" + voltCurMap.get(tempMaxPoint - k * 4));
						}
						else
						{
							break;
						}
					}
					Log.e("story","K的值为："+k);
					double average = sum / (k*2-1);
					mMaxCurList.add(average);
					String formatAverage = df.format(average);
					if(mDetectionRepeatTimes==5)
					{
						mTv_hint.append("\n #峰电位:" + maxPointList.get(i) + " 峰电流:" + formatAverage);
					}
					else
					{
						mTv_hint.append("\n #峰电位:" + maxPointList.get(i) + " 峰电流:" + formatAverage);
					}
					Log.e("story", "峰电位:" + maxPointList.get(i) + " 峰电位周围的平均值为:" + average);
				}
			}
			else
			{
				if(mSample==MyConstantValue.SAMPLE_0)
				{
					Log.e("story","自定义坐标值电位为298");
					mMaxCurList.add(voltCurMap.get(298));
					if(mDetectionRepeatTimes==5)
					{
						mTv_hint.append("\n #指定峰电位:298"+" 峰电流:"+voltCurMap.get(298));
					}
					else
					{
						mTv_hint.append(" #指定峰电位:298"+" 峰电流:"+voltCurMap.get(298));
					}

				}
				else mTv_hint.append("——没有极值点");
			}
			switch (mDetectionRepeatTimes) {
				case 1:
					mAchartEngineManager.updateChart(bluetoothData_up_Volt, bluetoothData_up_Cur);
					break;
				case 2:
					mAchartEngineManager.AddXYseries("曲线2", Color.YELLOW, bluetoothData_up_Volt, bluetoothData_up_Cur);
					break;
				case 3:
					mAchartEngineManager.AddXYseries("曲线3", Color.MAGENTA, bluetoothData_up_Volt, bluetoothData_up_Cur);
					break;
				case 4:
					mAchartEngineManager.AddXYseries("曲线4", Color.GREEN, bluetoothData_up_Volt, bluetoothData_up_Cur);
					break;
				case 5:
					mAchartEngineManager.AddXYseries("曲线5", Color.WHITE, bluetoothData_up_Volt, bluetoothData_up_Cur);
					break;
				default:
					break;
			}

			Log.e("story", "mMaxCurList是否为空：" + mMaxCurList.isEmpty());
			Log.e("story", "mMaxCurList的值：" + mMaxCurList);
			if (mDetectionRepeatTimes == 5)
			{
				if(mMaxCurList.isEmpty())
				{
					Log.e("story", "mMaxCurList为空,直接返回");
					return;
				}
				Log.e("story", "开始计算电流平均值.....mMaxCurList：" + mMaxCurList);
				double average = MyUsingUtils.averageList(mMaxCurList);
//				mTv_hint.append(" ----电流平均值：" + average);
				Log.e("story", "电流平均值为：" + average);
				switch (mSample)
				{
					case MyConstantValue.SAMPLE_1000:
						mCurLineMap.put(MyConstantValue.SAMPLE_1000, average);
						break;
					case MyConstantValue.SAMPLE_750:
						mCurLineMap.put(MyConstantValue.SAMPLE_750, average);
						break;
					case MyConstantValue.SAMPLE_600:
						mCurLineMap.put(MyConstantValue.SAMPLE_600, average);
						break;
					case MyConstantValue.SAMPLE_500:
						mCurLineMap.put(MyConstantValue.SAMPLE_500, average);
						break;
					case MyConstantValue.SAMPLE_400:
						mCurLineMap.put(MyConstantValue.SAMPLE_400, average);
						break;
					case MyConstantValue.SAMPLE_250:
						mCurLineMap.put(MyConstantValue.SAMPLE_250, average);
						break;
					case MyConstantValue.SAMPLE_200:
						mCurLineMap.put(MyConstantValue.SAMPLE_200, average);
						break;
					case MyConstantValue.SAMPLE_125:
						mCurLineMap.put(MyConstantValue.SAMPLE_125, average);
						break;
					case MyConstantValue.SAMPLE_100:
						mCurLineMap.put(MyConstantValue.SAMPLE_100, average);
						break;
					case MyConstantValue.SAMPLE_75:
						mCurLineMap.put(MyConstantValue.SAMPLE_75, average);
						break;
					case MyConstantValue.SAMPLE_50:
						mCurLineMap.put(MyConstantValue.SAMPLE_50, average);
						break;
					case MyConstantValue.SAMPLE_25:
						mCurLineMap.put(MyConstantValue.SAMPLE_25, average);
						break;
					case MyConstantValue.SAMPLE_0:
						mCurLineMap.put(MyConstantValue.SAMPLE_0, average);
						break;
					case MyConstantValue.SAMPLE_ACTUAL:
						showCalibrationCurve();
						calculationResult(average);
						break;
					default:
						break;
				}
				mMaxCurList.clear();
			}
			else if(mSample==MyConstantValue.SAMPLE_ACTUAL)
			{
				if(mMaxCurList.isEmpty())
				{
					Log.e("story", "mMaxCurList为空,直接返回");
					return;
				}
				showCalibrationCurve();
				calculationResult(mMaxCurList.get(mMaxCurList.size()-1));
			}
		}
	}

	/**计算检测结果
	 * @param average 需要计算的值
	 */
	private void calculationResult(double average)
	{
		if(MyGlobalStaticVar.isCurveFitting)
		{
			double result=(average-MyGlobalStaticVar.Curve_Intercept)/MyGlobalStaticVar.Curve_Slope;
			ArrayList<Double> resultList_X=new ArrayList<>();
			ArrayList<Double> resultList_Y=new ArrayList<>();
			resultList_X.add(result);
			resultList_Y.add(average);
			mAchartEngineManager.AddXYseries("实际检测点",Color.RED,resultList_X,resultList_Y);
			mTv_hint.append("\n 检测的浓度为；"+result);
			Log.e("story","最终的检测结果为："+result);
		}

	}

	/**
	 * 富集操作的所有任务
	 */
	@SuppressLint({ "InflateParams", "HandlerLeak" })
	protected void accumlationProcess() {
		if(mAccumulationHandler==null)
		{
			mAccumulationHandler = new Handler()
			{
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case 0:
						//开启富集时间倒计时
						accumulationTimeCountDown(mTv_timeCountdown,msg.arg1);
						break;
					case 1:
						mTv_timeCountdown.setTextColor(Color.BLACK);
						mTv_timeCountdown.setText("富集操作已经取消");
						break;
	
					default:
						break;
					}				
				}
				
			};
		}
		//显示时间选择对话框
		showTimeSetDialog(mAccumulationHandler);		
	}
	
	/**根据总时间进行倒计时显示
	 * @param totalTime 需要倒计时的总时间
	 * @param timeView 需要显示倒计时的view控件
	 */
	protected void accumulationTimeCountDown(final TextView timeView,int totalTime)
	{
		if(mCountdownHandler==null)
		{
			mCountdownHandler = new MyHandler(){
				@SuppressLint("HandlerLeak")
				private int h;
				private int m;
				private int s;
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case 0:
						int timedata=msg.arg1;
						timedata--;
						if(timedata>0)
						{
							h=(timedata/3600);
							m=(timedata%3600)/60;
							s=(timedata%3600)%60;
							timeView.setTextColor(0xff6A5ACD);
							timeView.setText("富集时间剩余："+h+"时"+m+"分"+s+"秒");
							Message message = this.obtainMessage(0);
							message.arg1=timedata;
							this.sendMessageDelayed(message, 1000);
							
						}
						else
						{
							this.removeMessages(0);
							timeView.setTextColor(0xFF8A2BE2);
							timeView.setText("富集时间结束，请开始检测");
							//设置富集操作的状态为富集完成
							setProcessAccumlationState(2);
							//隐藏"结束富集"按钮
							mBtn_end_accumulation.setVisibility(View.GONE);
						}
						break;
					case 1:
						this.removeMessages(0);
						//隐藏"结束富集"按钮
						mBtn_end_accumulation.setVisibility(View.GONE);
						timeView.setText("显示计数");
						break;
					default:
						break;
					}
					
				}
			};
			
			Message obtainMessage = mCountdownHandler.obtainMessage(0);
			obtainMessage.arg1=totalTime;
			mCountdownHandler.sendMessage(obtainMessage);
		}
		else
		{
			//先去除上一次的消息
			mCountdownHandler.removeMessages(0);
			//重新发送新的消息,并设置新的倒计时时间
			Message obtainMessage = mCountdownHandler.obtainMessage(0);
			obtainMessage.arg1=totalTime;
			mCountdownHandler.sendMessage(obtainMessage);
		}			
	}

	/**
	 * 显示一个对话框，用于设置输入的时间。并获取时间的总秒数
	 */
	private void showTimeSetDialog(final Handler handler) {	
		final Dialog setTimeDialog = new Dialog(getActivity());
		setTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTimeDialog.setCanceledOnTouchOutside(false);
		setTimeDialog.show();
		//设置对话框的宽度和长度
		LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);		
		View view=View.inflate(mContext, R.layout.dialog_set_accumation_time, null);
		Window window=setTimeDialog.getWindow();		
		window.setGravity(Gravity.CENTER);
		window.setBackgroundDrawableResource(R.drawable.dialog_window_background);		
		setTimeDialog.setContentView(view, layoutParams);
		
		Button bt_dialog_ok=(Button) view.findViewById(R.id.bt_dialog_ok);
		Button bt_dialog_cancel=(Button) view.findViewById(R.id.bt_dialog_cancel);
		final EditText et_dialog_hour=(EditText) view.findViewById(R.id.et_dialog_hour);
		final EditText et_dialog_min=(EditText) view.findViewById(R.id.et_dialog_min);
		final EditText et_dialog_sec=(EditText) view.findViewById(R.id.et_dialog_sec);
		et_dialog_min.addTextChangedListener(new TextWatcher() {						
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				if(!TextUtils.isEmpty(s))
				{
					if(Integer.parseInt(s.toString())>59)
					{
						et_dialog_min.setText("59");
						//设置光标的位置
						et_dialog_min.setSelection(2);
					}
				}				
			}
		});
		et_dialog_sec.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				if(!TextUtils.isEmpty(s))
				{
					if(Integer.parseInt(s.toString())>59)
					{
						et_dialog_sec.setText("59");
						//设置光标的位置
						et_dialog_sec.setSelection(2);
					}
				}		
			}
		});
		
		bt_dialog_ok.setOnClickListener(new OnClickListener() {			
			private int Time_h;
			private int Time_m;
			private int Time_s;
			private int mTotalTime=60;
			@Override
			public void onClick(View v) {
				String ValueOfHou = et_dialog_hour.getText().toString();
				String ValueOfMin  = et_dialog_min.getText().toString();
				String ValueOfSec  = et_dialog_sec.getText().toString();
				if(!TextUtils.isEmpty(ValueOfHou+ValueOfMin+ValueOfSec))
				{
					if(!TextUtils.isEmpty(ValueOfHou))
						Time_h=Integer.parseInt(ValueOfHou);
					else	
						Time_h=0;
					if(!TextUtils.isEmpty(ValueOfMin))
						Time_m=Integer.parseInt(ValueOfMin);
					else	
						Time_m=0;
					if(!TextUtils.isEmpty(ValueOfSec))
						Time_s=Integer.parseInt(ValueOfSec);
					else	
						Time_s=0;
					mTotalTime=Time_h*3600+Time_m*60+Time_s;										
				}
				//将“开始”状态设置为完成状态
				setProcessStartState(2);
				//将“富集”状态设置为正在富集状态
				setProcessAccumlationState(1);
				//发送消息，启动计数器，进行倒计时
				Message message=new Message();
				message.what=0;
				message.arg1=mTotalTime;
				handler.sendMessage(message);
				//关闭对话框
				setTimeDialog.dismiss();
				//显示"结束富集"按钮
				mBtn_end_accumulation.setVisibility(View.VISIBLE);
				//将富集时间通过蓝牙发送给检测设备
				Log.e("story", "准备进入发送函数");
//				MyBluetoothManager.sendBluetoothData(myBluetoothManager, "StartEnrichment!"+mTotalTime+"!");
			}			
		});
		bt_dialog_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//发送消息提示已经取消富集
				handler.sendEmptyMessage(1);
				setTimeDialog.dismiss();
				if(mTv_timeCountdown.getText().toString().contains("富集时间剩余"))
				{
					//将检测按钮设置为不可点击状态
					setButtonState(mBtn_detection,false,R.drawable.bt_home_delection_no_clickable);
				}
				else
				{
					//将检测按钮设置为点击状态
					setButtonState(mBtn_detection,true,R.drawable.bt_home_delection);
				}
			}
		});
	}

	/**设置按钮的状态
	 * @param button 需要设置的按钮控件
	 * @param isClickable 是否允许点击
	 * @param resDrawableId 图片的资源ID，将图片设置在按钮的top。剩下的left，right，bottom均不设图片
	 */
	protected void setButtonState(Button button, boolean isClickable,int resDrawableId) {
		button.setClickable(isClickable);
		button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, resDrawableId, 0, 0);	
	}
	/**
	 * 百度地图初始化定位函数，定位成功则显示定位位置，失败者提示设置网络或者刷新重新定位。
	 */
	private void BaiduMapInit() {
		// TODO Auto-generated method stub
		boolean isInitMap = mBaiduMapManager.InitMapView();
		BaiduMapInfo mapInfo = mBaiduMapManager.getMapInfo();
		if(isInitMap)
		{
			if(mBaiduMapManager.startLocation())
			{
				showLocationText(mapInfo);
			}
		}
		else
		{
			showLocationText(mapInfo);			
		}
	}

	/**显示定位位置，有10秒的定位时间
	 * @param mapInfo 百度地图的信息管理对象，可根据此对象判断是否获取了定位信息
	 */
	@SuppressLint("HandlerLeak")
	private void showLocationText(final BaiduMapInfo mapInfo) 
	{
		if(mMapTimeOut!=10) return;
		mHandler = new Handler(){
			int reSendTimes=0;
			@SuppressLint("HandlerLeak")
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					mMapTimeOut=10;
					mTv_Location.setTextColor(Color.BLUE);
					mTv_Location.getPaint().setUnderlineText(false);
					mTv_Location.getPaint().setTextSkewX(0);
					mTv_Location.setText("定位地址："+mapInfo.getAddress());
					mHandler.removeMessages(msg.what);
					mImgBtn_location_refresh.clearAnimation();
					mImgBtn_location_refresh.setVisibility(View.GONE);
					break;
				case 1:
					mMapTimeOut=10;
					mTv_Location.setTextColor(Color.RED);
					mTv_Location.getPaint().setUnderlineText(true);
					mTv_Location.getPaint().setTextSkewX(-0.5f);
					mTv_Location.setText("定位异常：请检测GPS和网络是否连接正常\n点击跳转到设置？");
					mHandler.removeMessages(msg.what);
					mImgBtn_location_refresh.setVisibility(View.VISIBLE);
					break;
				case 2:
					mTv_Location.setTextColor(Color.BLUE);
					mTv_Location.getPaint().setUnderlineText(false);
					mTv_Location.getPaint().setTextSkewX(0);
					mTv_Location.setText("正在定位，请稍后..."+mMapTimeOut);
					if(!TextUtils.isEmpty(mapInfo.getAddress()))
					{
						Message message=mHandler.obtainMessage(0);
						mHandler.sendMessageDelayed(message, 1000);
					}
					else
					{
						mMapTimeOut--;											    					    
						if(mMapTimeOut<=0)
						{
							Message message=mHandler.obtainMessage(1);
							mHandler.sendMessageDelayed(message, 1000);
						}
						else
						{
							Message message=mHandler.obtainMessage(2);
							mHandler.sendMessageDelayed(message, 1000);
						}					
					}
					break;
				//检测电池电量，临时加的处理函数
					case 3:
						if(mBLEManager.isConnected())
						{
							mHandler.sendEmptyMessageDelayed(3,10000);
							mBLEManager.readBatteryLevel();
						}
						else
						{
							connectBluetooth();
						}
						break;
					case 4:
						Log.e("story","！！！！BLE蓝牙命令失败，再次发送");
						isBLESendCommandSuccess=false;
						if(reSendTimes<5)
						{
							BLECommandSend(mBluetoothCommand,true);
							reSendTimes++;
						}
						else
						{
							reSendTimes=0;
							cancelConnectBluetooth();
						    mHandler.sendEmptyMessageDelayed(6,3000);
						}
						break;
					case 5:
						  BLECommandSend(mBluetoothCommand,true);
						break;
					case 6:
						  connectBluetooth();
						break;
				default:
					break;
				}
			}
		};
		Message message = mHandler.obtainMessage(2);
		mHandler.sendMessageDelayed(message, 1000);
	}
	/**
	 * 获取控件的资源ID
	 */
	private void InitID() {
		mDisplayCurveLayout = (LinearLayout) mHomeView.findViewById(R.id.ll_home_disp);
		mTv_Location = (TextView) mHomeView.findViewById(R.id.tv_home_location);
		mTv_timeCountdown = (TextView) mHomeView.findViewById(R.id.tv_home_countdown);
		mTv_hint = (TextView) mHomeView.findViewById(R.id.tv_home_delection_hint);
		mBtn_showMap = (Button) mHomeView.findViewById(R.id.bt_home_showMap);
		mIv_reconnect_ble = (ImageView) mHomeView.findViewById(R.id.iv_home_pick_bluetooth);
		mBtn_uploadCloud = (Button) mHomeView.findViewById(R.id.bt_home_upload);
		mBtn_accumulation = (Button) mHomeView.findViewById(R.id.bt_home_accumulation);
		mBtn_detection = (Button) mHomeView.findViewById(R.id.bt_home_delection);
		mBtn_showCurve = (Button) mHomeView.findViewById(R.id.bt_home_showline);
		mBtn_end_accumulation = (Button) mHomeView.findViewById(R.id.bt_home_end_accumlation);
		mMapView = (MapView)mHomeView.findViewById(R.id.home_BmapView);
		mImgBtn_location_refresh = (ImageButton) mHomeView.findViewById(R.id.imbt_home_location_refresh);
		mLayout_process_animation = (LinearLayout) mHomeView.findViewById(R.id.ll_accumlation_animation);
		mShow_process_start = (MyViewShowProcess) mHomeView.findViewById(R.id.showprocess_start);
		mShow_process_start.MySetShowText("开始");
		mShow_process_accumulation = (MyViewShowProcess) mHomeView.findViewById(R.id.showprocess_accumlation);
		mShow_process_detection = (MyViewShowProcess) mHomeView.findViewById(R.id.showprocess_delection);
		mShow_process_detection.MySetShowText("检测");
		mShow_process_end = (MyViewShowProcess) mHomeView.findViewById(R.id.showprocess_end);
		mShow_process_end.MySetShowText("结束");
		mIv_start_to_accumulation = (ImageView) mHomeView.findViewById(R.id.iv_showprocess_start_to_accumlation);
		mIv_accumulation_to_detection = (ImageView) mHomeView.findViewById(R.id.iv_showprocess_accumlation_to_delection);
		mIv_detection_to_end = (ImageView) mHomeView.findViewById(R.id.iv_showprocess_delection_to_end);
	}
	/**
	 * 设置process的“开始”的状态
	 * @param state 状态值， 0  表示初始状态
	 *                    2  表示完成状态
	 */
	private void setProcessStartState(int state)
	{
		switch (state) {
		case 0:
			//将字体颜色恢复到初始状态（灰色）
			mShow_process_start.MySetShowTextColor(0xFFBFBFBF);
			//设置进度条状态
			Drawable drawable = mContext.getDrawable(R.drawable.prograss_background_wathet);
			mShow_process_start.MySetProgressBarDrawable(drawable);
			//将连接线恢复成初始状态
			mIv_start_to_accumulation.setImageResource(R.drawable.process_disconnect);
			//将富集按钮设置为可点击状态
			setButtonState(mBtn_accumulation,true,R.drawable.bt_home_accumlation);
			//将检测按钮设置为可点击状态
			setButtonState(mBtn_detection,true,R.drawable.bt_home_delection);
			break;
		case 2:
			//将字体颜色值设置为蓝色，表示已经执行
			mShow_process_start.MySetShowTextColor(Color.BLUE);
			//设置进度条为已经完成状态
			Drawable drawable1 = mContext.getDrawable(R.drawable.prograss_background_blue);
			mShow_process_start.MySetProgressBarDrawable(drawable1);
			//将富集按钮设置为可点击状态
			setButtonState(mBtn_accumulation,true,R.drawable.bt_home_accumlation);
			//将检测按钮设置为可点击状态
			setButtonState(mBtn_detection,true,R.drawable.bt_home_delection);
			break;
		default:
			break;
		}
	}
	/**
	 * 设置process的“结束”的状态
	 * @param state 状态值， 0  表示初始状态
	 *                    2  表示完成状态
	 */
	private void setProcessEndState(int state)
	{
		switch (state) {
		case 0:
			//将字体颜色设置成为初始颜色即灰色
			mShow_process_end.MySetShowTextColor(0xFFBFBFBF);
			//将进度条设置成为初始状态
			Drawable drawable = mContext.getDrawable(R.drawable.prograss_background_wathet);
			mShow_process_end.MySetProgressBarDrawable(drawable);
			break;
		case 2:
			//将检测到结束状态连接线设置成为已连接
			mIv_detection_to_end.setImageResource(R.drawable.process_connected);
			//将字体设置成为蓝色，表示已经完成
			mShow_process_end.MySetShowTextColor(Color.BLUE);
			//将进度条设置成完成状态
			Drawable drawable1 = mContext.getDrawable(R.drawable.prograss_background_blue);
			mShow_process_end.MySetProgressBarDrawable(drawable1);
			//将富集按钮设置为可点击状态
			setButtonState(mBtn_accumulation,true,R.drawable.bt_home_accumlation);
			//将检测按钮设置为可点击状态
			setButtonState(mBtn_detection,true,R.drawable.bt_home_delection);
			break;
		default:
			break;
		}
	}
	/**设置process过程中“富集”的状态
	 * @param state 状态值： 0:初始化状态   
	 * 					  1:正在富集状态  
	 *                    2:富集完成状态  
	 */
	private void setProcessAccumlationState(int state)
	{
		switch (state) {
		case 0:
			//将富集到检测的连接线设置成未连接状态
			mIv_accumulation_to_detection.setImageResource(R.drawable.process_disconnect);
			//将字体颜色设置成初始状态，即灰色
			mShow_process_accumulation.MySetShowTextColor(0xFFBFBFBF);
			//将进度条的设置成为初始状态
			Drawable drawable0 = mContext.getDrawable(R.drawable.prograss_background_wathet);
			mShow_process_accumulation.MySetProgressBarDrawable(drawable0);
			//取消动画
			mLayout_process_animation.setVisibility(View.INVISIBLE);
			//将检测按钮设置为可点击状态
			setButtonState(mBtn_detection,true,R.drawable.bt_home_delection);
			//隐藏"富集结束"按钮
			mBtn_end_accumulation.setVisibility(View.INVISIBLE);
			//将富集提示语设置为“显示计数”
			mTv_timeCountdown.setText("显示计数");
			break;
		case 1:
			//将开始到富集状态的连接线设置成正在连接的状态
			mIv_start_to_accumulation.setImageResource(R.drawable.process_connecting);
			//将字体设置成为蓝色
			mShow_process_accumulation.MySetShowTextColor(Color.BLUE);
			//将进度条设置成为正在运行状态
			Drawable drawable1 = mContext.getDrawable(R.drawable.prograss_background_rotate);
			mShow_process_accumulation.MySetProgressBarDrawable(drawable1);
			//将富集到检测的连接线设置成未连接状态(因为此时检测的状态肯定是初始化未检测状态)
			mIv_accumulation_to_detection.setImageResource(R.drawable.process_disconnect);
			//开启富集动画
			setAccumulationAnimation();
			//将检测按钮设置为不可点击状态
			setButtonState(mBtn_detection,false,R.drawable.bt_home_delection_no_clickable);
			break;
		case 2:
			//将开始到富集状态的连接线设置成为已连接状态
			mIv_start_to_accumulation.setImageResource(R.drawable.process_connected);
			//将字体设置为蓝色，即已经完成状态
			mShow_process_accumulation.MySetShowTextColor(Color.BLUE);
			//将进度条设置为，完成状态
			Drawable drawable2 = mContext.getDrawable(R.drawable.prograss_background_blue);
			mShow_process_accumulation.MySetProgressBarDrawable(drawable2);
			//取消动画
			mLayout_process_animation.setVisibility(View.INVISIBLE);
			//将检测按钮设置为可点击状态
			setButtonState(mBtn_detection,true,R.drawable.bt_home_delection);
			break;

		default:
			break;
		}
	}
	/**设置process过程中的“检测”的状态
	 * @param state 状态值： 0  初始化状态
	 * 					  1  正在富集状态
	 *                    2  富集完成状态   
	 */
	private void setProcessDeletionState(int state)
	{
		switch (state) {
		case 0:
			//将检测到结束的状态连接线设置成未连接状态，因为此时结束状态肯定是初始化状态
			mIv_detection_to_end.setImageResource(R.drawable.process_disconnect);
			//将字体颜色设置成为初始化状态值，即灰色
			mShow_process_detection.MySetShowTextColor(0xFFBFBFBF);
			//将进度条设置成初始化状态
			Drawable drawable0 = mContext.getDrawable(R.drawable.prograss_background_wathet);
			mShow_process_detection.MySetProgressBarDrawable(drawable0);
			//取消动画
			mLayout_process_animation.setVisibility(View.INVISIBLE);
			//将富集按钮设置为可点击状态
			setButtonState(mBtn_accumulation,true,R.drawable.bt_home_accumlation);
			break;
		case 1:
			//将富集到检测状态的连接线设置成为正在连接状态
			mIv_accumulation_to_detection.setImageResource(R.drawable.process_connecting);
			//设置字体颜色为蓝色,表示检测状态
			mShow_process_detection.MySetShowTextColor(Color.BLUE);
			//设置进度条为正在检测状态
			Drawable drawable1 = mContext.getDrawable(R.drawable.prograss_background_rotate);
			mShow_process_detection.MySetProgressBarDrawable(drawable1);
			//开启检测动画
			setDetectionAnimation();
			//将富集按钮设置为不可点击状态
			// TODO: 2017/12/19
			//此处应该为false，测试时改为了true,图标应该为no_clickable，现在改为clickable
			setButtonState(mBtn_accumulation,true,R.drawable.bt_home_accumlation);
			break;
		case 2:
			//将富集到检测状态的连接线设置成为已经连接
			mIv_accumulation_to_detection.setImageResource(R.drawable.process_connected);
			//将字体设置为蓝色
			mShow_process_detection.MySetShowTextColor(Color.BLUE);
			//将进度条设置成为完成状态
			Drawable drawable2 = mContext.getDrawable(R.drawable.prograss_background_blue);
			mShow_process_detection.MySetProgressBarDrawable(drawable2);
			//取消动画
			mLayout_process_animation.setVisibility(View.INVISIBLE);
			//将富集按钮设置为可点击状态
			setButtonState(mBtn_accumulation,true,R.drawable.bt_home_accumlation);
			break;
		default:
			break;
		}
	}
	
	private void Test() {
		for(int i=2;i<10;i++)
		{
//			BluetoothData_up.add((50-i*10)+"#"+df.format(i*10.5));
			mHeartDataList_X.add(i*10.0);
			mHeartDataList_Y.add(i*1.0);
		}
		mAchartEngineManager.updateChart(mHeartDataList_X,mHeartDataList_Y);
	}
	
	@Override
	public void onDestroy() {
		mBLEManager.disConnectDevice();
		mBaiduMapManager.BaiduMapDestroy();
		mContext.unregisterReceiver(mBluetoothReceiver);
		super.onDestroy();
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		mBaiduMapManager.BaiduMapPause();
		super.onPause();
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		mBaiduMapManager.BaiduMapResume();
		super.onResume();
	}
}
