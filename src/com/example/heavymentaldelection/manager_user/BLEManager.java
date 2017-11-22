package com.example.heavymentaldelection.manager_user;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.heavymentaldelection.R;
import com.example.heavymentaldelection.my_utils.MyConstantValue;
import com.example.heavymentaldelection.my_utils.MySpUtils;
import com.example.heavymentaldelection.service.BLEService;

import java.util.ArrayList;
import java.util.List;

/**
 * 蓝牙4.0，接收服务程序 蓝牙模块管理函数，USR-BLE101
 * Created by story2 on 2017/10/24.
 */

public class BLEManager {
    public static final int REQUEST_ENABLE_BT=1;
    private Context mContext;
    private Handler mHandler;
    private List<BluetoothDevice> BleDevicesList;
    private MyListAdapter myListAdapter;
    private BluetoothDevice mUseDevice;
    private boolean mScanning=false;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private boolean BleOpenState=false;
    private BluetoothLeScanner mBleScanner;
    private Activity mActivity;
    private ScanCallback mBLeCallback;
    private static final long SCAN_TIME=20000;
    private BluetoothGatt mBluetoothGatt;

    /**
     * Connection status Constants
     */
    public static final int STATE_DISCONNECTED = 0;
    private final static String ACTION_GATT_DISCONNECTING =
            "com.usr.bluetooth.le.ACTION_GATT_DISCONNECTING";
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_DISCONNECTING = 4;

    private static int mConnectionState = STATE_DISCONNECTED;
    public BLEManager(Context context, Activity activity) {
        mContext=context;
        mActivity=activity;
        mHandler=new Handler();
        mBluetoothManager=(BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter=mBluetoothManager.getAdapter();
    }

    /*
    * 判断蓝牙是否开起，没有开启则提示开启蓝牙
    * */
    public void InitBluetoothLE() {
        BleDevicesList=new ArrayList<>();
        mBleScanner=mBluetoothAdapter.getBluetoothLeScanner();
        mBLeCallback=new ScanCallback()
        {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice resultDevice = result.getDevice();
                if(!BleDevicesList.contains(resultDevice))
                {
                    Log.e("story","BLE蓝牙搜索结果----"+resultDevice.getName()+"  地址:"+resultDevice.getAddress());
                    BleDevicesList.add(resultDevice);
                    myListAdapter.notifyDataSetChanged();
                }

            }
            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.e("story","BLE蓝牙搜索失败");
            }
        };
    }
    /*
    *开启蓝牙连接，如果本地没有存储蓝牙，则先扫描蓝牙设备
    * */
    public void startBleDeviceConnect() {
         String bleAddress= MySpUtils.getString(mContext, MyConstantValue.BLUETOOTH_ADDRESS,null);
         if(bleAddress==null)
         {
             //先创建一个对话框作为蓝牙的选择
             showBLEListDialog();
             scanLeDevice(true);
         }
         else
         {
             BleDeviceConnect(bleAddress);
         }

    }
    private void BleDeviceConnect(String bleAddress)
    {
        Log.e("story","BLE开始连接蓝牙.......");
        if(BLEService.getConnectionState()!=BLEService.STATE_DISCONNECTED)
        {
            disConnectDevice();
        }
        BLEService.connect(bleAddress,"USR-BLE101",mContext,mBluetoothAdapter);
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
            if (mBluetoothAdapter != null)
                mScanning=false;
                mBleScanner.stopScan(mBLeCallback);
                Log.e("story","BLE蓝牙---结束搜索");
        }
    };
    /*
    * 蓝牙扫描，扫描结果在mBleCallback的回调函数中
    * */
    public void scanLeDevice(final boolean isEnable)
    {
        if(isEnable)
        {
            Log.e("story","BLE蓝牙---开始搜索");
            mHandler.postDelayed(stopScanRunnable,SCAN_TIME);
            mScanning=true;
            mBleScanner.startScan(mBLeCallback);
        }
        else
        {
            mScanning=false;
            mBleScanner.stopScan(mBLeCallback);
        }
    }
    public boolean isConnected()
    {
        return BLEService.getConnectionState() != BLEService.STATE_DISCONNECTED;
    }
    /**
     * 显示一个列表对话框，用以选择蓝牙名
     */
    @SuppressLint("InflateParams")
    private void showBLEListDialog() {
        LayoutInflater mLayoutInflater=LayoutInflater.from(mContext);
        View view = mLayoutInflater.inflate( R.layout.dialog_list_bluetooth, null);
        ListView listview=(ListView)view.findViewById(R.id.lv_dialog_bluetooth);
        myListAdapter=new MyListAdapter();
        listview.setAdapter(myListAdapter);
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
                // TODO Auto-generated method stub
                String bleAddress = BleDevicesList.get(position).getAddress();
                String bluetoothName = BleDevicesList.get(position).getName();
                Log.e("story", "----你选择的是："+bluetoothName);
                Toast.makeText(mContext, "开始连接"+bluetoothName, Toast.LENGTH_SHORT).show();
                MySpUtils.putString(mContext, MyConstantValue.BLUETOOTH_ADDRESS, bleAddress);
                MySpUtils.putBoolean(mContext, MyConstantValue.BLUETOOTH_FIRST_INIT, false);
                BleDeviceConnect(bleAddress);
                alertBluetoothDialog.dismiss();
            }
        });
    }

    /**
     * @author story
     *自定义的adapter
     */
    public class MyListAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return BleDevicesList.size();
        }
        @Override
        public Object getItem(int position) {
            return BleDevicesList.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(mContext, R.layout.dialog_bluetooth_listview_item, null);
            TextView tv=(TextView) view.findViewById(R.id.tv_dialog_bluetooth_listview_item);
            tv.setText(BleDevicesList.get(position).getName());
            return view;
        }
    }
}
