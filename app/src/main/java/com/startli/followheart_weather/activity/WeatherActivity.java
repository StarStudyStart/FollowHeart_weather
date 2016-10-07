package com.startli.followheart_weather.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.startli.followheart_weather.R;
import com.startli.followheart_weather.fragment.FragmentControl;
import com.startli.followheart_weather.fragment.WeatherInfoFragment;
import com.startli.followheart_weather.util.HttpCallBackListener;
import com.startli.followheart_weather.util.HttpUtil;
import com.startli.followheart_weather.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class WeatherActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private ViewPager mViewPager;

    /**
     * 位置信息
     */
    private LocationManager locationManager;

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
    private ProgressDialog progressDialog;
    //保存实例
    private boolean haveFragmentState = false;
//    private boolean isFirstRun;
    private SharedPreferences pref;
    private SharedPreferences pref1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        pref = getSharedPreferences("FragmentState", MODE_PRIVATE);
//        pref1 = getSharedPreferences("data", MODE_PRIVATE);
        fragmentManager = getSupportFragmentManager();
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setmDrawerLayout(mDrawerLayout);
        initViewpager();
        haveFragmentState = pref.getBoolean("haveFragmentState", false);
//        isFirstRun = pref1.getBoolean("is_first_run", true);
        if (haveFragmentState) {
            int count = pref.getInt("fragment_count", -999);
            for (int i = 0; i <= count; i++) {
                String countyName = pref.getString("fragment_countyname" + i, "");
                boolean isNative = pref.getBoolean("fragment_isnative" + i, false);
                FragmentControl.addWeatherInfoFragment(countyName, isNative);
//                fragmentList = FragmentControl.getWeatherInfoFragment();
//                fragAdapter.notifyDataSetChanged();
            }
        }
//         else if (isFirstRun) {
//            SharedPreferences.Editor editor = pref1.edit();
//            editor.putBoolean("is_first_run", false);
//            editor.commit();
//            getLocation("first_run");
//        } else if (!haveFragmentState) {
//            // 自动定位
//            getLocation("first_run");
//        }
        //后台自动运行。
//        // 检查当前位置，如有变更则
//        getLocation("check_location");
    }

    /**
     * 本地定位信息因为是异步执行的，所以返回数据时必须弹出进度框（也可以不弹出，在数据返回时直接更新fragment；但是有重复代码）
     * 当前的weatherActivity才会重新调用onResume（）方法
     * 才会更新fragment
     */
    @Override
    protected void onResume() {
        super.onResume();
        // 判断是否需要重新定位
        Intent intent = getIntent();
        boolean isNeedLcation = intent.getBooleanExtra("location_again",false);
        if (isNeedLcation) {
            getLocation("check_location");
        }
        if (!haveFragmentState) {
            // 如果当前城市列表为空，则自动定位。(防止 城市管理中所有城市都删除以后，返回weatherActivity，为空白页)
            getLocation("first_run");
            haveFragmentState = true;
        }
        fragmentList = FragmentControl.getWeatherInfoFragment();
        fragAdapter.notifyDataSetChanged();
    }

    /**
     * 解决singleTask模式下，无法获取Intent传值的问题
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // must store the new intent unless getIntent() will return the old one
        setIntent(intent);
    }

    /**
     * 处理从ChooseAreaActivity中返回的数据
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == ChooseAreaActivity.RESULT_CODE) {
            countyName = data.getStringExtra("county_name");
            FragmentControl.addWeatherInfoFragment(countyName, false);
            fragmentList = FragmentControl.getWeatherInfoFragment();
            fragAdapter.notifyDataSetChanged();
            mViewPager.setCurrentItem(fragmentList.size() - 1);
        }
    }

    /**
     * 保存程序退出时的状态
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = pref.edit();

        //防止已经删除的fragment实例，再次加载
        //每次退出程序时都重新加载当前fragment的状态。
        editor.clear();
        List<Fragment> fragmentState = FragmentControl.getWeatherInfoFragment();
        //取出各个城市名称
        if (fragmentState.size() != 0) {
            int i = 0;
            editor.putBoolean("haveFragmentState", true);
            for (Fragment fragment :
                    fragmentState) {
                WeatherInfoFragment weatherInfoFragment = (WeatherInfoFragment) fragment;
                String countyName = weatherInfoFragment.getCountyName();
                boolean isNative = weatherInfoFragment.isNative();
                editor.putString("fragment_countyname" + i, countyName);
                if (isNative) {
                    editor.putBoolean("fragment_isnative" + i, isNative);
                }
                editor.putInt("fragment_count", i);
                i++;
            }
        } else {
            editor.putBoolean("haveFragmentState", false);
        }

        editor.apply();

        // 清楚所有临时数据，防止重复加载
        FragmentControl.removeAllWeatherInfoFragment();
    }

    /**
     * 初始化导航栏和toolbar控件
     */
    public void initNavigation(DrawerLayout drawerLayout, Toolbar toolbar) {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        toggle.syncState();
        drawerLayout.addDrawerListener(toggle);
    }

    /**
     * 重写Fragment适配器中的方法，FragmentStatePagerAdapter可以防止自动加载，因为他只会加载当前的fragment实例。
     * FragmentPagerAdapter默认加载两边的fragement实例，所以数据改变时，如果处于该位置的itemId没有改变，
     * 默认显示的还是内存中已经加载好的fragment实例
     * 故此加载的还是原来已经加载到内存中的fragmetn实例
     */
    public class FragAdapter extends FragmentStatePagerAdapter {
        private List<android.support.v4.app.Fragment> fms;

        FragAdapter(FragmentManager fm, List<android.support.v4.app.Fragment> fms) {
            super(fm);
            this.fms = fms;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return fms.get(position);
        }

        @Override
        public int getCount() {
            return fms.size();
        }
    }

    /**
     * 获取本地位置的经纬度信息
     */
    public void getLocation(String type) {
        String provider;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            Toast.makeText(this, "NO location provider to use", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        locationManager.requestLocationUpdates(provider, 1000, 1, locationListener);
        while (location == null) {
            location = locationManager.getLastKnownLocation(provider);
        }
        queryLocationFromSever(location, type);
        locationManager.removeUpdates(locationListener);
    }

    /**
     * 多次调用locationListener，获取当前位置信息
     */

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    /**
     * 查根据经纬度获取当前位置的名称
     * 返回数据时新建一个fragment
     */
    private void queryLocationFromSever(Location location, final String type) {
        String address = "http://maps.google.cn/maps/api/geocode/json?latlng=" + location.getLatitude() + "," + location.getLongitude() + "&sensor=false&language=zh-CN";
        //开启进度提示
        if ("first_run".equals(type)) {
            progressDialog = Utility.showProgressDialog("自动定位中...", progressDialog, this);
        }
        HttpUtil.snedHttpRequest(address, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray result = jsonObject.getJSONArray("results");
                    if (result.length() > 0) {
                        JSONObject subObject = result.getJSONObject(0);
                        JSONArray addressArray = subObject.getJSONArray("address_components");
                        JSONObject addressCity = addressArray.getJSONObject(1);
                        String countyName_network = addressCity.getString("short_name");
                        final String countyName = countyName_network.substring(0, countyName_network.length() - 1);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utility.closeProgressDialog(progressDialog);
                                if ("first_run".equals(type)) {
                                    FragmentControl.addWeatherInfoFragment(countyName, true);
                                } else if ("check_location".equals(type)) {
                                    WeatherInfoFragment weatherInfoFragment = (WeatherInfoFragment) FragmentControl.getWeatherInfoFragment().get(0);
                                    String currentLocationName = weatherInfoFragment.getCountyName();
                                    if (!countyName.equals(currentLocationName)) {
                                        FragmentControl.replaceLocation(countyName, true);
                                    }
                                }
//                                // 不能注释掉，因为异步操作的数据，不确定什么时候能够返回
                                fragmentList = FragmentControl.getWeatherInfoFragment();
                                fragAdapter.notifyDataSetChanged();
                            }
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // 定位失败后跳转到城市管理界面。
            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "定位失败，请手动选择关注城市" +
                                "或者重新定位", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(WeatherActivity.this, CityManagerActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
    }

    /**
     * 初始化viewpager，适配器adapter、数据FragmentList()
     */
    public void initViewpager() {
        fragmentList = FragmentControl.getWeatherInfoFragment();
        fragAdapter = new FragAdapter(fragmentManager, fragmentList);
        mViewPager.setAdapter(fragAdapter);
    }


    public DrawerLayout getmDrawerLayout() {
        return mDrawerLayout;
    }

    public void setmDrawerLayout(DrawerLayout mDrawerLayout) {
        this.mDrawerLayout = mDrawerLayout;
    }

}
