package com.example.heavymentaldelection.dbDao;

import java.util.ArrayList;

import com.example.heavymentaldelection.Info.HMDataBaseInfo;
import com.example.heavymentaldelection.database.DataBaseOpenHepler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class HeavyMentalDataBaseDao {
	private DataBaseOpenHepler heavyMentalOpenHepler;
	private static String tableName="heavymental01";
	private static Context mContext;
	private HeavyMentalDataBaseDao(Context context)
	{
		heavyMentalOpenHepler=new DataBaseOpenHepler(context, tableName, null, 1, null);
	}
	//单例设计模式
	private static HeavyMentalDataBaseDao mHeavyMentalDao=null;
	
	public static HeavyMentalDataBaseDao getInstance(Context context,String tablename)
	{
		mContext=context;
		tableName=tablename;
		if(mHeavyMentalDao==null)
		{
			mHeavyMentalDao=new HeavyMentalDataBaseDao(context);
		}
		return mHeavyMentalDao;
		
	}
	
	/**在数据库中插入一条检测信息
	 * @param date 时间
	 * @param city 城市
	 * @param address 详细地址
	 * @param pollution 污染情况
	 * @param details 详细信息以及备注等
	 */
	public void insert(String date,String city,String address,String pollution,String details,double latitude,double longitde)
	{
		SQLiteDatabase writableDatabase = heavyMentalOpenHepler.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("mydate", date);
		contentValues.put("city", city);
		contentValues.put("address", address);
		contentValues.put("pollution", pollution);
		contentValues.put("details", details);
		contentValues.put("latitude", latitude);
		contentValues.put("longitude", longitde);
		writableDatabase.insert(tableName, null, contentValues);
		writableDatabase.close();
	}
	/**按条件删除数据库中的条目
	 * @param date 需要删除的日期
	 * @param city 需要删除的城市
	 * @param address 需要删除的详细地址
	 * @param pollution 需要删除的污染情况
	 */
	public void delete(String date,String city,String address,String pollution)
	{
		SQLiteDatabase writableDatabase = heavyMentalOpenHepler.getWritableDatabase();
		writableDatabase.delete(tableName, "mydate=? and city=? and address=? and pollution=?", new String[]{date,city,address,pollution});
	}
	/**按详细地址删除信息
	 * @param address 需要删除的详细地址（该地址为百度地图根据经纬度定位的详细地址）
	 */
	public void deleteFromAddress(String address)
	{
		SQLiteDatabase writableDatabase = heavyMentalOpenHepler.getWritableDatabase();
		writableDatabase.delete(tableName, "address=?", new String[]{address});
	}
	/**按日期删除数据库的中条目
	 * @param date 需要删除的日期
	 */
	public void deleteFromDate(String date)
	{
		SQLiteDatabase writableDatabase = heavyMentalOpenHepler.getWritableDatabase();
		writableDatabase.delete(tableName, "mydate=?", new String[]{date});
	}
	/**按城市删除数据库中的条目
	 * @param city 需要删除的城市
	 */
	public void deleteFromCity(String city)
	{
		SQLiteDatabase writableDatabase = heavyMentalOpenHepler.getWritableDatabase();
		writableDatabase.delete(tableName, "city=?", new String[]{city});
	}
	/**
	 * 删除全部数据库中的数据
	 */
	public void deleteAll()
	{
		SQLiteDatabase writableDatabase = heavyMentalOpenHepler.getWritableDatabase();
		writableDatabase.delete(tableName, null, null);
	}
	/**
	 * 删除数据库文件
	 */
	public boolean deleteDatabase()
	{
		return mContext.deleteDatabase(tableName);
	}
	/**按污染情况删除数据库的条目
	 * @param pollution 需要删除的污染情况
	 */
	public void deleteFromPollution(String pollution)
	{
		SQLiteDatabase writableDatabase = heavyMentalOpenHepler.getWritableDatabase();
		writableDatabase.delete(tableName, "pollution=?", new String[]{pollution});
	}
	
	/**按地址更新数据库
	 * @param date 更新的时间
	 * @param address 需要更新的地址（该地址必须事先存在于数据库中）
	 * @param pollution 需要更新的污染情况
	 * @param details  需要更新的详细内容(如果为null，则不更新详细内容)
	 */
	public void updateFromAddress(String date,String address,String pollution,String details)
	{
		SQLiteDatabase writableDatabase = heavyMentalOpenHepler.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("mydate", date);
		contentValues.put("pllution", pollution);
		if(details!=null)
		{
			contentValues.put("details", details);
		}
		writableDatabase.update(tableName, contentValues, "address=?",new String[]{address});
	}
	
	/**获取数据库的中条目数
	 * @return int型，数据的总条目数，如果没有则返回0
	 */
	public int getCount()
	{
		int count=0;
		SQLiteDatabase writableDatabase = heavyMentalOpenHepler.getWritableDatabase();
		Cursor cursor = writableDatabase.rawQuery("select count(*) from "+tableName, null);
		if(cursor.moveToNext())
		{
			count=cursor.getInt(0);
		}
		cursor.close();
		writableDatabase.close();
		return count;
	}
	
	/**得到数据库中的所有条目
	 * @return 返回HeavyMentalDataBaseInfo的集合对象
	 */
	public ArrayList<HMDataBaseInfo> getAll()
	{
		SQLiteDatabase writableDatabase = heavyMentalOpenHepler.getWritableDatabase();
		Cursor cursor = writableDatabase.query(tableName, 
				new String[]{"mydate","city","address","pollution","details","latitude","longitude"}, null, null, null, null, "_id desc");
		ArrayList<HMDataBaseInfo> list = new ArrayList<HMDataBaseInfo>();
		while(cursor.moveToNext())
		{
			HMDataBaseInfo hmDataBaseInfo = new HMDataBaseInfo();
			hmDataBaseInfo.setDate(cursor.getString(0));
			hmDataBaseInfo.setCity(cursor.getString(1));
			hmDataBaseInfo.setAddress(cursor.getString(2));
			hmDataBaseInfo.setPollution(cursor.getString(3));
			hmDataBaseInfo.setDetails(cursor.getString(4));
			hmDataBaseInfo.setLatitude(cursor.getDouble(5));
			hmDataBaseInfo.setLongitude(cursor.getDouble(6));
			list.add(hmDataBaseInfo);
		}
		cursor.close();
		writableDatabase.close();
		return list;
	}
	/**根据索引查询数据，每次返回15条数据
	 * @param index 需要查询的索引值
	 * @return 返回以索引值开始的15条数据
	 */
	public ArrayList<HMDataBaseInfo> find(int index)
	{
		SQLiteDatabase writableDatabase = heavyMentalOpenHepler.getWritableDatabase();
		Cursor cursor = writableDatabase.rawQuery(
				"select mydate,city,address,pollution,details from "+tableName+" order by _id desc limit ?,15", new String[]{index+""});
		ArrayList<HMDataBaseInfo> list = new ArrayList<HMDataBaseInfo>();
		while(cursor.moveToNext())
		{
			HMDataBaseInfo hmDataBaseInfo = new HMDataBaseInfo();
			hmDataBaseInfo.setDate(cursor.getString(1));
			hmDataBaseInfo.setCity(cursor.getString(2));
			hmDataBaseInfo.setAddress(cursor.getString(3));
			hmDataBaseInfo.setPollution(cursor.getString(4));
			hmDataBaseInfo.setDetails(cursor.getString(5));
			hmDataBaseInfo.setLatitude(cursor.getDouble(6));
			hmDataBaseInfo.setLongitude(cursor.getDouble(7));
			list.add(hmDataBaseInfo);
		}
		cursor.close();
		writableDatabase.close();
		return list;
	}
}
