package com.happylrd.magicweather.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.happylrd.magicweather.R;
import com.happylrd.magicweather.util.HttpCallbackListener;
import com.happylrd.magicweather.util.HttpUtil;
import com.happylrd.magicweather.util.Utility;

public class WeatherActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout mWeatherInfoLayout;
    private TextView mCityNameText;
    private TextView mPublishText;
    private TextView mWeatherDespText;
    private TextView mTemp1Text;
    private TextView mTemp2Text;
    private TextView mCurrentDateText;
    private Button mSwitchCity;
    private Button mRefreshWeather;

    public static Intent newIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, WeatherActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);

        mWeatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        mCityNameText = (TextView) findViewById(R.id.city_name);
        mPublishText = (TextView) findViewById(R.id.publish_text);
        mWeatherDespText = (TextView) findViewById(R.id.weather_desp);
        mTemp1Text = (TextView) findViewById(R.id.temp1);
        mTemp2Text = (TextView) findViewById(R.id.temp2);
        mCurrentDateText = (TextView) findViewById(R.id.current_date);
        mSwitchCity = (Button) findViewById(R.id.switch_city);
        mRefreshWeather = (Button) findViewById(R.id.refresh_weather);

        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) {
            mPublishText.setText("同步中...");
            mWeatherInfoLayout.setVisibility(View.INVISIBLE);
            mCityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        } else {
            showWeather();
        }

        mSwitchCity.setOnClickListener(this);
        mRefreshWeather.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_city:
                Intent intent = ChooseAreaActivity.newIntent(this);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                mPublishText.setText("同步中...");
                SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(this);
                String weatherCode = preferences.getString("weather_code", "");
                if (!TextUtils.isEmpty(weatherCode)) {
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:
                break;
        }
    }

    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address, "countyCode");
    }

    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }

    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPublishText.setText("同步失败");
                    }
                });
            }
        });
    }

    private void showWeather() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        mCityNameText.setText(preferences.getString("city_name", ""));
        mTemp1Text.setText(preferences.getString("temp1", ""));
        mTemp2Text.setText(preferences.getString("temp2", ""));
        mWeatherDespText.setText(preferences.getString("weather_desp", ""));
        mPublishText.setText("今天" + preferences.getString("publish_time", "") + "发布");
        mCurrentDateText.setText(preferences.getString("current_date", ""));
        mWeatherInfoLayout.setVisibility(View.VISIBLE);
        mCityNameText.setVisibility(View.VISIBLE);
    }
}
