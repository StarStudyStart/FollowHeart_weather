package com.startli.followheart_weather.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
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
import com.startli.followheart_weather.util.HttpCallBackListener;
import com.startli.followheart_weather.util.HttpUtil;
import com.startli.followheart_weather.util.Utility;
import com.startli.followheart_weather.util.WeatherIconUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class CityManagerActivity extends AppCompatActivity {

    /**
     * 用于网络缓冲
     */
    private ProgressDialog progressDialog;

    private GridView cityManagerGridView;
    private List<Fragment> fragmentList;
    private GridViewAdapter gridViewAdapter;

    private static final int REQUEST_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_manager);
        cityManagerGridView = (GridView) findViewById(R.id.city_manager_list);

        fragmentList = FragmentControl.getWeatherInfoFragment();
        gridViewAdapter = new GridViewAdapter();
        cityManagerGridView.setAdapter(gridViewAdapter);
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
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gridview_last_item, parent, false);
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
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gridview_item, parent, false);
                viewHolder.cityName = (TextView) convertView.findViewById(R.id.city_manager_weather_cityname);
                viewHolder.currTemp = (TextView) convertView.findViewById(R.id.city_manager_weather_temp);
                viewHolder.currWeatherIcon = (ImageView) convertView.findViewById(R.id.city_manager_weather_icon);
                viewHolder.deleteButton = (ImageButton) convertView.findViewById(R.id.delete_city);
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            WeatherInfoFragment weatherInfoFragment = (WeatherInfoFragment) fragmentList.get(position);
            String countyName = weatherInfoFragment.getCountyName();
            SharedPreferences preferences = getSharedPreferences(countyName, MODE_PRIVATE);
            String type = preferences.getString("weather_Desp", "");
            int typeCode = Constants.getIntType(type);
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
        cityManagerGridView.setAdapter(gridViewAdapter);
        gridViewAdapter.notifyDataSetChanged();
    }
}
