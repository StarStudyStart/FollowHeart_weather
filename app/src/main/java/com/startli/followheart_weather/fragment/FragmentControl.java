package com.startli.followheart_weather.fragment;

import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lyb on 2016-09-04.
 */
public class FragmentControl {
    public static List<Fragment> fragmentList = new ArrayList<Fragment>();
    public static List<String> cityNameList = new ArrayList<String>();

    public static void addWeatherInfoFragment(String countyName, boolean isNative) {
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

    public static void replaceLocation(String countyName, boolean isNative) {
        if (countyName != null) {
            fragmentList.remove(0);
            WeatherInfoFragment weatherInfoFragment = new WeatherInfoFragment();
//            WeatherInfoFragment weatherInfoFragment = (WeatherInfoFragment) fragmentList.get(0);
            addCityName(countyName);
            weatherInfoFragment.setCountyName(countyName);
            weatherInfoFragment.setNative(isNative);
            fragmentList.add(0, weatherInfoFragment);
        }
    }

    public static List<String> getCityNameList() {
        return cityNameList;
    }

    public static void removeAllWeatherInfoFragment() {
        fragmentList.removeAll(fragmentList);
    }

    public static void removeWeahterInfoFragment(int position) {
        fragmentList.remove(position);
    }

}
