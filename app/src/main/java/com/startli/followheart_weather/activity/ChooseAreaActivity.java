package com.startli.followheart_weather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.startli.followheart_weather.R;
import com.startli.followheart_weather.fragment.FragmentControl;
import com.startli.followheart_weather.model.City;
import com.startli.followheart_weather.model.CoolWeatherDB;
import com.startli.followheart_weather.model.County;
import com.startli.followheart_weather.model.Province;
import com.startli.followheart_weather.util.HttpCallBackListener;
import com.startli.followheart_weather.util.HttpUtil;
import com.startli.followheart_weather.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends Activity implements View.OnClickListener {
    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;
    public static final int RESULT_CODE = 101;

    private ProgressDialog progressDialog;
    private CoolWeatherDB coolWeatherDB;
    private TextView title_text;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private List<String> dataList = new ArrayList<String>();
    /**
     * 省级列表
     */
    private List<Province> provinceList;
    /**
     * 市级列表
     */
    private List<City> cityList;
    /**
     * 县级列表
     */
    private List<County> countyList;
    /**
     * 选中的省份
     */
    private Province selectedProvince;
    /**
     * 选中的市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;

    /**
     * 是否从weatheractivity跳转过来
     */
    private boolean isFromWeatherActivity;

    /**
     * 返回按钮
     */
    private ImageButton mBackButton;

    /**
     * 当前页面主题、标志
     */
    private TextView mCurrentFlag;

    private boolean isFromCityManagerActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
        isFromCityManagerActivity = getIntent().getBooleanExtra("is_from_citymanageractivity", false);
        // 取消標題欄
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        // 加载控件
        title_text = (TextView) findViewById(R.id.title_text);
        listView = (ListView) findViewById(R.id.listView);
        mBackButton = (ImageButton) findViewById(R.id.choose_area_back_button);
        mCurrentFlag = (TextView) findViewById(R.id.choose_area_text_flag);
        mBackButton.setOnClickListener(this);

        arrayAdapter = new ArrayAdapter<String>(this, R.layout.city_listview_item, dataList);
        listView.setAdapter(arrayAdapter);
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String countyCode = countyList.get(position).getCountyCode();
                    String countyName = countyList.get(position).getCountyName();
                    if (isFromWeatherActivity) {
                        Intent intent = new Intent();
                        intent.putExtra("county_name", countyName);
                        intent.putExtra("county_code", countyCode);
                        setResult(RESULT_CODE, intent);
                    } else if (isFromCityManagerActivity) {
                        Intent intent1 = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                        intent1.putExtra("county_name_city_manager", countyName);
                        intent1.putExtra("county_code_city_manager", countyCode);
                        FragmentControl.addWeatherInfoFragment(countyName, false);
                        startActivity(intent1);
                    }
                    finish();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国所有的省，优先从数据库中查询，如果没有再到服务器上查询
     */
    private void queryProvinces() {
        provinceList = coolWeatherDB.loadProvince();
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            title_text.setText("中国");
            mCurrentFlag.setText("省");
            currentLevel = LEVEL_PROVINCE;
        } else {
            // 从服务器中读取数据
            queryFromServer(null, "Province");
        }
    }

    /**
     * 查询某省下面所有的市，优先从数据库中查询，如果没有再到服务器上查询
     */
    private void queryCities() {
        cityList = coolWeatherDB.loadCity(selectedProvince.getId());
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            arrayAdapter.notifyDataSetChanged();
            title_text.setText(selectedProvince.getProvinceName());
            mCurrentFlag.setText("市");
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            // 從服務器中獲取數據
            queryFromServer(selectedProvince.getProvinceCode(), "City");
        }

    }

    /**
     * 查询某市下面所有的县，优先从数据库中查询，如果没有再到服务器上查询
     */

    private void queryCounties() {
        countyList = coolWeatherDB.loadCounty(selectedCity.getId());
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            arrayAdapter.notifyDataSetChanged();
            title_text.setText(selectedCity.getCityName());
            mCurrentFlag.setText("县");
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            queryFromServer(selectedCity.getCityCode(), "County");

        }
    }

    /**
     * 從服務器中直接加載各個省市縣的信息
     */
    private void queryFromServer(final String code, final String type) {
        String address;
        if (code != null) {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        // 开启进度对话框
        progressDialog = Utility.showProgressDialog("加载城市中...", progressDialog, this);
        HttpUtil.snedHttpRequest(address, new HttpCallBackListener() {

            @Override
            public void onFinish(String response) {
                boolean result = false;
                if ("Province".equals(type)) {
                    result = Utility.handleProvincesResponse(response, coolWeatherDB);
                } else if ("City".equals(type)) {
                    result = Utility.handleCitiesResponse(response, coolWeatherDB, selectedProvince.getId());
                } else if ("County".equals(type)) {
                    result = Utility.handleCountiesResponse(response, coolWeatherDB, selectedCity.getId());
                }
                if (result) {
                    // 回到主线程中处理数据
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // 关闭进度对话框
                            Utility.closeProgressDialog(progressDialog);
                            if ("Province".equals(type)) {
                                // 如果返回结果为零,易造成死循环
                                queryProvinces();
                            } else if ("City".equals(type)) {
                                queryCities();
                            } else if ("County".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                // TODO Auto-generated method stub
                // 回到主线程处理逻辑
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Utility.closeProgressDialog(progressDialog);
                        Toast.makeText(ChooseAreaActivity.this, "加载失败...", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }


    /**
     * 捕获back键，根据当前的级别来判断，此时应该返回市列表、省列表、还是直接退出
     */
    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY) {
            queryCities();
        } else if (currentLevel == LEVEL_CITY) {
            queryProvinces();
        } else {
            if (isFromWeatherActivity) {
                Intent intent = new Intent(this, WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.choose_area_back_button:
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                } else {
                    if (isFromWeatherActivity) {
                        Intent intent = new Intent(this, WeatherActivity.class);
                        startActivity(intent);
                    }
                    finish();
                }
                break;
        }
    }
}
