package com.ebel_frank.pumpanalysis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class ConnectionReceiver extends BroadcastReceiver {
    private Snackbar customBar;
    public ConnectionReceiver(View blurBG) {
        customBar = Snackbar.make(blurBG, "No network connection", Snackbar.LENGTH_INDEFINITE);
        customBar.getView().setBackgroundColor(Color.BLACK);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        customBar.setAction("SETTINGS", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS));
            }
        });
        if (!isConnectedToInternet(context)) {
            customBar.show();
        } else {
            customBar.dismiss();
        }
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}
