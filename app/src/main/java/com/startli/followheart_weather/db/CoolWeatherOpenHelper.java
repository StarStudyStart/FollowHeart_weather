package com.startli.followheart_weather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class CoolWeatherOpenHelper extends SQLiteOpenHelper{
	/**
	 * Province建表语句
	 */
	private final static String CREATE_PROVINCE = "create table Province("
			+"id integer primary key autoincrement,"
			+"province_name text,"
			+"province_code text"
			+")";
	/**
	 * City建表语句
	 *
	 */
	private final static String CREATE_CTITY = "create table City("
			+"id integer primary key autoincrement,"
			+"city_name text,"
			+"city_code text,"
			+"province_id integer"
			+")";
	/**
	 * Country表建表语句
	 *
	 */
	private final static String CREATE_COUNTY = "create table County("
			+"id integer primary key autoincrement,"
			+"county_name text,"
			+"county_code text,"
			+"city_id integer"
			+")";

	public CoolWeatherOpenHelper(Context context, String name,
								 CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_PROVINCE);
		db.execSQL(CREATE_CTITY);
		db.execSQL(CREATE_COUNTY);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
