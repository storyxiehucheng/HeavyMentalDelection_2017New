package com.example.heavymentaldelection.manager_user;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import com.example.heavymentaldelection.R;
import com.example.heavymentaldelection.my_utils.MyConstantValue;
import com.example.heavymentaldelection.my_utils.MySpUtils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("ViewHolder")
public class MyBluetoothManager {
	private BluetoothSocket mBluetoothSocket;
	private BluetoothAdapter mBluetoothAdapter;
	private Handler mHandler;
	private static Context mContext;	
	private Activity mActivity;
	private ArrayList<BluetoothDevice> mBluetoothDevicesList=new ArrayList<BluetoothDevice>();
	private BluetoothDevice mBluetoothDevice;
	private boolean isConnected=false;
	private Context mmContext;
	
	public MyBluetoothManager(Context mContext,Activity mActivity) {
		super();
		this.mmContext = mContext;
		this.mActivity=mActivity;
		mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
		setContext();
	}	
	private void setContext()
	{
		mContext=mmContext;
	}
	public boolean isConnected() {
		return isConnected;
	}

	private void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	/**初始化蓝牙连接
	 * 
	 */
	public void BluetoothInit(Handler handler)
	{
		mHandler=handler;	
		boolean isOpenBluetooth=MySpUtils.getBoolean(mContext.getApplicationContext(), MyConstantValue.BLUETOOTH_OPEN, false);
		if(isOpenBluetooth)
		{  
			Log.e("story", "----进入初始化"); 
			boolean isFirstInit = MySpUtils.getBoolean(mContext.getApplicationContext(), MyConstantValue.BLUETOOTH_FIRST_INIT, true);
			if(isFirstInit)
			{   Log.e("story", "----进入第一次初始化");
				Log.e("story", "----进入选择对话框");
				choiceBluetoothDevice();
			}
			else
			{
				if(isExistBluetoothDevice())
				{
					
					if(isConnected()==false)
					{   
						Log.e("story", "----开始连接线程");
						connectBluetoothDevice(mBluetoothDevice);
					}
					else
					{
						Log.e("story", "----线程早就连接了");
						Message obtainMessage = mHandler.obtainMessage(3);
						mHandler.sendMessage(obtainMessage);
					}
					
				}
				else
				{
					Log.e("story", "----重新初始化");
					MySpUtils.putBoolean(mContext, MyConstantValue.BLUETOOTH_FIRST_INIT, true);
					BluetoothInit(mHandler);
				}
			}
		}
		else
		{
			Message obtainMessage = mHandler.obtainMessage(6);
			mHandler.sendMessage(obtainMessage);
		}
	}
	
	/**
	 * 判断储存的蓝牙名，是否还存在系统的蓝牙设备列表中
	 * @return TRUE表示还存在，可以进行连接； FALSE表示不存在，则需要重新连接
	 */
	private boolean isExistBluetoothDevice() {
		String bluetoothName = MySpUtils.getString(mContext, MyConstantValue.BLUETOOTH_NAME, "");
		if(!TextUtils.isEmpty(bluetoothName))
		{
			Set<BluetoothDevice> pairedDevice=mBluetoothAdapter.getBondedDevices();
			if(pairedDevice.size()>0)
			{
				for (BluetoothDevice bluetoothDevice : pairedDevice) {
					if(bluetoothDevice.getName().equals(bluetoothName))
					{
						mBluetoothDevice=bluetoothDevice;
						return true;
					}
				}
			}
			return false;
		}
		return false;
	}

	/**
	 * 弹出对话框，选择需要连接的蓝牙设备。并根据选择的蓝牙设备进行连接,同时储存蓝牙名
	 */
	private void choiceBluetoothDevice() {
		Set<BluetoothDevice> pairedDevice=mBluetoothAdapter.getBondedDevices();
		if(pairedDevice.size()>0)
		{
			mBluetoothDevicesList.clear();
			for (BluetoothDevice bluetoothDevice : pairedDevice) {
				mBluetoothDevicesList.add(bluetoothDevice);
				Log.e("story", "----蓝牙名："+bluetoothDevice.getName());
			}
			//showListDialog();
		}
		else
		{
			Log.e("story", "----没有蓝牙设备");
		}		
	}
	/**
	 * 显示一个列表对话框，用以选择蓝牙名
	 */
	@SuppressLint("InflateParams")
	private void showListDialog() {
		LayoutInflater mlayoutInflater=LayoutInflater.from(mContext);
		View view = mlayoutInflater.inflate( R.layout.dialog_list_bluetooth, null);
		ListView listview=(ListView)view.findViewById(R.id.lv_dialog_scan_ble);
		MyListAdapter myListAdapter=new MyListAdapter();
		listview.setAdapter(myListAdapter);
		final Dialog alertBluetoothDialog = new Dialog(mActivity);
		alertBluetoothDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window dialogWindow = alertBluetoothDialog.getWindow();
		dialogWindow.setBackgroundDrawableResource(R.drawable.listview_background_250);
		alertBluetoothDialog.show();
		alertBluetoothDialog.setContentView(view,
				new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				String bluetoothName = mBluetoothDevicesList.get(position).getName();
				Log.e("story", "----你选择的是："+bluetoothName);
				Toast.makeText(mContext, "开始连接"+bluetoothName, Toast.LENGTH_SHORT).show();
				MySpUtils.putString(mContext, MyConstantValue.BLUETOOTH_NAME, bluetoothName);
				MySpUtils.putBoolean(mContext, MyConstantValue.BLUETOOTH_FIRST_INIT, false);
				connectBluetoothDevice(mBluetoothDevicesList.get(position));
				alertBluetoothDialog.dismiss();
			}
		});
	}
	
	
	/**根据蓝牙设备，连接蓝牙
	 * @param  bluetoothDevice 需要连接的蓝牙设备
	 */
	private void connectBluetoothDevice(BluetoothDevice bluetoothDevice) {
		ConnectThread connectThread = new ConnectThread(bluetoothDevice);
		connectThread.start();
	}
	
	/**
	 * @author story
	 *开启一个多线程用于连接蓝牙设备
	 */
	public class ConnectThread extends Thread {
		private BluetoothDevice mConnectBluetoothDevice;
		public ConnectThread(BluetoothDevice mConnectBluetoothDevice) {
			super();
			this.mConnectBluetoothDevice = mConnectBluetoothDevice;
		}
		@Override
		public void run() {
			BluetoothSocket tmp=null;
			try {
				System.out.println("客户端初始化开始建立");
				Method m=mConnectBluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
				tmp=(BluetoothSocket)m.invoke(mConnectBluetoothDevice, 1);
			} catch (Exception e) {
				Log.e("story", "客户端初始化建立失败----");
				e.printStackTrace();
			}
			Log.e("story", "客户端初始化建立成功");
			if(tmp!=null)
			{
				mBluetoothSocket=tmp;
				mBluetoothAdapter.cancelDiscovery();
				try {
					Log.e("story", "客户端连接状态："+mBluetoothSocket.isConnected());
					if(!mBluetoothSocket.isConnected())
					{
						Log.e("story", "客户端线程准备连接");
						mBluetoothSocket.connect();
						Log.e("story", "客户端线程连接成功");
					}
					else
					{
						Log.e("story", "客户端线程早就连接成功了");
					}
						setConnected(true);
						Message obtainMessage = mHandler.obtainMessage(3);
						mHandler.sendMessage(obtainMessage);
					
				} catch (IOException e) {
					setConnected(false);
					Message obtainMessage = mHandler.obtainMessage(4);
					mHandler.sendMessage(obtainMessage);
					Log.e("story", "客户端线程连接失败----IOException");
					e.printStackTrace(System.out);
					disConnectBluetooth();
				}
			}
			else
			{
				Log.e("story", "客户端初始化建立失败--tmp==null");
			}			
		}		
		
	}
	public void disConnectBluetooth()
	{
		try {
			mBluetoothSocket.close();
			Log.e("story", "客户端线程已经关闭");
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("story", "客户端线程关闭失败");
		}
	}		
	/**
	 * 接收蓝牙数据,蓝牙数据将通过Handler返回
	 * @param handler 用于处理接收数据的Handler
	 * @return 返回TRUE表示开启接收线程成功，返回FALSE表示开启接收线程失败。
	 */
	public boolean receiveBluetoothData(Handler handler)
	{
		if(isConnected())
		{
			Log.e("story", "----开启接收线程");
			ReceiveDataManageThread receiveDataManageThread = new ReceiveDataManageThread(mBluetoothSocket,handler);
			receiveDataManageThread.start();
			return true;
		}
		else
		{
			Message obtainMessage = handler.obtainMessage(4);
			handler.sendMessageDelayed(obtainMessage, 2000);
			return false;
		}
	}
	/**开启一个接收线程,接收蓝牙传输过来的数据
	 * @author story
	 *
	 */
	private class ReceiveDataManageThread extends Thread {		
		private InputStream mInstream;
		private ArrayList<String> bluetoothdataList_Up=new ArrayList<>();
		private ArrayList<String> bluetoothdataList_Down=new ArrayList<>();
		private ReceiveDataManageThread( BluetoothSocket socket,Handler handler) {
			mHandler=handler;
			InputStream tmpIn=null;
			try {
				tmpIn=socket.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(tmpIn!=null)
			{
				mInstream = tmpIn;
			}			
		}
		@Override
		public void run() {						
			boolean flag_R=false;
			boolean error_flag=false;
			boolean Done_flag=false;
			boolean listen_flag=true;
			byte[] myBuffer=new byte[80];
			byte[] MyReceiveBuffer = new byte[80];	
			int mybytes=0;
			int num=0;
			int i=0;
			String Res="";
			String Data_res="";
			while(listen_flag)
			{	
				num=0;
				flag_R=false;
				Done_flag=false;
				Log.e("myBluetooth", "----开始准备读取数据-----");
				while(!Done_flag)
				{
					try {
							Log.i("myBluetooth", "----开始读取数据-----");
							mybytes=mInstream.read(myBuffer);							
//							Log.i("myBluetooth", "读取数据的字节为：-----"+mybytes);
//							Log.i("myBluetooth", "读取的原始数据为：-----"+myBuffer[0]+"~~"+myBuffer[1]+"~~"+myBuffer[2]);
							if((myBuffer[0]=='U')||(flag_R)||(myBuffer[0]=='D')||(myBuffer[0]=='E'))
							{
								flag_R=true;
								for(i=0;i<mybytes;i++)
								{
									if(myBuffer[i]=='!')
									{
										Done_flag=true;
										break;
									}
									MyReceiveBuffer[num+i]=myBuffer[i];							
								}
								num+=i;						
							}
//							if((myBuffer[0]=='P')||(flag_R==true))
//							{
//								flag_R=true;
//								for(i=0;i<mybytes;i++)
//								{
//									if(myBuffer[i]=='!')
//									{
//										Done_flag=true;
//										break;
//									}
//									MyReceiveBuffer[num+i]=myBuffer[i];							
//								}
//								num+=i;		
//							}
						
					} catch (IOException e) {
						e.printStackTrace();
						System.out.println("数据读取出错");
						try {
							mInstream.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						Log.i("myBluetooth", "----读取数据出错-----");
						error_flag=true;
						break;
					}					
				}
//				Res = new String(MyReceiveBuffer,0,num);
//				MyReceiveBuffer = new byte[80];
//				Log.w("myBluetooth", "----接收到的原始数据:"+Res);
				if(error_flag)
				{					
					setConnected(false);
					Message obtainMessage = mHandler.obtainMessage(4);
					mHandler.sendMessage(obtainMessage);
					Log.i("myBluetooth", "蓝牙已经断开连接了，请重试");				    
					break;
				}
				else 
				{
					try {
						Res = new String(MyReceiveBuffer,0,num,"GBK");
						Log.i("myBluetooth", "~~~读取的数据为："+Res);							
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						break;
					}
					String[] CString=new String[3];
					CString=Res.split("#");
					Log.w("myBluetooth", "CString0~~~<"+CString[0]+">~~~");
					Log.w("myBluetooth", "CString1~~~<"+CString[1]+">~~~");
					Log.w("myBluetooth", "CString2~~~<"+CString[2]+">~~~");
					if("U".equals(CString[0]))
					{	
						Log.i("myBluetooth", "进入U区");
						bluetoothdataList_Up.add(CString[1]+"#"+CString[2]);
					}
					if("D".equals(CString[0]))
					{							
						Log.i("myBluetooth", "进入D区");
						bluetoothdataList_Down.add(CString[1]+"#"+CString[2]);
					}
					if("E".equals(CString[0]))
					{
						if(bluetoothdataList_Down.isEmpty())
						{
							bluetoothdataList_Down.add("0#0");
						}
						Log.i("myBluetooth", "进入E区");
						Log.e("story", "bluetoothdataList_Up-----"+bluetoothdataList_Up.size());
						Log.e("story", "bluetoothdataList_Down-----"+bluetoothdataList_Down.size());
						Bundle bundle = new Bundle();
						bundle.putSerializable("bluetoothUp", bluetoothdataList_Up);
						bundle.putSerializable("bluetoothDown", bluetoothdataList_Down);
						Message obtainMessage = mHandler.obtainMessage(1);
						obtainMessage.setData(bundle);
						mHandler.sendMessage(obtainMessage);
						break;
					}						
				}				
			 }			
		  }		
	}
	
	/**通过蓝牙发送数据
	 * @param myBluetoothManager
	 * @param sendContant 需要发送的内容
	 * @return 发送成功返回TRUE，失败则返回FALSE
	 */
	public static boolean sendBluetoothData(MyBluetoothManager myBluetoothManager, String sendContant)
	{
		//蓝牙接收必须接收到\r\n即回车换行符才表示此次接收完毕
		String sendContentBT=sendContant+"\r\n";
		if(myBluetoothManager.isConnected())
		{
			Log.e("story", "进入发送函数，准备进入发送线程");
			SendDataManageThread sendDataManageThread = myBluetoothManager.new SendDataManageThread(myBluetoothManager.mBluetoothSocket, sendContentBT);
			sendDataManageThread.start();
			return true;
		}
		else
		{
			return false;
		}
	}
		
	/**开启一个发送数据的线程,利用蓝牙发送数据
	 * @author story
	 *
	 */
	private class SendDataManageThread extends Thread
	{
		private OutputStream mOutstream=null;
		private String mSendContent;
		public SendDataManageThread(BluetoothSocket blueToothSendSocket,String sendContent) {									
			Log.e("story", "----进入发送线程成功-----");
			mSendContent=sendContent;
			OutputStream tmpOut=null;
			try {
				Log.e("story", "----尝试得到输出流-----");
				tmpOut=blueToothSendSocket.getOutputStream();
				Log.i("story", "----得到输出流成功-----");
			} catch (IOException e) {
				Log.i("story", "----得到输出流失败-----");
				e.printStackTrace();
			}
			if(tmpOut!=null)
			{
				mOutstream=tmpOut;
				Log.i("story", "----mmOutstream得到输出流成功-----");
			}				
		}
		@Override
		public void run() {
			Log.i("story", "----进入发送阶段-----");
			byte[] SendBuffer=new byte[50];
			String s2=mSendContent;
			SendBuffer=s2.getBytes();
			Log.i("story", "----开始发送数据-----");
			try {
				mOutstream.write(SendBuffer);
				mOutstream.flush();
				Log.i("story", "----发送数据成功-----");
			} catch (IOException e) {
				setConnected(false);
				Message obtainMessage = mHandler.obtainMessage(4);
				mHandler.sendMessageDelayed(obtainMessage, 5000);
				Log.i("story", "----发送数据失败-----");
				try {
					mOutstream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
	}

	/**
	 * @author story
	 *自定义的adapter
	 */
	public class MyListAdapter extends BaseAdapter 
	{

		@Override
		public int getCount() {
			return mBluetoothDevicesList.size();
		}
		@Override
		public Object getItem(int position) {
			return mBluetoothDevicesList.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = View.inflate(mContext, R.layout.dialog_bluetooth_listview_item, null);
			TextView tv=(TextView) view.findViewById(R.id.tv_dialog_bluetooth_listview_item);
			tv.setText(mBluetoothDevicesList.get(position).getName());
			return view;
		}
	}

}
