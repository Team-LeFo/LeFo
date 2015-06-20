package com.lmntrx.lefo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class FollowCode extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_code);

    }

    public void connect(View v){
        //Code for checking code entered is valid
        //------------------------
        //------------------------
        Intent intent=new Intent(this,Navigation.class);
        startActivity(intent);
    }

    //Intent-To Be changed later
    public void qrCode(View v){
        Intent intent=new Intent(this,FollowQR.class);
        startActivity(intent);
    }
}
