package com.yurixahri.ahrify;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yurixahri.ahrify.adapters.DefaultListView;
import com.yurixahri.ahrify.models.defaultListItem;
import com.yurixahri.ahrify.notSingleton.Mediaplayer;
import com.yurixahri.ahrify.utils.BitmapCompressor;
import com.yurixahri.ahrify.utils.CustomVolley;
import com.google.android.exoplayer2.Player;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PlaylistActivity extends AppCompatActivity {
    ImageButton button;
    ImageView place_holder;
    FrameLayout container;

    Mediaplayer mediaplayer;
    boolean isBound = false;
    CustomVolley volley;

    DefaultListView adapter;
    SharedPreferences prefs;

    private final Player.Listener playerListener = new Player.Listener() {

        @Override
        public void onPlaybackStateChanged(int state) {
            if (state == Player.STATE_READY) {
                adapter.setHighlightIndex(mediaplayer.index);
            }
        }


    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.playlist_activity);

        prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        updateBackgroundOnStart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().setNavigationBarColor(Color.BLACK);
            getWindow().setNavigationBarDividerColor(Color.BLACK);// or any dark color
        }

        volley = new CustomVolley(this);

        button = findViewById(R.id.back_button);
        place_holder = findViewById(R.id.place_holder);
        container = findViewById(R.id.container);

        button.setOnClickListener(this::CloseTest);



        Intent intent = new Intent(this, Mediaplayer.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE); // Then bind to it

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,
                    systemBars.bottom);
            return insets;
        });

    }

    @Override
    protected void onDestroy(){
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        super.onDestroy();
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Mediaplayer.LocalBinder binder = (Mediaplayer.LocalBinder) service;
            mediaplayer = binder.getService();
            isBound = true;

            if (mediaplayer.playlist.length() > 0){
                place_holder.setVisibility(View.GONE);
                ArrayList<defaultListItem> list = new ArrayList<>();
                ListView list_view = new ListView(PlaylistActivity.this);
                list_view.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));

                FrameLayout container = findViewById(R.id.container);
                for (short i =0; i < mediaplayer.playlist.length(); i++){
                    try {
                        JSONObject item = mediaplayer.playlist.getJSONObject(i);
                        String text = (item.has("song_name") && !item.getString("song_name").isEmpty()) ? item.getString("song_name") : item.getString("file_name");
                            list.add(new defaultListItem(text, R.drawable.default_icon, item.has("cover")  ? (Bitmap) item.get("cover") : mediaplayer.cover));
                    } catch (JSONException e) {
                        Log.e("playlist", e.getMessage() );
                    }

                }

                adapter = new DefaultListView(PlaylistActivity.this, R.layout.default_listview_item, list);
                list_view.setAdapter(adapter);

                adapter.setOnItemClickListener(new DefaultListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(defaultListItem item, int position) {
                        mediaplayer.index = position;
                        adapter.setHighlightIndex(position);
                        playSong();
                    }
                    @Override
                    public void onItemLongClick(defaultListItem item, int position) {}
                });

                container.addView(list_view);
                adapter.setHighlightIndex(mediaplayer.index);

                Mediaplayer.setPlayerListener(playerListener);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };


    private void playSong(){
        try {
            JSONObject object = mediaplayer.playlist.getJSONObject(mediaplayer.index);
            mediaplayer.getSongInfo(this, volley, object.getString("folder"), object.getString("file_name"), new Mediaplayer.callback() {
                @Override
                public void afterGetInfo(String url) {
                    mediaplayer.setUrl(url, new Mediaplayer.OnMediaStartListener() {
                        @Override
                        public void onMediaStarted(String url) {
                        }
                    });

                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateBackgroundOnStart(){
        String base64 = prefs.getString("background", "");
        if (!base64.isEmpty()) {
            ImageView background = findViewById(R.id.background);
            background.setImageBitmap(BitmapCompressor.base64ToBitmap(base64));
        }
    }

    public void CloseTest(View view) {
        finish();
    }
}
