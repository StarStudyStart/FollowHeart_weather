package com.startli.followheart_weather.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
public class WeatherInfoFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
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
     * 用于接收地名
     */
    private String countyName;
    /**
     * 用于网络缓冲
     */
    private ProgressDialog progressDialog;

    /**
     * 是否问家中是否已经加载天气信息
     */
    private boolean isLoader;

    /**
     * 是否是本地地名
     */
    private boolean isNative = false;

    public static final int REQUEST_CODE = 123;
    private Toolbar mToolBar;
    private Button mSwitchCity;
    private SharedPreferences pref;

    //一系列天气信息的显示
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mRecyclerViewAdapter;

    /**
     * 实现下拉刷新
     */
    public static final int SWIPE_WHAT = 233;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SWIPE_WHAT){
                //取消刷新状态
                mSwipeRefreshLayout.setRefreshing(false);
                mRecyclerViewAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View weatherInfoView = inflater.inflate(R.layout.frament_item_weather_info, container, false);
        // 寻找控件
        mToolBar = (Toolbar) weatherInfoView.findViewById(R.id.toolbar);
        cityName = (TextView) weatherInfoView.findViewById(R.id.city_name_456);
        mSwitchCity = (Button) weatherInfoView.findViewById(R.id.switch_city);

        //获取与fragment关联的activity
        weatherActivity = (WeatherActivity) getActivity();
        // 初始化toolbar
        DrawerLayout drawerLayout = weatherActivity.getmDrawerLayout();
        // ？每次创建fragment时，都会重新创讲一个关联
        weatherActivity.initNavigation(drawerLayout, mToolBar);
        countyName = getCountyName();
        isNative = isNative();
        pref = weatherActivity.getSharedPreferences(countyName, Context.MODE_PRIVATE);
        isLoader = pref.getBoolean("info_loaded", false);
       //RecyclerView是可以自动回收的大量数据显示的控件，这里正好用来一系列天气信息的显示
        mRecyclerView = (RecyclerView) weatherInfoView.findViewById(R.id.recycle_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(weatherActivity));
        mRecyclerViewAdapter = new RecyclerViewAdapter(isLoader,countyName,weatherActivity,cityName,mRecyclerView,isNative);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        //下拉刷新
        mSwipeRefreshLayout = (SwipeRefreshLayout) weatherInfoView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.swipe_color_1);
        mSwipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.swipe_color_2);
        mSwipeRefreshLayout.setProgressViewEndTarget(true,70);
        mSwipeRefreshLayout.setOnRefreshListener(this);

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
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 下拉刷新所要进行的操作
     * 即：重新联网获取最新的天气信息
     */
    @Override
    public void onRefresh() {
        mRecyclerViewAdapter.queryWeatherInfo(countyName,true,handler);
//        handler.sendEmptyMessageDelayed(SWIPE_WHAT,4000);
    }
    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public boolean isNative() {
        return isNative;
    }

    public void setNative(boolean local) {
        isNative = local;
    }


}
