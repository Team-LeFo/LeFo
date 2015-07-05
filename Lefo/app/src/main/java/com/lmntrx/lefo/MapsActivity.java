package com.lmntrx.lefo;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //Parse Class Name
    public static final String PARSE_CLASS = "LeFo_DB";

    //Parse Keys
    public static final String KEY_QRCODE = "QR_CODE";
    public static final String KEY_LOCATION = "LOCATION";

    //Parse ObjectID
    public static String objectId = null;
    ParseGeoPoint leaderLoc;
    String code;
    String objectID;

    Context CON;

    Activity mapsActivity = this;

    boolean doubleBackToExitPressedOnce;
    boolean resumed = false;

    MarkerOptions leaderMarkerOptions;
    GoogleMap googleMap;
    Marker leaderMarker;
    LatLng leaderLocation=new LatLng(10.141792312058117, 76.43611420148119);  //ignored

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        CON = this;
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        code = getIntent().getStringExtra("CODE");
        objectId = getIntent().getStringExtra("OBJECT_ID");
    }


    public void getLeaderLoc(final GoogleMap map) {
        TimerTask feedLocation;
        final Handler handler = new Handler();
        Timer t = new Timer();
        feedLocation = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (objectId != null) {
                            ParseQuery<ParseObject> query = ParseQuery.getQuery(PARSE_CLASS);
                            query.getInBackground(objectId, new GetCallback<ParseObject>() {
                                public void done(ParseObject object, ParseException e) {
                                    if (e == null) {
                                        if (object != null) {
                                            leaderLoc = object.getParseGeoPoint(KEY_LOCATION);
                                            showLeaderLoc(map);
                                        }
                                    } else {
                                        Toast.makeText(CON, "Object not found", Toast.LENGTH_LONG).show();
                                        e.printStackTrace();
                                        return;
                                    }
                                }
                            });
                        }
                        Log.d("TIMER", "Timer Running");
                    }
                });
            }
        };
        t.schedule(feedLocation, 500, 500); //updates every 0.5s

    }

    private void showLeaderLoc(GoogleMap map) {
        if (leaderLoc != null) {
            leaderLocation = new LatLng(leaderLoc.getLatitude(), leaderLoc.getLongitude());
            setMarker(map,leaderLocation);
        } else {
            Toast.makeText(CON, "Failed to Locate", Toast.LENGTH_LONG).show();
            //Default :D
            LatLng kanjoor = new LatLng(10.141792312058117, 76.43611420148119);
            map.addMarker(new MarkerOptions().position(kanjoor).title("Marker in Random Location"));
            map.setBuildingsEnabled(true);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(kanjoor, 16.5f));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(kanjoor, 16.5f));
        }
    }

    private void setMarker(GoogleMap map, LatLng leaderLocation) {
        /*leaderMarker.setPosition(leaderLocation);
        leaderMarker.setFlat(true);*/
        map.addMarker(new MarkerOptions().position(leaderLocation).flat(true));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(leaderLocation, 18.5f));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(leaderLocation, 18.5f));
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //leaderMarkerOptions = new MarkerOptions().flat(true).title("Leader's Location").position(leaderLocation);
        //leaderMarker = map.addMarker(leaderMarkerOptions);
        getLeaderLoc(map);
    }

    public void reset() {
        if (resumed) {
            Intent intent = new Intent(CON, MainActivity.class); //I ll change it later
            startActivity(intent);
            mapsActivity.finish();
            return;
        } else {
            resumed = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //reset();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            //On Back
            MainActivity.getMainActivity.finish();
            Intent intent = new Intent(CON, MainActivity.class);
            startActivity(intent);
            mapsActivity.finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please tap BACK again to Exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}