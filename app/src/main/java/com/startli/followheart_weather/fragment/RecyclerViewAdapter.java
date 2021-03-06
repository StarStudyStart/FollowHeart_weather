package com.startli.followheart_weather.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
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
import com.startli.followheart_weather.util.Constants;
import com.startli.followheart_weather.util.HttpCallBackListener;
import com.startli.followheart_weather.util.HttpUtil;
import com.startli.followheart_weather.util.Utility;
import com.startli.followheart_weather.util.WeatherIconUtils;

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
     * 用于显示更新时间差
     */
    private TextView timeDiffer;

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

    /**
     * 根据时差判断是否需要更新
     */
    private boolean isNeedRefresh;

    private SimpleAdapter simpleAdapter;
    List<Map<String, Object>> dataList;

    public RecyclerViewAdapter(boolean isLoader, String countyName, Activity weatherActivity, TextView cityName, RecyclerView recyclerView, boolean isNeedRefresh) {
        this.isLoader = isLoader;
        this.countyName = countyName;
        this.weatherActivity = weatherActivity;
        this.cityName = cityName;
        this.mRecyclerView = recyclerView;
        this.isNeedRefresh = isNeedRefresh;
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
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_HEADER) {
            //判断语句
            if (!isLoader || isNeedRefresh) {
                if (countyName != null) {
                    queryWeatherInfo(countyName, false, null);
                }
            } else {
                Log.i("isLoader", "" + isLoader);
                showCurrentWeather();
            }
        } else if (holder.getItemViewType() == TYPE_FORECAST) {
            dataList = getForecastWeatherData();
            simpleAdapter = new SimpleAdapter(holder.itemView.getContext(), dataList, R.layout.recyclerview_weather_forecast_item, new String[]{"forecast_date", "forecast_icon", "forecast_high", "forecast_low"}, new int[]{R.id.forecast_date, R.id.forecast_icon, R.id.forecast_high, R.id.forecast_low});
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

    public class WeatherHeaderViewHolder extends RecyclerView.ViewHolder {
        public WeatherHeaderViewHolder(View itemView) {
            super(itemView);
            currentTemp = (TextView) itemView.findViewById(R.id.current_temp);
            weatherDesp = (TextView) itemView.findViewById(R.id.weather_desp);
            tempLow = (TextView) itemView.findViewById(R.id.temp_low);
            tempHigh = (TextView) itemView.findViewById(R.id.temp_high);
            main_icon = (ImageView) itemView.findViewById(R.id.main_icon);
            timeDiffer = (TextView) itemView.findViewById(R.id.update_time_differ);
            pref = weatherActivity.getSharedPreferences(countyName, Context.MODE_PRIVATE);
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
     * 查询所在地的天气信息
     */
    public void queryWeatherInfo(String cityName, boolean isFromRefresh, Handler handler) {
//        String address = "http://wthrcdn.etouch.cn/weather_mini?city=余杭区"+cityNmae;
//        String address = "http://php.weather.sina.com.cn/iframe/index/w_cl.php?code=js&day=2&city="
//                + cityName + "&dfc=3";
        String address = null;
        try {
            address = "http://wthrcdn.etouch.cn/weather_mini?city=" + URLEncoder.encode(cityName, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        queryFromSever(address, "local_weather", isFromRefresh, handler);
    }

    /**
     * 根据查询的类型，对返回的数据进行处理
     */
    public void queryFromSever(String address, final String type, final boolean isFromRefresh, final Handler handler) {
        //开启进度提示
        if (!isFromRefresh) {
            Log.i("address", address);
            progressDialog = Utility.showProgressDialog("正在加载数据...", progressDialog, weatherActivity);
        }
        HttpUtil.snedHttpRequest(address, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                if ("local_weather".equals(type)) {
                    boolean isSave = Utility.handleWeatherInfoResponse(weatherActivity, response);
                    if (isSave) {
                        weatherActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showCurrentWeather();
                                Log.i("isFromRefresh", "" + isFromRefresh);
                                if (isFromRefresh) {
                                    handler.sendEmptyMessage(WeatherInfoFragment.SWIPE_SUCCEED_WHAT);
                                } else {
                                    Utility.closeProgressDialog(progressDialog);
                                }
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
                        if (isFromRefresh) {
                            handler.sendEmptyMessage(WeatherInfoFragment.SWIPE_FAILURE_WHAT);
                        }
                        Toast.makeText(weatherActivity, "网络请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void showCurrentWeather() {
        //重新获取pref对象，因为如果首次进入程序会首先，加载一个默认名称的pref对象
        Log.i("weatheractivity:countyName", countyName);
        pref = weatherActivity.getSharedPreferences(countyName, Context.MODE_PRIVATE);
//       publishText.setText(pref.getString("current_date", ""));
        currentTemp.setText(pref.getString("current_temp", "123"));
        String type = pref.getString("forecast_type0", "");
        weatherDesp.setText(type);
        cityName.setText(countyName);
        tempLow.setText(pref.getString("forecast_low0", ""));
        tempHigh.setText(pref.getString("forecast_high0", ""));
        timeDiffer.setText(pref.getString("update_date", "") + "更新");
        int typeCode = Constants.getIntType(type);
        // set weatherIcon
        main_icon.setBackgroundResource(WeatherIconUtils.getWeatherIcon(typeCode));
        // set weatherBackGround
        mRecyclerView.setBackgroundResource(WeatherIconUtils.getWeatherBg(typeCode));
    }

    /**
     * 从本地文件中读取天气预报的数据
     */
    private List<Map<String, Object>> getForecastWeatherData() {
        pref = weatherActivity.getSharedPreferences(countyName, Context.MODE_PRIVATE);
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
