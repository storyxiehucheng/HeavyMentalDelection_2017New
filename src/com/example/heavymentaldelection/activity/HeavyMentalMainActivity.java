package com.example.heavymentaldelection.activity;

import com.baidu.mapapi.SDKInitializer;
import com.example.heavymentaldelection.R;
import com.example.heavymentaldelection.fragment.FragmentHistory;
import com.example.heavymentaldelection.fragment.FragmentHome;
import com.example.heavymentaldelection.manager_user.BLEManager;
import com.example.heavymentaldelection.manager_user.SpeechManager;
import com.example.heavymentaldelection.my_utils.MyConstantValue;
import com.example.heavymentaldelection.global.MyGlobalStaticVar;
import com.example.heavymentaldelection.my_utils.MySpUtils;
import com.example.heavymentaldelection.service.BLEService;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * 主要的Activity，主要是蓝牙和网络的检测以及控制fragment的切换等
 *
 */
public class HeavyMentalMainActivity extends Activity {

	private FragmentManager fragmentManager;//Fragment的管理器
	private FragmentTransaction beginTransaction;//fragment的事务管理，fragment的主要操作在这里面完成
	private ImageView mIv_home_menu;//总体界面右上角菜单按钮
	private TextView mTv_main_warning;//总体界面的温馨提示语句
	private BroadcastReceiver mBluetoothDeviceReceiver; //创建一个蓝牙receiver来监听蓝牙开关状态
	private ImageView mIv_main_battery;//电池电量图标

    private ArrayList<Integer> tempBatteryList=new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//初始化百度地图
		SDKInitializer.initialize(getApplicationContext());
		// TODO: 2017/11/20  IFLYTEK voice prompt initialization operation
		//科大讯飞语音初始化
		SpeechUtility.createUtility(getApplicationContext(), SpeechConstant.APPID +"=5a6d80c4,"+ SpeechConstant.FORCE_LOGIN +"=true");

		//初始化布局文件
	    setContentView(R.layout.activity_heavy_mental_main);

		//初始化UI界面
		InitUI();
		//初始化菜单按钮
		MyMenu();
		//检查必要的配置
		checkNecessaryConfigure();
	}

	/**
	 * 检查应用所需的必要配置，如蓝牙等。
	 */
	private void checkNecessaryConfigure() {
		checkBluetooth();
		checkNetWork();
	}
	/**
	 * 检查蓝牙是否可用
	 */
	private void checkBluetooth() {
		bluetoothReceiverInit();
		//检查设备是否支持BLE蓝牙，如果不支持,则提示用户，无法连接检测设备
		if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
		{
			mTv_main_warning.setText(R.string.no_ble_warning);
		}
		else//如果支持BLE蓝牙，则检查是否打开蓝牙，如果没有这提示用户打开蓝牙
		{
			BluetoothManager bluetoothManager=(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
			if(!mBluetoothAdapter.isEnabled())
			{
				MyGlobalStaticVar.isBleOpen=false;
				mTv_main_warning.setText(R.string.no_open_ble_warning);
			}
			else
			{
				MyGlobalStaticVar.isBleOpen=true;//标记蓝牙已经打开了
			}
		}

	}

	/*
	* 初始化一个蓝牙监听器，用来监听手机蓝牙的打开和关闭操作
	* */
	private void bluetoothReceiverInit() {
		mBluetoothDeviceReceiver=new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action=intent.getAction();
				if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
				{
					int state=intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
					switch(state)
					{
						case BluetoothAdapter.STATE_OFF:
							mTv_main_warning.setVisibility(View.VISIBLE);
							mTv_main_warning.setText(R.string.no_open_ble_warning);
							MyGlobalStaticVar.isBleOpen=false;
							break;
						case BluetoothAdapter.STATE_ON:
							mTv_main_warning.setVisibility(View.INVISIBLE);
							MyGlobalStaticVar.isBleOpen=true;
							break;
					}
				}
				if(MyConstantValue.ACTION_SHOULD_OPEN_BLE.equals(action))
				{
					showRequestDialog();
				}
				if(MyConstantValue.ACTION_BATTERY.equals(action))
				{
					int battery_level=intent.getIntExtra(MyConstantValue.BATTERY_LEVEL,100);
                    tempBatteryList.add(battery_level);
					mTv_main_warning.setVisibility(View.VISIBLE);
					mTv_main_warning.setText("电量为："+battery_level);

					if(battery_level>90)
					{
						mIv_main_battery.setBackground(getDrawable(R.drawable.battery_100_48px));
					}
					else if(battery_level>70 && battery_level<=90)
					{
						mIv_main_battery.setBackground(getDrawable(R.drawable.battery_80_48px));
					}
					else if(battery_level>60 && battery_level<=70)
					{
						mIv_main_battery.setBackground(getDrawable(R.drawable.battery_60_48px));
					}
					else if(battery_level>50 && battery_level<=60)
					{
						mIv_main_battery.setBackground(getDrawable(R.drawable.battery_50_48px));
					}
					else if(battery_level>40 && battery_level<=50)
					{
						mIv_main_battery.setBackground(getDrawable(R.drawable.battery_30_48px));
					}
					else if(battery_level>30 && battery_level<=40)
					{
						mIv_main_battery.setBackground(getDrawable(R.drawable.battery_10_48px));
					}
					else
					{
						mTv_main_warning.setText("检测设备电量过低，请及时充电");
						mIv_main_battery.setBackground(getDrawable(R.drawable.battery_0_alert_48px));
						SpeechManager speechManager = new SpeechManager(getApplicationContext());
						speechManager.setSpeechString("检测设备电量低，请及时充电");
					}
//					Log.e("story","电池数据为："+tempBatteryList.size());
//                    Log.e("story","电池电量为："+tempBatteryList);
				}
			}
		};
		IntentFilter intentFilter=new IntentFilter();
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		intentFilter.addAction(MyConstantValue.ACTION_SHOULD_OPEN_BLE);
		intentFilter.addAction(MyConstantValue.ACTION_BATTERY);
		registerReceiver(mBluetoothDeviceReceiver,intentFilter);
	}

	/**判断网络是否可用
	 *
	 */
	private void checkNetWork()
	{
		ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager.getActiveNetworkInfo()!=null)
		{
			if(connectivityManager.getActiveNetworkInfo().isAvailable())
			{
				MySpUtils.putBoolean(getApplicationContext(), MyConstantValue.NETWORK_AVAILABLE, true);
			}
			else
			{
				MySpUtils.putBoolean(getApplicationContext(), MyConstantValue.NETWORK_AVAILABLE, false);
			}
		}
		else
		{
			MySpUtils.putBoolean(getApplicationContext(), MyConstantValue.NETWORK_AVAILABLE, false);
		}
	}
	/**
	 * 显示一个对话框，提示是否打开蓝牙
	 */
	private void showRequestDialog() {
		final Dialog requestBluetoothDialog = new Dialog(this);
		requestBluetoothDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);//设置对话框无标题
		View view = View.inflate(this,R.layout.dialog_request_bluetooth, null);
		requestBluetoothDialog.show();
		requestBluetoothDialog.setContentView(view, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		Window window = requestBluetoothDialog.getWindow();
		if(window==null) return;
		window.setBackgroundDrawableResource(R.drawable.warning_background);
		window.setGravity(Gravity.CENTER);
		
		Button bt_warning_ok=(Button) view.findViewById(R.id.bt_warning_ok);
		Button bt_warning_cancel=(Button) view.findViewById(R.id.bt_warning_cancel);
		
		bt_warning_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(intent,BLEManager.REQUEST_ENABLE_BT);
				requestBluetoothDialog.dismiss();
			}
		});
		
		bt_warning_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mTv_main_warning.setVisibility(View.VISIBLE);
				mTv_main_warning.setText(R.string.no_open_ble_warning);
				requestBluetoothDialog.dismiss();
			}
		});		
	}

	/**
	 * 退出主程序，并且关闭线程
	 */
	private void MyMenu() {
		mIv_home_menu = (ImageView) findViewById(R.id.iv_home_menu);
		mIv_home_menu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showPopupWindowDialog();//弹出pop窗口，显示菜单
			}
		});
	}

	/**
	 * 显示一个popupWindowDialog框，用于选择是否退出
	 */
	protected void showPopupWindowDialog() {
		// TODO we can add a new menu here
		View view = View.inflate(getApplicationContext(), R.layout.dialog_popupwindow_menu, null);
		PopupWindow popupWindow=new PopupWindow(view,130,120,true);
		popupWindow.showAsDropDown(mIv_home_menu, -20, -5);//位置在主按钮的下面
		//设置进入进出动画
		//进入动画的标签：android:windowEnterAnimation
		//退出动画的标签：android:windowExitAnimation
		popupWindow.setAnimationStyle(R.style.dialog_popupwindow_anim);
		//对popupWindow进行设置后，需要利用update来更新窗口
		popupWindow.update();
		Button btn_dialog_exit= (Button) view.findViewById(R.id.bt_dialog_exit);
		btn_dialog_exit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	/**
	 * 初始化主界面
	 */
	private void InitUI()
	{
		fragmentManager = getFragmentManager();//首先获取fragment的管理器
		final FragmentHome fragmentHome = new FragmentHome();
		final FragmentHistory fragmentHistory = new FragmentHistory();

		mTv_main_warning = (TextView) findViewById(R.id.tv_main_warning);

		mIv_main_battery=(ImageView)findViewById(R.id.iv_main_battery);
		final Button btn_home_home=(Button)findViewById(R.id.bt_home_home);
		final Button btn_home_history=(Button)findViewById(R.id.bt_home_history);

		//将fragment依次添加进入住框架当中
		addFragment(fragmentHistory,MyConstantValue.FRAGMENT_HISTORY_TAG);
		addFragment(fragmentHome,MyConstantValue.FRAGMENT_HOME_TAG);

		btn_home_home.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				//设置图标为彩色，相应的将另一个图标设置成灰色
				setButtonIconPicture(btn_home_home, R.drawable.bt_home_home_colorful);
				setButtonIconPicture(btn_home_history, R.drawable.bt_home_history_black);
				//获得fragmentHome的对象
				hideFragment(MyConstantValue.FRAGMENT_HISTORY_TAG);//将历史界面的fragment隐藏
				showFragment(MyConstantValue.FRAGMENT_HOME_TAG);//显示home界面的fragment
			}
		});
		btn_home_history.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//设置历史数据界面的按钮颜色
				setButtonIconPicture(btn_home_home, R.drawable.bt_home_home_black);
				setButtonIconPicture(btn_home_history, R.drawable.bt_home_history_colorful);

				//隐藏home界面，显示主界面
				hideFragment(MyConstantValue.FRAGMENT_HOME_TAG);
				showFragment(MyConstantValue.FRAGMENT_HISTORY_TAG);
			}
		});
		mTv_main_warning.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mTv_main_warning.getText().toString().contains("点击跳转到蓝牙设置"))
				{
					Intent intent=new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
					startActivity(intent);
				}
			}
		});
	}

	/**根据标识（TAG）显示fragment
	 * @param Tag 要显示的fragment的标识（TAG）
	 */
	protected void showFragment(String Tag) {
		beginTransaction = fragmentManager.beginTransaction();
		Fragment fragment = fragmentManager.findFragmentByTag(Tag);
		beginTransaction.show(fragment);
		beginTransaction.addToBackStack(MyConstantValue.FRAGMENT_TAG);
		beginTransaction.commit();
	}
	/**根据标识隐藏一个fragment
	 * @param Tag 需要隐藏的标识（标识）
	 */
	protected void hideFragment(String Tag) {
		beginTransaction = fragmentManager.beginTransaction();
		Fragment fragment = fragmentManager.findFragmentByTag(Tag);
		beginTransaction.hide(fragment);
		beginTransaction.addToBackStack(MyConstantValue.FRAGMENT_TAG);
		beginTransaction.commit();
	}

	/**添加一个fragment
	 * @param fragment 要添加的fragment的对象
	 * @param tag      添加的标识
	 */
	protected void addFragment(Fragment fragment,String tag) {
		beginTransaction = fragmentManager.beginTransaction();
		beginTransaction.add(R.id.fragment_container,fragment,tag);
		beginTransaction.commit();
	}

	/**设置Button的图片
	 * @param button     需要添加的Button
	 * @param pictureId  图片的资源ID
	 */
	protected void setButtonIconPicture(Button button, int pictureId) {
		button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, pictureId, 0, 0);
	}

	/*
	* 初始化服务
	* */
	private void initService() {
		Intent gattServiceIntent = new Intent(getApplicationContext(),
				BLEService.class);
		startService(gattServiceIntent);
	}
	@Override
	public void onBackPressed() {
		//让程序在后台进行，参数为boolean型， TRUE表示在任何界面都直接在后台运行，如果是FALSE则表示只是在根activity才在后台运行
		moveTaskToBack(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//取消广播注册
		unregisterReceiver(mBluetoothDeviceReceiver);
	}

	@Override
	protected void onStart() {
		super.onStart();
		initService();//初始化BLE蓝牙服务
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==BLEManager.REQUEST_ENABLE_BT)
		{
			if(resultCode==Activity.RESULT_OK)
			{
				MyGlobalStaticVar.isBleOpen=true;
			}
			else
			{
				mTv_main_warning.setVisibility(View.VISIBLE);
				mTv_main_warning.setText(R.string.no_open_ble_warning);
				MyGlobalStaticVar.isBleOpen=false;
			}
		}
	}
}
