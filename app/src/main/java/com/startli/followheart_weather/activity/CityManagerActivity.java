package com.startli.followheart_weather.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.startli.followheart_weather.R;
import com.startli.followheart_weather.fragment.FragmentControl;
import com.startli.followheart_weather.fragment.WeatherInfoFragment;
import com.startli.followheart_weather.util.Constants;
import com.startli.followheart_weather.util.WeatherIconUtils;

import java.util.List;

public class CityManagerActivity extends AppCompatActivity {

    /**
     * 用于网络缓冲
     */
    private ProgressDialog progressDialog;

    /**
     * 顶部toolbar，用于返回。
     */
    private Toolbar toolbar;

    /**
     * 重新定位按钮
     */
    private Button reLocation;

    private GridView cityManagerGridView;
    private List<Fragment> fragmentList;
    private GridViewAdapter gridViewAdapter;

    private static final int REQUEST_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_manager);
        // 加载控件
        cityManagerGridView = (GridView) findViewById(R.id.city_manager_list);
        reLocation = (Button) findViewById(R.id.reLocation);

        fragmentList = FragmentControl.getWeatherInfoFragment();
        gridViewAdapter = new GridViewAdapter();
        cityManagerGridView.setAdapter(gridViewAdapter);
        toolbar = (Toolbar) findViewById(R.id.city_manager_toolbar);
        // 设置toolbar的一系列属性
        toolbar.setTitle("");
//        toolbar.setTitle("城市管理");不能改变字体和颜色 弃用。。。。
        setSupportActionBar(toolbar);
        // 放在setSupportActionBar之后
        toolbar.setNavigationIcon(R.drawable.ic_back_3);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CityManagerActivity.this.finish();
            }
        });

        // 跳转到主界面，重新定位
        reLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CityManagerActivity.this, WeatherActivity.class);
                intent.putExtra("location_again", true);
//                fragmentList.remove(0);
                startActivity(intent);
                CityManagerActivity.this.finish();
            }
        });

    }

    /***
     * 自定义GridView适配器
     */
    class GridViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return fragmentList.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (position == fragmentList.size()) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.city_manager_gridview_last_item, parent, false);
                ImageButton addCityButton = (ImageButton) view.findViewById(R.id.add_concern_city);
                addCityButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(CityManagerActivity.this, ChooseAreaActivity.class);
                        intent.putExtra("is_from_citymanageractivity", true);
                        startActivity(intent);
                    }
                });
                return view;
            }
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.city_manager_gridview_item, parent, false);
                viewHolder.cityName = (TextView) convertView.findViewById(R.id.city_manager_weather_cityname);
                viewHolder.currTemp = (TextView) convertView.findViewById(R.id.city_manager_weather_temp);
                viewHolder.currWeatherIcon = (ImageView) convertView.findViewById(R.id.city_manager_weather_icon);
                viewHolder.deleteButton = (ImageButton) convertView.findViewById(R.id.delete_city);
                viewHolder.nativeIcon = (ImageView) convertView.findViewById(R.id.city_manager_native_icon);
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            WeatherInfoFragment weatherInfoFragment = (WeatherInfoFragment) fragmentList.get(position);
            String countyName = weatherInfoFragment.getCountyName();
            Boolean isNative = weatherInfoFragment.isNative();
            SharedPreferences preferences = getSharedPreferences(countyName, MODE_PRIVATE);
            String type = preferences.getString("weather_Desp", "");
            int typeCode = Constants.getIntType(type);
            if (isNative) {
                viewHolder.nativeIcon.setVisibility(View.VISIBLE);
                viewHolder.deleteButton.setVisibility(View.GONE);
            }
            viewHolder.currWeatherIcon.setBackgroundResource(WeatherIconUtils.getWeatherIcon(typeCode));
            viewHolder.cityName.setText(countyName);
            viewHolder.currTemp.setText(preferences.getString("current_temp", "") + "°");
            viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentControl.removeWeahterInfoFragment(position);
                    fragmentList = FragmentControl.getWeatherInfoFragment();
                    gridViewAdapter.notifyDataSetChanged();
                }
            });
            return convertView;
        }

        final class ViewHolder {
            TextView currTemp;
            TextView cityName;
            ImageView currWeatherIcon;
            ImageView nativeIcon;
            ImageButton deleteButton;

        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK){
//            String countyName = data.getStringExtra("county_name_city_manager");
//            FragmentControl.addWeatherInfoFragment(countyName,false);
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        fragmentList = FragmentControl.getWeatherInfoFragment();
        //  重新绑定适配器，防止数据更新后，gridview不能及时的刷新
//        cityManagerGridView.setAdapter(gridViewAdapter);
        gridViewAdapter.notifyDataSetChanged();
    }
}
