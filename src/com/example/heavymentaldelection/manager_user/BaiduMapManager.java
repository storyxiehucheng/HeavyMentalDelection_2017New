package com.example.heavymentaldelection.manager_user;

import java.util.ArrayList;
import java.util.List;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.model.LatLng;
import com.example.heavymentaldelection.R;
import com.example.heavymentaldelection.Info.BaiduMapInfo;
import com.example.heavymentaldelection.Info.HMDataBaseInfo;
import com.example.heavymentaldelection.my_utils.MyConstantValue;
import com.example.heavymentaldelection.my_utils.MySpUtils;
import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author story
 *百度地图的管理
 */
public class BaiduMapManager {
	private MapView mMapView;
	private Context mContext;
	private BaiduMap mBaiduMap;
	private boolean  isInitSucceed=false;
	private LocationClient locationClient;
	private MbdLocationListener mbdLocationListener;
	private BaiduMapInfo mBaiduMapInfo=new BaiduMapInfo();
	private BitmapDescriptor mCurrentMarker;
	private boolean isFirstIn=true;
	private HMDataBaseInfo mHMDataBaseInfo;
	private ArrayList<Overlay> OverLaylist=new ArrayList<Overlay>();
	public BaiduMapManager(MapView mapView, Context mContext) {
		this.mMapView = mapView;
		this.mContext = mContext;
		mBaiduMap=mMapView.getMap();
	}
	/**初始化百度地图
	 * @return 初始化成功则返回TRUE，失败（例如没有网络或者GPS）则返回FALSE
	 */
	public boolean InitMapView() {
		// TODO Auto-generated method stub
		//绑定布局的中百度地图的ID			
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);	
		//设置放大缩小参数
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(13.0f);        
        mBaiduMap.setMapStatus(msu);
        mBaiduMap.setOnMapClickListener(new OnMapClickListener() {
			
			@Override
			public boolean onMapPoiClick(MapPoi arg0) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void onMapClick(LatLng arg0) {
				mBaiduMap.hideInfoWindow();
			}
		});
        //使用LocationManager判断可以使用的定位方式，
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);     
        List<String> providerList= locationManager.getProviders(true);
        boolean isNetworkAvailable = MySpUtils.getBoolean(mContext.getApplicationContext(), MyConstantValue.NETWORK_AVAILABLE, false);
        if(providerList.contains(LocationManager.GPS_PROVIDER))
        {    
        	if(isNetworkAvailable)
        	{
        		Toast.makeText(mContext, "使用网络和GPS双定位模式", Toast.LENGTH_LONG).show();
        	}
        	else
        	{
        		Toast.makeText(mContext, "使用GPS定位系统", Toast.LENGTH_SHORT).show();
        	}
        	isInitSucceed=true;
        	return true;
        }
        else if(isNetworkAvailable)
        {
            Toast.makeText(mContext, "使用网络定位系统", Toast.LENGTH_LONG).show();
            isInitSucceed=true;
            return true;
        } 
        else
        {
            Toast.makeText(mContext, "没有可用的定位方式，请检查GPS或者网络是否正常!!", Toast.LENGTH_LONG).show();
            isInitSucceed=false;
            return false;
        }
	}
	
	/**开启定位模式，并设置定位监听器，定位成功后会将信息
	 * 保留在BaiduMapInfo的对象当中
	 * @return 返回TRUE表示定位开启正常，返回FALSE表示定位开启失败（可能是GPS定位没有开启或者网络无法连接等）
	 */
	public boolean startLocation()
	{
		if(!isInitSucceed)  return false;
		//创建LocationClient实例
		System.out.println("初始化定位");
        locationClient= new LocationClient(mContext);
        mBaiduMap.setMyLocationEnabled(true);
        mbdLocationListener= new MbdLocationListener();
        //注册监听器，定位成功后回调
        locationClient.registerLocationListener(mbdLocationListener);
        //地图定位模式初始化
	    System.out.println("设置定位模式");
	    LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
        option.setIsNeedAddress(true);// 位置，一定要设置，否则后面得不到地址
        option.setIsNeedLocationDescribe(true);//显示定位点的描述信息
        option.setOpenGps(true);// 打开GPS
        option.setScanSpan(5000);// 设置请求间隔时间
        option.setLocationMode(LocationMode.Hight_Accuracy);// 设置为高精度定位模式（GPS和网络同时定位，返回精度高的定位结果）
        locationClient.setLocOption(option);// 使用设置
        System.out.println("设置完成");
        //启动LocationClient开始定位,定位结果返回在监听器的onReceiveLocation中        
        locationClient.start();
        System.out.println("开启定位");
		return true;			
	}
	
	/**
	 * @author story
	 *定位结果监听器，定位的结果会保存到BaiduMapInfo当中
	 */
	private class MbdLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			//创建MyLocationData类 
			mBaiduMapInfo.setLatitude(location.getLatitude());
			mBaiduMapInfo.setLongitude(location.getLongitude());
			mBaiduMapInfo.setAddress(location.getAddrStr());
			mBaiduMapInfo.setCity(location.getCity());
            MyLocationData locationData= new MyLocationData.Builder()
            .accuracy(location.getRadius())
            .latitude(location.getLatitude())
            .longitude(location.getLongitude())
            .build();
           
           //在BaiduMap中设置定位数据
            mBaiduMap.setMyLocationData(locationData);

            //判断是否第一次定位，如果是执行如下代码
            if(isFirstIn){
            	setNewCenter(location.getLatitude(),location.getLongitude(),16);
                isFirstIn=false;               
                Toast.makeText(
                		mContext,
                         "地址是："+ location.getAddrStr()+"\n"                       
                          +"定位精度是："+location.getRadius()+"\n"
                          ,Toast.LENGTH_LONG).show();           
                System.out.println("Address:-------"+String.valueOf(location.getCity()));
                System.out.println("District:------"+String.valueOf(location.getAddrStr()));
                System.out.println("getLatitude:------"+String.valueOf(location.getLatitude()));                    
                System.out.println("getLongitude:------"+String.valueOf(location.getLongitude()));                    
              }
           }           
		}
	
	/**根据经纬度设置标注，以及点击标注后弹出的窗口信息
	 * @param latitude  标注点的经度
	 * @param longitude 标注点纬度
	 * @param markText  地图上标注点的的简单提示信息
	 * @param infoWindowView 弹出信息框的View
	 * @param myInfoWindowListener 接口对象，用以处理点击信息窗口的处理事件。
	 */
	public void setOverMark(HMDataBaseInfo hmDataBaseInfo,double latitude,double longitude,String markText,final View infoWindowView,
			final MyInfoWindowClickListener myInfoWindowListener)
	   {
		   mHMDataBaseInfo = hmDataBaseInfo;
	       LatLng Text_position = new LatLng(latitude, longitude);  	        
	       //构建文字Option对象，用于在地图上添加文字  
	        OverlayOptions textOption = new TextOptions()  
	            .bgColor(0xAAFFFF00)  
	            .fontSize(28)  
	            .fontColor(0xFFFF00FF)  
	            .text(markText)
	            .rotate(0)  
	            .position(Text_position);  
	        //在地图上添加该文字对象并显示  
	        mBaiduMap.addOverlay(textOption);
	        String strPollution = hmDataBaseInfo.getPollution();
	        switch (strPollution) {
			case "无污染":
				mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.location_pollution_1);
				break;
			case "轻度污染":
				mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.location_pollution_2);
				break;
			case "中度污染":
				mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.location_pollution_3);
				break;
			case "重度污染":
				mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.location_pollution_4);
				break;
			default:
				break;
			}
	        Bundle bundle = new Bundle();
	        bundle.putString("details", hmDataBaseInfo.getDetails());
	        MarkerOptions markOption=new MarkerOptions()
	        		.position(Text_position)
	        		.icon(mCurrentMarker)
	        		.title(markText)
	        		.period(5)
	        		.zIndex(2)
	        		.extraInfo(bundle);
	        markOption.animateType(MarkerAnimateType.grow);
	        mBaiduMap.addOverlay(markOption);
	        if(infoWindowView==null)
	        {
	        	return;
	        }
	        mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {			
				@Override
				public boolean onMarkerClick(final Marker marker) {
					Bundle extraInfo = marker.getExtraInfo();
					String strDetail = (String) extraInfo.get("details");
					TextView tv_history_map_marker=(TextView) infoWindowView.findViewById(R.id.tv_history_map_marker);
					tv_history_map_marker.setText(strDetail);
					mCurrentMarker=BitmapDescriptorFactory.fromView(infoWindowView);
					InfoWindow infoWindow = new InfoWindow(mCurrentMarker,marker.getPosition(),infoWindowView.getHeight() ,new OnInfoWindowClickListener() {						
						@Override
						public void onInfoWindowClick() {
							myInfoWindowListener.MyBaiduInfoWindowClickListener(mBaiduMap, marker);
						}
					});									
					mBaiduMap.showInfoWindow(infoWindow);
				    return false;					
				}
			});
	    }
				
	/**设置新的地图的中心
	 * @param latitude 地图中心的纬度值
	 * @param longitude 地图中心的经度值
	 * @param zoomValue 地图显示的放大值
	 */
	public void setNewCenter(double latitude, double longitude, float zoomValue) {
		LatLng  latLng =new LatLng(latitude,longitude);	                	                
        MapStatus mMapStatus = new MapStatus.Builder().target(latLng).zoom(zoomValue).build();
        MapStatusUpdate msu = MapStatusUpdateFactory.newMapStatus(mMapStatus);        	    	      	                    
        mBaiduMap.setMapStatus(msu);
	}
	/**
	 * 清除地图上的所有Overlay覆盖物以及infoWindow
	 */
	public void clearMapOverLay()
	{
		mBaiduMap.clear();
	}
	public void clearCircelOvrelay()
	{
		for (Overlay overlay : OverLaylist) {
			overlay.remove();
		}
	}
	/**设置圆形覆盖物，经纬度为圆心
	 * @param latitude 覆盖物的纬度
	 * @param longitude  覆盖物的经度
	 * @param radius 圆的半径
	 */
	public void setCircleOverlay(double latitude,double longitude,int radius)
	{
		LatLng latLng = new LatLng(latitude,longitude);
		OverlayOptions circleOption=new CircleOptions().center(latLng).fillColor(0x9987CEFF).radius(radius).stroke(new Stroke(2, 0xAA436EEE));
        Overlay circleOverlay = mBaiduMap.addOverlay(circleOption);
        OverLaylist.add(circleOverlay);
	}
	
	/**返回一个BaiduMapInfo对象，根据该对象可以获取定位信息
	 * 包括：经度，纬度，以及详细地址
	 * @return
	 */
	public BaiduMapInfo getMapInfo()
	{
		return mBaiduMapInfo;
	}
	
	/**跟随activity的生命周期
	 * 注销定位
	 */
	public void UnregisterLocation()
	{
		locationClient.unRegisterLocationListener(mbdLocationListener);
	}
	
	/**
	 * 跟随activity的生命周期
	 * 摧毁百度地图
	 */
	public void BaiduMapDestroy()
	{
		mMapView.onDestroy();
	}
	
	/**
	 * 跟随activity的生命周期
	 * 暂停百度地图
	 */
	public void BaiduMapPause()
	{
		mMapView.onPause();
	}
	
	/**
	 * 跟随activity的生命周期，从暂停中恢复
	 * 恢复百度地图
	 */
	public void BaiduMapResume()
	{
		mMapView.onResume();
		InitMapView();
		
	}
		
	/**该接口是为了实现点击弹出窗口后作出的处理
	 * @author story
	 *
	 */
	public interface MyInfoWindowClickListener
	{
		public void MyBaiduInfoWindowClickListener(BaiduMap baidumap,Marker marker);
	}
}