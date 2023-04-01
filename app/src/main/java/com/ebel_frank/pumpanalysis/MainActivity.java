package com.ebel_frank.pumpanalysis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ebel_frank.pumpanalysis.model.ThinkSpeakData;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.itangqi.waveloadingview.WaveLoadingView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.ebel_frank.pumpanalysis.SettingsActivity.PREFS;

public class MainActivity extends AppCompatActivity {

    private TextView level, status;
    private WaveLoadingView waveLoadingView;
    private ThinkSpeakData thinkSpeakData;
    private SharedPreferences settings;
    private LineChart chart;
    private View blurBG;
    private Thread thread;

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean("darkTheme", false)
        && AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = getSharedPreferences(PREFS, 0);

        NotificationUtils.createNotificationChannel(MainActivity.this);

        blurBG = findViewById(R.id.blurBG);
        level = findViewById(R.id.level);
        status = findViewById(R.id.status);

        receiver = new ConnectionReceiver(blurBG);
        registerNetworkBroadcast();

        Log.d("Tag", "Called once");
        waveLoadingView = findViewById(R.id.waveLoadingView);
        waveLoadingView.setAnimDuration(3000);
        waveLoadingView.startAnimation();

        // // Chart Style // //
        chart = findViewById(R.id.chart1);
        chart.setBackgroundColor(Color.WHITE);
        chart.getDescription().setEnabled(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setAxisMaximum(100);
        chart.getAxisRight().setAxisMinimum(0);

        if (settings.getFloat("level", 0)!=0){
            float wLevel = settings.getFloat("level", 0);
            waveLoadingView.setProgressValue((int)wLevel);
            waveLoadingView.setCenterTitle(wLevel+"%");

            level.setText(String.format("%s%%", wLevel));
            status.setText(settings.getString("status", null));
        }

        thread = new Thread() {
            @Override
            public void run() {
                while (!interrupted()) {
                    try {
                        Thread.sleep(30000);    // delay for 20seconds
                        getThingSpeakData();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }

    private void getThingSpeakData() {
        final String GET_URL = "https://api.thingspeak.com/channels/1399710/feeds.json";
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url(GET_URL)
                .method("GET", null)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, final @NotNull IOException e) {
                // do nothing
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String json = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONArray dataArray = new JSONObject(json).getJSONArray("feeds");

                            if (dataArray.length() == 0) {
                                blurBG.setVisibility(View.GONE);
                                return;
                            }

                            List<Float> waterLvl = new ArrayList<>();
                            for (int i=0, n = dataArray.length()-2; i < n; i++) {
                                waterLvl.add(Float.parseFloat(
                                        dataArray.getJSONObject(i).getString("field1")
                                ));
                            }

                            JSONObject dataObject = dataArray.getJSONObject(dataArray.length()-3);
                            thinkSpeakData = new ThinkSpeakData(waterLvl, dataObject.getString("field2"));

                            if (thinkSpeakData.getWaterLevel() == 100) {
                                NotificationUtils.notify(MainActivity.this,"Tank full and device automatically disconnected");
                            }

                            waveLoadingView.setProgressValue((int)thinkSpeakData.getWaterLevel());
                            waveLoadingView.setCenterTitle(thinkSpeakData.getWaterLevel()+"%");

                            level.setText(String.format("%s%%", thinkSpeakData.getWaterLevel()));
                            status.setText(thinkSpeakData.getPumpStatus());

                            ArrayList<Entry> dataVals = new ArrayList<>();
                            for (int i=0, n=thinkSpeakData.getWaterLvlList().size(); i<n; i++) {
                                dataVals.add(new Entry(i, thinkSpeakData.getWaterLvlList().get(i)));
                            }

                            LineDataSet lineDataSet = new LineDataSet(dataVals, "");
                            lineDataSet.setDrawFilled(true);
                            lineDataSet.setFillDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.fade_blue));
                            lineDataSet.setLineWidth(3);
                            lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
                            lineDataSet.setDrawValues(false);
                            lineDataSet.setDrawCircles(false);

                            LineData data = new LineData(lineDataSet);

                            chart.setData(data);
                            blurBG.setVisibility(View.GONE);

                            SharedPreferences.Editor editor = settings.edit();
                            editor.putFloat("level", thinkSpeakData.getWaterLevel());
                            editor.putString("status", thinkSpeakData.getPumpStatus());
                            editor.putInt("height", (int)thinkSpeakData.getWaterLevel());
                            editor.apply();
                        } catch (JSONException e) {
                            Log.e("Tag", "Error: "+e.getMessage());
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.action_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            default:
                return false;
        }
        return true;
    }

    protected void registerNetworkBroadcast() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisterNetwork() {
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetwork();
        thread.interrupt();
    }
}
