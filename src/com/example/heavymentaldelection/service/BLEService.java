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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.content.ContentValues.TAG;

/**
 * Created by story2 on 2017/10/24.
 */

public class BLEService extends Service {


    public static final String ACTION_HEART_DETECTED_RECEIVE_OK = "action_heart_detected_receive_ok";
    public static StringBuilder stringBuilder=new StringBuilder();
    public static byte[] validateCode=new byte[1024];
    public static int receiveDataCount=0;
    private static int sendVolt=0;
    private static int lastSendVolt=0;
    private static boolean isReceiveOK=false;
    private static boolean isFirstPreProcess=true;
    private static int mReceiveTimes=0;
    private static int step=4;
    private final IBinder mBinder = new LocalBinder();
    /**
     * Flag to check the mBound status
     */
    public boolean mBound;

    static BluetoothGattService mService;

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

    public static final String ACTION_COMMAND_RECEIVE_OK =
            "action_command_receive_ok" ;
    public static final String ACTION_PRE_PROCESSING_RECEIVE_OK=
            "action_pre_processing_receive_ok";

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
                Log.e("story","BLE蓝牙搜索服务成功....");
                broadcastConnectionUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                if(sendVolt!=0)
                {
                    String sendStr="RT!"+(sendVolt+step);
                    Log.e("story","蓝牙连接后重新发送的数据为：----"+(sendVolt+step));
                    writeData(sendStr,true);
                }
            }
        }

        //读数据的方法
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            Log.e("story","BLE蓝牙已经接收到数据,-----");
            if(status==BluetoothGatt.GATT_SUCCESS)
            {
                byte[] bytes = characteristic.getValue();
                String str=new String(bytes);
//                Log.e("story","BLE蓝牙已经接收到数据---"+str);
                UUID charUUID=characteristic.getUuid();
                if(charUUID.equals(UUID.fromString(MyConstantValue.BATTERY_CHARACTERISTIC)))
                {
                    int value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Log.e("story","BLE蓝牙接收到的电池电量为："+value);
                    Intent intent=new Intent(MyConstantValue.ACTION_BATTERY);
                    intent.putExtra(MyConstantValue.BATTERY_LEVEL,value);
                    mContext.sendBroadcast(intent);
                }
//                final Intent intent=new Intent(ACTION_DATA_AVAILABLE);
//                Bundle mBundle=new Bundle();
//                mBundle.putByteArray(MyConstantValue.EXTRA_BYTE_VALUE,characteristic.getValue());
//                intent.putExtras(mBundle);
//                mContext.sendBroadcast(intent);
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

                BluetoothGattCharacteristic readCharacteristic = mService.getCharacteristic(UUID.fromString(MyConstantValue.NOTIFY_DATA_CHARACTERISTIC));
//                Log.e("story","BLE蓝牙获取readCharacteristic是否成功："+(readCharacteristic!=null));
                if(readCharacteristic!=null)
                {
//                    Log.e("story","BLE蓝牙设置完成，准备读取数据");
                    mBluetoothGatt.setCharacteristicNotification(readCharacteristic,true);
                    readCharacteristic(readCharacteristic);
                    readBatteryLevel();
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
            String str=new String(value);
            Log.e("story","BLE蓝牙接收到的str数据为: "+str);
            if(str.contains("U#"))
            {
                stringBuilder.append(str);
//                int length = stringBuilder.length();
//                Log.e("story","stringBuilder的长度"+length);
                String[] strs=str.split("!");
                for(int i=0;i<strs.length;i++)
                {
                    String[] temps=strs[i].split("#");
                    if("U".equals(temps[0]))
                    {
                        try {
                            sendVolt=Integer.parseInt(temps[1]);
                        }catch (Exception e)
                        {
                            Log.e("story","BLE蓝牙电压接收错误："+temps[1]);
                        }
                    }
                }
                //Todo
                if(sendVolt != lastSendVolt)
                {
                    lastSendVolt=sendVolt;
                    mReceiveTimes=0;
                }
                else
                {
                    mReceiveTimes++;
                    if(mReceiveTimes==5)
                    {
                        mReceiveTimes=0;
//                        saveState();
                        disconnect();
                        mConnectionState=STATE_DISCONNECTED;
                        broadcastConnectionUpdate(ACTION_GATT_DISCONNECTED);
                    }
                }
                String sendStr="RT!"+(sendVolt+step);
                Log.e("story","!!!!!!需要发送的数据为："+(sendVolt+step));
                writeData(sendStr,true);
            }
            if(str.contains("E#"))
            {
                isReceiveOK=true;
                writeData("Over",true);
                Log.e("story","数据接收完毕，发送Over");
            }
            if(str.contains("T#"))
            {
                Bundle bundle=new Bundle();
                bundle.putString("BLEData",str);
                Intent intent=new Intent(ACTION_HEART_DETECTED_RECEIVE_OK);
                intent.putExtra("bluetoothData",bundle);
                mContext.sendBroadcast(intent);
            }
            else if(str.contains("Receive_OK"))
            {
                if(isReceiveOK)
                {
                    isReceiveOK=false;
                    Log.e("story","BLE蓝牙数据全部接收完成！！！！");
                    sendVolt=0;
                    lastSendVolt=0;
                    String tempStr=stringBuilder.toString();
                    Bundle bundle=new Bundle();
                    bundle.putString("BLEData",tempStr);
                    Intent intent=new Intent(ACTION_RECEIVED_AVAILABLE);
                    intent.putExtra("bluetoothData",bundle);
                    mContext.sendBroadcast(intent);
                    stringBuilder=new StringBuilder();
                    validateCode=new byte[1024];
                    receiveDataCount=0;
//                    isFirstPreProcess=true;
                }
            }
            else if(str.contains("Command_OK"))
            {
                Intent intent=new Intent(ACTION_COMMAND_RECEIVE_OK);
                mContext.sendBroadcast(intent);
            }
            else if(str.contains("PreProcessing_OK"))
            {
                if(isFirstPreProcess)
                {
                    isFirstPreProcess=false;
                    Intent intent=new Intent(ACTION_PRE_PROCESSING_RECEIVE_OK);
                    mContext.sendBroadcast(intent);
                }
            }
        }
    };

    /**
     * 保存临时数据
     */
    private static void saveState()
    {

    }

    //关闭蓝牙连接
   public static void close()
   {
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
        if (mBluetoothAdapter != null && mBluetoothGatt != null)
        {
            Log.e("story", "BLE蓝牙连接成功，现在正准备搜索服务...");
            mBluetoothGatt.discoverServices();
        }
    }
    /*
    *从一个characteristic中读取数据
     *  */
    public static void readCharacteristic(BluetoothGattCharacteristic characteristic)
    {
//      Log.e("story","BLE蓝牙开始设置读的操作");
        if(mBluetoothAdapter==null || mBluetoothGatt==null) return;
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**蓝牙写数据函数*/
    public static void writeData(String writeData,boolean isRsend)
    {
        if(!isRsend)
        {
            // TODO: 2018/3/9 这个地方需要重新注意！！！！
            isReceiveOK=false;
            sendVolt=0;
            lastSendVolt=0;
            stringBuilder=new StringBuilder();
            validateCode=new byte[1024];
            receiveDataCount=0;
        }
        if(mBluetoothAdapter==null || mBluetoothGatt==null) return;
        String writeDataSend=writeData+"\r\n";
        //利用UUID获取服务
        mService = mBluetoothGatt.getService(UUID.fromString(MyConstantValue.USR_SERVICE));
        if(mService!=null)//判断是否获取成功
        {
            //利用服务中的UUID获取写操作数据特征的UUID
            BluetoothGattCharacteristic gattCharacteristic = mService.getCharacteristic(UUID.fromString(MyConstantValue.WRITE_DATA_CHARACTERISTIC));
            if(gattCharacteristic!=null)
            {
                //设置该特征服务的通知为true
                mBluetoothGatt.setCharacteristicNotification(gattCharacteristic,true);
                try {
                    byte[] writeDataSendBytes = writeDataSend.getBytes("US-ASCII");
                    gattCharacteristic.setValue(writeDataSendBytes);
                    //利用GATT服务器执行写数据操作
                    mBluetoothGatt.writeCharacteristic(gattCharacteristic);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            else Log.e("story","BLE蓝牙获取Characteristic失败");
        }
        else Log.e("story","BLE蓝牙获取service失败");
    }



    /*检测系统电量读取功能*/
    public static void readBatteryLevel()
    {
        if(mBluetoothAdapter==null || mBluetoothGatt==null) return;
        //根据UUID获取电量检测服务
        BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(MyConstantValue.BATTERY_SERVICE));
        if(service!=null)
        {
            //根据服务中的UUID获取电量检测的服务特征
            BluetoothGattCharacteristic batteryCharacteristic = service.getCharacteristic(UUID.fromString(MyConstantValue.BATTERY_CHARACTERISTIC));
            mBluetoothGatt.setCharacteristicNotification(batteryCharacteristic,true);
            //发送电量读取命令
            mBluetoothGatt.readCharacteristic(batteryCharacteristic);
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
    public static boolean connect(final String address,final String deviceName, Context context,BluetoothAdapter bluetoothAdapter)
    {
       if(bluetoothAdapter==null || address==null) return false;
        mContext=context;
        mBluetoothAdapter=bluetoothAdapter;
        BluetoothDevice remoteDevice = mBluetoothAdapter.getRemoteDevice(address);
        mBluetoothGatt=remoteDevice.connectGatt(context,false,mGattCallback);
        Log.e("story","蓝牙调用结果mBluetoothGatt是否为null: "+(mBluetoothGatt==null));
        return mBluetoothGatt != null;
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
        if (mBluetoothAdapter != null && mBluetoothGatt != null)
        {
            mConnectionState=STATE_DISCONNECTED;
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            Log.e("story","退出前，蓝牙关闭成功");
        }
        else
        {
            Log.e("story","退出前，蓝牙关闭失败,mBluetoothAdapter或者mBluetoothGatt为null");
        }
    }
    /**
     * Clears the internal cache and forces a refresh of the services from the
     * remote device.
     */
    public static boolean refreshDeviceCache() {
        if (mBluetoothGatt != null) {
            try {
                BluetoothGatt localBluetoothGatt = mBluetoothGatt;
                Method localMethod = localBluetoothGatt.getClass().getMethod("refresh");
                if (localMethod != null) {
                    return (Boolean) localMethod.invoke(localBluetoothGatt);
                }
            } catch (Exception localException) {
                Log.i("story", "An exception occured while refreshing device");
            }
        }
        return false;
    }
}
