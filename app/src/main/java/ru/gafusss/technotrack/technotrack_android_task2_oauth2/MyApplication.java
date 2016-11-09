package ru.gafusss.technotrack.technotrack_android_task2_oauth2;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.util.Log;

import java.net.URL;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import static ru.gafusss.technotrack.technotrack_android_task2_oauth2.Utils.STATUS_OK;
import static ru.gafusss.technotrack.technotrack_android_task2_oauth2.Utils.getResponseString;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    public String token = null;
    public String valid_through = null;
    public boolean web_view_finished = false;
    public boolean splash_screen_finished = false;
    public boolean is_online;

    SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        is_online = isOnline();
        sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        token = sharedPreferences.getString(getString(R.string.preference_token_key), null);
        valid_through = sharedPreferences.getString(getString(R.string.preference_valid_through_key), null);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public void onAccessTokenGetterError(WebViewActivity.Token token) {
        Log.d(TAG, "onAccessTokenGetterError: " + token.access_token);
        web_view_finished = true;
    }

    public void onAccessTokenGetterOk(WebViewActivity.Token token) {
        Log.d(TAG, "onAccessTokenGetterOk: " + token.toString());
        this.token = token.access_token;

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(java.util.Calendar.SECOND, token.expires_in);

        this.valid_through = DateFormat.format(getString(R.string.valid_through_format), calendar.getTime()).toString();

        Log.d(TAG, "onAccessTokenGetterOk: token: " + this.token);
        Log.d(TAG, "onAccessTokenGetterOk: valid_through: " + this.valid_through);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.preference_token_key), this.token);
        editor.putString(getString(R.string.preference_valid_through_key), valid_through);
        editor.apply();

        web_view_finished = true;

        Intent intent = new Intent(this, Activity2.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    public void validateToken() {
        Log.d(TAG, "validateToken: " + token);
        TokenValidator tokenValidator = new TokenValidator();
        tokenValidator.execute(token);
    }

    private void onTokenValidated(boolean status) {
        Log.d(TAG, "onTokenValidated: " + status);
        if (!status) {
            invalidateToken();
        }
        splash_screen_finished = true;

        Intent intent = new Intent(this, Activity2.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    private void invalidateToken() {
        token = null;
        valid_through = null;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.preference_token_key), null);
        editor.putString(getString(R.string.preference_valid_through_key), null);
        editor.apply();
    }

    private class TokenValidator extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                URL u = new URL(getString(R.string.validate_url));
                HttpsURLConnection connection = (HttpsURLConnection) u.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + params[0]);
                connection.setDoInput(false);
                connection.setDoOutput(false);
                connection.connect();
                int status = connection.getResponseCode();
                return status == STATUS_OK;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean s) {
            onTokenValidated(s);
        }
    }
}
