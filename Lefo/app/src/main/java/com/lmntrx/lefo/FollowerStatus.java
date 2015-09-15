package com.lmntrx.lefo;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FollowerStatus extends Activity {

    int myCode;
    public static final String PARSE_FCLASS = "Followers";
    public static final String KEY_CON_CODE = "Con_Code";
    public static final String KEY_DEVICE = "deviceName";
    public static final String KEY_isActive = "isActive";

    //Parse ObjectID
    public static String objectId[] = new String[50];

    int i=0;

    ProgressBar mProgressBar;

    Context CON;

    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower_status);

        myCode=getIntent().getIntExtra("CODE", 000000);
        mProgressBar=(ProgressBar)findViewById(R.id.progressBar1);

        lv=(ListView)findViewById(R.id.list);

        CON=this;

        loadFollowers();

        Timer t=new Timer();
        final Handler handler = new Handler();
        TimerTask task=new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshList();
                    }
                });
            }
        };
        t.schedule(task,2000,2000);

    }

    public void refreshList(){
        ParseQuery query=new ParseQuery(PARSE_FCLASS);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> results, com.parse.ParseException e) {
                if(e==null){
                    ArrayList<HashMap<String,String>> infos= new ArrayList<HashMap<String,String>>();
                    for (ParseObject result : results){
                        HashMap<String,String> info=new HashMap<String, String>();
                        if ((myCode+"").equals(result.getString(KEY_CON_CODE))){
                            info.put(KEY_DEVICE,result.getString(KEY_DEVICE));
                            infos.add(info);
                        }
                    }
                    if (infos.isEmpty())
                    {
                        HashMap<String,String> info=new HashMap<String, String>();
                        info.put(KEY_DEVICE,"No Followers");
                        infos.add(info);
                    }
                    SimpleAdapter adapter=new SimpleAdapter(CON,infos,R.layout.list_item,new String[]{KEY_DEVICE},new int[] {R.id.list_item_field});
                    lv.setAdapter(adapter);
                }
                else {
                    e.printStackTrace();
                    System.out.print(e.getMessage());
                }
            }
        });

    }



    public void loadFollowers(){
        mProgressBar.setVisibility(View.VISIBLE);

        ParseQuery query=new ParseQuery(PARSE_FCLASS);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> results, com.parse.ParseException e) {
                mProgressBar.setVisibility(View.INVISIBLE);
                if(e==null){
                    ArrayList<HashMap<String,String>> infos= new ArrayList<HashMap<String,String>>();
                    for (ParseObject result : results){
                        HashMap<String,String> info=new HashMap<String, String>();
                        if ((myCode+"").equals(result.getString(KEY_CON_CODE))){
                            info.put(KEY_DEVICE,result.getString(KEY_DEVICE));
                            infos.add(info);
                        }
                    }
                    if (infos.isEmpty())
                    {
                        HashMap<String,String> info=new HashMap<String, String>();
                        info.put(KEY_DEVICE,"No Followers");
                        infos.add(info);
                    }
                    SimpleAdapter adapter=new SimpleAdapter(CON,infos,R.layout.list_item,new String[]{KEY_DEVICE},new int[] {R.id.list_item_field});
                    lv.setAdapter(adapter);
                }
                else {
                    e.printStackTrace();
                    System.out.print(e.getMessage());
                }
            }
        });
    }
}

