package com.lmntrx.lefo;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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

    Activity mapsActivity=this;

    boolean doubleBackToExitPressedOnce;
    boolean resumed=false;

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
    }

    private void showLeaderLoc(GoogleMap map) {
        if (leaderLoc != null) {
            LatLng leaderLocation = new LatLng(leaderLoc.getLatitude(), leaderLoc.getLongitude());
            map.addMarker(new MarkerOptions().position(leaderLocation).title("Leader's Location"));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(leaderLocation, 18.5f));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(leaderLocation,18.5f));
        }else {
            Toast.makeText(CON,"Failed to Locate",Toast.LENGTH_LONG).show();
            //Default
            LatLng kanjoor = new LatLng(10.141792312058117, 76.43611420148119);
            map.addMarker(new MarkerOptions().position(kanjoor).title("Marker in Random Location"));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(kanjoor,16.5f));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(kanjoor,16.5f));
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        getLeaderLoc(map);
    }

    public void reset(){
        if(resumed){
            Intent intent=new Intent(CON,MainActivity.class); //I ll change it later
            startActivity(intent);
            mapsActivity.finish();
            return;
        }else{
            resumed=true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        reset();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Intent intent=new Intent(CON,MainActivity.class);
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