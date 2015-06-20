package com.lmntrx.lefo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;
import java.util.Random;


public class LeadQR extends Activity {

    //Context
    Context CON;

    //Parse Class Name
    public static final String PARSE_CLASS = "LeFo_DB";

    //Parse Keys
    public static final String KEY_QRCODE = "QR_CODE";
    public static final String KEY_LOCATION = "LOCATION";

    //Parse ObjectID
    public static String objectId = null;


    //Code Generation
    public int qrcode;

    //URL for generating QRCode for generated random code
    //Use any of the following servers
    //public String qrUrl1 = "https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=";
    public String qrUrl2 = "http://qrickit.com/api/qr?d=";
    public String qrUrl2Size = "&qrsize=500"; //500px. PS: When using url1 remove qrUrl2Size from ImageLoadTask

    //Choosing server for qrCode generation
    public String qrUrl = qrUrl2;

    //ImageView to display QRCode
    public ImageView qr_Img;

    //Location Variables
    Location old_location = null;
    Location current_location = null;
    Location gps_location = null;
    Location network_location = null;
    public final long MIN_TIME = 5000; //5000ms=5s
    public final float MIN_DISTANCE = 2;//2m


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lead_qr);
        qr_Img = (ImageView) findViewById(R.id.qrIMG);
        CON = this.getApplicationContext();
        qrcode = randNum();

        //Async Task ImageLoadTask loads qr code from qrUrl to qr_Img
        new ImageLoadTask(qrUrl + qrcode + qrUrl2Size, qr_Img).execute();

        //Fetch Location
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Initializing Location
        gps_location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        current_location = gps_location;

        //Checking if current location is set using GPS provider
        if (current_location == null) {
            //if unable to find gps location
            Toast.makeText(CON, "Unable to find GPS location", Toast.LENGTH_SHORT).show();
            network_location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            current_location = network_location;
            if (current_location == null) {
                //If due to some cause location is not found
                Toast.makeText(CON, "Unable to find location", Toast.LENGTH_SHORT).show();
                return;
            } else {
                //Syncing with Parse database for the first time
                syncDB(qrcode, current_location);
            }
        } else {
            Toast.makeText(CON, current_location + "", Toast.LENGTH_SHORT).show();
            //Syncing with Parse database for the first time
            syncDB(qrcode, current_location);
        }


        LocationListener locationListener = new LeFo_LocationListener();                 //LeFo_LocationListener is defined below

        if (gps_location != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
        }


    }

    //Random Code Generator
    public int randNum() {
        Random random = new Random();
        int min = 999, max = 99999999;
        return random.nextInt((max - min + 1) + min);
    }

    //Function for syncing generated data with ParseDB
    public void syncDB(int code, Location location) {
        ParseObject parseObject = new ParseObject(PARSE_CLASS);
        ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        parseObject.put(KEY_QRCODE, code);
        parseObject.put(KEY_LOCATION, geoPoint);
        parseObject.saveInBackground();


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
                    if (objectId != null) {
                        ParseQuery<ParseObject> query = ParseQuery.getQuery(PARSE_CLASS);
                        query.getInBackground(objectId, new GetCallback<ParseObject>() {
                            public void done(ParseObject parseUpdateObject, ParseException e) {
                                if (e == null) {
                                    System.out.println("here");
                                    ParseGeoPoint newGeoPoint = new ParseGeoPoint(current_location.getLatitude(), current_location.getLongitude());
                                    parseUpdateObject.put(KEY_LOCATION, newGeoPoint);
                                    parseUpdateObject.saveInBackground();
                                } else {
                                    Toast.makeText(CON, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                } else { //Incase of an error
                    Toast.makeText(CON, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    //InnerClass LocationListener
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
            Toast.makeText(getApplicationContext(), "Location Fetch Disabled", Toast.LENGTH_SHORT).show(); //Message when GPS is turned off
        }
    }

    //Share Dialog Builder
    public void shareCode(View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("" + qrcode)
                .setTitle("LeFo Connection Code")
                .setCancelable(true)
                .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "LeFo Connection Code");
                        String shareBody = qrcode + "";
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

}
