package com.startli.followheart_weather.fragment;

import android.support.v4.app.Fragment;

import com.startli.followheart_weather.fragment.WeatherInfoFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lyb on 2016-09-04.
 */
public class FragmentControl {
    public static List<Fragment> fragmentList = new ArrayList<Fragment>();
    public static List<String> cityNameList = new ArrayList<String>();

    public static void addWeatherInfoFragment(String countyName) {
        WeatherInfoFragment weatherInfoFragment = new WeatherInfoFragment();
        weatherInfoFragment.setCountyName(countyName);
        fragmentList.add(weatherInfoFragment);
    }

    public static List<Fragment> getWeatherInfoFragment() {
        return fragmentList;
    }

    public static void addCityName(String cityName) {
        cityNameList.add(cityName);
    }
    public static List<String> getCityNameList(){
        return cityNameList;
    }
    public static void removeAllWeatherInfoFragment() {
        fragmentList.removeAll(fragmentList);
    }

}
