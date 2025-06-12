package com.yurixahri.ahrify.notSingleton;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.yurixahri.ahrify.utils.CustomVolley;

import org.json.JSONArray;
import org.json.JSONException;


public class Songs {
    public JSONArray songs = new JSONArray();

    final String url_file = "https://server.yurixahri.net/";
    final String url_songs = "https://ahrify.api.yurixahri.net/songs";


    public boolean isLoading = false;
    public int currentPage = 1;
    public final int ITEMS_PER_PAGE = 20;
    public String search = "";
    public Songs(){}

    public interface SongListCallback {
        void onListLoaded();
        void onSongsLoaded();
    }
    public void getSongs(CustomVolley custom_volley, SongListCallback callback){
        isLoading = true;
        String pagedUrl = url_songs + "?page=" + currentPage + "&limit=" + ITEMS_PER_PAGE + "&search=" + search;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, pagedUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length() == 0) {
                    isLoading = false;
                    return;
                }

                for (int i = 0; i < response.length(); i++){
                    try {
                        songs.put(response.getJSONObject(i));
                    } catch (JSONException e) {
                        Log.e("artists", "Main request failed: " + e.getMessage());
                    }
                }

                isLoading = false;
                callback.onListLoaded();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                isLoading = false;
                Log.e("artists", "Main request failed: " + e.getMessage());
                callback.onListLoaded();
            }
        });
        request.setTag("request");
        custom_volley.getRequestQueue().add(request);
    }



    public void log(){
//        Log.d("folders size", String.valueOf(folders.size()));
//        for (String folder: folders){
//            Log.d("folders", folder);
//        }
//        for (String file: files){
//            Log.d("files", file);
//        }

    }


}
