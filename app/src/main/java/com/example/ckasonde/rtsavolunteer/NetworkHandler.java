package com.example.ckasonde.rtsavolunteer;

import android.annotation.TargetApi;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by C.Kasonde on 3/9/2019.
 */
public class NetworkHandler {
    Context context;

    public NetworkHandler(Context context) {
        this.context = context;
    }
    String UrlSite = "http://your url to sql list of push notification tokens.php";
    public void PostToServer(final String incident_type, final String location_id, final String latitude, final String longitude){
        final StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, UrlSite, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @TargetApi(24)
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();

                params.put("incident_type", incident_type);
                params.put("location_id", location_id);
                params.put("latitude", latitude);
                params.put("longitude", longitude);

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);



    }







}
