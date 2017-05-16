package com.example.lfz_pc.myapplication;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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


public class ReserveActivity extends AppCompatActivity {
    TextView date;
    TextView startTime;
    TextView endTime;
    Button reserve;
    Spinner instrument;
    TextView autoReserveDate;
    TextView autoReserveTime;
    Button autoReserve;
    String instrumentId;
    Calendar autoReserveCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve);
        date = (TextView) findViewById(R.id.reserveDate);
        startTime = (TextView) findViewById(R.id.reserveStartTime);
        endTime = (TextView) findViewById(R.id.reserveEndTime);
        reserve = (Button) findViewById(R.id.reserveButton);
        instrument = (Spinner) findViewById(R.id.spinner);
        autoReserveDate = (TextView) findViewById(R.id.autoReserveDate);
        autoReserveTime = (TextView) findViewById(R.id.autoReserveTime);
        autoReserve = (Button) findViewById(R.id.autoReserve);
        autoReserveCalendar = Calendar.getInstance();
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(ReserveActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                Log.d("time", "onDateSet: " + month);
                                date.setText(String.format("%04d年%02d月%02d日", year, month + 1, dayOfMonth));
                            }
                        },
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                new TimePickerDialog(ReserveActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                startTime.setText(String.format("%d:%02d", hourOfDay, minute));
                            }
                        },
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            }
        });
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                new TimePickerDialog(ReserveActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                endTime.setText(String.format("%d:%02d", hourOfDay, minute));
                            }
                        },
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            }
        });
        instrument.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        instrumentId = "563e690aae7b41dfb6da1880f291e65b";
                        break;
                    case 1:
                        instrumentId = "28ad18ae3ebb4f91b1d52553019ca381";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        reserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("tag", "onClick: " + instrumentId + date.getText().toString());
                RequestBody requestBody = new FormBody.Builder()
                        .add("reserveDate", date.getText().toString())
                        .add("instrumentId", instrumentId)
                        .add("reserveStartTime", startTime.getText().toString())
                        .add("reserveEndTime", endTime.getText().toString())
                        .build();
                Request request = new Request.Builder()
                        .url("http://cem.ylab.cn/user/doReserve.action")
                        .post(requestBody)
                        .build();
                OkHttpClient client = new OkHttpClient.Builder().cookieJar(new CookieJar() {
                    private static final String TAG = "ReserveActivity";
                    SharedPreferences pref = getSharedPreferences("cookies", MODE_PRIVATE);
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
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(ReserveActivity.this, response.body().string(), Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                    }
                });
            }
        });
        autoReserveTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                new TimePickerDialog(ReserveActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                autoReserveTime.setText(String.format("%d:%02d", hourOfDay, minute));
                                autoReserveCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                autoReserveCalendar.set(Calendar.MINUTE, minute);
                            }
                        },
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            }
        });
        autoReserveDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(ReserveActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                Log.d("time", "onDateSet: " + month);
                                autoReserveDate.setText(String.format("%04d年%02d月%02d日", year, month + 1, dayOfMonth));
                                autoReserveCalendar.set(Calendar.YEAR, year);
                                autoReserveCalendar.set(Calendar.MONTH, month);
                                autoReserveCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            }
                        },
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        autoReserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.example.lfz_pc.autoReserve");
                intent.setClass(ReserveActivity.this, AutoReserve.class);
                intent.putExtra("reserveDate", date.getText().toString());
                intent.putExtra("instrumentId", instrumentId);
                intent.putExtra("reserveStartTime", startTime.getText().toString());
                intent.putExtra("reserveEndTime", endTime.getText().toString());
                PendingIntent pi = PendingIntent.getBroadcast(ReserveActivity.this, 0, intent, 0);
                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                am.setExact(AlarmManager.RTC_WAKEUP, autoReserveCalendar.getTimeInMillis(), pi);
                Toast.makeText(ReserveActivity.this, "设定成功", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
