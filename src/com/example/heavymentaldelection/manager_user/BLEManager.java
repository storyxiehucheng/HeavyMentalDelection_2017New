package com.example.heavymentaldelection.manager_user;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.heavymentaldelection.R;
import com.example.heavymentaldelection.my_utils.MyConstantValue;
import com.example.heavymentaldelection.my_utils.MySpUtils;
import com.example.heavymentaldelection.service.BLEService;

import java.util.ArrayList;
import java.util.List;

/**
 * 蓝牙4.0，接收服务程序 蓝牙模块管理函数，蓝牙模块：USR-BLE101
 * Created by story2 on 2017/10/24.
 */

public class BLEManager {
    /**
     * 跳转到打开蓝牙界面后的回调判断标志位
     */
    public static final int REQUEST_ENABLE_BT=1;
    private static final int SCAN_TIME = 15;//设置蓝牙扫描时间，默认为15秒
    private Context mContext;
    private Handler mHandler;//定义一个handler，用来计时蓝牙搜索时间
    private List<BluetoothDevice> mBleDevicesList=new ArrayList<>();//缓存搜索到的蓝牙设备
    private MyListAdapter mListAdapter;//蓝牙列表的ListAdapter
    private boolean mScanning=false;
    private BluetoothAdapter mBluetoothAdapter;//蓝牙适配器
    private boolean BleOpenState=false;
    private BluetoothLeScanner mBleScanner;
    private Activity mActivity;
    private static int mScanTime=1;//蓝牙扫描时间倒计时
    private TextView mTv_dialog_scan_ble;//蓝牙扫描对话框中标题
    private ProgressBar mPb_dialog_scan_ble;//蓝牙扫描对话框中进度条
    private TextView mTv_dialog_scan_ble_refresh;//蓝牙扫描对话框中，重新扫描控件
    /**
     *蓝牙扫描结果回调函数
     */
    private ScanCallback mBLeCallback=new ScanCallback()
        {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice resultDevice = result.getDevice();
            if(resultDevice.getName()==null) return; //去除蓝牙名为null的情况
            if(!mBleDevicesList.contains(resultDevice))
            {
                Log.e("story","BLE蓝牙搜索结果----"+resultDevice.getName()+"  地址:"+resultDevice.getAddress());
                mBleDevicesList.add(resultDevice);
                mListAdapter.notifyDataSetChanged();
            }
        }
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("story","BLE蓝牙搜索失败");
        }
    };

    //初始化蓝牙
    public BLEManager(Context context, Activity activity) {
        mContext=context;
        mActivity=activity;
        mHandler=new Handler();
        initBle();
    }

    /*
    *开启蓝牙连接，如果本地没有存储蓝牙，则先扫描蓝牙设备
    * */
    public boolean startBleDeviceConnect() {
        if(mBleScanner!=null && mBluetoothAdapter!=null)
        {
            //获取本地连接地址
            String bleAddress= MySpUtils.getString(mContext, MyConstantValue.BLUETOOTH_ADDRESS,null);
            String bleName= MySpUtils.getString(mContext, MyConstantValue.BLUETOOTH_NAME,null);
            if(bleAddress!=null && bleName!=null)
            {
                BleDeviceConnect(bleAddress,bleName);
            }
            else
            {
                //先创建一个对话框作为蓝牙的选择
                mBleDevicesList.clear();
                showBLEListDialog();
                scanLeDevice();
            }
            return true;
        }
        else
        {
            return false;
        }

    }

    private void BleDeviceConnect(String bleAddress,String bleName)
    {
        Log.e("story","BLE开始连接蓝牙.......");
        //如果蓝牙已经连接，则先断开，再连接
        if(BLEService.getConnectionState()!=BLEService.STATE_DISCONNECTED)
        {
            disConnectDevice();
        }
       BLEService.connect(bleAddress,bleName,mContext,mBluetoothAdapter);
    }

    //取消连接
    public void disConnectDevice()
    {
        BLEService.disconnect();
    }
    /*
    * 设置蓝牙状态是否可用
    * */
    public void setBleOpen(boolean isOpen)
    {
        BleOpenState=isOpen;
    }
    /*
    *判断手机是否支持BLE功能
    * */
    public boolean isSupportBLE()
    {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }
    /*
    * 判断蓝牙是否打开
    * */
    public Boolean isOpenBLE()
    {
        return mBluetoothAdapter!=null && mBluetoothAdapter.isEnabled();
    }

    //停止蓝牙扫描
    private Runnable stopScanRunnable = new Runnable() {
        @Override
        public void run() {
            if(mScanTime<SCAN_TIME)
            {
                mScanTime++;
                mPb_dialog_scan_ble.setProgress(mScanTime);
                mHandler.postDelayed(stopScanRunnable,1000);
            }
            else
            {
                mScanTime=1;
                mPb_dialog_scan_ble.setVisibility(View.GONE);
                mTv_dialog_scan_ble_refresh.setVisibility(View.VISIBLE);
                mTv_dialog_scan_ble.setText(R.string.scan_ble_device_over);

                if (mBluetoothAdapter != null)
                    mScanning=false;
                mBleScanner.stopScan(mBLeCallback);
                Log.e("story","BLE蓝牙---结束搜索");
            }

        }
    };
    /*
    * 蓝牙扫描，扫描结果在mBleCallback的回调函数中
    * */
    private void scanLeDevice()
    {
        Log.e("story","BLE蓝牙---开始搜索");
        //限制搜索时间
        mHandler.postDelayed(stopScanRunnable,1000);
        mBleScanner.startScan(mBLeCallback);
    }


    /**
     * 初始化蓝牙，获得bleScanner
     */
    public boolean initBle() {
        BluetoothManager mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter= mBluetoothManager.getAdapter();
        mBleScanner=mBluetoothAdapter.getBluetoothLeScanner();
        return mBleScanner!=null;
    }

    public boolean isConnected()
    {
        return BLEService.getConnectionState() != BLEService.STATE_DISCONNECTED;
    }
    /**
     * 显示一个列表对话框，用以选择蓝牙名
     */
    @SuppressLint("InflateParams")
    private void showBLEListDialog()
    {
        LayoutInflater mLayoutInflater=LayoutInflater.from(mContext);
        View view = mLayoutInflater.inflate( R.layout.dialog_list_bluetooth, null);
        ListView listview=(ListView)view.findViewById(R.id.lv_dialog_scan_ble);
        mTv_dialog_scan_ble_refresh=(TextView)view.findViewById(R.id.tv_dialog_scan_ble_refresh);
        mTv_dialog_scan_ble=(TextView)view.findViewById(R.id.tv_dialog_scan_ble_title);
        mPb_dialog_scan_ble=(ProgressBar)view.findViewById(R.id.pb_dialog_scan_ble);
        mPb_dialog_scan_ble.setMax(SCAN_TIME);
        mListAdapter=new MyListAdapter();
        listview.setAdapter(mListAdapter);
        final Dialog alertBluetoothDialog = new Dialog(mActivity);
        alertBluetoothDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window dialogWindow = alertBluetoothDialog.getWindow();
        dialogWindow.setBackgroundDrawableResource(R.drawable.listview_background_250);
        alertBluetoothDialog.show();
        alertBluetoothDialog.setContentView(view,
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String bleAddress = mBleDevicesList.get(position).getAddress();
                String bluetoothName = mBleDevicesList.get(position).getName();
                Log.e("story", "----你选择的是："+bluetoothName);
                Toast.makeText(mContext, "开始连接"+bluetoothName, Toast.LENGTH_LONG).show();
                MySpUtils.putString(mContext, MyConstantValue.BLUETOOTH_ADDRESS, bleAddress);
                MySpUtils.putString(mContext, MyConstantValue.BLUETOOTH_NAME, bluetoothName);
                BleDeviceConnect(bleAddress,bluetoothName);
                mScanTime=SCAN_TIME;
                alertBluetoothDialog.dismiss();
            }
        });
        mTv_dialog_scan_ble_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPb_dialog_scan_ble.setVisibility(View.VISIBLE);
                mTv_dialog_scan_ble_refresh.setVisibility(View.GONE);
                mTv_dialog_scan_ble.setText(R.string.scan_ble_device);
                mPb_dialog_scan_ble.setProgress(0);
                mBleDevicesList.clear();
                mScanTime=1;
                mHandler.postDelayed(stopScanRunnable,1000);
                mBleScanner.startScan(mBLeCallback);
            }
        });
    }

    public void readBatteryLevel()
    {
        BLEService.readBatteryLevel();
    }
    /**
     * @author story
     *自定义的adapter
     */
    private class MyListAdapter extends BaseAdapter
    {
        @Override
        public int getCount() {
            return mBleDevicesList.size();
        }
        @Override
        public Object getItem(int position) {
            return mBleDevicesList.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null)
            {
                convertView= View.inflate(mContext, R.layout.dialog_bluetooth_listview_item, null);
            }
            TextView tv=(TextView) convertView.findViewById(R.id.tv_dialog_bluetooth_listview_item);
            tv.setText(mBleDevicesList.get(position).getName());
            return convertView;
        }
    }
}
