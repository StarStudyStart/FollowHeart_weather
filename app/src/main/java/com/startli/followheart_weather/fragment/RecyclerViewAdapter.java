package com.startli.followheart_weather.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.startli.followheart_weather.R;
import com.startli.followheart_weather.model.Forecast;
import com.startli.followheart_weather.util.Constants;
import com.startli.followheart_weather.util.HttpCallBackListener;
import com.startli.followheart_weather.util.HttpUtil;
import com.startli.followheart_weather.util.Utility;
import com.startli.followheart_weather.util.WeatherIconUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lyb on 2016-09-08.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static int ITEM_COUNT = 5;
    private final static int TYPE_HEADER = 0;
    private final static int TYPE_FORECAST = 1;
    private final static int TYPE_ITEM = 2;

    /**
     * 用于接收当前城市名称
     */
    private TextView cityName;
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
    private TextView tempLow;

    /**
     * 用于显示最高气温
     */
    private TextView tempHigh;

    /**
     * 用于设置天气背景
     */
    private RecyclerView mRecyclerView;

    /**
     * 用于设置当前天气图标
     */
    private ImageView main_icon;

    /**
     * 用于接收本地本地地名
     */
    private String localName = "";
    /**
     * 用于网络缓冲
     */
    private ProgressDialog progressDialog;

    /**
     * 用于存储一些数据
     */
    private SharedPreferences pref;

    /**
     * 用于判断该城市的天气信息是否已经存储
     */
    private boolean isLoader;

    /**
     * 用于接收城市名称
     */
    private String countyName;

    /**
     * 用于接收当前Activity
     */
    private Activity weatherActivity;

    /**
     * 用于天气预测显示
     */
    private ListView listView_forecast;
    private SimpleAdapter simpleAdapter;
    List<Map<String, Object>> dataList;

    public RecyclerViewAdapter(boolean isLoader, String countyName, Activity weatherActivity, TextView cityName, RecyclerView recyclerView) {
        this.isLoader = isLoader;
        this.countyName = countyName;
        this.weatherActivity = weatherActivity;
        this.cityName = cityName;
        this.mRecyclerView = recyclerView;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new WeatherHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_weather_current, parent, false));
        } else if (viewType == TYPE_FORECAST) {
            return new WeatherForecastViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_weather_forecast, parent, false));
        }
        return new WeatherItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false));
    }

    /**
     * 这个方法中可以设置控件的内容等等，但是前提是控件的内容必须能读取到，如果在onCreateV方法中开启新线程查询天气天气信息
     * 等到onBnidViewHolder方法执行时可能，天气信息还没有返回，
     * 所以必须在该方法中，开启线程查询天气信息，这样保证天气信息一定会在更新后显示在控件上
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_HEADER) {
            //判断语句
            //????每次加载本地城市都会重新连网获取天气信息，直接在Avtivity或者FragmentControl中获取本地城市信息
            if (!isLoader) {
                if (countyName == null) {
                    getLocation();
                } else {
                    queryLocationWeatherInfo(countyName);
                }
            } else {
                showCurrentWeather();
            }
        } else if (holder.getItemViewType() == TYPE_FORECAST) {
            dataList = getForecastWeatherData();
            simpleAdapter = new SimpleAdapter(holder.itemView.getContext(), dataList, R.layout.recyclerview_weather_forecast_item,
                    new String[]{"forecast_date", "forecast_icon", "forecast_high", "forecast_low"},
                    new int[]{R.id.forecast_date, R.id.forecast_icon, R.id.forecast_high, R.id.forecast_low});
            listView_forecast.setAdapter(simpleAdapter);
            // ？？？listview在RecyclerView中只显示一条数据的解决方法
            Utility.setListViewHeightBasedOnChildren(listView_forecast);
        }
    }

    @Override
    public int getItemCount() {
        return ITEM_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else if (position == 1) {
            return TYPE_FORECAST;
        } else {
            return TYPE_ITEM;
        }
    }

    class WeatherHeaderViewHolder extends RecyclerView.ViewHolder {

        public WeatherHeaderViewHolder(View itemView) {
            super(itemView);
            currentTemp = (TextView) itemView.findViewById(R.id.current_temp);
            weatherDesp = (TextView) itemView.findViewById(R.id.weather_desp);
            tempLow = (TextView) itemView.findViewById(R.id.temp_low);
            tempHigh = (TextView) itemView.findViewById(R.id.temp_high);
            main_icon = (ImageView) itemView.findViewById(R.id.main_icon);

        }
    }

    class WeatherForecastViewHolder extends RecyclerView.ViewHolder {

        public WeatherForecastViewHolder(View itemView) {
            super(itemView);
            listView_forecast = (ListView) itemView.findViewById(R.id.forecast_listview);

        }
    }

    class WeatherItemViewHolder extends RecyclerView.ViewHolder {

        public WeatherItemViewHolder(View itemView) {
            super(itemView);
        }
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
        progressDialog = Utility.showProgressDialog(progressDialog, weatherActivity);
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
                            String countyName_network = addressCity.getString("short_name");
                            countyName = countyName_network.substring(0, countyName_network.length() - 1);
                            //  ????
                            localName = countyName;
//                            FragmentControl.addCityName(countyName_Handle);
                            queryLocationWeatherInfo(countyName);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if ("local_weather".equals(type)) {
                    boolean isSave = Utility.handleWeatherInfoResponse(weatherActivity, response);
                    if (isSave) {
                        weatherActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utility.closeProgressDialog(progressDialog);
                                showCurrentWeather();
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

    public void showCurrentWeather() {
        //重新获取pref对象，因为如果首次进入程序会首先，加载一个默认名称的pref对象
        if (localName != null) {
            pref = weatherActivity.getSharedPreferences(countyName, Context.MODE_PRIVATE);
        }
//        publishText.setText(pref.getString("current_date", ""));
        currentTemp.setText(pref.getString("current_temp", ""));
        String type = pref.getString("forecast_type0", "");
        weatherDesp.setText(type);
        cityName.setText(countyName);
        tempLow.setText(pref.getString("forecast_low0", ""));
        tempHigh.setText(pref.getString("forecast_high0", ""));
        int typeCode = Constants.getIntType(type);
        // set weatherIcon
        main_icon.setBackgroundResource(WeatherIconUtils.getWeatherIcon(typeCode));
        // set weatherBackGround
        mRecyclerView.setBackgroundResource(WeatherIconUtils.getWeatherBg(typeCode));
/*        long time = System.currentTimeMillis();
        final Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(time);

        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        if (hour > 18 || hour < 6) {
            setNightBackground(type);
        } else {
            setDayBackgroud(type);
        }*/
    }

    /**
     * 从本地文件中读取天气预报的数据
     */
    private List<Map<String, Object>> getForecastWeatherData() {
        if (localName != null) {
            pref = weatherActivity.getSharedPreferences(countyName, Context.MODE_PRIVATE);
        }
        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
        ;
        for (int i = 0; i < 5; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("forecast_date", pref.getString("forecast_date" + i, ""));
            String type = pref.getString("forecast_type" + i, "");
            int typeCode = Constants.getIntType(type);
            map.put("forecast_icon", WeatherIconUtils.getWeatherIcon(typeCode));
            map.put("forecast_high", pref.getString("forecast_high" + i, ""));
            map.put("forecast_low", pref.getString("forecast_low" + i, ""));
            dataList.add(map);
        }
        return dataList;
    }

}
