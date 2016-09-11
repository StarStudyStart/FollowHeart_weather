package com.startli.followheart_weather.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.startli.followheart_weather.model.City;
import com.startli.followheart_weather.model.CoolWeatherDB;
import com.startli.followheart_weather.model.County;
import com.startli.followheart_weather.model.Forecast;
import com.startli.followheart_weather.model.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
            String jsonData = weatherInfo.getString("forecast");
            Gson gson = new Gson();
            List<Forecast> forecastList = gson.fromJson(jsonData, new TypeToken<List<Forecast>>() {
            }.getType());
            JSONArray jsonArray = weatherInfo.getJSONArray("forecast");

            String temp1 = jsonArray.getJSONObject(0).getString("low");
            String[] array1 = temp1.split(" ");
            String lowTemp = array1[1];

            String temp2 = jsonArray.getJSONObject(0).getString("high");
            String[] array2 = temp2.split(" ");
            String highTemp = array2[1];

            String weatherDesp = jsonArray.getJSONObject(0).getString("type");
            isSave = saveWeatherInfo(context, cityName, currentTemp, lowTemp, highTemp,
                    weatherDesp,forecastList);

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
                                          String currentTemp, String lowTemp, String highTemp, String weatherDesp, List<Forecast> forecastList) {
        // 获取当前时间
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy年M月d日",
                Locale.CHINA);
        String currentDate = simpleFormat.format(new Date());
        // 获取本地存储对象
        SharedPreferences.Editor editor = context.getSharedPreferences(cityName, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.putString("city_name", cityName);
        editor.putString("current_temp", currentTemp);
        editor.putString("low_temp", lowTemp);
        editor.putString("high_temp", highTemp);
        editor.putString("weather_Desp", weatherDesp);
        editor.putString("current_date", currentDate);
        editor.putBoolean("info_loaded", true);
        int i = 0;
        for (Forecast forecast:
                forecastList) {
            editor.putString("forecast_type"+i,forecast.getType());
            String high = forecast.getHigh();
            String[] highs = high.split(" ");
            editor.putString("forecast_high"+i,highs[1]);
            String low = forecast.getLow();
            String[] lows = low.split(" ");
            editor.putString("forecast_low"+i,lows[1]);
            String date = forecast.getDate();
            String[] dates = date.split("日");
            editor.putString("forecast_date"+i,dates[1]);
            i++;
        }
        editor.commit();
        if (weatherDesp == null) {
            return false;
        }
        return true;
    }

    /**
     * 顯示進度對話框
     */
    public static ProgressDialog showProgressDialog(String text,ProgressDialog progressDialog, Context context) {
        //避免重复弹出对话框，而出现异常
        //弹出对话框的所在方法，可能在进度框关闭之前，被再次调用。
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(text);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
        return progressDialog;
    }

    /**
     * 關閉進度對話框
     */
    public static void closeProgressDialog(ProgressDialog progressDialog) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /**
     * 为了解决ListView在ScrollView中只能显示一行数据的问题
     *
     * @param listView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) { // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0); // 计算子项View 的宽高
            totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }


}
