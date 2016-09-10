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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
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
    private RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View weatherInfoView = inflater.inflate(R.layout.frament_item_weather_info, container, false);

        // 寻找控件
        weatherInfoLayout = (LinearLayout) weatherInfoView.findViewById(R.id.weather_info_layout);
//        weather_layout = (RelativeLayout) weatherInfoView.findViewById(R.id.weather_layout);

//      publishText = (TextView) weatherInfoView.findViewById(R.id.publish_text);


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

        mRecyclerView = (RecyclerView) weatherInfoView.findViewById(R.id.recycle_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(weatherActivity));
        mRecyclerView.setAdapter(new RecyclerViewAdapter(isLoader,countyName,weatherActivity,cityName,mRecyclerView));

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




    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

   /* public class RecyclerViewAdapter1 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final static int ITEM_COUNT = 5;
        private final static int TYPE_HEADER = 0;
        private final static int TYPE_ITEM = 1;

        @Override

        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d("tag","onCreateViewHolder");
            if (viewType == TYPE_HEADER) {
                return new WeatherHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_weather_current, parent, false));
            }
            return new WeatherItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false));
        }

        *//**
//         * 这个方法中可以设置控件的内容等等，但是前提是控件的内容必须能读取到，如果在onCreateV方法中开启新线程查询天气天气信息
//         * 等到onBnidViewHolder方法执行时可能，天气信息还没有返回，
//         * 所以必须在该方法中，开启线程查询天气信息，这样保证天气信息一定会在更新后显示在控件上
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Log.d("tag","onBindViewHolder");
            //判断语句
            if (!isLoader) {
                if (countyName == null) {
                    getLocation();
                } else {
                    queryLocationWeatherInfo(countyName);
                }
            }else{
                showWeather();
            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            Log.d("tag","onViewAttachedToWindow");
        }

        @Override
        public int getItemCount() {
            return ITEM_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HEADER;
            } else {
                return TYPE_ITEM;
            }
        }

        class WeatherHeaderViewHolder extends RecyclerView.ViewHolder {

            public WeatherHeaderViewHolder(View itemView) {
                super(itemView);

            }
        }

        class WeatherItemViewHolder extends RecyclerView.ViewHolder {

            public WeatherItemViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
*/
    @Override
    public void onResume() {
        super.onResume();
    }
}
