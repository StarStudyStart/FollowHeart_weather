package com.startli.followheart_weather.activity;

import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.startli.followheart_weather.R;

public class SplashActivity extends AppCompatActivity {
    private static final int ACTIVITY_GO_TO_NEXT = 0;
    private static final long DELAYED_TIME = 1000;
    private TextView introduction;
    private TextView by;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        introduction = (TextView) findViewById(R.id.intruduction);
        by = (TextView) findViewById(R.id.by);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnimationSet animationSet = (AnimationSet) AnimationUtils.loadAnimation(this,R.anim.rotate_in);
        introduction.setAnimation(animationSet);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                by.setVisibility(View.VISIBLE);
                handler.sendEmptyMessageDelayed(ACTIVITY_GO_TO_NEXT, DELAYED_TIME);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ACTIVITY_GO_TO_NEXT:
                    Intent intent = new Intent(SplashActivity.this,WeatherActivity.class);
                    startActivity(intent);
                    SplashActivity.this.finish();
                    break;
                default:
                    break;
            }
        }
    };
}
