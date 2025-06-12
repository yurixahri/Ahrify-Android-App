package com.yurixahri.ahrify.utils;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class CustomVolley {

//    private static VolleySingleton instance;
    private final RequestQueue requestQueue;

    public CustomVolley(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }
//    public static VolleySingleton getInstance(Context context) {
//        if (instance == null) {
//            instance = new VolleySingleton(context);
//        }
//        return  instance;
//    }

    public RequestQueue getRequestQueue(){
        return requestQueue;
    }
}