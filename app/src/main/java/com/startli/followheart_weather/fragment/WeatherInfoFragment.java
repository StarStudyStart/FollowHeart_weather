package com.startli.followheart_weather.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.startli.followheart_weather.R;
import com.startli.followheart_weather.activity.AboutActivity;
import com.startli.followheart_weather.activity.ChooseAreaActivity;
import com.startli.followheart_weather.activity.CityManagerActivity;
import com.startli.followheart_weather.activity.WeatherActivity;

/**
 * Created by lyb on 2016-09-04.
 */
public class WeatherInfoFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private WeatherActivity weatherActivity;
    public static final int REQUEST_CODE = 999;

    /**
     * 本地图标
     */
    private ImageView locationView;

    /**
     * 显示城市名称
     */
    private TextView cityName;

    /**
     * 用于切换城市
     */
    private Button mSwitchCity;

    /**
     * 用于接收地名
     */
    private String countyName;

    /**
     * 是否问家中是否已经加载天气信息
     */
    private boolean isLoader;

    /**
     * 是否是本地地名
     */
    private boolean isNative = false;

    private Toolbar mToolBar;
    private SharedPreferences pref;

    //一系列天气信息的显示
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mRecyclerViewAdapter;

    /**
     * 实现下拉刷新
     */
    public static final int SWIPE_SUCCEED_WHAT = 233;
    public static final int SWIPE_FAILURE_WHAT = -1;
    //  判断是否已经发送了取消刷新状态的Message，如果为true则表明已经取消了刷新状态
    private boolean flag_isSendMessage = false;

    /**
     * 下拉刷新
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * 根据时差判断是否需要更新
     */
    private boolean isNeedRefresh;

    /**
     * 设置导航栏的监听事件
     */
    private NavigationView mNavigationView;
    private DrawerLayout drawerLayout;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == SWIPE_SUCCEED_WHAT) {
                flag_isSendMessage = true;
                Toast.makeText(weatherActivity, "天气刷新成功", Toast.LENGTH_SHORT).show();
            } else if (msg.what == SWIPE_FAILURE_WHAT)
                if (!flag_isSendMessage) {
                    Toast.makeText(weatherActivity, "天气刷新失败", Toast.LENGTH_SHORT).show();
                }
            //取消刷新状态
            mSwipeRefreshLayout.setRefreshing(false);
            mRecyclerViewAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View weatherInfoView = inflater.inflate(R.layout.frament_item_weather_info, container, false);
        // 寻找控件
        mToolBar = (Toolbar) weatherInfoView.findViewById(R.id.toolbar);
        cityName = (TextView) weatherInfoView.findViewById(R.id.city_name_456);
        mSwitchCity = (Button) weatherInfoView.findViewById(R.id.switch_city);
        locationView = (ImageView) weatherInfoView.findViewById(R.id.native_icon);

        //获取与fragment关联的activity
        weatherActivity = (WeatherActivity) getActivity();
        // 初始化toolbar
        drawerLayout = weatherActivity.getmDrawerLayout();
        // ？每次创建fragment时，都会重新创讲一个关联
        weatherActivity.initNavigation(drawerLayout, mToolBar);
        countyName = getCountyName();
        isNative = isNative();
        //设置本地图标
        if (isNative) {
            locationView.setVisibility(View.VISIBLE);
        }
        // 天气数据是否已经加载过
        pref = weatherActivity.getSharedPreferences(countyName, Context.MODE_PRIVATE);
        isLoader = pref.getBoolean("info_loaded", false);

        //判断时差是否超过两个小时，首先要获取pref对象，所以要放在pref对象获取之后
        isNeedRefresh = getDiffer();

        //RecyclerView是可以自动回收的大量数据显示的控件，这里正好用来一系列天气信息的显示
        mRecyclerView = (RecyclerView) weatherInfoView.findViewById(R.id.recycle_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(weatherActivity));
        mRecyclerViewAdapter = new RecyclerViewAdapter(isLoader, countyName, weatherActivity, cityName, mRecyclerView, isNeedRefresh);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        //下拉刷新
        mSwipeRefreshLayout = (SwipeRefreshLayout) weatherInfoView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.swipe_color_1);
        mSwipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.swipe_color_2);
        mSwipeRefreshLayout.setProgressViewEndTarget(true, 70);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // 添加城市按钮的监听事件
        mSwitchCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(weatherActivity, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        mNavigationView = (NavigationView) weatherActivity.findViewById(R.id.navigation_view);
        // navigationView的menu监听事件
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectedMenuItem(item);
                return false;
            }
        });
        return weatherInfoView;
    }

    /**
     * 下拉刷新所要进行的操作
     * 即：重新联网获取最新的天气信息
     */
    @Override
    public void onRefresh() {
        mRecyclerViewAdapter.queryWeatherInfo(countyName, true, handler);
        // 因为网络访问超时为8秒，所以最迟8秒后取消刷新状态，最早看数据什么时候返回
        handler.sendEmptyMessageDelayed(SWIPE_FAILURE_WHAT, 8000);
    }

    /**
     * menuItem的监听事件
     * navigation_item_1 城市管理
     * navigation_item_2 生活资讯
     */
    private void selectedMenuItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.navigation_item1:
                Intent intent = new Intent(weatherActivity, CityManagerActivity.class);
                startActivity(intent);
                break;
            case R.id.exit_app:
                weatherActivity.finish();
                break;
            case R.id.navigation_sub_item3:
                Intent intent2 = new Intent(weatherActivity, AboutActivity.class);
                startActivity(intent2);
                break;
            default:
                break;
        }
        drawerLayout.closeDrawers();
    }

    /**
     * 根据时间差的大小
     * 判断是否需要更新数据
     */
    private boolean getDiffer() {
        long updateTime = pref.getLong("update_time", -1);
        long currTime = System.currentTimeMillis();
        long missTiming = currTime - updateTime;
        int timeDifferHour = (int) (missTiming / (60 * 60 * 1000));
        if (updateTime != -1) {
            if (timeDifferHour >= 2) {
                return true;
            }
        }
        return false;
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
