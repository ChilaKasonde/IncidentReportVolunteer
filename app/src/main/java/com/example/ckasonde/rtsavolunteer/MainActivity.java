package com.example.ckasonde.rtsavolunteer;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, AdapterView.OnItemSelectedListener {

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    Button btnAccident, btnFaultyCar, btnFaultyLights, btnObstraction;
    private Location location;
    GoogleApiClient googleApiClient;
    ProgressBar progressBar,progressBar2;
    private String latitude, longitude;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000;
    private String QrResult;
    private String json_string, policeLocaton,hospitalLocation;
    NetworkHandler networkHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        networkHandler = new NetworkHandler(MainActivity.this);

        progressBar =(ProgressBar)findViewById(R.id.progressBar);
        progressBar2 =(ProgressBar)findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.INVISIBLE);
        btnAccident = (Button)findViewById(R.id.btnAccident);
        btnFaultyCar = (Button)findViewById(R.id.btnFaultyCar);
        btnFaultyLights = (Button)findViewById(R.id.btnFaultyTraffic);
        btnObstraction = (Button)findViewById(R.id.btnObstraction);

        btnFaultyLights.setEnabled(false);
        btnAccident.setEnabled(false);
        btnFaultyCar.setEnabled(false);
        btnFaultyLights.setEnabled(false);
        btnObstraction.setEnabled(false);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                btnFaultyLights.setEnabled(true);
                btnAccident.setEnabled(true);
                btnFaultyCar.setEnabled(true);
                btnFaultyLights.setEnabled(true);
                btnObstraction.setEnabled(true);
                progressBar2.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this, "Location Ready", Toast.LENGTH_LONG).show();

            }
        }, 5000);




        btnAccident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new HospitalTaskNested().execute();
                new PoliceTaskNested().execute();
                progressBar.setVisibility(View.VISIBLE);

            }
        });

        btnObstraction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                networkHandler.PostToServer("Obstraction","RTSA",latitude,longitude );
                progressBar.setVisibility(View.VISIBLE);
                progressLoad();

            }
        });

        btnFaultyCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                networkHandler.PostToServer("Faulty Car","RTSA",latitude,longitude );
                progressBar.setVisibility(View.VISIBLE);
                progressLoad();
            }
        });

        btnFaultyLights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                networkHandler.PostToServer("Faulty Street Lights","RTSA",latitude,longitude );
                progressBar.setVisibility(View.VISIBLE);
                progressLoad();
            }
        });


        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();
    }

    private void progressLoad() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
            }
        }, 3000);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }

        if (currentUser == null) {
            sendToLogin();
        } else {

        }
    }

    private void sendToLogin() {

        Intent intentHome = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intentHome);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // stop location updates
        if (googleApiClient != null  &&  googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        // Permissions ok, we get last location
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (location != null) {
//            Toast.makeText(MainActivity.this, "Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude(), Toast.LENGTH_LONG).show();
        }

        startLocationUpdates();
    }


    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);



        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
//            Toast.makeText(MainActivity.this, "Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude(), Toast.LENGTH_LONG).show();
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());

        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    class PoliceTaskNested extends AsyncTask<Void,Void,String> {

        String json_url ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+latitude+","+longitude+"&rankby=distance&keyword=police&key=your key from maps";


        @Override
        protected String doInBackground(Void... voids) {

            try {
                URL url = new  URL(json_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();

                while((json_string=bufferedReader.readLine())!= null){
                    stringBuilder.append(json_string+"\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                return stringBuilder.toString().trim();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        public PoliceTaskNested() {
            super();
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject parentObject = new JSONObject(result);
                JSONArray parentArray = parentObject.getJSONArray("results");
                JSONObject jsonObject = parentArray.getJSONObject(0);
                JSONObject locationObj = jsonObject.getJSONObject("geometry").getJSONObject("location");
                String latitude = locationObj.getString("lat");
                String longitude = locationObj.getString("lng");

                JSONObject nameObject = parentArray.getJSONObject(0);

                String location_id = nameObject.getString("id");
                String location_name = nameObject.getString("name");
                networkHandler.PostToServer("Accident",location_id,latitude,longitude );
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this, location_name, Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }


    class HospitalTaskNested extends AsyncTask<Void,Void,String> {

        String json_url ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+latitude+","+longitude+"&rankby=distance&keyword=hospital&key=your key from maps";


        @Override
        protected String doInBackground(Void... voids) {

            try {
                URL url = new  URL(json_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();

                while((json_string=bufferedReader.readLine())!= null){
                    stringBuilder.append(json_string+"\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                return stringBuilder.toString().trim();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        public HospitalTaskNested() {
            super();
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject parentObject = new JSONObject(result);
                JSONArray parentArray = parentObject.getJSONArray("results");
                JSONObject jsonObject = parentArray.getJSONObject(0);
                JSONObject locationObj = jsonObject.getJSONObject("geometry").getJSONObject("location");
                String latitude = locationObj.getString("lat");
                String longitude = locationObj.getString("lng");

                JSONObject nameObject = parentArray.getJSONObject(0);
                String location_name = nameObject.getString("name");
                String location_id = nameObject.getString("id");
                networkHandler.PostToServer("Accident", location_id, latitude, longitude);
                Toast.makeText(MainActivity.this, location_name, Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }








}
