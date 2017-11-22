package com.example.heavymentaldelection.database;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseOpenHepler extends SQLiteOpenHelper{
	private String name;
	public DataBaseOpenHepler(Context context, String name, CursorFactory factory, int version,
			DatabaseErrorHandler errorHandler) 
	{		
		super(context, name+".db", factory, version, errorHandler);
		this.name=name;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table "+name+" (_id integer primary key autoincrement, mydate varchar(20), city varchar(20) ,address,pollution varchar(8),details,latitude double,longitude double); ");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
	
}
