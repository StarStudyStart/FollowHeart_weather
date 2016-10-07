package com.startli.followheart_weather.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.startli.followheart_weather.R;

public class AboutActivity extends AppCompatActivity {
    private TextView aboutText;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        aboutText = (TextView) findViewById(R.id.about_text);
        backButton = (ImageButton) findViewById(R.id.about_back_button);
        // 可滑动的TexView
        aboutText.setMovementMethod(new ScrollingMovementMethod());
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
        String about = getResources().getString(R.string.about);
        aboutText.setText(about);
    }
}
