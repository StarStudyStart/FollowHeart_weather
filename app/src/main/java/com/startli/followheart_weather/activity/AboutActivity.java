package com.startli.followheart_weather.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.startli.followheart_weather.R;

public class AboutActivity extends AppCompatActivity {
    private TextView aboutText1;
    private TextView aboutText2;
    private TextView aboutText3;
    private TextView aboutText4;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        aboutText1 = (TextView) findViewById(R.id.about_text1);
        aboutText2 = (TextView) findViewById(R.id.about_text2);
        aboutText3 = (TextView) findViewById(R.id.about_text3);
        aboutText4 = (TextView) findViewById(R.id.about_text4);
        backButton = (ImageButton) findViewById(R.id.about_back_button);
        // 可滑动的TexView
        aboutText1.setMovementMethod(new ScrollingMovementMethod());
        aboutText2.setMovementMethod(new ScrollingMovementMethod());
        aboutText3.setMovementMethod(new ScrollingMovementMethod());
        aboutText4.setMovementMethod(new ScrollingMovementMethod());
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutActivity.this.finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String about1 = getResources().getString(R.string.about1);
        String about2 = getResources().getString(R.string.about2);
        String about3 = getResources().getString(R.string.about3);
        String about4 = getResources().getString(R.string.about4);
        aboutText1.setText(about1);
        aboutText2.setText(about2);
        aboutText3.setText(about3);
        aboutText4.setText(about4);
    }
}
