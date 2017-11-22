package com.example.heavymentaldelection.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.util.Log;

import com.example.heavymentaldelection.my_utils.MyConstantValue;
import com.example.heavymentaldelection.my_utils.MySpUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by story2 on 2017/10/24.
 */

public class BLEService extends Service {

    public static ArrayList<String> BluetoothList_UP=new ArrayList<>();
    public static ArrayList<String> BluetoothList_Down=new ArrayList<>();
    public static StringBuilder stringBuilder=new StringBuilder();
    private final IBinder mBinder = new LocalBinder();
    /**
     * Flag to check the mBound status
     */
    public boolean mBound;

    static BluetoothGattService mService;
    /**
     * BlueTooth manager for handling connections
     */
    private BluetoothManager mBluetoothManager;

    /**
     * GATT 状态常量
     */
    public final static String ACTION_GATT_CONNECTED =
            "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_DISCONNECTED_CAROUSEL =
            "ACTION_GATT_DISCONNECTED_CAROUSEL";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "ACTION_DATA_AVAILABLE";
    public final static String ACTION_RECEIVED_AVAILABLE =
            "ACTION_RECEIVED_AVAILABLE";
    public final static String ACTION_GATT_CHARACTERISTIC_ERROR =
            "ACTION_GATT_CHARACTERISTIC_ERROR";

    /**
     * add by usr_ljq
     */
    public final static String ACTION_GATT_DESCRIPTORWRITE_RESULT =
            "com.usr.bluetooth.le.ACTION_GATT_DESCRIPTORWRITE_RESULT";
    public final static String ACTION_GATT_CHARACTERISTIC_WRITE_SUCCESS =
            "com.usr.bluetooth.le.ACTION_GATT_CHARACTERISTIC_SUCCESS";

    /**
     * 连接状态常量
     */
    public static final int STATE_DISCONNECTED = 0;
    private final static String ACTION_GATT_DISCONNECTING =
            "ACTION_GATT_DISCONNECTING";
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_DISCONNECTING = 4;

    /**
     * BluetoothAdapter for handling connections
     * 连接蓝牙都需要，用来管理手机上的蓝牙
     */
    public static BluetoothAdapter mBluetoothAdapter;

    /**
     * a) BluetoothGattServer作为周边来提供数据；BluetoothGattServerCallback返回周边的状态。
     * b) BluetoothGatt作为中央来使用和处理数据；BluetoothGattCallback返回中央的状态和周边提供的数据
     */
    public static BluetoothGatt mBluetoothGatt;
    private static int mConnectionState = STATE_DISCONNECTED;
    /**
     * Device address
     */
    private static String mBluetoothDeviceAddress;
    private static String mBluetoothDeviceName;
    private static Context mContext;
    private final static BluetoothGattCallback mGattCallback=new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            //GATT Server Connected
            if(newState== BluetoothProfile.STATE_CONNECTED)
            {
                Log.e("story","BLE蓝牙已经连接");
                intentAction=ACTION_GATT_CONNECTED;
                mConnectionState=STATE_CONNECTED;
                broadcastConnectionUpdate(intentAction);
            }
            //GATT Server DisConnected
            else if(newState==BluetoothProfile.STATE_DISCONNECTED)
            {
                Log.e("story","BLE蓝牙已经断开");
                intentAction=ACTION_GATT_DISCONNECTED;
                mConnectionState=STATE_DISCONNECTED;
                mBluetoothGatt.close();
                broadcastConnectionUpdate(intentAction);
            }
        }

        //发现服务的回调方法
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status==BluetoothGatt.GATT_SUCCESS)
            {
                broadcastConnectionUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }

        }

        //读数据的方法
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e("story","BLE蓝牙已经接收到数据,-----");
            if(status==BluetoothGatt.GATT_SUCCESS)
            {
                byte[] bytes = characteristic.getValue();
                String str=new String(bytes);
                Log.e("story","BLE蓝牙已经接收到数据---"+str);
                UUID charUUID=characteristic.getUuid();
                final Intent intent=new Intent(ACTION_DATA_AVAILABLE);
                Bundle mBundle=new Bundle();
                mBundle.putByteArray(MyConstantValue.EXTRA_BYTE_VALUE,characteristic.getValue());
                intent.putExtras(mBundle);
                mContext.sendBroadcast(intent);
            }
            else
            {
                Log.e("story","BLE蓝牙已经接收到数据,但数据接收失败-----");
            }

        }

        //写数据的方法
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(status==BluetoothGatt.GATT_SUCCESS)
            {
                Log.e("story","BLE蓝牙数据发送成功");
                Intent intent=new Intent(ACTION_GATT_CHARACTERISTIC_WRITE_SUCCESS);
                mContext.sendBroadcast(intent);

                BluetoothGattCharacteristic readCharacteristic = mService.getCharacteristic(UUID.fromString("0003cdd1-0000-1000-8000-00805f9b0131"));
                Log.e("story","BLE蓝牙获取readCharacteristic是否成功："+(readCharacteristic!=null));
                if(readCharacteristic!=null)
                {
                    Log.e("story","BLE蓝牙设置完成，准备读取数据");
                    mBluetoothGatt.setCharacteristicNotification(readCharacteristic,true);
                    readCharacteristic(readCharacteristic);
                }
                else
                {
                    Log.e("story","BLE蓝牙设置完成，读取数据失败");
                }

            }
            else
            {
                Log.e("story","BLE蓝牙数据发送失败");
                Intent intent=new Intent(ACTION_GATT_CHARACTERISTIC_WRITE_SUCCESS);
                intent.putExtra(MyConstantValue.EXTRA_CHARACTERISTIC_ERROR_MESSAGE,"错误代码为："+status);
                mContext.sendBroadcast(intent);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] value = characteristic.getValue();
//            String byteToASCII = MySpUtils.byteToASCII(value);
            String str=new String(value);
            if(!str.contains("E"))
            {
                stringBuilder.append(str);
            }
            else
            {
                String tempStr=stringBuilder.toString();
                Bundle bundle=new Bundle();
                bundle.putString("BLEData",tempStr);
                Intent intent=new Intent(ACTION_RECEIVED_AVAILABLE);
                intent.putExtra("bluetoothData",bundle);
                mContext.sendBroadcast(intent);
                stringBuilder=new StringBuilder();
                System.out.println();
            }

//            String[] strings = str.split("#");
//            if(strings[0].equals("U"))
//            {
//                BluetoothList_UP.add(strings[1]+"#"+strings[2]);
//            }
//            else if(strings[0].equals("D"))
//            {
//                BluetoothList_Down.add(strings[1]+"#"+strings[2]);
//            }
//            else if(strings[0].equals("E"))
//            {
//                Log.e("story","进入E区");
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("bluetoothUp", BluetoothList_UP);
//                bundle.putSerializable("bluetoothDown", BluetoothList_Down);
//                Intent intent=new Intent(ACTION_RECEIVED_AVAILABLE);
//                intent.putExtra("bluetoothData",bundle);
//                Log.e("story","发送广播");
//                mContext.sendBroadcast(intent);
//                BluetoothList_UP.clear();
//                BluetoothList_Down.clear();
//                Log.e("story","退出E区");
//            }
              Log.e("story","BLE蓝牙接收到的str数据为: "+str);
        }
    };

    //关闭蓝牙连接
   public static void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /*
    * 发送蓝牙连接的广播
    * */
    private static void broadcastConnectionUpdate(String intentAction) {
        Intent intent=new Intent(intentAction);
        mContext.sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        mBound=true;
        return mBinder;
    }

    public static void discoverService() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            return;
        } else {
            Log.e("story","BLE蓝牙搜索成功，现在正准备搜索服务...");
            boolean result = mBluetoothGatt.discoverServices();
            if(result) Log.e("story","BLE蓝牙,远程设备服务搜索成功");
            else Log.e("story","BLE蓝牙，远程设备搜索失败");
        }
    }
    /*
    *从一个characteristic中读取数据
     *  */
    public static void readCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        Log.e("story","BLE蓝牙开始设置读的操作");
        if(mBluetoothAdapter==null || mBluetoothGatt==null) return;
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public static void writeData(String writeData) {
        if(mBluetoothAdapter==null || mBluetoothGatt==null) return;
        String writeDataSend=writeData+"\r\n";
        mService = mBluetoothGatt.getService(UUID.fromString("0003cdd0-0000-1000-8000-00805f9b0131"));
        Log.e("story","BLE蓝牙获取service是否成功："+(mService!=null));
        if(mService!=null)
        {
            List<BluetoothGattCharacteristic> characteristics = mService.getCharacteristics();
            int nums=characteristics.size();
            Log.e("story","获取到的characteristic的数目为"+nums);
            BluetoothGattCharacteristic gattCharacteristic = mService.getCharacteristic(UUID.fromString("0003cdd2-0000-1000-8000-00805f9b0131"));
            Log.e("story","BLE蓝牙获取gattCharacteristic是否成功："+(gattCharacteristic!=null));
            if(gattCharacteristic!=null)
            {
                Log.e("story","BLE蓝牙设置完成，准备写入命令");
                mBluetoothGatt.setCharacteristicNotification(gattCharacteristic,true);
                writeCharacteristicGatt(gattCharacteristic,writeDataSend.getBytes());
            }
            else
            {
                Log.e("story","BLE蓝牙获取Characteristic失败");
            }
        }
        else
        {
            Log.e("story","BLE蓝牙获取service失败");
        }
    }

    /**
     * Local binder class
     */
    private class LocalBinder extends Binder {
        public BLEService getService() {
            return BLEService.this;
        }
    }
    /*
    * 蓝牙连接函数的主入口
    * */
    public static void connect(final String address,final String deviceName, Context context,BluetoothAdapter bluetoothAdapter)
    {
       if(bluetoothAdapter==null || address==null) return ;
        mContext=context;
        mBluetoothAdapter=bluetoothAdapter;
        BluetoothDevice remoteDevice = mBluetoothAdapter.getRemoteDevice(address);
        if(remoteDevice==null) return;
        mBluetoothGatt=remoteDevice.connectGatt(context,false,mGattCallback);
        mBluetoothDeviceAddress=address;
        mBluetoothDeviceName=deviceName;
        mConnectionState=STATE_CONNECTED;
    }

    //BLE蓝牙写数据
    public static void writeCharacteristicGatt(BluetoothGattCharacteristic characteristic, byte[] byteArray)
    {
        if(mBluetoothAdapter==null || mBluetoothGatt==null) return ;
        characteristic.setValue(byteArray);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    //设置单次发送数据的长度
    public static boolean requestMtu(int mtu) {
        return mBluetoothGatt != null && mBluetoothGatt.requestMtu(mtu);
    }

    //返回连接状态
    public static int getConnectionState() {
        return mConnectionState;
    }

    /**
     * 断开连接
     */
    public static void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mConnectionState=STATE_DISCONNECTED;
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
    }
}
