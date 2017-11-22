package com.example.heavymentaldelection.my_utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.example.heavymentaldelection.Info.HMDataBaseInfo;

import android.util.Log;

public class MySortFilterArrayList {
	
	/**按指定顺序排序
	 * @param hMInfoList 需要排序的集合
	 * @param sortName 需要排序的名称
	 * @return 排序后的集合
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<HMDataBaseInfo> sortList(String sortName,ArrayList<HMDataBaseInfo> hMInfoList) {
		ArrayList<HMDataBaseInfo> tempList=new ArrayList<HMDataBaseInfo>();
		tempList=(ArrayList<HMDataBaseInfo>) hMInfoList.clone();
		if(!sortName.contains("默认"))
		{
			if(sortName.contains("由早到晚"))
			{
				Log.e("story", "time--由早到晚");
				tempList=sortTimeByUp(tempList);
			}
			if(sortName.contains("由晚到早"))
			{
				Log.e("story", "time--由晚到早");
				tempList=sortTimeByDown(tempList);
			}
			if(sortName.contains("由重到轻"))
			{
				Log.e("story", "strPollution--由重到轻");
				tempList=sortPollutionByDown(tempList);
			}
			if(sortName.contains("由轻到重"))
			{
				Log.e("story", "strPollution--由轻到重");
				tempList=sortPollutionByUp(tempList);
			}
		}
		return tempList;
	}
	

	/**按污染程度由轻到重排序
	 * @param tempList 需要排序的集合
	 * @return 返回排序后的集合
	 */
	protected static ArrayList<HMDataBaseInfo> sortPollutionByUp(ArrayList<HMDataBaseInfo> tempList) {
		ArrayList<HMDataBaseInfo> sortList=tempList;
		Comparator<HMDataBaseInfo> comparator=new Comparator<HMDataBaseInfo>() {
			@Override
			public int compare(HMDataBaseInfo firstInfo, HMDataBaseInfo lastInfo) {
				String strfirst=pollutionStrprocess(firstInfo);
				String strlast=pollutionStrprocess(lastInfo);
				if(!strfirst.equals(strlast))
				{
					return strfirst.compareTo(strlast);
				}
				else if(!firstInfo.getDate().equals(lastInfo.getDate()))
				{
					return firstInfo.getDate().compareTo(lastInfo.getDate());
				}
				else
				{
					return -1;
				}
			}
		};
		Collections.sort(sortList, comparator);
		return sortList;
	}
	/**将污染程度,将污染程度前面添加一个数字，以便用于排序
	 * @param firstInfo
	 * @return
	 */
	protected static  String pollutionStrprocess(HMDataBaseInfo firstInfo) {
		String tempStr=firstInfo.getPollution();
		if(tempStr.equals("无污染"))  tempStr="0"+tempStr;
		if(tempStr.equals("轻度污染")) tempStr="1"+tempStr;
		if(tempStr.equals("中度污染")) tempStr="2"+tempStr;
		if(tempStr.equals("重度污染")) tempStr="3"+tempStr;
				
		return tempStr;
		
	}
	/**按污染程度由重到轻排序
	 * @param tempList 需要排序的集合list
	 * @return 返回排序后的集合
	 */
	protected static ArrayList<HMDataBaseInfo> sortPollutionByDown(ArrayList<HMDataBaseInfo> tempList) {
		ArrayList<HMDataBaseInfo> sortList=tempList;
		Comparator<HMDataBaseInfo> comparator=new Comparator<HMDataBaseInfo>() {
			@Override
			public int compare(HMDataBaseInfo firstInfo, HMDataBaseInfo lastInfo) {
				String strfirst=pollutionStrprocess(firstInfo);
				String strlast=pollutionStrprocess(lastInfo);
				if(!strfirst.equals(strlast))
				{
					return -strfirst.compareTo(strlast);
				}
				else if(!firstInfo.getDate().equals(lastInfo.getDate()))
				{
					return firstInfo.getDate().compareTo(lastInfo.getDate());
				}
				else
				{
					return 1;
				}
			}
		};
		Collections.sort(sortList, comparator);
		return sortList;
	}
	/**按时间顺序由晚到早排序
	 * @param tempList 需要排序的集合list
	 * @return 返回排序后的集合list
	 */
	protected static ArrayList<HMDataBaseInfo> sortTimeByDown(ArrayList<HMDataBaseInfo> tempList) {
		ArrayList<HMDataBaseInfo> sortList=tempList;
		Comparator<HMDataBaseInfo> comparator=new Comparator<HMDataBaseInfo>() {
			@Override
			public int compare(HMDataBaseInfo firstInfo, HMDataBaseInfo lastInfo) {
				String strfirst=pollutionStrprocess(firstInfo);
				String strlast=pollutionStrprocess(lastInfo);
				if(!firstInfo.getDate().equals(lastInfo.getDate()))
				{
					return -firstInfo.getDate().compareTo(lastInfo.getDate());
				}
				else if(!strfirst.equals(strlast))
				{
					return -strfirst.compareTo(strlast);
				}
				else
				{
					return 1;
				}
			}
		};
		Collections.sort(sortList, comparator);
		return sortList;
	}
	/**按时间顺序，由早到晚排序
	 * @param tempList 需要排序的集合list
	 * @return 返回排序后的集合list
	 */ 
	protected static ArrayList<HMDataBaseInfo> sortTimeByUp(ArrayList<HMDataBaseInfo> tempList) {
		ArrayList<HMDataBaseInfo> sortList=tempList;
		Comparator<HMDataBaseInfo> comparator=new Comparator<HMDataBaseInfo>() {
			@Override
			public int compare(HMDataBaseInfo firstInfo, HMDataBaseInfo lastInfo) {
				String strfirst=pollutionStrprocess(firstInfo);
				String strlast=pollutionStrprocess(lastInfo);
				if(!firstInfo.getDate().equals(lastInfo.getDate()))
				{
					return firstInfo.getDate().compareTo(lastInfo.getDate());
				}
				else if(!strfirst.equals(strlast))
				{
					return strfirst.compareTo(strlast);
				}
				else
				{
					return -1;
				}
			}
		};
		Collections.sort(sortList, comparator);
		return sortList;
	}
	
	
	/**过滤显示数据的操作
	 * @param filterList 需要过滤的集合
	 * @param filterDate 需要过滤的日期
	 * @param filterCity 需要过滤的城市
	 * @param filterPollution 需要过滤的污染程度
	 * @return 返回过滤后的集合
	 */
	public static ArrayList<HMDataBaseInfo> filterProcess(String filterDate,String filterCity,String filterPollution,ArrayList<HMDataBaseInfo> filterList) {
		ArrayList<HMDataBaseInfo> tempList=new ArrayList<HMDataBaseInfo>();
		tempList=filterList;
		if(!filterDate.equals("全部"))
		{   
			String[] strings = filterDate.split("\n~\n");
			tempList=filterDates(tempList,strings[0],strings[1]);
		}
		if(!filterCity.equals("全部"))
		{
			tempList=filterCities(tempList,filterCity);
		}
		if(!filterPollution.equals("全部"))
		{
			tempList=filterPollution(tempList,filterPollution);
		}
		return tempList;
	}
		
	/**
	 * 显示指定日期中的内容
	 * @param startDate 开始的日期
	 * @param endDate   结束的日期
	 * @param filterList 需要过滤的数据集合
	 * @return 返回过滤之后的list集合
	 */
	protected static ArrayList<HMDataBaseInfo> filterDates(ArrayList<HMDataBaseInfo> filterList,String startDate,String endDate) {
		ArrayList<HMDataBaseInfo> arrayList = new ArrayList<HMDataBaseInfo>();
		for (HMDataBaseInfo hmDataBaseInfo : filterList) 
		{
			if((hmDataBaseInfo.getDate().compareTo(startDate)>=0)
					&&(hmDataBaseInfo.getDate().compareTo(endDate)<=0))
			{
				arrayList.add(hmDataBaseInfo);
			}
		}
		return arrayList;
	}
	/**显示指定城市的内容
	 * @param city 需要显示的城市名称
	 * @param filterList 需要过滤的数据集合
	 * @return 返回过滤之后的list集合
	 */
	protected static ArrayList<HMDataBaseInfo> filterCities(ArrayList<HMDataBaseInfo> filterList,String city) {
		ArrayList<HMDataBaseInfo> arrayList = new ArrayList<HMDataBaseInfo>();
		for (HMDataBaseInfo hmDataBaseInfo : filterList) {
			if(hmDataBaseInfo.getCity().contains(city))
			{
				arrayList.add(hmDataBaseInfo);
			}
		}
		return arrayList;
	}
	/**显示指定的污染程度
	 * @param pollution 需要显示的污染程度
	 * @param filterList 需要过滤的数据集合
	 * @return 返回过滤之后的list集合
	 */
	protected static ArrayList<HMDataBaseInfo> filterPollution(ArrayList<HMDataBaseInfo> filterList,String pollution) {
		ArrayList<HMDataBaseInfo> arrayList = new ArrayList<HMDataBaseInfo>();
		for (HMDataBaseInfo hmDataBaseInfo : filterList) {
			if(hmDataBaseInfo.getPollution().equals(pollution))
			{
				arrayList.add(hmDataBaseInfo);
			}
		}
		return arrayList;
	}
	
	/**排列数据的大小
	 * @param data  需要排列的数据集合 
	 * @param isDsc 是升序还是降序排列(true 表示降序，    FALSE 表示升序)
	 * @return
	 */
	public static ArrayList<Double> sortData(ArrayList<Double> data,final boolean isDsc)
	{
		if(data.isEmpty())
		{
			return data;
		}
		@SuppressWarnings("unchecked")
		ArrayList<Double> dataArrayList = (ArrayList<Double>) data.clone();
		Comparator<Double> comparator = new Comparator<Double>() {

			@Override
			public int compare(Double first, Double second) {
				if(isDsc)
				{
					//默认是升序排序，如果要得到降序，只需要将要比较的数据换一下
					return second.compareTo(first);
				}
				else
				{
					//如果是升序排序，则调用默认顺序
					return first.compareTo(second);
				}
			}
		};
		Collections.sort(dataArrayList, comparator);
		return dataArrayList;
	}
	
	/**数据排序
	 * @param sortList 需要排列的集合
	 * @param isVolt   是否是按照电压排序，TRUE表示按照电压排序，FALSE表示按照电流排序
	 * @return 返回排序后的结果
	 */
	public static ArrayList<String> sortString(ArrayList<String> sortList,final boolean isVolt)
	{
		if(sortList.isEmpty())
		{
			return sortList;
		}
		@SuppressWarnings("unchecked")
		ArrayList<String> needSortList=(ArrayList<String>) sortList.clone();
		Comparator<String> comparator = new Comparator<String>() {
			String[] cstringFirst;
			String[] cstringSecond;
			@Override
			public int compare(String first, String second) {
				cstringFirst=first.split("#");
				cstringSecond=second.split("#");
				if(isVolt)
				{
					return Double.valueOf(cstringFirst[0]).compareTo(Double.valueOf(cstringSecond[0]));
				}
				else
				{
					return Double.valueOf(cstringFirst[1]).compareTo(Double.valueOf(cstringSecond[1]));
				}
			}
		};
		Collections.sort(needSortList, comparator);
		return needSortList;
	}
}
