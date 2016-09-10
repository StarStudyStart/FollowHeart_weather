package com.startli.followheart_weather.util;

/**
 * Created by lyb on 2016-09-10.
 */
public class Constants {
    public static final int NO_VALUE_FLAG = -999;//无
    public static final int SUNNY = 0;//晴
    public static final int CLOUDY = 1;//多云
    public static final int OVERCAST = 2;//阴
    public static final int FOGGY = 3;//雾
    public static final int SEVERE_STORM = 4;//飓风
    public static final int HEAVY_STORM = 5;//大暴风雨
    public static final int STORM = 6;//暴风雨
    public static final int THUNDERSHOWER = 7;//雷阵雨
    public static final int SHOWER = 8;//阵雨
    public static final int HEAVY_RAIN = 9;//大雨
    public static final int MODERATE_RAIN = 10;//中雨
    public static final int LIGHT_RAIN = 11;//小雨
    public static final int SLEET = 12;//雨夹雪
    public static final int SNOWSTORM = 13;//暴雪
    public static final int SNOW_SHOWER = 14;//阵雪
    public static final int HEAVY_SNOW = 15;//大雪
    public static final int MODERATE_SNOW = 16;//中雪
    public static final int LIGHT_SNOW = 17;//小雪
    public static final int STRONGSANDSTORM = 18;//强沙尘暴
    public static final int SANDSTORM = 19;//沙尘暴
    public static final int SAND = 20;//沙尘
    public static final int BLOWING_SAND = 21;//风沙
    public static final int ICE_RAIN = 22;//冻雨
    public static final int DUST = 23;//尘土
    public static final int HAZE = 24;//霾

    public static int getIntType(String type) {
        if ("晴".equals(type)) {
            return SUNNY;
        } else if ("多云".equals(type)) {
            return CLOUDY;
        } else if ("阴".equals(type)) {
            return OVERCAST;
        } else if ("雾".equals(type)) {
            return FOGGY;
        } else if ("飓风".equals(type)) {
            return SEVERE_STORM;
        } else if ("大暴风雨".equals(type)) {
            return HEAVY_STORM;
        } else if ("暴风雨".equals(type)) {
            return STORM;
        } else if ("雷阵雨".equals(type)) {
            return THUNDERSHOWER;
        } else if ("阵雨".equals(type)) {
            return SHOWER;
        } else if ("大雨".equals(type)) {
            return HEAVY_RAIN;
        } else if ("中雨".equals(type)) {
            return MODERATE_RAIN;
        } else if ("小雨".equals(type)) {
            return LIGHT_RAIN;
        } else if ("雨夹雪".equals(type)) {
            return SLEET;
        } else if ("暴雪".equals(type)) {
            return SNOWSTORM;
        } else if ("阵雪".equals(type)) {
            return SNOW_SHOWER;
        } else if ("大雪".equals(type)) {
            return HEAVY_SNOW;
        } else if ("中雪".equals(type)) {
            return MODERATE_SNOW;
        } else if ("小雪".equals(type)) {
            return LIGHT_SNOW;
        } else if ("强沙尘暴".equals(type)) {
            return STRONGSANDSTORM;
        } else if ("沙尘暴".equals(type)) {
            return SANDSTORM;
        } else if ("沙尘".equals(type)) {
            return SAND;
        } else if ("风沙".equals(type)) {
            return BLOWING_SAND;
        } else if ("冻雨".equals(type)) {
            return ICE_RAIN;
        } else if ("尘土".equals(type)) {
            return DUST;
        } else if ("霾".equals(type)) {
            return HAZE;
        } else {
            return NO_VALUE_FLAG;
        }
    }
}
