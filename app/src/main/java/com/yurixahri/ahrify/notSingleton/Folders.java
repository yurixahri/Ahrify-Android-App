package com.yurixahri.ahrify.notSingleton;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.yurixahri.ahrify.utils.CustomVolley;
import com.yurixahri.ahrify.utils.NaturalOrderComparator;


public class Folders {
    public ArrayList<String> folders = new ArrayList<String>();
    public ArrayList<String> files = new ArrayList<String>();
    public ArrayList<String> current_path = new ArrayList<String>();
    public ArrayList<String> previous_path = new ArrayList<String>();

    final String url = "https://server.yurixahri.net/~/api/get_file_list?uri=";


    private Folders(){}

    private static final class InstanceHolder {
        private static final Folders instance = new Folders();
    }

    public static Folders getInstance(){
        return InstanceHolder.instance;
    }

    public interface FolderListCallback {
        void onListLoaded();
    }
    public void getList(CustomVolley custom_volley, FolderListCallback callback){
        folders.clear();
        files.clear();
        String uri = String.join("/", current_path);

        try {
            uri = URLEncoder.encode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //todo
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url+uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray list = new JSONArray();
                try {
                    list = response.getJSONArray("list");
                }catch (Exception e){

                }

                for (short i = 0; i < list.length(); i++){
                    try {
                        JSONObject object = list.getJSONObject(i);
                        if (object.getString("n").contains("/")){
                            folders.add(object.getString("n").replace("/", ""));
                        }else{
                            String[] temp = object.getString("n").split("[.]");
                            if (temp[temp.length - 1].matches("mp3|m4a|wav|flac|ogg|aiff")){
                                files.add(object.getString("n"));
                            }
                        }
                    }catch (Exception e){
                        Log.e("folders", e.getMessage());
                    }
                }

                NaturalOrderComparator comparator = new NaturalOrderComparator();
                folders.sort(comparator);
                files.sort(comparator);

                callback.onListLoaded();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                Log.e("folders", e.getMessage());
            }
        });
        request.setTag("request");
        custom_volley.getRequestQueue().add(request);
    }

    public void log(){
        Log.d("folders size", String.valueOf(folders.size()));
        for (String folder: folders){
            Log.d("folders", folder);
        }
        for (String file: files){
            Log.d("files", file);
        }

    }


}
