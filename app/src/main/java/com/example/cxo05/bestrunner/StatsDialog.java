package com.example.cxo05.bestrunner;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by xavie on 11/11/2017.
 */

public class StatsDialog extends Dialog implements View.OnClickListener{

    DatabaseReference mDatabase;
    Context context;
    TextView details;

    public StatsDialog (@NonNull Context context){
        super(context);
        this.context=context;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.info_dialog);
        ImageButton button = (ImageButton) findViewById(R.id.close);
        details = (TextView) findViewById(R.id.stats_details);
        button.setOnClickListener(this);
    }

    public void DisplayOwnDetails(){
        SharedPreferences sharedPref = context.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        LevelSystem asd = new LevelSystem(context);
        String levelString = "Level " + String.valueOf(asd.getPlayerLevel().getLevel());
        Log.d("errorlol",(details==null)+" ");
        details.setText(levelString+"\nTotal Distance Ran: "+sharedPref.getInt("Distance",0)+"m");
    }

    public void DisplayDetails (final String ID, final Location current){
        mDatabase= FirebaseDatabase.getInstance().getReference();
            mDatabase.child("accounts").child(ID).child("Location").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final String[] latlong = dataSnapshot.getValue().toString().split(",");
                    mDatabase.child("accounts").child(ID).child("Rank").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            double lat1 = Double.parseDouble(latlong[0]);
                            double lon1 = Double.parseDouble(latlong[1]);
                            int Radius = 6371;
                            double lat2 = current.getLatitude();
                            double lon2 = current.getLongitude();
                            double dLat = Math.toRadians(lat2 - lat1);
                            double dLon = Math.toRadians(lon2 - lon1);
                            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                                    + Math.cos(Math.toRadians(lat1))
                                    * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                                    * Math.sin(dLon / 2);
                            double c = 2 * Math.asin(Math.sqrt(a));
                            Double valueResult = Radius * c * 1000;
                            TextView details = (TextView) findViewById(R.id.stats_details);
                            details.setText("Level "+dataSnapshot.getValue()+"\n"+new Double(valueResult).intValue()+"m away");
                            TextView title = (TextView) findViewById(R.id.about_title);
                            title.setText(ID);
                            Button challenge = (Button) findViewById(R.id.Challenge);
                            challenge.setVisibility(View.VISIBLE);
                            challenge.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ((MapsActivity)context).Challenge(ID);
                                    dismiss();
                                }
                            });
                            Log.d("people",new LatLng(lat1,lon1).toString());
                        }
                        @Override
                        public void onCancelled(DatabaseError firebaseError) {
                            Log.e("The read failed: " ,firebaseError.getMessage());

                        }
                    });
                }
                @Override
                public void onCancelled(DatabaseError firebaseError) {
                    Log.e("The read failed: " ,firebaseError.getMessage());

                }
            });
    }


    @Override
    public void onClick(View view) {
        dismiss();
    }
}


