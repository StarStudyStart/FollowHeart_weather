package com.startli.followheart_weather.util;

import com.startli.followheart_weather.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lyb on 2016-09-10.
 */
public class WeatherIconUtils {
    private WeatherIconUtils() {
    }

    /**
     * 获取天气图标
     *
     * @param typeCode
     * @return
     */
    public static int getWeatherIcon(int typeCode) {
        // 如果是晚上
        if (isNight(System.currentTimeMillis()))
            switch (typeCode) {
                case Constants.SUNNY:
                    return R.drawable.ic_nightsunny_big;
                case Constants.CLOUDY:
                    return R.drawable.ic_nightcloudy_big;
                case Constants.HEAVY_RAIN:
                case Constants.LIGHT_RAIN:
                case Constants.MODERATE_RAIN:
                case Constants.SHOWER:
                case Constants.STORM:
                    return R.drawable.ic_nightrain_big;
                case Constants.SNOWSTORM:
                case Constants.LIGHT_SNOW:
                case Constants.MODERATE_SNOW:
                case Constants.HEAVY_SNOW:
                case Constants.SNOW_SHOWER:
                    return R.drawable.ic_nightsnow_big;
                default:
                    break;
            }
        // 如果是白天
        switch (typeCode) {
            case Constants.SUNNY:
                return R.drawable.ic_sunny_big;
            case Constants.CLOUDY:
                return R.drawable.ic_cloudy_big;
            case Constants.OVERCAST:
                return R.drawable.ic_overcast_big;
            case Constants.FOGGY:
                return R.drawable.tornado_day_night;
            case Constants.SEVERE_STORM:
                return R.drawable.hurricane_day_night;
            case Constants.HEAVY_STORM:
                return R.drawable.ic_heavyrain_big;
            case Constants.STORM:
                return R.drawable.ic_heavyrain_big;
            case Constants.THUNDERSHOWER:
                return R.drawable.ic_thundeshower_big;
            case Constants.SHOWER:
                return R.drawable.ic_shower_big;
            case Constants.HEAVY_RAIN:
                return R.drawable.ic_heavyrain_big;
            case Constants.MODERATE_RAIN:
                return R.drawable.ic_moderraterain_big;
            case Constants.LIGHT_RAIN:
                return R.drawable.ic_lightrain_big;
            case Constants.SLEET:
                return R.drawable.ic_sleet_big;
            case Constants.SNOWSTORM:
                return R.drawable.ic_snow_big;
            case Constants.SNOW_SHOWER:
                return R.drawable.ic_snow_big;
            case Constants.HEAVY_SNOW:
                return R.drawable.ic_heavysnow_big;
            case Constants.MODERATE_SNOW:
                return R.drawable.ic_snow_big;
            case Constants.LIGHT_SNOW:
                return R.drawable.ic_snow_big;
            case Constants.STRONGSANDSTORM:
                return R.drawable.ic_sandstorm_big;
            case Constants.SANDSTORM:
                return R.drawable.ic_sandstorm_big;
            case Constants.SAND:
                return R.drawable.ic_sandstorm_big;
            case Constants.BLOWING_SAND:
                return R.drawable.ic_sandstorm_big;
            case Constants.ICE_RAIN:
                return R.drawable.freezing_rain_day_night;
            case Constants.DUST:
                return R.drawable.ic_dust_big;
            case Constants.HAZE:
                return R.drawable.ic_haze_big;
            default:
                return R.drawable.ic_default_big;
        }
    }

    /**
     * 获取天气背景
     */
    public static int getWeatherBg(int typeCode) {
//        晚上的天气背景
        if (isNight(System.currentTimeMillis())) {
            switch (typeCode) {
                case Constants.SUNNY:
                    return R.drawable.bg_night_sunny;
                case Constants.CLOUDY:
                    return R.drawable.bg_cloudy_night;
                case Constants.OVERCAST:
                    return R.drawable.bg_night_other;
                case Constants.FOGGY:
                    return R.drawable.bg_foggy;
                case Constants.LIGHT_RAIN:
                case Constants.ICE_RAIN:
                case Constants.MODERATE_RAIN:
                    return R.drawable.bg_rain_middle;
                case Constants.HEAVY_RAIN:
                    return R.drawable.bg_rain_large;
                case Constants.THUNDERSHOWER:
                case Constants.STORM:
                case Constants.SHOWER:
                    return R.drawable.bg_rainstorm;
                case Constants.STRONGSANDSTORM:
                case Constants.SANDSTORM:
                    return R.drawable.bg_sand_storm;
                case Constants.SAND:
                case Constants.BLOWING_SAND:
                    return R.drawable.bg_dirt;
                case Constants.DUST:
                case Constants.HAZE:
                    return R.drawable.bg_haze;
                case Constants.LIGHT_SNOW:
                case Constants.MODERATE_SNOW:
                case Constants.SLEET:
                    return R.drawable.bg_snow_m;
                case Constants.HEAVY_SNOW:
                case Constants.SNOW_SHOWER:
                case Constants.SNOWSTORM:
                    return R.drawable.bg_snowy_l;
                default:
                    break;
            }
        }
        switch (typeCode) {
            case Constants.SUNNY:
                return R.drawable.bg_sunny;
            case Constants.CLOUDY:
                return R.drawable.bg_cloudy;
            case Constants.OVERCAST:
                return R.drawable.bg_overcastsky;
            case Constants.FOGGY:
                return R.drawable.bg_foggy;
            case Constants.LIGHT_RAIN:
            case Constants.ICE_RAIN:
            case Constants.MODERATE_RAIN:
                return R.drawable.bg_rain_middle;
            case Constants.HEAVY_RAIN:
            case Constants.SHOWER:
                return R.drawable.bg_rain_large;
            case Constants.THUNDERSHOWER:
            case Constants.STORM:
                return R.drawable.bg_rainstorm;
            case Constants.STRONGSANDSTORM:
            case Constants.SANDSTORM:
                return R.drawable.bg_sand_storm;
            case Constants.SAND:
            case Constants.BLOWING_SAND:
                return R.drawable.bg_dirt;
            case Constants.DUST:
            case Constants.HAZE:
                return R.drawable.bg_haze;
            case Constants.LIGHT_SNOW:
            case Constants.MODERATE_SNOW:
            case Constants.SLEET:
                return R.drawable.bg_snow_m;
            case Constants.HEAVY_SNOW:
            case Constants.SNOW_SHOWER:
            case Constants.SNOWSTORM:
                return R.drawable.bg_snowy_l;
            default:
                return R.drawable.bg_default_dayu;
        }
    }

    public static boolean isNight(long time) {
        SimpleDateFormat df = new SimpleDateFormat("HH");
        String timeStr = df.format(new Date(System.currentTimeMillis()));
        // L.i("liweiping", "timeStr = " + timeStr);
        try {
            int timeHour = Integer.parseInt(timeStr);
            return (timeHour >= 18 || timeHour <= 6);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }
}
