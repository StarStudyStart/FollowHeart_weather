package com.startli.followheart_weather.model;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.startli.followheart_weather.R;
import com.startli.followheart_weather.activity.ChooseAreaActivity;
import com.startli.followheart_weather.activity.WeatherActivity;
import com.startli.followheart_weather.util.FragmentControl;
import com.startli.followheart_weather.util.HttpCallBackListener;
import com.startli.followheart_weather.util.HttpUtil;
import com.startli.followheart_weather.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;

/**
 * Created by lyb on 2016-09-04.
 */
public class WeatherInfoFragment extends Fragment {
    private LinearLayout weatherInfoLayout;
    private RelativeLayout weather_layout;
    private WeatherActivity weatherActivity;
    /**
     * 显示城市名称
     */
    private TextView cityName;

    /**
     * 用于显示当前日期
     */
    private TextView publishText;

    /**
     * 用于显示当前温度
     */
    private TextView currentTemp;

    /**
     * 用于显示具体的天气信息
     */
    private TextView weatherDesp;

    /**
     * 用于显示最低气温
     */
    private TextView temp1;

    /**
     * 用于显示最高气温
     */
    private TextView temp2;

    /**
     * 用于切换城市
     */
    private ImageButton switch_city;

    /**
     * 用于刷新天气信息
     */
    private ImageButton refresh_weather;

    /**
     * 用於接收choosAreaActivity中傳遞過來的countycode
     */
    private String countyCode = "";
    /**
     * 用于接收返回的天气代码
     */
    private String weatherCode = "";

    /**
     * 用于接收本地本地地名
     */
    private String localName = "";
    private String countyName;
    /**
     * 用于网络缓冲
     */
    private ProgressDialog progressDialog;

    public static final int REQUEST_CODE = 123;
    private Toolbar mToolBar;
    private Button mSwitchCity;
    private SharedPreferences pref;
    private boolean isLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View weatherInfoView = inflater.inflate(R.layout.frament_item_weather_info, container, false);

        // 寻找控件
        weatherInfoLayout = (LinearLayout) weatherInfoView.findViewById(R.id.weather_info_layout);
        weather_layout = (RelativeLayout) weatherInfoView.findViewById(R.id.weather_layout);

        publishText = (TextView) weatherInfoView.findViewById(R.id.publish_text);
        currentTemp = (TextView) weatherInfoView.findViewById(R.id.current_temp);
        weatherDesp = (TextView) weatherInfoView.findViewById(R.id.weather_desp);
        temp1 = (TextView) weatherInfoView.findViewById(R.id.temp1);
        temp2 = (TextView) weatherInfoView.findViewById(R.id.temp2);
        mToolBar = (Toolbar) weatherInfoView.findViewById(R.id.toolbar);
        cityName = (TextView) weatherInfoView.findViewById(R.id.city_name_456);
        mSwitchCity = (Button) weatherInfoView.findViewById(R.id.switch_city);


        //寻找activity
        weatherActivity = (WeatherActivity) getActivity();
        // 初始化toolbar
        DrawerLayout drawerLayout = weatherActivity.getmDrawerLayout();
        // ？每次创建fragment时，都会重新创讲一个关联
        weatherActivity.initNavigation(drawerLayout, mToolBar);

        countyName = getCountyName();
        pref = weatherActivity.getSharedPreferences(countyName, Context.MODE_PRIVATE);
        isLoader = pref.getBoolean("info_loaded", false);
        //判断语句
        if (!isLoader) {
            if (countyName == null) {
                getLocation();
            } else {
                queryLocationWeatherInfo(countyName);
            }
        } else {
            showWeather();
        }

        mSwitchCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(weatherActivity, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });


        return weatherInfoView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * 获取当前的地理位置
     */
    private void getLocation() {
        String provider = null;
        LocationManager locationManager = (LocationManager) weatherActivity.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            Toast.makeText(weatherActivity, "NO location provider to use", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ActivityCompat.checkSelfPermission(weatherActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(weatherActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //      Returns a Location indicating the data from the last known location fix obtained from the given provider.
        //      这个方法只是返回 最近已知的位置信息。如果是新的机器  从来没有获取过位置信息；那location为空了
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            queryLocationName(location);
        }
    }

    /**
     * 查询当前位置信息
     */
    private void queryLocationName(Location location) {
        String address = "http://maps.google.cn/maps/api/geocode/json?latlng=" + location.getLatitude() + "," + location.getLongitude() + "&sensor=false&language=zh-CN";
        queryFromSever(address, "locationInfo");
    }

    /**
     * 查询所在地的天气信息
     */
    private void queryLocationWeatherInfo(String cityNmae) {
//        String address = "http://wthrcdn.etouch.cn/weather_mini?city="+cityNmae;
//        String address = "http://php.weather.sina.com.cn/iframe/index/w_cl.php?code=js&day=2&city="
//                + cityName + "&dfc=3";
        String address = null;
        try {
            address = "http://wthrcdn.etouch.cn/weather_mini?city=" + URLEncoder.encode(cityNmae, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        queryFromSever(address, "local_weather");
    }

    /**
     * 根据查询的类型，对返回的数据进行处理
     */


    private void queryFromSever(String address, final String type) {
        //开启进度提示
        progressDialog = Utility.showProgressDialog(progressDialog,weatherActivity);
        HttpUtil.snedHttpRequest(address, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                if ("locationInfo".equals(type)) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray result = jsonObject.getJSONArray("results");
                        if (result.length() > 0) {
                            JSONObject subObject = result.getJSONObject(0);
                            JSONArray addressArray = subObject.getJSONArray("address_components");
                            JSONObject addressCity = addressArray.getJSONObject(1);
                            String countyName = addressCity.getString("short_name");
                            final String countyName_Handle = countyName.substring(0, countyName.length() - 1);

                            localName = countyName_Handle;
                            setCountyName(countyName_Handle);
//                            FragmentControl.addCityName(countyName_Handle);

                            queryLocationWeatherInfo(countyName_Handle);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if ("local_weather".equals(type)) {

                    Log.d("tag",response);
                    boolean isSave = Utility.handleWeatherInfoResponse(weatherActivity, response);
                    if (isSave) {
                        weatherActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utility.closeProgressDialog(progressDialog);
                                showWeather();
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                weatherActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utility.closeProgressDialog(progressDialog);
                        Toast.makeText(weatherActivity, "请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void showWeather() {
        //重新获取pref对象，因为如果首次进入程序会首先，加载一个默认名称的pref对象
        if (localName != null) {
            pref = weatherActivity.getSharedPreferences(countyName, Context.MODE_PRIVATE);
        }
        publishText.setText(pref.getString("current_date", ""));
        currentTemp.setText(pref.getString("current_temp", ""));
        String type = pref.getString("weather_Desp", "");
        weatherDesp.setText(type);
        cityName.setText(countyName);
        temp1.setText(pref.getString("low_temp", ""));
        temp2.setText(pref.getString("high_temp", ""));

        long time = System.currentTimeMillis();
        final Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(time);

        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        if (hour > 18 || hour < 6) {
            setNightBackground(type);
        } else {
            setDayBackgroud(type);
        }
        weather_layout.setVisibility(View.VISIBLE);

    }

    /**
     * 根据得到的天气类型，设置白天的背景图片
     *
     * @param type
     */
    private void setDayBackgroud(String type) {
        if ("晴".equals(type)) {
            weather_layout.setBackgroundResource(R.drawable.sunny);
        } else if ("多云".equals(type)) {
            weather_layout.setBackgroundResource(R.drawable.overcast_sky);
        } else if ("雷阵雨".equals(type)) {
            weather_layout.setBackgroundResource(R.drawable.rainstorm);
        } else if ("小雨".equals(type) || "中雨".equals(type)) {
            weather_layout.setBackgroundResource(R.drawable.rain_m);
        } else if ("阵雨".equals(type) || "大雨".equals(type) || "暴雨".equals(type)
                || "大暴雨".equals(type) || "特大暴雨".equals(type)) {
            weather_layout.setBackgroundResource(R.drawable.rain_l);
        } else if ("小雪".equals(type) || "中雪".equals(type)) {
            weather_layout.setBackgroundResource(R.drawable.snow_m);

        } else if ("大雪".equals(type) || "暴雪".equals(type) || "阵雪".equals(type)) {
            weather_layout.setBackgroundResource(R.drawable.snowy_l);
        } else if ("雾".equals(type) || "大雾".equals(type) || "霾".equals(type)) {
            weather_layout.setBackgroundResource(R.drawable.foggy);
        } else if ("阴".equals(type)) {
            weather_layout.setBackgroundResource(R.drawable.cloudy);

        } else if ("沙尘暴".equals(type)) {
            weather_layout.setBackgroundResource(R.drawable.dirt);
        }

    }

    /**
     * 根据得到的天气类型，设置夜晚的背景图片
     *
     * @param type
     */
    private void setNightBackground(String type) {
        if ("晴".equals(type)) {
            weather_layout.setBackgroundResource(R.drawable.night_sunny);
        } else {
            weather_layout.setBackgroundResource(R.drawable.night_other);
        }
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }
}
