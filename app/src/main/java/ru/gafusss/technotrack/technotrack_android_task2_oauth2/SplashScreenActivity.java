package ru.gafusss.technotrack.technotrack_android_task2_oauth2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
    }

    @Override
    protected void onPause() {
        super.onPause();

        MyApplication myApplication = (MyApplication) getApplication();
        if (myApplication.splash_screen_finished) {
            finish();
        }
    }
}
