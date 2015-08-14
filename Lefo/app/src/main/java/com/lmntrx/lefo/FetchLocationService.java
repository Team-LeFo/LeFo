package com.lmntrx.lefo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by Livin on 25-06-2015.
 */
public class FetchLocationService extends Service {

    //Parse Class Name
    public static final String PARSE_CLASS = "LeFo_DB";

    //Parse Keys
    public static final String KEY_QRCODE = "QR_CODE";
    public static final String KEY_LOCATION = "LOCATION";

    //Parse ObjectID
    public static String objectId = null;


    //Location Variables
    Location old_location = null;
    Location current_location = null;
    Location gps_location = null;
    Location network_location = null;
    public final long MIN_TIME = 5000; //5000ms=5s
    public final float MIN_DISTANCE = 2;//2m
    //Fetch Location
    LocationManager locationManager;

    //LeFo_LocationListener is defined below
    LocationListener locationListener;

    //Context
    Context CON;

    //variable for holding Code
    public int qrcode;

    //Variable for checking syncDb() status
    boolean syncStatus = false;


    @Override
    public void onCreate() {
        super.onCreate();

        //Initializing Context Variable
        CON = this;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LeFo_LocationListener();
        try {
            qrcode = intent.getIntExtra("CODE", 000000);
        } catch (Exception e) {
            Log.d("ERROR", "Exited App");
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
        if (!(qrcode + "").isEmpty()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);

            //--------------------------------------------------
            Thread thread = new Thread() {
                public void run() {
                    try {
                        int i = 0;
                        while (!getLocation() && i < 2) {
                            Log.d("Status", "Getting Location");
                            i++;
                        }
                        if (i >= 2) {
                            Toast.makeText(CON, "Sorry, Something is not right. Retry", Toast.LENGTH_LONG);
                            //restartApp();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(CON, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            };
            thread.run();
            //----------------------------------------------------
        }

        //Choosing appropriate location listener
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);

        return super.onStartCommand(intent, flags, startId);
    }

    private void restartApp() {
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("DESTROY", "Service Stopped");

    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;

    }

    //Method for finding and saving location
    public boolean getLocation() {
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
            } else {
                //Syncing with Parse database for the first time
                syncDB(qrcode, current_location);
                return true;
            }
        } else {
            //Syncing with Parse database for the first time
            syncDB(qrcode, current_location);
            return true;
        }

        Thread confirmer = new Thread() {      //Confirmer LOL :D
            public void run() {
                try {
                    Thread.sleep(2000);
                    if (!confirmSync()) {
                        //Empty Cause confirmSync() is already done
                    }
                } catch (Exception e) {
                    Toast.makeText(CON, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        };
        confirmer.run();

        return current_location != null;
    }

    public void syncDB(int code, Location location) {

        if (!(code + "").isEmpty() || code != 0) {
            ParseObject parseObject = new ParseObject(PARSE_CLASS);
            ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
            parseObject.put(KEY_QRCODE, code);
            parseObject.put(KEY_LOCATION, geoPoint);
            parseObject.saveInBackground();
        } else {
            Log.d("SYNC", "Code is empty");
        }
    }

    class LeFo_LocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                current_location = location;
                //Registering new location in database
                if (old_location != current_location) {
                    updateParseDB(qrcode);
                    old_location = current_location;
                }
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

    //Function for updating user Location
    public void updateParseDB(int code) {
        //Object ID has to be fetched first
        //then using it the object is updated with new location
        ParseQuery<ParseObject> queryID = ParseQuery.getQuery(PARSE_CLASS);
        queryID.whereEqualTo(KEY_QRCODE, code);
        queryID.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (ParseObject result : parseObjects) {
                        // Retrieving objectId
                        objectId = result.getObjectId();
                    }
                    // Retrieving data from object
                    if (objectId != null) {
                        ParseQuery<ParseObject> query = ParseQuery.getQuery(PARSE_CLASS);
                        query.getInBackground(objectId, new GetCallback<ParseObject>() {
                            public void done(ParseObject parseUpdateObject, ParseException e) {
                                if (e == null) {
                                    ParseGeoPoint newGeoPoint = new ParseGeoPoint(current_location.getLatitude(), current_location.getLongitude());
                                    parseUpdateObject.put(KEY_LOCATION, newGeoPoint);
                                    parseUpdateObject.saveInBackground();
                                } else {
                                    Toast.makeText(CON, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                } else {
                    //Incase of an unknown error
                    Toast.makeText(CON, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public boolean confirmSync() {
        //       Toast.makeText(CON,"confirmSync() called",Toast.LENGTH_LONG).show();
        syncStatus = false;
        ParseQuery<ParseObject> queryID = ParseQuery.getQuery(PARSE_CLASS);
        queryID.whereEqualTo(KEY_QRCODE, qrcode);
        queryID.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    if (parseObjects.isEmpty()) {
                        Log.d("SyncStatus", "Bad");
                        //Toast.makeText(CON, " inside if", Toast.LENGTH_LONG).show();
                        syncStatus = false;
                        syncDB(qrcode, current_location);
                        return;
                    } /*else {
                        Toast.makeText(CON, " inside else", Toast.LENGTH_LONG).show();
                    }*/
                } else {
                    Log.d("SyncStatus", "Bad");
                    //Toast.makeText(CON, " inside  second else", Toast.LENGTH_LONG).show();
                    syncStatus = false;
                    syncDB(qrcode, current_location);
                    return;
                }
            }
        });
        return syncStatus;
    }
}