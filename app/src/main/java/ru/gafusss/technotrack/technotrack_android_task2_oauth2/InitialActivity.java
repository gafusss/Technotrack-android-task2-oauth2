package ru.gafusss.technotrack.technotrack_android_task2_oauth2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class InitialActivity extends AppCompatActivity {
    private static final String TAG = "InitialActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication myApplication = (MyApplication) getApplication();
        String token = myApplication.token;
        if (myApplication.is_online) {
            if (token == null) {
                Log.d(TAG, "onCreate: Starting webview activity, token is null");
                myApplication.web_view_finished = false;
                Intent intent = new Intent(this, WebViewActivity.class);
                startActivity(intent);
            } else {
                Log.d(TAG, "onCreate: gonna validate token " + token);
                myApplication.validateToken();
                Log.d(TAG, "onCreate: starting splashscreen activity");
                myApplication.splash_screen_finished = false;
                Intent intent = new Intent(this, SplashScreenActivity.class);
                startActivity(intent);
            }
            finish();
        } else {
            setContentView(R.layout.offline);
        }
    }


    @Override
    public void onBackPressed()
    {
        finish();
        System.exit(0);
    }
}
