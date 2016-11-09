package ru.gafusss.technotrack.technotrack_android_task2_oauth2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static ru.gafusss.technotrack.technotrack_android_task2_oauth2.Utils.STATUS_OK;
import static ru.gafusss.technotrack.technotrack_android_task2_oauth2.Utils.getResponseString;

public class WebViewActivity extends AppCompatActivity {
    private static final String TAG = "WebViewActivity";

    private WebView webView;

    private String clientId;
    private String clientSecret;

    private String authUrlTemplate;
    private String tokenUrlTemplate;
    private String redirectUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        MyApplication myApplication = (MyApplication) getApplication();
        if (myApplication.web_view_finished) {
            finish();
            return;
        }

        clientId = getString(R.string.client_id);
        clientSecret = getString(R.string.client_secret);

        authUrlTemplate = getString(R.string.auth_url);
        tokenUrlTemplate = getString(R.string.token_url);
        redirectUrl = getString(R.string.callback_url);

        webView = (WebView) findViewById(R.id.web_view);

        if (savedInstanceState == null) {
            String url = String.format(authUrlTemplate, clientId, "&", redirectUrl, "&");

            URI uri = URI.create(url);
            webView.setWebViewClient(new OAuthWebClient());
            webView.getSettings().setJavaScriptEnabled(true);
            clearCookies(this);
            webView.loadUrl(uri.toString());
        }

    }

    @SuppressWarnings("deprecation")
    public static void clearCookies(Context context)
    {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Log.d(TAG, "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            Log.d(TAG, "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        MyApplication myApplication = (MyApplication) getApplication();
        if (myApplication.web_view_finished) {
            finish();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState:");
        if (webView != null) {
            webView.restoreState(savedInstanceState);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: ");
        if (webView != null) {
            webView.saveState(outState);
        }
    }

    private class OAuthWebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "shouldOverrideUrlLoading: string url " + url);
            URL url1 = null;
            try {
                url1 = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            URL callback_url = null;
            try {
                callback_url = new URL(view.getResources().getString(R.string.callback_url));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (url1 != null && callback_url != null) {
                if (url1.getHost().equals(callback_url.getHost()) && url1.getPath().equals(callback_url.getPath())){
                    Log.d(TAG, "shouldOverrideUrlLoading: equals");
                    String[] urls = url.split("=");
                    new AccessTokenGetter().execute(urls[1]);
                    return true;
                }
            }
            Log.d(TAG, "shouldOverrideUrlLoading: not equals");
            return false;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            MyApplication myApplication = (MyApplication) getApplication();
            myApplication.onAccessTokenGetterError(new Token(error.toString(), 0));
        }
    }

    public class Token {
        String access_token;
        int expires_in;

        public Token(String access_token, int expires_in) {
            this.access_token = access_token;
            this.expires_in = expires_in;
        }
    }

    private class AccessTokenGetter extends AsyncTask<String, Void, Token> {

        @Override
        protected Token doInBackground(String... params) {
            String url = String.format(tokenUrlTemplate, clientId, "&", clientSecret, "&", params[0], '&', '&', redirectUrl);
            Log.d(TAG, "doInBackground: " + url);
            try {
                URL u = new URL(url);
                HttpsURLConnection connection = (HttpsURLConnection) u.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.connect();
                int status = connection.getResponseCode();
                if (status != STATUS_OK) {
                    Log.d(TAG, "doInBackground: " + connection.getResponseMessage());
                    return new Token("Error with status " + status, 0);
                } else {
                    String response = getResponseString(connection.getInputStream());
                    return getAccessToken(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new Token("Error", 0);
        }

        @Override
        protected void onPostExecute(Token token) {
            MyApplication myApplication = (MyApplication) getApplication();
            if (token.access_token.contains("Error")) {
                myApplication.onAccessTokenGetterError(token);
            } else {
                myApplication.onAccessTokenGetterOk(token);
            }
        }
    }

    @Nullable
    private Token getAccessToken(String response) {
        Log.d(TAG, "getAccessToken: " + response);
        JSONObject json;
        try {
            json = new JSONObject(response);
            String token = json.getString(getString(R.string.oauth2_json_key_access_token));
            int expires_in = json.getInt(getString(R.string.oauth2_json_key_expires_in));
            return new Token(token, expires_in);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
