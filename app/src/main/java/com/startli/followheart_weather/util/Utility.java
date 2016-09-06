package com.startli.followheart_weather.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.startli.followheart_weather.model.City;
import com.startli.followheart_weather.model.CoolWeatherDB;
import com.startli.followheart_weather.model.County;
import com.startli.followheart_weather.model.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utility {
    /**
     * 解析和处理服务器返回的省级数据
     */
    public synchronized static boolean handleProvincesResponse(String response,
                                                               CoolWeatherDB coolWeatherDB) {
        if (!TextUtils.isEmpty(response)) {
            String[] allProvinces = response.split(",");
            if (allProvinces != null && allProvinces.length > 0) {
                for (String p : allProvinces) {
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    // 将解析出来的的数据存储到Province表中
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }

        }
        return false;

    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCitiesResponse(String response,
                                               CoolWeatherDB coolWeatherDB, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCities = response.split(",");
            if (allCities != null && allCities.length > 0) {
                for (String p : allCities) {
                    String[] array = p.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    // 将解析出来的的数据存储到City表中
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }

        }
        return false;

    }

    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountiesResponse(String response,
                                                 CoolWeatherDB coolWeatherDB, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCounties = response.split(",");
            if (allCounties != null && allCounties.length > 0) {
                for (String p : allCounties) {
                    String[] array = p.split("\\|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    // 将解析出来的的数据存储到County表中
                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }

        }
        return false;

    }


    /**
     * 解析服务器返回的json数据，并且将解析出的数据存储到本地
     */
    public static boolean handleWeatherInfoResponse(Context context,
                                                    String response) {
        boolean isSave = false;
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherInfo = jsonObject.getJSONObject("data");
            String cityName = weatherInfo.getString("city");
            String currentTemp = weatherInfo.getString("wendu");
            JSONArray jsonArray = weatherInfo.getJSONArray("forecast");

            String temp1 = jsonArray.getJSONObject(0).getString("low");
            String[] array1 = temp1.split(" ");
            String lowTemp = array1[1];

            String temp2 = jsonArray.getJSONObject(0).getString("high");
            String[] array2 = temp2.split(" ");
            String highTemp = array2[1];

            String weatherDesp = jsonArray.getJSONObject(0).getString("type");
            isSave = saveWeatherInfo(context, cityName, currentTemp, lowTemp, highTemp,
                    weatherDesp);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return isSave;
    }

    /**
     * 将一系列天气信息存入到pref
     */
    public static boolean saveWeatherInfo(Context context, String cityName,
                                          String currentTemp, String lowTemp, String highTemp, String weatherDesp) {
        // 获取当前时间
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy年M月d日",
                Locale.CHINA);
        String currentDate = simpleFormat.format(new Date());
        // 获取本地存储对象
        SharedPreferences.Editor editor = context.getSharedPreferences(cityName, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.putString("city_name", cityName);
        editor.putString("current_temp", currentTemp + "℃");
        editor.putString("low_temp", lowTemp);
        editor.putString("high_temp", highTemp);
        editor.putString("weather_Desp", weatherDesp);
        editor.putString("current_date", currentDate);
        editor.putBoolean("info_loaded", true);
        editor.commit();
        if (weatherDesp == null) {
            return false;
        }
        return true;
    }

    /**
     * 顯示進度對話框
     */
    public static ProgressDialog showProgressDialog(ProgressDialog progressDialog,Context context) {
        //避免重复弹出对话框，而出现异常
        //弹出对话框的所在方法，可能在进度框关闭之前，被再次调用。
        if(progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
        return progressDialog;
    }

    /**
     * 關閉進度對話框
     */
    public  static void closeProgressDialog(ProgressDialog progressDialog) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
