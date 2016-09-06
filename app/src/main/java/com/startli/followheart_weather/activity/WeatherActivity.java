package com.startli.followheart_weather.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.startli.followheart_weather.R;
import com.startli.followheart_weather.util.FragmentControl;

import java.util.List;


public class WeatherActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private ViewPager mViewPager;
    private boolean isFromChoose;


    /**
     * 接收县市的名称
     */
    private String countyName = "";

    /**
     * toolbar中的城市名称
     */
    private TextView cityName;

    private List<String> cityNameList;
    private List<android.support.v4.app.Fragment> fragmentList;
    public FragAdapter fragAdapter;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);
        fragmentManager = getSupportFragmentManager();
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        setmDrawerLayout(mDrawerLayout);

        fragmentList = FragmentControl.getWeatherInfoFragment();
        if (fragmentList.size() == 0) {
            FragmentControl.addWeatherInfoFragment(null);
            fragmentList = FragmentControl.getWeatherInfoFragment();
        }
        fragAdapter = new FragAdapter(fragmentManager, fragmentList);
        mViewPager.setAdapter(fragAdapter);
        mViewPager.setOffscreenPageLimit(0);
    }


    /**
     * 处理从ChooseAreaActivity中返回的数据
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            countyName = data.getStringExtra("county_name");
            FragmentControl.addCityName(countyName);

            FragmentControl.addWeatherInfoFragment(countyName);
            fragmentList = FragmentControl.getWeatherInfoFragment();
            fragAdapter.notifyDataSetChanged();
            mViewPager.setCurrentItem(fragmentList.size());


        }

    }


    /**
     * 初始化导航栏和toolbar控件
     */
    public void initNavigation(DrawerLayout drawerLayout, Toolbar toolbar) {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close);
        toggle.syncState();
        drawerLayout.addDrawerListener(toggle);
    }

    /**
     * 保存程序退出时的状态
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        fragmentList = fragmentManager.getFragments();
        //取出各个城市名称
        FragmentControl.cityNameList = FragmentControl.getCityNameList();
//        FragmentControl.removeAllWeatherInfoFragment();
//        fragAdapter.notifyDataSetChanged();
//        SharedPreferences.Editor editor = getSharedPreferences("framentState", MODE_PRIVATE).edit();
//        int i =0;
//        for(String cityName: cityNameList){
//
//            editor.putString("frament_state"+i, cityName);
//            i++;
//        }
//        editor.commit();


    }

    /**
     * 重写Fragment适配器中的方法
     */
    public class FragAdapter extends FragmentPagerAdapter {

        private List<android.support.v4.app.Fragment> mfs;

        public FragAdapter(FragmentManager fm, List<android.support.v4.app.Fragment> fms) {
            super(fm);
            mfs = fms;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return mfs.get(position);
        }

        @Override
        public int getCount() {
            return mfs.size();
        }
    }

    public DrawerLayout getmDrawerLayout() {
        return mDrawerLayout;
    }

    public void setmDrawerLayout(DrawerLayout mDrawerLayout) {
        this.mDrawerLayout = mDrawerLayout;
    }

}
