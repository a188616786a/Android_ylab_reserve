package com.example.lfz_pc.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    EditText usernameText;
    EditText passwordText;
    CheckBox rememberMe;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String username;
    String password;
    private HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        pref = getSharedPreferences("cookies", MODE_PRIVATE);
//        if (!pref.getString("tokenIdcookie", "").equals("")) {
//            Intent intent = new Intent(this, ReserveActivity.class);
//            startActivity(intent);
//        }
        setContentView(R.layout.activity_main);
        usernameText = (EditText) findViewById(R.id.username);
        passwordText = (EditText) findViewById(R.id.password);
        rememberMe = (CheckBox) findViewById(R.id.checkBox);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (pref.getBoolean("rememberMe", false)) {
            usernameText.setText(pref.getString("username", ""));
            passwordText.setText(pref.getString("password", ""));
            rememberMe.setChecked(true);
        }

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = usernameText.getText().toString();
                password = passwordText.getText().toString();

                try {
                    RequestBody requestBody = new FormBody.Builder()
                            .add("username", username)
                            .add("password", password)
                            .add("rememberMe", "false")
                            .build();
                    Request request = new Request.Builder()
                            .url("http://cem.ylab.cn/doLogin.action")
                            .post(requestBody)
                            .build();
                    OkHttpClient client = new OkHttpClient.Builder().cookieJar(new CookieJar() {
                        @Override
                        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                            SharedPreferences pref = getSharedPreferences("cookies", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            for (Cookie cookie : cookies) {
                                Log.d(TAG, "saveFromResponse: cookie:" + cookie + "cookies:" + cookies);
                                editor.putString(cookie.name() + "cookie", cookie.toString());
                            }
                            editor.apply();
                            Log.d(TAG, "saveFromResponse: " + cookies);
                        }

                        @Override
                        public List<Cookie> loadForRequest(HttpUrl url) {
                            SharedPreferences pref = getSharedPreferences("cookies", MODE_PRIVATE);
                            List<Cookie> cookies = new ArrayList<>();
                            if (!pref.getString("JSESSIONID" + "cookie", "").equals(""))
                                cookies.add(Cookie.parse(url, pref.getString("JSESSIONID" + "cookie", "")));
                            if (!pref.getString("tokenId" + "cookie", "").equals(""))
                                cookies.add(Cookie.parse(url, pref.getString("tokenId" + "cookie", "")));
                            if (!pref.getString("email" + "cookie", "").equals(""))
                                cookies.add(Cookie.parse(url, pref.getString("email" + "cookie", "")));
                            if (!pref.getString("username" + "cookie", "").equals("")) {
                                cookies.add(Cookie.parse(url, pref.getString("username" + "cookie", "")));
                            }
                            Log.d(TAG, "loadForRequest: " + cookies);
                            return cookies;
                        }
                    }).followRedirects(false).build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                        }

                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {
                            final int responseCode = response.code();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (responseCode==302) { //登录成功
                                        editor = pref.edit();
                                        if (rememberMe.isChecked()) {
                                            editor.putBoolean("rememberMe", true);
                                            editor.putString("username", username);
                                            editor.putString("password", password);
                                            editor.apply();
                                        } else {
                                            editor.clear();
                                        }
                                        Intent intent = new Intent(MainActivity.this, ReserveActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(MainActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }
}
