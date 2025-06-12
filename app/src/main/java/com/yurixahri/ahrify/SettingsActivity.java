package com.yurixahri.ahrify;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.yurixahri.ahrify.notSingleton.Mediaplayer;
import com.yurixahri.ahrify.utils.BitmapCompressor;

import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {
    private static final int IMAGE_PICK_CODE = 1000;
    SharedPreferences prefs;
    Button set_background;
    Button clear_background;
    SeekBar volume_control;
    ImageButton back_button;
    Mediaplayer mediaplayer;
    boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.settings_activity);

        Intent intent = new Intent(this, Mediaplayer.class);
        startForegroundService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        updateBackgroundOnStart();

        set_background = findViewById(R.id.set_background);
        volume_control = findViewById(R.id.volume_control);
        back_button = findViewById(R.id.back_button);
        clear_background = findViewById(R.id.clear_background);



        back_button.setOnClickListener(v -> endActivity());
        set_background.setOnClickListener(v -> setBackground());
        volume_control.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float volume = progress / 100f;
                mediaplayer.player.setVolume(volume);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("volume", progress);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        clear_background.setOnClickListener(v -> {
            prefs.edit().remove("background").apply();
            Intent send = new Intent("com.yurixahri.AHRIFY_UPDATE_BACKGROUND");
            sendBroadcast(send);
            updateBackgroundOnStart();
        });
    }

    private void setBackground(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            // Example: set image as background of a layout
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                //Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                SharedPreferences.Editor editor = prefs.edit();
                String base64 = BitmapCompressor.bitmapToBase64(bitmap);
                editor.putString("background", base64);
                editor.apply();
                updateBackgroundOnStart();

                Intent intent = new Intent("com.yurixahri.AHRIFY_UPDATE_BACKGROUND");
                sendBroadcast(intent);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Mediaplayer.LocalBinder binder = (Mediaplayer.LocalBinder) service;
            mediaplayer = binder.getService();
            isBound = true;

            volume_control.setProgress(prefs.getInt("volume", 100));
            mediaplayer.player.setVolume(prefs.getInt("volume", 100) / 100f);

        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private void endActivity(){
        finish();
    }

    private void updateBackgroundOnStart(){
        String base64 = prefs.getString("background", "");
        ImageView background = findViewById(R.id.background);
        if (!base64.isEmpty()) {
            background.setImageBitmap(BitmapCompressor.base64ToBitmap(base64));
        }else{
            background.setImageResource(R.drawable.bg);
        }
    }
}