package com.startli.followheart_weather.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.startli.followheart_weather.R;
import com.startli.followheart_weather.fragment.FragmentControl;
import com.startli.followheart_weather.fragment.WeatherInfoFragment;
import com.startli.followheart_weather.util.HttpCallBackListener;
import com.startli.followheart_weather.util.HttpUtil;
import com.startli.followheart_weather.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class WeatherActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private ViewPager mViewPager;

    /**
     * 位置信息
     */
    //声明AMapLocationClient类对象
    private AMapLocationClient mLocationClient = null;
    // 声明定位选项
    private AMapLocationClientOption mLocationOption = null;

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
    public boolean haveFragmentState = false;
    protected boolean isFirstRun = false;
    private SharedPreferences pref;

    boolean isNeedLcation = false;
    boolean isFromChooseCity = false;
    boolean isLocation = false;
//    private SharedPreferences pref1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);
        pref = getSharedPreferences("FragmentState", MODE_PRIVATE);
        fragmentManager = getSupportFragmentManager();
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setmDrawerLayout(mDrawerLayout);
        initViewpager();
        haveFragmentState = pref.getBoolean("haveFragmentState", false);

        //  不能防止到start中，不然每次都要重新加载，耗费资源
        if (haveFragmentState) {
            int count = pref.getInt("fragment_count", -999);
            for (int i = 0; i <= count; i++) {
                String countyName = pref.getString("fragment_countyname" + i, "");
                boolean isNative = pref.getBoolean("fragment_isnative" + i, false);
                FragmentControl.addWeatherInfoFragment(countyName, isNative);
            }
        } else if (FragmentControl.getWeatherInfoFragment().isEmpty()) {
            // 如果当前城市列表为空，则自动定位。
            initMapLocation("first_run");
            haveFragmentState = true;
        }
        //  每次启动重新定位
         isFirstRun = true;
    }

    /*
    *  在任何fragment实例中获取的isNeedLcation都为True,因为你总能获取到这个值
    *  重新定位一次后，isNeedLcation变量就永远变为True.在OnResume（）就不停的重新定位
    *  */
    @Override
    protected void onStart() {
        super.onStart();
        Log.i("testcycle", "onStart()");

        // 判断是否需要重新定位
        Intent intent = getIntent();
        if (!isFromChooseCity) {
            Log.i("testisNeedLcation:", "" + isNeedLcation + "  isFromChooseCity:" + isFromChooseCity);
            isNeedLcation = intent.getBooleanExtra("location_again", false);
        } else {
            // 防止isFromChooseCity永远变为True,
            isFromChooseCity = false;
        }
    }

    /**
     * 本地定位信息因为是异步执行的，所以返回数据时必须弹出进度框（也可以不弹出，在数据返回时直接更新fragment；但是有重复代码）
     * 当前的weatherActivity才会重新调用onResume（）方法
     * 才会更新fragment
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.i("testcycle", "onResume()");
        Log.i("test Onreume isNeedLcation:", "" + isNeedLcation + "  isFromChooseCity:" + isFromChooseCity);
        if (isNeedLcation || isFirstRun) {
            initMapLocation("check_location");
            isNeedLcation = false;
            isFirstRun = false;
        }
        fragmentList = FragmentControl.getWeatherInfoFragment();
        fragAdapter.notifyDataSetChanged();

//  必须与isNeedLcation 分开不确定定位信息什么时候返回
        if (!isLocation) {
            // 判断是否重新定位，如果重新定位，自动切换至第0个gragment
            mViewPager.setCurrentItem(fragmentList.size() - 1);
        } else {
            mViewPager.setCurrentItem(0);
            isLocation = false;
        }
        Log.i("frag_list", "" + fragmentList);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFirstRun = false;
//        isNeedLcation = false;
    }

    /**
     * 处理从ChooseAreaActivity中返回的数据
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("cycle", "onActivityResult()");
        if (resultCode == ChooseAreaActivity.RESULT_CODE) {
            countyName = data.getStringExtra("county_name");
            FragmentControl.addWeatherInfoFragment(countyName, false);
            fragmentList = FragmentControl.getWeatherInfoFragment();
            fragAdapter.notifyDataSetChanged();
            Log.i("frag_list", "" + fragmentList);
            Log.i("frag_list", "" + countyName);
            isFromChooseCity = true;

        }
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
            for (Fragment fragment : fragmentState) {
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

    /**
     * 初始化option
     */
    public void initMapLocation(String type) {
        // 初始化对象
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位监听
        AMapLocationListener mapLocationListener = new LocationListener(type);
        mLocationClient.setLocationListener(mapLocationListener);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置返回地址信息，默认为true
        mLocationOption.setNeedAddress(true);
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置单次定位
        // mLocationOption.setInterval(60000);
        mLocationOption.setOnceLocationLatest(true);
        //设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        //启动定位
        mLocationClient.startLocation();
        progressDialog = Utility.showProgressDialog("自动定位中...", progressDialog, this);
    }

    /**
     * 高德地图接口定位监听及回调参数
     */
    private class LocationListener implements AMapLocationListener {
        private String type;

        private LocationListener(String type) {
            this.type = type;

        }

        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    //定位成功回调信息，设置相关消息
                    amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                    amapLocation.getLatitude();//获取纬度
                    amapLocation.getLongitude();//获取经度
                    amapLocation.getAccuracy();//获取精度信息
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date(amapLocation.getTime());
                    df.format(date);//定位时间
                    amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                    amapLocation.getCountry();//国家信息
                    amapLocation.getProvince();//省信息
                    String cityName = amapLocation.getCity();//城市信息
                    Log.i("CityName", cityName);
                    amapLocation.getAdCode();//地区编码
                    countyName = amapLocation.getDistrict();//城区信息
                    if (Utility.isContainChinese(countyName)) {
                        Utility.closeProgressDialog(progressDialog);
                        mLocationClient.stopLocation();
                        countyName = countyName.substring(0, countyName.length() - 1);
                        if ("first_run".equals(type)) {
                            FragmentControl.addWeatherInfoFragment(countyName, true);
                        } else if ("check_location".equals(type)) {
                            Log.i("CityName", "check_location");
//                            WeatherInfoFragment weatherInfoFragment = (WeatherInfoFragment) FragmentControl.getWeatherInfoFragment().get(0);
//                            String currentLocationName = weatherInfoFragment.getCountyName();
                            FragmentControl.replaceLocation(countyName, true);
                        }
                    } else {
                        countyName = "余杭";
                        Utility.closeProgressDialog(progressDialog);
                        mLocationClient.stopLocation();
                        if ("check_location".equals(type)) {
                            Log.i("CityName", "check_location");
//                            WeatherInfoFragment weatherInfoFragment = (WeatherInfoFragment) FragmentControl.getWeatherInfoFragment().get(0);
//                            String currentLocationName = weatherInfoFragment.getCountyName();
                            FragmentControl.replaceLocation("余杭", true);
                        } else {
                            FragmentControl.addWeatherInfoFragment(countyName, true);
                        }
                    }
                    isLocation = true;
                    onResume();
                    Log.i("CoutyName", countyName);
                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息。
                    Log.e("AmapError", "location Error, ErrCode:" + amapLocation.getErrorCode() + ", errInfo:" + amapLocation.getErrorInfo());
                }
            }
        }
    }


    /**
     * 查根据经纬度获取当前位置的名称
     * 返回数据时新建一个fragment
     */
    private void queryLocationFromSever(Location location, final String type) {
        String address = "http://maps.google.cn/maps/api/geocode/json?latlng=" + location.getLatitude() + "," + location.getLongitude() + "&sensor=false&language=zh-CN";
        //开启进度提示
        progressDialog = Utility.showProgressDialog("自动定位中...", progressDialog, this);
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
                        Utility.closeProgressDialog(progressDialog);
                        Toast.makeText(WeatherActivity.this, "定位失败，请手动选择关注城市" + "或者重新定位", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(WeatherActivity.this, CityManagerActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
    }
}
