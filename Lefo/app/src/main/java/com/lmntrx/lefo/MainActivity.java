package com.lmntrx.lefo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;

import com.parse.Parse;


public class MainActivity extends Activity {
    Context context;
    public static String PARSE_APP_KEY = "U4lYViqyMsMmvicbKzvKWLV4mkOJN6VfPbtfvHmp";
    public static String PARSE_CLIENT_KEY = "PPNey0aT3L0LAuj9LuEgBgtSpn4eEALQ5WMJzAM6";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;


        //Initializing Parse BackEnd Support
        Parse.initialize(this, PARSE_APP_KEY, PARSE_CLIENT_KEY);


        //Internet Connectivity Status Check
        if (!isNetworkAvailable()) {
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Connection Error")
                    .setMessage("Unable to connect with the server. Check your internet connection and try again.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        //GPS Enabled Check
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }

    }


    //LeadQR Intent
    public void lead(View v) {
        Intent intent = new Intent(this, LeadQR.class);
        startActivity(intent);
    }


    //FollowCode Intent PS:Later Change it to FollowQR
    public void follow(View v) {
        Intent intent = new Intent(this, FollowCode.class);
        startActivity(intent);
    }


    //Internet Connectivity Status Check Function
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    //EnableGPS Dialog
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, please enable it to continue.")
                .setCancelable(false)
                .setTitle("Location Turned Off")
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        System.exit(0);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }




}
