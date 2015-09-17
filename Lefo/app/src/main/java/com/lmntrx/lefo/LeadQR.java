package com.lmntrx.lefo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class LeadQR extends Activity {

    //Context
    public static Context CON;


    //Code Generation
    public static int qrcode;

    //URL for generating QRCode for generated random code
    //Use any of the following servers
    //public String qrUrl1 = "https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=";
    public String qrUrl2 = "http://qrickit.com/api/qr?d=";
    public String qrUrl2Size = "&qrsize=500"; //500px. PS: When using url1 remove qrUrl2Size from ImageLoadTask

    //Choosing server for qrCode generation
    public String qrUrl = qrUrl2;

    //ImageView to display QRCode
    public ImageView qr_Img;

    //Activity
    public final Activity leadQRActivity = this;

    private ShareActionProvider mShareActionProvider;

    Intent sharingIntent;

    //Parse Class Name
    public static final String PARSE_CLASS = "LeFo_DB";
    public static final String PARSE_FCLASS = "Followers";

    //Parse Keys
    public static final String KEY_QRCODE = "QR_CODE";
    public static final String KEY_CON_CODE = "Con_Code";
    public static final String KEY_DEVICE = "deviceName";
    public static final String KEY_isActive = "isActive";
    public static final String KEY_LOCATION = "LOCATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lead_qr);

        qr_Img = (ImageView) findViewById(R.id.qrIMG);
        CON = this.getApplicationContext();
        qrcode = randNum();

        //Share Intent customisation
        sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "LeFo Connection Code");
        String shareBody = qrcode + "";
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

        //Starting location fetch
        startLocationService();

        //Async Task ImageLoadTask loads qr code from qrUrl to qr_Img
        new ImageLoadTask(qrUrl + qrcode + qrUrl2Size, qr_Img).execute();

        //Dealing with ProgressBar
        MainActivity.mProgressBar.setVisibility(View.INVISIBLE);

        //Display Lefo_Connection_Code
        TextView lcodeTXT=(TextView)findViewById(R.id.lCodeTXT);
        lcodeTXT.setText(qrcode+"");




    }

    //Random Code Generator
    public int randNum() {
        Random random = new Random();
        int min = 999, max = 99999999;
        return random.nextInt((max - min + 1) + min);
    }

    //Starts Location Service
    public void startLocationService() {
        /*Service is called on the same thread. Even if I enclose the following code in a new thread
        the service will only run on the main thread. I have put code into different threads in FetchLocationService.java*/
        Intent serviceIntent = new Intent(CON, FetchLocationService.class);
        serviceIntent.putExtra("CODE", qrcode);
        startService(serviceIntent);

    }

    @Override
    public void onBackPressed() {
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("End Session")
                    .setMessage("Do you want to end this LeFo Session?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            deleteSession();
                            leadQRActivity.finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
    }

    private void deleteSession() {
        ParseQuery query=new ParseQuery(PARSE_CLASS);
        query.whereEqualTo(KEY_QRCODE, qrcode);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> results, com.parse.ParseException e) {
                if (e == null) {
                    for (ParseObject result : results) {
                        try {
                            result.delete();
                        }catch (ParseException e1){
                            e1.printStackTrace();
                        }
                        result.saveInBackground();
                    }
                } else {
                    e.printStackTrace();
                    System.out.print(e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu_lead_qr, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        setShareIntent(sharingIntent);

        // Return true to display menu
        return true;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    public void statusActivity(View view){
        Intent intent=new Intent(CON,FollowerStatus.class);
        intent.putExtra("CODE", qrcode);
        startActivity(intent);
    }


}

