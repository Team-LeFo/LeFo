package com.lmntrx.lefo;

 /*
    -----------------------------------------------------------------------------------------
        I am currently working on this activity.
        I'll update comments sections once the activity is complete
    -----------------------------------------------------------------------------------------
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
    ParseGeoPoint oldleaderLoc;
    String code;
    String objectID;

    Context CON;

    Activity mapsActivity = this;

    boolean doubleBackToExitPressedOnce;
    boolean resumed = false;

    MarkerOptions leaderMarkerOptions;
    GoogleMap googleMap;
    Marker leaderMarker;
    LatLng leaderLocation = new LatLng(10.141792312058117, 76.43611420148119);  //ignored
    LatLng followerLocation;
    public final long MIN_TIME = 5000; //5000ms=5s
    public final float MIN_DISTANCE = 2;//2m

    int count;

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

        count = 0;
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
                                            //if(leaderLoc!=oldleaderLoc){
                                            leaderLoc = object.getParseGeoPoint(KEY_LOCATION);
                                            showLeaderLoc(map);
                                            oldleaderLoc = leaderLoc;
                                            //}else {
                                            //Toast.makeText(CON,"Here",Toast.LENGTH_LONG).show();
                                            //}
                                        }
                                    } else {
                                        Toast.makeText(CON, "Location not found", Toast.LENGTH_LONG).show();
                                        e.printStackTrace();
                                        return;
                                    }
                                }
                            });
                        }
                        getFollowerLoc(map);
                        Log.d("TIMER", "Timer Running");
                    }
                });
            }
        };
        t.schedule(feedLocation, 500, 500); //updates every 0.5s

    }

    private void showLeaderLoc(GoogleMap map) {
        if (leaderLoc != null) {
            //Toast.makeText(CON, "Showing LeaderLoc", Toast.LENGTH_LONG).show();
            leaderLocation = new LatLng(leaderLoc.getLatitude(), leaderLoc.getLongitude());
            setMarker(map, leaderLocation);
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

    private void setMarker(GoogleMap map, LatLng location) {
        //leaderMarker.setPosition(leaderLocation);
        //leaderMarker.setFlat(true);
        map.addMarker(new MarkerOptions().position(location).flat(true));
        if (count == 0) {
            map.setTrafficEnabled(true);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16.5f));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16.5f));
            count = 1;
        }
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
            /*MainActivity.getMainActivity.finish();
            Intent intent = new Intent(CON, MainActivity.class);
            startActivity(intent);
            mapsActivity.finish();*/
            System.exit(0);
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

    void getFollowerLoc(GoogleMap map) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LeFo_LocationListener();
        Location current_location, gps_location, network_location, followerLoc;
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);

        //Initializing Location
        gps_location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        current_location = gps_location;


        //Checking if current location is set using GPS provider
        if (current_location == null) {
            //if unable to find gps location
            //try again
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                for (int i = 0; i < 100; i++) {
                    gps_location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    current_location = gps_location;
                    if (current_location != null) {
                        break;
                    }
                }
            }
            if (current_location == null) {
                //Searching for network location
                network_location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                current_location = network_location;
            }
            if (current_location == null) {
                //trying again
                for (int i = 0; i < 100; i++) {
                    gps_location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    current_location = gps_location;
                    if (current_location != null) {
                        break;
                    }
                }
                if (current_location == null) {
                    //trying again
                    for (int i = 0; i < 100; i++) {
                        network_location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        current_location = network_location;
                        if (current_location != null) {
                            break;
                        }
                    }
                }
            }
        }
        followerLoc = current_location;
        followerLocation=new LatLng(followerLoc.getLatitude(),followerLoc.getLongitude());
        showFollowerLoc(map);
    }

    private void showFollowerLoc(GoogleMap map) {
        if (followerLocation != null) {
            setMarker(map, followerLocation);
        } else {
            Toast.makeText(CON, "Failed to Locate You", Toast.LENGTH_LONG).show();
            //Default :D
            LatLng kanjoor = new LatLng(10.141792312058117, 76.43611420148119);
            map.addMarker(new MarkerOptions().position(kanjoor).title("Marker in Random Location"));
            map.setBuildingsEnabled(true);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(kanjoor, 16.5f));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(kanjoor, 16.5f));
        }
    }

    class LeFo_LocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                followerLocation = new LatLng(location.getLatitude(), location.getLongitude());
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
            Toast.makeText(CON, "Location was disabled. Please enable it to continue.", Toast.LENGTH_SHORT).show(); //Message when GPS is turned off

        }
    }
}