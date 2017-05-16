package com.example.lfz_pc.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by LFZ-PC on 2017/5/16.
 */

public class AutoReserve extends BroadcastReceiver {
    private  Response theReseponse;
    @Override
    public void onReceive(final Context context, Intent intent) {
        String reserveDate = intent.getStringExtra("reserveDate");
        String instrumentId = intent.getStringExtra("instrumentId");
        String reserveStartTime = intent.getStringExtra("reserveStartTime");
        String reserveEndTime = intent.getStringExtra("reserveEndTime");
        RequestBody requestBody = new FormBody.Builder()
                .add("reserveDate", reserveDate)
                .add("instrumentId", instrumentId)
                .add("reserveStartTime", reserveStartTime)
                .add("reserveEndTime", reserveEndTime)
                .build();
        Request request = new Request.Builder()
                .url("http://cem.ylab.cn/user/doReserve.action")
                .post(requestBody)
                .build();
        OkHttpClient client = new OkHttpClient.Builder().cookieJar(new CookieJar() {
            private static final String TAG = "ReserveActivity";
            SharedPreferences pref = context.getSharedPreferences("cookies", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                editor = pref.edit();
                for (Cookie cookie : cookies) {
                    Log.d(TAG, "saveFromResponse: cookie:" + cookie + "cookies:" + cookies);
                    editor.putString(cookie.name() + "cookie", cookie.toString());
                }
                editor.apply();
                Log.d(TAG, "saveFromResponse: " + cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
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
        for (int i=0; i<10; i++) {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    theReseponse = response;
                }
            });
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            Toast.makeText(context, theReseponse.body().string(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
