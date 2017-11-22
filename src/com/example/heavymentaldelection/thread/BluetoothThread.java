package com.example.heavymentaldelection.thread;

import android.bluetooth.BluetoothDevice;

public class BluetoothThread extends Thread {
	private BluetoothDevice mBluetoothDevice=null;

	public BluetoothThread(BluetoothDevice mBluetoothDevice) {
		super();
		this.mBluetoothDevice = mBluetoothDevice;
	}
	
}
