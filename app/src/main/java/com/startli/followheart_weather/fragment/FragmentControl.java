package com.startli.followheart_weather.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.startli.followheart_weather.fragment.WeatherInfoFragment;
import com.startli.followheart_weather.util.HttpCallBackListener;
import com.startli.followheart_weather.util.HttpUtil;
import com.startli.followheart_weather.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lyb on 2016-09-04.
 */
public class FragmentControl {
    public static List<Fragment> fragmentList = new ArrayList<Fragment>();
    public static List<String> cityNameList = new ArrayList<String>();

    public static void addWeatherInfoFragment(String countyName,boolean isNative) {
        WeatherInfoFragment weatherInfoFragment = new WeatherInfoFragment();
        addCityName(countyName);
        weatherInfoFragment.setCountyName(countyName);
        weatherInfoFragment.setNative(isNative);
        fragmentList.add(weatherInfoFragment);
    }

    public static List<Fragment> getWeatherInfoFragment() {
        return fragmentList;
    }

    public static void addCityName(String cityName) {
        cityNameList.add(cityName);
    }
    public static void replaceLocation(String countyName,boolean isNative){
        if (countyName!=null) {
            fragmentList.remove(0);
            WeatherInfoFragment weatherInfoFragment = new WeatherInfoFragment();
            addCityName(countyName);
            weatherInfoFragment.setCountyName(countyName);
            weatherInfoFragment.setNative(isNative);
            fragmentList.add(0,weatherInfoFragment);
        }
    }

    public static List<String> getCityNameList() {
        return cityNameList;
    }

    public static void removeAllWeatherInfoFragment() {
        fragmentList.removeAll(fragmentList);
    }
    public static void removeWeahterInfoFragment(int position){
        fragmentList.remove(position);
    }

}
