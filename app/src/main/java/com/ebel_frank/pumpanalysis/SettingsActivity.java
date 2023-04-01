package com.ebel_frank.pumpanalysis;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS = "com.ebel_frank.pumpanalysis_preferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Objects.requireNonNull(findPreference("clearHistory")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    sendData();
                    return false;
                }
            });

            Objects.requireNonNull(findPreference("reset")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getContext(), "reset btn clicked", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });


            Objects.requireNonNull(findPreference("darkTheme")).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if ((boolean)newValue) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                    return true;
                }
            });

            Objects.requireNonNull(findPreference("notification")).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if ((boolean)newValue && !NotificationManagerCompat.from(getContext().getApplicationContext()).areNotificationsEnabled()) {
                        // Because the user took an action to create a notification, we create a prompt to let
                        // the user re-enable notifications for this application again.
                        Snackbar snackbar = Snackbar
                                .make(
                                        getView(),
                                        "You need to enable notifications for this app",
                                        Snackbar.LENGTH_LONG)
                                .setAction("ENABLE", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // Links to this app's notification settings
                                        startActivity(NotificationUtils.openNotificationSettingsForApp(getActivity().getPackageName(), getActivity().getApplicationInfo().uid));
                                    }
                                });
                        snackbar.show();
                        return  false;
                    }
                    return true;
                }
            });
        }

        private void sendData() {
            final String URL = "https://api.thingspeak.com/channels/1399710/feeds.json";
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "api_key=ZK157J65GRMY7EAX");
            Request request = new Request.Builder()
                    .url(URL)
                    .method("DELETE", body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, final @NotNull IOException e) {
                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Operation failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Success, history cleared", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }
}