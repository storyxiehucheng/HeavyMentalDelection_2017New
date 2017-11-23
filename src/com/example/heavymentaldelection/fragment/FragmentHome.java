package com.example.heavymentaldelection.fragment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.TreeMap;
import org.achartengine.GraphicalView;
import com.baidu.mapapi.map.MapView;
import com.example.heavymentaldelection.R;
import com.example.heavymentaldelection.Info.BaiduMapInfo;
import com.example.heavymentaldelection.manager_user.AchartEngineManager;
import com.example.heavymentaldelection.manager_user.BLEManager;
import com.example.heavymentaldelection.manager_user.BaiduMapManager;
import com.example.heavymentaldelection.manager_user.SpeechManager;
import com.example.heavymentaldelection.my_utils.MyAnimationUtils;
import com.example.heavymentaldelection.my_utils.MyConstantValue;
import com.example.heavymentaldelection.my_utils.MyDataAnalysis;
import com.example.heavymentaldelection.my_utils.MyGlobalStaticVar;
import com.example.heavymentaldelection.my_utils.MySortFilterArrayList;
import com.example.heavymentaldelection.my_utils.MySpUtils;
import com.example.heavymentaldelection.service.BLEService;
import com.example.heavymentaldelection.view.MyViewShowProcess;
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
import android.view.Gravity;
import android.view.LayoutInflater;
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
		Test();
    }
	private void startMainTask() {
		BaiduMapInit();
		mBLEManager=new BLEManager(this.getActivity().getApplicationContext(),this.getActivity());
		startBluetoothBroadcastReceiver();
		speechManager = new SpeechManager(mContext);
		connectBluetooth();
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
				if(BLEService.ACTION_GATT_CONNECTED.equals(action))
				{
					Log.e("story","BLE ACTION_GATT_CONNECTED----");
					BLEService.discoverService();
				}
				if(BLEService.ACTION_GATT_DISCONNECTED.equals(action))
				{
					Log.e("story","BLE ACTION_GATT_DISCONNECTED");
					mTv_hint.setText("蓝牙已经断开，正在重新连接...");
					mIv_reconnect_ble.setVisibility(View.VISIBLE);
					mTv_hint.setTextColor(Color.RED);
					mTv_hint.setText("与检测设备连接失败，请检查后重新连接");
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
				}
				if(BLEService.ACTION_RECEIVED_AVAILABLE.equals(action))
				{
					Log.e("story","准备接收蓝牙数据ACTION_RECEIVED_AVAILABLE");
					Bundle bundle=intent.getBundleExtra("bluetoothData");
					try {
						String tempStr=bundle.getString("BLEData");
//						Log.e("story","广播接收到的蓝牙数据为："+tempStr);
						receiveBLEDataSuccess(tempStr);
					}
					catch (Exception e)
					{
						Log.e("story","！！！！！蓝牙数据接收错误");
						mTv_hint.setText("蓝牙数据接收错误");
						mBLEManager.disConnectDevice();
					}
				}
				if(BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
				{
					BLEService.requestMtu(512);
					Log.e("story","------BLE蓝牙服务连接成功");
					mTv_hint.setTextColor(Color.BLUE);
					mTv_hint.setText("蓝牙连接成功");
				}
			}
		};
		mContext.registerReceiver(mBluetoothReceiver,MySpUtils.makeGattUpdateIntentFilter());
	}

	/*
	* 接收成功后的，蓝牙数据处理
	* */
	private void receiveBLEDataSuccess(String BLEData) {
		//解析接收的蓝牙数据
		String[] bleStrings=BLEData.split("!");
		BluetoothData_up.clear();
		BluetoothData_down.clear();
		for (String bleString : bleStrings)
		{
			String[] strings = bleString.split("#");
			{
				if (strings[0].equals("U"))
				{
					BluetoothData_up.add(strings[1] + "#" + strings[2]);
				}
				else if (strings[0].equals("D"))
				{
					BluetoothData_down.add(strings[1] + "#" + strings[2]);
				}
			}
		}

		//设置语音提醒
		speechManager.setSpeechString("检测结束");
		//设置结束状态为完成状态
		setProcessEndState(2);
		//设置检测状态为完成状态
		setProcessDeletionState(2);
//		//将检测结果存入本地文件
//		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy年MM月dd日-HH时mm分ss秒", Locale.getDefault());
//		String date=dateFormat.format(new Date());
//		DataSaveToFile(date+"-5ug-1"+"-"+"#"+detelectRepateTimes);
		//数据处理，得出结果
		bluetoothDataProcess(BluetoothData_up,BluetoothData_down);
//		if(detelectRepateTimes<3)
//		{
//			++detelectRepateTimes;
//			delectionProcess(BluetoothCommand);
//		}
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

				String writeData="StartCV";

				BLEService.writeData(writeData);
//				if(myBluetoothManager.isConnected())
//				{
//					accumlationProcess();
//				}
//				else
//				{
//					connectBluetooth();
//				}
//				delectionProcess("SWV");
			}
		});
		//检测操作按钮
		mBtn_detection.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showScanningMethodDialog();//选择电化学检测方法,有CV，DPV，SWV
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
//				Log.e("story", "语音准备");
//				speechManager.setSpeechString("开始检测");
			    if(BluetoothData_up==null) return;
				Log.e("story", "准备存储数据");
				String path=Environment.getExternalStorageDirectory().getAbsoluteFile()+File.separator+"StoryData.txt";
				FileWriter fw=null;
				BufferedWriter bufferedWriter=null;
				try {
					fw = new FileWriter(path);
					bufferedWriter = new BufferedWriter(fw);
					bufferedWriter.write("~~~~up Data");
					bufferedWriter.newLine();
					for (String double1 : BluetoothData_up) {
						String Cstring=double1.replaceAll("#", "\t");
						bufferedWriter.write(Cstring);
						bufferedWriter.newLine();
					}
					bufferedWriter.write("~~~~~Down Data");
					bufferedWriter.newLine();
					for (String double1 : BluetoothData_down) {
						String replaceAll = double1.replaceAll("#", "\t");
						bufferedWriter.write(replaceAll);
						bufferedWriter.newLine();
					}
					bufferedWriter.write("~~~~All data");
					bufferedWriter.flush();
					Log.e("story", "数据已经储存完毕");
				} catch (IOException e) {
					e.printStackTrace();
				} finally
				{
					if(fw!=null)
					{
						try {
							fw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if(bufferedWriter!=null)
					{
						try {
							bufferedWriter.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
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

	private void DataSaveToFile(String name)
	{
		if(BluetoothData_up==null) return;
		Log.e("story", "准备存储数据");
		String path=Environment.getExternalStorageDirectory().getAbsoluteFile()+File.separator+name+".txt";
		FileWriter fw=null;
		BufferedWriter bufferedWriter=null;
		try {
			fw = new FileWriter(path);
			bufferedWriter = new BufferedWriter(fw);
//			bufferedWriter.write("~~~~up Data");
//			bufferedWriter.newLine();
			for (String double1 : BluetoothData_up) {
				String Cstring=double1.replaceAll("#", "\t");
				bufferedWriter.write(Cstring);
				bufferedWriter.newLine();
			}
//			bufferedWriter.write("~~~~~Down Data");
			bufferedWriter.newLine();
			for (String double1 : BluetoothData_down) {
				String replaceAll = double1.replaceAll("#", "\t");
				bufferedWriter.write(replaceAll);
				bufferedWriter.newLine();
			}
////			bufferedWriter.write("~~~~All data");
			bufferedWriter.flush();
			Log.e("story", "数据已经储存完毕");
		} catch (IOException e) {
			e.printStackTrace();
		} finally
		{
			if(fw!=null)
			{
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(bufferedWriter!=null)
			{
				try {
					bufferedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

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
		Button cvButton=(Button)view.findViewById(R.id.bt_CV);
		Button dpvButton=(Button)view.findViewById(R.id.bt_DPV);
		swvButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
				showSWVParameterSet();
			}
		});
		cvButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
				mBluetoothCommand="StartCV";
				delectionProcess(mBluetoothCommand);
			}
		});
		dpvButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
				mBluetoothCommand="StartDPV";
				delectionProcess(mBluetoothCommand);
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
		et_dialog_InitV.setHint("-200");
		final EditText et_dialog_EndV=(EditText)view.findViewById(R.id.et_dialog_EndV);
		et_dialog_EndV.setHint("600");
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
					InitV="-200";
				}
				if(EndV.isEmpty())
				{
//					et_dialog_EndV.setError("结束值不能设置为空");
					EndV="600";
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
					mBluetoothCommand="SWV"+InitV+"$"+EndV+"!"+Frequency+"&"+Amplitude;
					mDetectionRepeatTimes=1;
					delectionProcess(mBluetoothCommand);
				}
			}
		});
	}

	/**
	 * 检测操作的所有任务
	 */
	protected void delectionProcess(String bluetoothCommand) {
		if(mBLEManager.isConnected())
		{
			//设置文字提示"正在检测,请稍后"
			mTv_timeCountdown.setText("正在进行第"+mDetectionRepeatTimes+"检测，请稍后...");
			//将检测状态设置为正在检测
			setProcessDeletionState(1);
			//发送数据，提示检测设备可以开始检测
			BLEService.writeData(bluetoothCommand);
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
	private void setAccumlationAnimation()
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
		mBLEManager.disConnectDevice();
	}
	/**
	 * 连接蓝牙，并在蓝牙重新开启的时候再次通过此函数连接蓝牙
	 */
	private void connectBluetooth() {

		//首先判断蓝牙是否打开,如果打开了，则进行连接
		if(MyGlobalStaticVar.isBleOpen)
		{
			mTv_hint.setTextColor(0xFF9B30FF);
			mIv_reconnect_ble.setVisibility(View.INVISIBLE);
			mTv_hint.setText("正在连接蓝牙...请稍后");
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
		mTv_hint.setTextColor(Color.RED);
		mIv_reconnect_ble.setVisibility(View.VISIBLE);
		mTv_hint.setText("蓝牙设备已经关闭，请打开后重试");
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
		ArrayList<Double> bluetoothData_down_Volt=new ArrayList<>();
		ArrayList<Double> bluetoothData_down_Cur=new ArrayList<>();

		//定义一个treeMap来保留按电压排序后的电压-电流对
		TreeMap<Integer,Double> voltCurMap=new TreeMap<>();
		//设置显示格式
		DecimalFormat df=new DecimalFormat("#.0000");//保留4位小数
		//按电压的大小排序
		Log.e("story", "进入图形处理函数");
		if(!bluetoothData_up.isEmpty())
		{
			Log.e("story", "开始排序");
			ArrayList<String> bluetoothData_upSortVolt=MySortFilterArrayList.sortString(bluetoothData_up,true);
			ArrayList<String> bluetoothData_upSortCur=MySortFilterArrayList.sortString(bluetoothData_up,false);
			//遍历集合取出电压，电流的值
			for (String str : bluetoothData_upSortVolt) {
				String[] strVolt=str.split("#");
				tempVolt=df.format(Double.parseDouble(strVolt[0]));
				bluetoothData_up_Volt.add(Double.valueOf(tempVolt));
				tempCur=df.format(Double.valueOf(strVolt[1]));
				bluetoothData_up_Cur.add(Double.parseDouble(tempCur));

				//将电流电压键值对，放入treeMap当中
				voltCurMap.put(Integer.valueOf(strVolt[0]),Double.parseDouble(tempCur));
			}
			ArrayList<Integer> maxPointList = MyDataAnalysis.findExtremeMaxPoint(voltCurMap);//寻找到极大值点,返回对应的电压值集合
			if(!maxPointList.isEmpty())
			{
				for(int i=0;i<maxPointList.size();i++)
				{
					mTv_hint.append("\n"+"UpVolt:"+maxPointList.get(i)+"MaxCur:"+voltCurMap.get(maxPointList.get(i)));
				}
			}
			else
			{
				mTv_hint.append("\n"+"没有极值点");
			}
			//求出电流最大值以及对应的电压值
//			String tempString="";
//			String[] tempCString;
//			if(!bluetoothData_upSortCur.isEmpty())
//			{
//				tempString=bluetoothData_upSortCur.get(bluetoothData_upSortCur.size()-1);
//				tempCString=tempString.split("#");
//				tv_home_hint.append("\n"+"UpMaxCur:"+tempCString[1]+"  UpVolt:"+tempCString[0]);
//			}
		}
		if(!bluetoothData_down.isEmpty())
		{
			ArrayList<String> bluetoothData_downSortVolt=MySortFilterArrayList.sortString(bluetoothData_down,true);
			ArrayList<String> bluetoothData_downSortCur=MySortFilterArrayList.sortString(bluetoothData_down,false);
			for (String str : bluetoothData_downSortVolt) {
				String[] strVolt=str.split("#");
				tempVolt=strVolt[0];
				tempVolt=df.format(Double.parseDouble(tempVolt));
				bluetoothData_down_Volt.add(Double.valueOf(tempVolt));
				tempCur=strVolt[1];
				tempCur=df.format(Double.valueOf(tempCur));
				bluetoothData_down_Cur.add(Double.parseDouble(tempCur));
			}
//			for (Double double1 : bluetoothData_down_Volt) {
////				Log.e("story", "downVolt: "+double1);
//			}
////			Log.e("story", "bluetoothData_down_Volt: "+bluetoothData_down_Volt.size());
//			for (Double double1 : bluetoothData_down_Cur) {
////				Log.e("story", "DownCur: "+double1);
//			}
//			Log.e("story", "bluetoothData_down_Cur: "+bluetoothData_down_Cur.size());
			if(!bluetoothData_down_Cur.isEmpty())
			{
				String tempString=bluetoothData_downSortCur.get(0);
				String[] tempCString=tempString.split("#");
				String str=mTv_hint.getText().toString();
				mTv_hint.setText(str+"\nDownMaxCur:"+tempCString[1]+"  DownVlot:"+tempCString[0]);
			}
		}
		//显示曲线
		showChart(bluetoothData_up_Cur, bluetoothData_up_Volt, bluetoothData_down_Cur, bluetoothData_down_Volt);
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
						accumlationTimeCountDown(mTv_timeCountdown,msg.arg1);
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
	 * @param timeview 需要显示倒计时的view控件
	 */
	protected void accumlationTimeCountDown(final TextView timeview,int totalTime) 
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
							timeview.setTextColor(0xff6A5ACD);
							timeview.setText("富集时间剩余："+h+"时"+m+"分"+s+"秒");
							Message message = this.obtainMessage(0);
							message.arg1=timedata;
							this.sendMessageDelayed(message, 1000);
							
						}
						else
						{
							this.removeMessages(0);
							timeview.setTextColor(0xFF8A2BE2);
							timeview.setText("富集时间结束，请开始检测");
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
						timeview.setText("显示计数");
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
			@SuppressLint("HandlerLeak")
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
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
			setAccumlationAnimation();
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
			setButtonState(mBtn_accumulation,false,R.drawable.bt_home_accumlation_no_clickable);
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
		ArrayList<String> BluetoothData_up = new ArrayList<>();
		ArrayList<String> BluetoothData_down = new ArrayList<>();
		DecimalFormat df=new DecimalFormat("#.000");
		for(int i=0;i<10;i++)
		{
			BluetoothData_up.add((50-i*10)+"#"+df.format(i*10.5));
		}
		for(int i=0;i<20;i++)
		{
			BluetoothData_down.add((i*10)+"#"+df.format((i*i*0.5)/3));
		}
		bluetoothDataProcess(BluetoothData_up,BluetoothData_down);
		
	}
	/**将传进来的数据在图形上显示曲线,最多显示两条曲线
	 * @param dataOwn 第一条曲线     如果为空，则不显示
	 * @param dataTwo 第二条曲线     如果为空，则不显示
	 */
	private void showChart(ArrayList<Double> dataOwn,ArrayList<Double> x_Own ,ArrayList<Double> dataTwo,ArrayList<Double> x_Two)
	{
		mDisplayCurveLayout.removeAllViews();
		AchartEngineManager achartEngineManager = new AchartEngineManager(getActivity().getApplicationContext());
		achartEngineManager.setChartTitle("测试图形", "电压(mv)", "电流(uA)");
		GraphicalView graphicalView = achartEngineManager.DataSet(dataOwn,x_Own, "Up曲线",dataTwo,x_Two,"Down曲线");
		if(graphicalView!=null)
		{
			mDisplayCurveLayout.addView(graphicalView);
		}

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
