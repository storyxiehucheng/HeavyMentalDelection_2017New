package com.example.heavymentaldelection.receiver;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

public class BluetoothReceiver extends BroadcastReceiver {	
	private static final int BLUETOOTH_STATE_OFF = 1;
	private static final int BLUETOOTH_STATE_TURNING_OFF = 2;
	private static final int BLUETOOTH_STATE_ON = 3;
	private static final int BLUETOOTH_STATE_TURNING_ON = 4;
	private static final int BLUETOOTH_STATE_DISCONNECTED = 5;
	private static final int BLUETOOTH_STATE_DISCONNECTING = 6;
	private static final int BLUETOOTH_STATE_CONNECTED = 7;
	private static final int BLUETOOTH_STATE_CONNECTING = 8;
	private Handler mHandler;
	private Context mContext;
	
	public BluetoothReceiver(Context mContext,Handler handler) {
		this.mContext=mContext;
		this.mHandler=handler;
//		initSendBroadcast();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction()==BluetoothAdapter.ACTION_STATE_CHANGED)
		{
			int bluestate=intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
			switch (bluestate) {
			case BluetoothAdapter.STATE_OFF:
				mHandler.sendEmptyMessage(BLUETOOTH_STATE_OFF);
				break;
			case BluetoothAdapter.STATE_TURNING_OFF:
				mHandler.sendEmptyMessage(BLUETOOTH_STATE_TURNING_OFF);
				break;
			case BluetoothAdapter.STATE_ON:
				mHandler.sendEmptyMessage(BLUETOOTH_STATE_ON);
				break;
			case BluetoothAdapter.STATE_TURNING_ON:
				mHandler.sendEmptyMessage(BLUETOOTH_STATE_TURNING_ON);
				break;

			default:
				break;
			}
		}
		if(intent.getAction()==BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
		{
			int bluetoothConnectState=intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, 0);
			switch (bluetoothConnectState) {
			case BluetoothAdapter.STATE_DISCONNECTED:
				mHandler.sendEmptyMessage(BLUETOOTH_STATE_DISCONNECTED);
				break;
			case BluetoothAdapter.STATE_DISCONNECTING:
				mHandler.sendEmptyMessage(BLUETOOTH_STATE_DISCONNECTING);
				break;
			case BluetoothAdapter.STATE_CONNECTED:
				mHandler.sendEmptyMessage(BLUETOOTH_STATE_CONNECTED);
				break;
			case BluetoothAdapter.STATE_CONNECTING:
				mHandler.sendEmptyMessage(BLUETOOTH_STATE_CONNECTING);
				break;

			default:
				break;
			}
		}
	}
	
	@SuppressLint("HandlerLeak")
	private void initSendBroadcast()
	{
		mHandler=new Handler()
		{
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case BLUETOOTH_STATE_OFF:
					sendBroadcast("bluetooth_state_off");
					break;
				case BLUETOOTH_STATE_TURNING_OFF:
					
					break;
				case BLUETOOTH_STATE_ON:
					
					break;
				case BLUETOOTH_STATE_TURNING_ON:
					
					break;
				case BLUETOOTH_STATE_DISCONNECTED:
					
					break;
				case BLUETOOTH_STATE_DISCONNECTING:
					
					break;
				case BLUETOOTH_STATE_CONNECTED:
					
					break;
				case BLUETOOTH_STATE_CONNECTING:
					
					break;

				default:
					break;
				}
			}
		};
	}

	protected void sendBroadcast(String broadcasetAction) {
		
		
	}	
}
