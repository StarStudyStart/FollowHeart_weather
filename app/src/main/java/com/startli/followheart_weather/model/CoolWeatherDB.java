package com.startli.followheart_weather.model;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.startli.followheart_weather.db.CoolWeatherOpenHelper;


public class CoolWeatherDB {
	/**
	 * 数据库名
	 */
	public final static String DB_NAME = "cool_weather";
	/**
	 * 数据库版本
	 */
	public final static int VERSION = 1;
	private static CoolWeatherDB coolWeatherDB;
	private SQLiteDatabase db;

	/**
	 * 将构造方法私有化
	 */
	private CoolWeatherDB(Context context) {
		CoolWeatherOpenHelper openHelper = new CoolWeatherOpenHelper(context,
				DB_NAME, null, VERSION);
		db = openHelper.getWritableDatabase();
	}

	/**
	 * 获取CoolWeatherDB实例
	 */
	public synchronized static CoolWeatherDB getInstance(Context context) {
		if (coolWeatherDB == null) {
			coolWeatherDB = new CoolWeatherDB(context);
		}
		return coolWeatherDB;
	}

	/**
	 * 将province实例存储到数据库
	 */
	public void saveProvince(Province province) {
		if (province != null) {
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("Province", null, values);
		}
	}

	/**
	 * 从数据库中读取全国所有省份的信息
	 */
	public List<Province> loadProvince() {
		List<Province> provinces = new ArrayList<Province>();
		Cursor cursor = db
				.query("Province", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				Province province = new Province();
				String provinceName = cursor.getString(cursor
						.getColumnIndex("province_name"));
				String provinceCode = cursor.getString(cursor
						.getColumnIndex("province_code"));
				int id = cursor.getInt(cursor.getColumnIndex("id"));

				province.setId(id);
				province.setProvinceName(provinceName);
				province.setProvinceCode(provinceCode);
				provinces.add(province);
			} while (cursor.moveToNext());
		}
		if (cursor != null) {
			cursor.close();
		}
		return provinces;
	}

	/**
	 * 将city实例存入数据库中
	 */
	public void saveCity(City city) {
		if (city != null) {
			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			db.insert("City", null, values);
		}
	}

	/**
	 * 从数据库中读取某省所有的城市信息
	 */
	public List<City> loadCity(int provinceId) {
		List<City> cities = new ArrayList<City>();
		Cursor cursor = db.query("City", null, "province_id = ?",
				new String[] { String.valueOf(provinceId) }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				City city = new City();
				city.setCityCode(cursor.getString(cursor
						.getColumnIndex("city_code")));
				city.setCityName(cursor.getString(cursor
						.getColumnIndex("city_name")));
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setProvinceId(provinceId);
				cities.add(city);
			} while (cursor.moveToNext());
		}
		return cities;
	}

	/**
	 * 将county实例保存到数据库中
	 */
	public void saveCounty(County county) {
		if (county != null) {
			ContentValues values = new ContentValues();
			values.put("county_name", county.getCountyName());
			values.put("county_code", county.getCountyCode());
			values.put("city_id", county.getCityId());
			db.insert("County", null, values);
		}
	}

	/**
	 * 从数据库中读取某城市下所有县的信息
	 */
	public List<County> loadCounty(int cityId) {
		List<County> counties = new ArrayList<County>();
		Cursor cursor = db.query("County", null, "city_id = ?",
				new String[] { String.valueOf(cityId) }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				County county = new County();
				county.setCountyCode(cursor.getString(cursor
						.getColumnIndex("county_code")));
				county.setCountyName(cursor.getString(cursor
						.getColumnIndex("county_name")));
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setCityId(cityId);
				counties.add(county);
			} while (cursor.moveToNext());
		}
		return counties;

	}

}
