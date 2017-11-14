package com.example.cxo05.bestrunner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        GoogleMap.OnMarkerClickListener,
        OnMapReadyCallback,LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private ArrayList<Location> current=new ArrayList<>();
    SupportMapFragment mapFragment;
    double distanceTravelled=0;
    DatabaseReference mDatabase;
    String challenging="";
    String challenged="";
    double distanceSinceChallenge=0;
    long distanceByChallenger=0;
    ChildEventListener challengeListener;
    ChildEventListener amChallengedListener;
    TextView challenger;
    boolean accepted=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        challenger=(TextView)findViewById(R.id.challenger);
        final DatabaseReference mDatabase=FirebaseDatabase.getInstance().getReference();
        amChallengedListener=mDatabase.child("Challenges").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final SharedPreferences sharedPref = MapsActivity.this.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
                if(dataSnapshot.getKey().matches(sharedPref.getString("ID","user"))){
                    if(challenging.matches("")) {
                        ArrayList<DataSnapshot> children= new ArrayList<>();
                        for (DataSnapshot child:dataSnapshot.getChildren()){
                            children.add(child);
                        }
                        final String requester=children.get(0).getKey();
                        final long value=(Long)children.get(0).getValue();
                        Log.d("challenge",requester);
                        new AlertDialog.Builder(MapsActivity.this)
                                .setTitle("Accept Challenge?")
                                .setMessage(requester+" has challenged you!")
                                .setCancelable(false)
                                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        challenged=requester;
                                        challenger.setVisibility(View.VISIBLE);
                                        challenger.setText(requester+"\n"+value+"m");
                                        distanceSinceChallenge=0;
                                        mDatabase.child("Challenges").child(sharedPref.getString("ID","user")).child(sharedPref.getString("ID","user")).setValue(distanceSinceChallenge);
                                        mDatabase.child("Challenges").child(sharedPref.getString("ID","user")).child(challenged).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                challenger.setText(challenged+"\n"+dataSnapshot.getValue()+"m");
                                                distanceByChallenger=(Long)dataSnapshot.getValue();
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                })
                                .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(MapsActivity.this,"You rejected"+requester+"'s challenge",Toast.LENGTH_SHORT).show();
                                        mDatabase.child("Challenges").child(sharedPref.getString("ID","user")).child("Reject").setValue("sorry");
                                    }
                                })
                                .show();
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        /*mMap.clear();
			/*mMap.addMarker(new MarkerOptions()
					.position(caregiver)
					.title("You"));*/
        /*mMap.addMarker(new MarkerOptions()
                .position(new LatLng(current.get(current.size()-1).getLatitude(),current.get(current.size()-1).getLongitude()))
                .title("You are here"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(current.get(current.size()-1).getLatitude(),current.get(current.size()-1).getLongitude()), 10));*/
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("Location", "GoogleApiClient connection has been successful");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(2000); // Update location every second
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("Location", "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("Location", "GoogleApiClient connection has failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("Location", "Location found");
        current.add(location);
        Double valueResult=0.0;
        if(current.size()>1) {
            int Radius = 6371;
            double lat1 = current.get(current.size() - 1).getLatitude();
            double lat2 = current.get(current.size() - 2).getLatitude();
            double lon1 = current.get(current.size() - 1).getLongitude();
            double lon2 = current.get(current.size() - 2).getLongitude();
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(lat1))
                    * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                    * Math.sin(dLon / 2);
            double c = 2 * Math.asin(Math.sqrt(a));
            valueResult = Radius * c * 1000;
            distanceTravelled += valueResult;
            /*double km = valueResult / 1;
            DecimalFormat newFormat = new DecimalFormat("####");
            int kmInDec = Integer.valueOf(newFormat.format(km));
            double meter = valueResult*1000 % 1000;
            int meterInDec = Integer.valueOf(newFormat.format(meter));
            String distance= new String(kmInDec+" KM " + meterInDec+" Meter");*/
        }
        ((TextView)findViewById(R.id.distance)).setText("You\n"+new Double(distanceTravelled).intValue()+"m");
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        SharedPreferences sharedPref = MapsActivity.this.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        mDatabase.child("accounts").child(sharedPref.getString("ID","user")).child("Location").setValue(location.getLatitude()+","+location.getLongitude());
        if(mMap!=null)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()), 17.0f));
        if(!challenging.matches("") && accepted){
            distanceSinceChallenge+=valueResult;
            ((TextView)findViewById(R.id.distance)).setText("You\n"+new Double(distanceTravelled).intValue()+"m\nSince start of Chalenge: "+new Double(distanceSinceChallenge).intValue()+")");
            mDatabase.child("Challenges").child(challenging).child(sharedPref.getString("ID","user")).setValue(distanceSinceChallenge);
        }
        if(challenged.matches("")){
            distanceSinceChallenge+=valueResult;
            ((TextView)findViewById(R.id.distance)).setText("You\n"+new Double(distanceTravelled).intValue()+"m\nSince start of Chalenge: "+new Double(distanceSinceChallenge).intValue()+")");
            mDatabase.child("Challenges").child(sharedPref.getString("ID","user")).child(sharedPref.getString("ID","user")).setValue(distanceSinceChallenge);
        }
        resetMarkers();
    }

    public void resetMarkers(){
        if(mMap!=null)
            mMap.clear();
        LatLng origin =new LatLng(1.3071,103.7691) /*new LatLng(current.get(0).getLatitude(),current.get(0).getLongitude())*/;
        LatLng dest = new LatLng(current.get(current.size()-1).getLatitude(),current.get(current.size()-1).getLongitude());
        String url = getUrl(origin, dest);
        Log.d("onMapClick", url.toString());
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){
            FetchUrl FetchUrl = new FetchUrl();
            FetchUrl.execute(url);
        }
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("accounts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("people",dataSnapshot.getChildrenCount()+"");
                for (final DataSnapshot people: dataSnapshot.getChildren()) {
                    if (people.hasChild("Location")){
                        mDatabase.child("accounts").child(people.getKey()).child("Location").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String[] latlong = dataSnapshot.getValue().toString().split(",");
                                mDatabase.child("accounts").child(people.getKey()).child("Rank").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        double lat1 = Double.parseDouble(latlong[0]);
                                        double lon1 = Double.parseDouble(latlong[1]);
                                        SharedPreferences sharedPref = MapsActivity.this.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
                                        if (people.getKey().matches(sharedPref.getString("ID","user"))){
                                            mMap.addMarker(new MarkerOptions()
                                                    .position(new LatLng(lat1,lon1))
                                                    .title("You")
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                                                    .snippet("Rank: "+dataSnapshot.getValue()));
                                        }
                                        else{
                                            mMap.addMarker(new MarkerOptions()
                                                    .position(new LatLng(lat1,lon1))
                                                    .title(people.getKey())
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                                    .snippet("Rank: "+dataSnapshot.getValue()));
                                        }
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
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e("The read failed: " ,firebaseError.getMessage());

            }
        });

    }

    public void End(View v){
        new AlertDialog.Builder(this)
                .setTitle("End Run?")
                .setMessage("You have only ran "+new Double(distanceTravelled).intValue()+"m")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final SharedPreferences sharedPref=MapsActivity.this.getSharedPreferences("Preferences",Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("Distance", sharedPref.getInt("Distance",0)+new Double(distanceTravelled).intValue());
                        editor.commit();
                        if(!challenged.matches("") || (!challenging.matches("") && accepted)){
                            if(distanceSinceChallenge>distanceByChallenger){
                                new AlertDialog.Builder(MapsActivity.this)
                                        .setTitle("You won the challenge!")
                                        .setMessage("+2500xp")
                                        .setPositiveButton("Yay!", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                sharedPref.edit().putInt("Wins",sharedPref.getInt("Wins",0)+1).apply();
                                                Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                                                startActivity(intent);
                                            }
                                        }).show();
                            }
                            else{
                                new AlertDialog.Builder(MapsActivity.this)
                                        .setTitle("You lost the challenge!")
                                        .setMessage("+1000xp")
                                        .setPositiveButton("Will do better next time", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                sharedPref.edit().putInt("Losses",sharedPref.getInt("Losses",0)+1).apply();
                                                Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                                                startActivity(intent);
                                            }
                                        }).show();;
                            }
                        }
                        else{
                            Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                            startActivity(intent);
                        }
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void onBackPressed() {
        End(new View(this));
    }

    public void Challenge(final String ID){
        final DatabaseReference mDatabase= FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Challenges").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(ID)){
                    SharedPreferences sharedPref=MapsActivity.this.getSharedPreferences("Preferences",Context.MODE_PRIVATE);
                    Toast.makeText(MapsActivity.this, "Challenge sent to "+ID, Toast.LENGTH_SHORT).show();
                    challenging=ID;
                    distanceSinceChallenge=0;
                    challenger.setVisibility(View.VISIBLE);
                    challenger.setText(ID+"\n(Waiting for accept)");
                    mDatabase.child("Challenges").child(ID).child(sharedPref.getString("ID","user")).setValue(distanceSinceChallenge);
                    challengeListener=mDatabase.child("Challenges").child(ID).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            if(dataSnapshot.getKey().matches(ID)){
                                challenger.setText(ID+"\n"+dataSnapshot.getValue()+"m");
                                accepted=true;
                                distanceByChallenger=(Long)dataSnapshot.getValue();
                            }
                            if(dataSnapshot.getKey().matches("Reject")){
                                Toast.makeText(MapsActivity.this,ID+" has rejected your challenge",Toast.LENGTH_SHORT).show();
                                challenging="";
                                challenger.setVisibility(View.INVISIBLE);
                                distanceSinceChallenge=0;
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            if(dataSnapshot.getKey().matches(ID)){
                                challenger.setText(ID+"\n"+dataSnapshot.getValue()+"m");
                                distanceByChallenger=(Long)dataSnapshot.getValue();
                            }
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else{
                    Toast.makeText(MapsActivity.this, ID+" has already been challenged and cannot accept yours.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";
        
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String ID=marker.getTitle().toString();
        Log.d("IDtag",ID);
        SharedPreferences sharedPref = MapsActivity.this.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        if (ID.matches("You")) {
            StatsDialog asd = new StatsDialog(MapsActivity.this);
            asd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            asd.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            asd.show();
            asd.DisplayOwnDetails();
            asd.getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility());
            asd.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
        else{
            StatsDialog asd = new StatsDialog(MapsActivity.this);
            asd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            asd.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            asd.show();
            asd.DisplayDetails(ID,current.get(current.size()-1));
            asd.getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility());
            asd.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
        return false;
    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Connect the client.
        mGoogleApiClient.connect();
    }


    @Override
    public void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onStop();
    }
}
