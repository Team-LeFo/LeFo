package com.lmntrx.lefo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.parse.Parse;


public class MainActivity extends Activity {

    //Context Variable
    Context context;

    //Parse Authentication Keys
    public static String PARSE_APP_KEY = "U4lYViqyMsMmvicbKzvKWLV4mkOJN6VfPbtfvHmp";
    public static String PARSE_CLIENT_KEY = "PPNey0aT3L0LAuj9LuEgBgtSpn4eEALQ5WMJzAM6";

    //Location Variable
    Location current_location;

    //Progressbar
    public static ProgressBar mProgressBar;

    //Variable that checks is back button was pressed once
    boolean doubleBackToExitPressedOnce;

    //MainActivity Instance
    public static Activity getMainActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initializing Variables
        context = this;
        getMainActivity=this;
        mProgressBar=(ProgressBar)findViewById(R.id.progressBar);

        //Initially setting progressbar to invisible
        mProgressBar.setVisibility(View.INVISIBLE);

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
        }else{
            //GPS Enabled Check
            final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps();
            }else {
                kickStartGPS();
            }
        }
    }

    //InnerClass LocationListener
    class LeFo_LocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                current_location = location;
            }

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }


    //LeadQR Intent
    public void lead(View v) {
        mProgressBar.setVisibility(View.VISIBLE);
        kickStartGPS();
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

    private void kickStartGPS() {
        //KickStarting GPS
        LocationManager managerKickStart = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LeFo_LocationListener();                 //LeFo_LocationListener is defined below
        Location location=managerKickStart.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        managerKickStart.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        if (managerKickStart.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            for (int i=0;i<100;i++){
                location=managerKickStart.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location!=null){
                    break;
                }
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        kickStartGPS();
        int statusCode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (statusCode!= ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(statusCode)){
                Toast.makeText(this,statusCode+"",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this,statusCode+": Google play services not available",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            System.exit(0);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please tap BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


}
