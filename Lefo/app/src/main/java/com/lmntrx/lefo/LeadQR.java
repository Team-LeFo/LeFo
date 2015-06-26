package com.lmntrx.lefo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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

    boolean doubleBackToExitPressedOnce;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lead_qr);
        qr_Img = (ImageView) findViewById(R.id.qrIMG);
        CON = this.getApplicationContext();
        qrcode = randNum();

        startMethod();

        //Async Task ImageLoadTask loads qr code from qrUrl to qr_Img
        new ImageLoadTask(qrUrl + qrcode + qrUrl2Size, qr_Img).execute();

        MainActivity.mProgressBar.setVisibility(View.INVISIBLE);

    }

    //Random Code Generator
    public int randNum() {
        Random random = new Random();
        int min = 999, max = 99999999;
        return random.nextInt((max - min + 1) + min);
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

    public void startMethod() {
        Intent serviceIntent = new Intent(this, FetchLocationService.class);
        serviceIntent.putExtra("CODE", qrcode);
        startService(serviceIntent);
    }

    public void stopMethod() {
        Intent serviceIntent = new Intent(this, FetchLocationService.class);
        stopService(serviceIntent);
    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            leadQRActivity.finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please tap BACK again to go back", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}
