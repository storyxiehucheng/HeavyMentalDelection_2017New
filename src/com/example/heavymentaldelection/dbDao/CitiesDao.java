package com.example.heavymentaldelection.dbDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class CitiesDao {
	//数据库在手机中的储存路径
	private String filePath="data/data/com.example.heavymentaldelection/files/database/cities.db";
	//数据库在手机的储存的文件夹
	private String filedir="data/data/com.example.heavymentaldelection/files/database";
	//存放数据的文件夹
	private Context mContext;
	
	public CitiesDao(Context mContext) {
		super();
		this.mContext = mContext;
	}
	
	/**查询数据库中的城市列表，按拼音顺序显示
	 * @return 返回 数据库中的城市列表
	 *  错误则返回null
	 */
	public ArrayList<String> getCities()
	{
		SQLiteDatabase db=openDatabase();
		Locale defloc = Locale.getDefault();
		if(db!=null)
		{
			ArrayList<String> nameList = new ArrayList<String>();
			Cursor cursor = db.query("city", new String[]{"name","pinyin"}, null, null, null, null, "pinyin");
			String name=new String();
			String cityPingyin=new String();
			while(cursor.moveToNext())
			{
				name=cursor.getString(0);				
				cityPingyin=cursor.getString(1).substring(0, 1).toUpperCase(defloc);
				nameList.add(cityPingyin+name);
			}
			Log.e("story", "总城市数："+nameList.size());
			return nameList;
		}
		else
		{
			return null;
		}
	}
	
	/**将assets中数据库文件复制到手机内存当中，如果已经存在在，直接打开数据库，否则复制后打开。
	 * @return 正确者返回SQLiteDatabase对象，错误则返回null
	 */
	public SQLiteDatabase openDatabase()
	{	
		
		//判断数据库文件是否存在
		if(checkDataBase())
		{
			//如果存在则直接返回打开的数据库
			return SQLiteDatabase.openDatabase(filePath, null,
					SQLiteDatabase.OPEN_READONLY);
		}
		else
		{
			
			//创建目录
			File dbdir=new File(filedir);
			dbdir.mkdir();
			//打开文件
			File dbfile=new File(filePath);
			//得到资源
			AssetManager assetManager=null;
			//得到数据库的输入流
			InputStream is=null;
			FileOutputStream fos=null;
			try {
				assetManager = mContext.getAssets();
				Log.e("story", "开始建立输入流");
				is=assetManager.open("cities.db");
				Log.e("story", "建立输入流成功");
				fos=new FileOutputStream(dbfile);
				Log.e("story", "建立输出流成功");
				byte[] buffer=new byte[1024];
				int count=0;
				while((count=is.read(buffer))!=-1)
				{
					fos.write(buffer,0,count);
					fos.flush();
				}
				fos.flush();
				
			} catch (IOException e) {
				e.printStackTrace();
				Log.e("story", "数据库复制失败");
				return null;
			}
			finally
			{
				if(is!=null)
				{
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
				}
				if(fos!=null)
				{
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
				}
			}
			return openDatabase();
		}				
	}
	
	/**检查数据库是否有效
	 * @return 数据库有效则返回TRUE 无效则返回FALSE
	 */
	private boolean checkDataBase() {
		SQLiteDatabase checkDB = null;
		try {
			checkDB = SQLiteDatabase.openDatabase(filePath, null,
					SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			// database does't exist yet.
		}
		if (checkDB != null) {
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}
}
