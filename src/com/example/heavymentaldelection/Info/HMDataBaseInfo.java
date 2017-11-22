package com.example.heavymentaldelection.Info;

public class HMDataBaseInfo {
	private String date;
	private String city;
	private String address;
	private String pollution;
	private String details;
    private double mLatitude;//纬度
    private double mLongitude;//经度
    
	/**返回纬度
	 * @return
	 */
	public double getLatitude() {
		return mLatitude;
	}
	/**设置纬度值
	 * @param mLatitude double型，需要设置的纬度值
	 */
	public void setLatitude(double mLatitude) {
		this.mLatitude = mLatitude;
	}
	/**返回经度值
	 * @return
	 */
	public double getLongitude() {
		return mLongitude;
	}
	/**设置经度值
	 * @param mLongitude 需要设置的经度值
	 */
	public void setLongitude(double mLongitude) {
		this.mLongitude = mLongitude;
	}
	/**获取日期
	 * @return String 字符串型日期格式
	 */
	public String getDate() {
		return date;
	}
	/**设置日期
	 * @param date
	 */
	public void setDate(String date) {
		this.date = date;
	}
	/**获取城市名
	 * @return String
	 */
	public String getCity() {
		return city;
	}
	/**设置城市名
	 * @param city
	 */
	public void setCity(String city) {
		this.city = city;
	}
	/**获取详细地址
	 * @return 
	 */
	public String getAddress() {
		return address;
	}
	/**设置详细地址
	 * @param address 
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	/**获取污染情况
	 * @return  String (无污染、轻度污染、中度污染、重度污染)四选一
	 */
	public String getPollution() {
		return pollution;
	}
	/**设置污染情况
	 * @param pollution (无污染、轻度污染、中度污染、重度污染)必须为则四种中的一种
	 */
	public void setPollution(String pollution) {
		this.pollution = pollution;
	}
	/**获取详情，包括检测离子的参数等
	 * @return
	 */
	public String getDetails() {
		return details;
	}
	/**设置详情
	 * @param details 包括实际检测的参数以及一些备注信息等
	 */
	public void setDetails(String details) {
		this.details = details;
	}
	
}
