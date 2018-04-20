package com.example.heavymentaldelection.my_utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import com.example.heavymentaldelection.service.BLEService;

public class MySpUtils {
	private static SharedPreferences sp;
	/**
	 * 储存boolean数据
	 * @param context 上下文环境
	 * @param key	    存储节点的名称
	 * @param value   储存的值
	 */
	public static void putBoolean(Context context,String key,boolean value)
	{
		if(sp==null)
		{
			sp = context.getSharedPreferences("myconfig", Context.MODE_PRIVATE);
		}
		else
		{
			sp.edit().putBoolean(key, value).apply();
		}
	}
	/**读取boolean数据
	 * @param context   上下文环境
	 * @param key 	     节点名称
	 * @param defValue  默认返回值，当查询的节点不存在时，返回的默认值
	 * @return
	 */
	public static boolean getBoolean(Context context,String key,boolean defValue)
	{
		if(sp==null)
		{
			sp = context.getSharedPreferences("myconfig", Context.MODE_PRIVATE);
		}
		return sp.getBoolean(key, defValue);
	}
	/**
	 * 储存String数据
	 * @param context 上下文环境
	 * @param key	    存储节点的名称
	 * @param value   储存的值
	 */
	public static void putString(Context context,String key,String value)
	{
		if(sp==null)
		{
			sp = context.getSharedPreferences("myconfig", Context.MODE_PRIVATE);
		}
		else
		{
			sp.edit().putString(key, value).apply();
		}
	}
	/**读取String数据
	 * @param context  上下文环境
	 * @param key 	   节点名称
	 * @param defValue  默认返回值，当查询的节点不存在时，返回的默认值
	 * @return
	 */
	public static String getString(Context context,String key,String defValue)
	{
		if(sp==null)
		{
			sp = context.getSharedPreferences("myconfig", Context.MODE_PRIVATE);
		}
		return sp.getString(key, defValue);
	}
    
	/**
	 * @param context  上下文环境
	 * @param key      节点名称
	 * @param defValue 默认返回值
	 * @return 返回储存的Int型值，若查询的节点不存在，则返回默认值
	 */
	public static int getInt(Context context,String key, int defValue)
	{
		if(sp==null)
		{
			sp = context.getSharedPreferences("myconfig", Context.MODE_PRIVATE);
		}
		return sp.getInt(key, defValue);
	}
	/**
	 * @param context 上下文环境
	 * @param key     节点名称
	 * @param value   需要储存的Int型值
	 */
	public static void putInt(Context context,String key,int value)
	{
		if(sp==null)
		{
			sp = context.getSharedPreferences("myconfig", Context.MODE_PRIVATE);
		}
		else
		{
			sp.edit().putInt(key, value).apply();
		}
	}

	public static IntentFilter makeGattUpdateIntentFilter()
	{
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED_CAROUSEL);
		intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(BLEService.ACTION_GATT_CHARACTERISTIC_ERROR);
		intentFilter.addAction(BLEService.ACTION_GATT_CHARACTERISTIC_WRITE_SUCCESS);
		intentFilter.addAction(BLEService.ACTION_GATT_DESCRIPTORWRITE_RESULT);
		intentFilter.addAction(BLEService.ACTION_RECEIVED_AVAILABLE);
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		intentFilter.addAction(BLEService.ACTION_COMMAND_RECEIVE_OK);
		intentFilter.addAction(BLEService.ACTION_PRE_PROCESSING_RECEIVE_OK);
		intentFilter.addAction(BLEService.ACTION_HEART_DETECTED_RECEIVE_OK);
		return intentFilter;
	}

	/**
	 * bytes to hex string
	 */
	public static String ByteArraytoHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			String bs = String.format("%02X ", b);
			sb.append(bs);
		}
		return sb.toString();
	}

	public static String byteToASCII(byte[] array) {
		StringBuffer sb = new StringBuffer();
		for (byte byteChar : array) {
			if (byteChar >= 32 && byteChar < 127) {
				sb.append(String.format("%c", byteChar));
			} else {
				sb.append(String.format("%d ", byteChar & 0xFF)); // to convert
				// >127 to
				// positive
				// value
			}
		}
		return sb.toString();
	}
}
