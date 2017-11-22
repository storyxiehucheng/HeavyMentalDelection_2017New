package com.example.heavymentaldelection.Info;

public class BaiduMapInfo {
    private double mLatitude;//纬度
    private double mLongitude;//经度
    private String mAddress;//详细定位地址
    private String mCity;//定位城市	      
    /**
	 * 默认构造函数
	 */
	public BaiduMapInfo() {
		super();
	}
	/**利用经度，纬度，定位地址来构造对象
	 * @param mLatitude
	 * @param mLongitude
	 * @param address
	 */
	public BaiduMapInfo(double mLatitude, double mLongitude, String address) {
		super();
		this.mLatitude = mLatitude;
		this.mLongitude = mLongitude;
		mAddress = address;
	}
				
	/**返回定位城市
	 * @return
	 */
	public String getCity() {
		return mCity;
	}
	/**设置定位城市
	 * @param mCity
	 */
	public void setCity(String mCity) {
		this.mCity = mCity;
	}
	/**
	 * @return 返回纬度
	 */
	public double getLatitude() {
		return mLatitude;
	}
	/**
	 * @param mLatitude 设置纬度
	 */
	public void setLatitude(double mLatitude) {
		this.mLatitude = mLatitude;
	}
	/**
	 * @return 返回经度
	 */
	public double getLongitude() {
		return mLongitude;
	}
	/**
	 * @param mLongitude 设置经度
	 */
	public void setLongitude(double mLongitude) {
		this.mLongitude = mLongitude;
	}
	/**
	 * @return 返回定位地址,默认返回为空字符串
	 */
	public String getAddress() {
		return mAddress;
	}
	/**
	 * @param mAddress设置定位地址
	 */
	public void setAddress(String mAddress) {
		this.mAddress = mAddress;
	}
   
}
