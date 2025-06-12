package com.yurixahri.ahrify;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yurixahri.ahrify.notSingleton.Mediaplayer;
import com.yurixahri.ahrify.utils.BitmapCompressor;

public class SongDetailsActivity extends AppCompatActivity {
    ImageButton button;
    TextView title;
    TextView artist;
    TextView album;
    TextView file;
    TextView path;

    Mediaplayer mediaplayer;
    boolean isBound = false;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.song_details_activity);

        prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        updateBackgroundOnStart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().setNavigationBarColor(Color.BLACK);
            getWindow().setNavigationBarDividerColor(Color.BLACK);
        }

        button = findViewById(R.id.back_button);
        title = findViewById(R.id.title);
        artist = findViewById(R.id.artist);
        album = findViewById(R.id.album);
        file = findViewById(R.id.file);
        path = findViewById(R.id.path);



        button.setOnClickListener(this::endActivity);



        Intent intent = new Intent(this, Mediaplayer.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

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

            title.setText(mediaplayer.song_title);
            artist.setText(mediaplayer.song_artist);
            album.setText(mediaplayer.song_album);
            file.setText(mediaplayer.song_file);
            path.setText(mediaplayer.song_folder);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    public void endActivity(View view) {
        finish();
    }

    private void updateBackgroundOnStart(){
        String base64 = prefs.getString("background", "");
        if (!base64.isEmpty()) {
            ImageView background = findViewById(R.id.background);
            background.setImageBitmap(BitmapCompressor.base64ToBitmap(base64));
        }
    }
}
