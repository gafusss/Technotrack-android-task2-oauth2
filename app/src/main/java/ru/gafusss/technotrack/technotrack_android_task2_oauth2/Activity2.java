package ru.gafusss.technotrack.technotrack_android_task2_oauth2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Activity2 extends AppCompatActivity {
    private static final String TAG = "Activity2";

    String token;
    String valid_through;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: " + (savedInstanceState == null));
        setContentView(R.layout.activity_2);

        MyApplication myApplication = (MyApplication) getApplication();

        token = myApplication.token;
        valid_through = myApplication.valid_through;

        if (token == null || valid_through == null) {
            TextView textView = (TextView) findViewById(R.id.textView2);
            textView.setText(R.string.textview_token_failure);
            Button button = (Button) findViewById(R.id.button);
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(new ButtonClickListener(this));
            myApplication.web_view_finished = false;
        } else {
            TextView textView = (TextView) findViewById(R.id.textView2);
            textView.setText(String.format(getString(R.string.textview_token_success), valid_through));
        }
    }

    private class ButtonClickListener implements View.OnClickListener {
        Context context;

        ButtonClickListener(Context context) {
            this.context = context;
        }

        public void onClick(View v) {
            Intent intent = new Intent(context, WebViewActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
