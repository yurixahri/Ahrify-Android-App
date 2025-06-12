package com.yurixahri.ahrify.notSingleton;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.yurixahri.ahrify.models.album;
import com.yurixahri.ahrify.utils.CustomVolley;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class Albums {
    public JSONArray albums = new JSONArray();
    public JSONArray files = new JSONArray();
    public album current_album;

    final String url_file_list = "https://server.yurixahri.net/~/api/get_file_list?uri=";
    final String url_file = "https://server.yurixahri.net/";
    final String url_albums = "https://ahrify.api.yurixahri.net/albums";
    final String url_album_songs = "https://ahrify.api.yurixahri.net/album_songs?name=";


    public boolean isLoading = false;
    public int currentPage = 1;
    public final int ITEMS_PER_PAGE = 20;
    public String search = "";

    public Albums(){}



    public interface AlbumListCallback {
        void onListLoaded();
        void onSongsLoaded();
    }
    public void getAlbums(CustomVolley custom_volley, AlbumListCallback callback){
        isLoading = true;
        String pagedUrl = url_albums + "?page=" + currentPage + "&limit=" + ITEMS_PER_PAGE + "&search=" + search;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, pagedUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length() == 0) {
                    isLoading = false;

                    return;
                }

                for (int i = 0; i < response.length(); i++){
                    try {
                        albums.put(response.getJSONObject(i));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                isLoading = false;
                callback.onListLoaded();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                isLoading = false;
                Log.e("albums", "Main request failed: " + e.getMessage());
                callback.onListLoaded();
            }
        });
        request.setTag("request");
        custom_volley.getRequestQueue().add(request);
    }

    public void getSongs(CustomVolley custom_volley, String album, AlbumListCallback callback){
        String uri = album;
        try {
            uri = URLEncoder.encode(album, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //todo
        }

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url_album_songs+uri, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                files = response;
                callback.onSongsLoaded();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                Log.e("albums", "Main request failed: " + e.getMessage());

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
