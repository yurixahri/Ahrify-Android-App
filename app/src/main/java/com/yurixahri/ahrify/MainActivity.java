package com.yurixahri.ahrify;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.yurixahri.ahrify.adapters.MainViewPageAdapter;
import com.yurixahri.ahrify.interfaces.MainIterface;
import com.yurixahri.ahrify.notSingleton.DBSingleton;
import com.yurixahri.ahrify.notSingleton.Mediaplayer;

import nl.joery.animatedbottombar.AnimatedBottomBar;

import com.yurixahri.ahrify.utils.BitmapCompressor;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MainActivity extends AppCompatActivity implements MainIterface {
    ImageView logo_spin;
    AnimatedBottomBar top_nav;
    ViewPager main_view_pager;
    TextView mode_text;
    TextView song_title;
    ImageButton play_button;
    ImageButton next_button;
    ImageButton previous_button;
    ImageButton search_button;
    ImageButton settings_button;
    Mediaplayer mediaplayer;
    boolean isBound = false;


    SeekBar seekBar;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBar;
    private boolean userSeeking = false;

    SharedPreferences prefs;


    DBSingleton db = DBSingleton.getInstance();

    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            if (mediaplayer.isPlaying()){
                play_button.setImageResource(R.drawable.pause);
            }else{
                play_button.setImageResource(R.drawable.baseline_play_arrow_24);
            }
            Log.d("MusicService", "Playing: " + isPlaying);
        }

        @Override
        public void onMediaItemTransition(MediaItem mediaItem, int reason){
//            song_title.setText(mediaplayer.song_title);
        }

        @Override
        public void onPlaybackStateChanged(int state) {
            if (mediaplayer.song_cover != null) logo_spin.setImageBitmap(mediaplayer.song_cover);
            if (state == Player.STATE_READY) {
                handler.removeCallbacks(updateSeekBar);
                setupSeekBar(mediaplayer.player);
                if (mediaplayer.song_cover != null){
                    logo_spin.setImageBitmap(mediaplayer.song_cover);
                }else{
                    logo_spin.setImageResource(R.drawable.default_icon);
                }
                song_title.setText(mediaplayer.song_title);
            }

        }


    };


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().setNavigationBarColor(Color.BLACK);
            getWindow().setNavigationBarDividerColor(Color.BLACK);
        }

        db.init(this);
        prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        updateBackgroundOnStart();

        logo_spin = findViewById(R.id.imageView);

        Animation spin = AnimationUtils.loadAnimation(this, R.anim.spin);
        logo_spin.startAnimation(spin);

        top_nav = findViewById(R.id.top_navigation);
//        top_nav.setOnApplyWindowInsetsListener(null);
//        top_nav.setPadding(0,0,0,0);


        main_view_pager = findViewById(R.id.main_view_pager);
        MainViewPageAdapter mainViewPagerAdapter = new MainViewPageAdapter (getSupportFragmentManager()
                , FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        main_view_pager.setAdapter(mainViewPagerAdapter);
        main_view_pager.setCurrentItem(0);
        main_view_pager.setOffscreenPageLimit(3);

        mode_text = findViewById(R.id.mode_text);


        main_view_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        top_nav.selectTabById(R.id.folders, true);
                        mode_text.setText("Folders");
                        break;
                    case 1:
                        top_nav.selectTabById(R.id.albums, true);

                        mode_text.setText("Albums");
                        break;
                    case 2:
                        top_nav.selectTabById(R.id.artists, true);
                        mode_text.setText("Artists");
                        break;
                    case 3:
                        top_nav.selectTabById(R.id.playlists, true);
                        mode_text.setText("Playlists");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }


        });



        top_nav.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {
            @Override
            public void onTabSelected(int i, @Nullable AnimatedBottomBar.Tab tab, int i1, @NonNull AnimatedBottomBar.Tab tab1) {

                switch (i1){
                    case 0:
                        main_view_pager.setCurrentItem(0);
                        mode_text.setText("Folders");
                        break;
                    case 1:
                        main_view_pager.setCurrentItem(1);
                        mode_text.setText("Albums");
                        break;
                    case 2:
                        main_view_pager.setCurrentItem(2);
                        mode_text.setText("Artists");
                        break;
                    case 3:
                        main_view_pager.setCurrentItem(3);
                        mode_text.setText("Playlists");
                        break;

                }

            }

            @Override
            public void onTabReselected(int i, @NonNull AnimatedBottomBar.Tab tab) {

            }
        });

        //mediaplayer


        LinearLayout bottom_panel = findViewById(R.id.bottom_panel);
        bottom_panel.setOnClickListener(v->{
            ToActivity2();
        });


        Intent intent = new Intent(this, Mediaplayer.class);
        startForegroundService(intent); // Start the service in foreground
        bindService(intent, connection, Context.BIND_AUTO_CREATE); // Then bind to it


        play_button = findViewById(R.id.play_button);
        next_button = findViewById(R.id.next_song);
        previous_button = findViewById(R.id.previous_song);
        song_title = findViewById(R.id.song_title);
        seekBar = findViewById(R.id.seekBar);
        search_button = findViewById(R.id.search_button);
        settings_button = findViewById(R.id.settings_button);

        next_button.setOnClickListener(v -> playNext());
        previous_button.setOnClickListener(v -> playPrevious());
        song_title.setSelected(true);
        search_button.setOnClickListener(v -> {
            View bottom_sheet_dialog_view = LayoutInflater.from(this).inflate(R.layout.add_playlist_form, null);
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            bottomSheetDialog.setContentView(bottom_sheet_dialog_view);
            bottomSheetDialog.show();

            Button cancel = bottom_sheet_dialog_view.findViewById(R.id.cancel);
            cancel.setOnClickListener(v2 -> {
                bottomSheetDialog.dismiss();
            });

            Button submit = bottom_sheet_dialog_view.findViewById(R.id.submit);
            EditText edit_text = bottom_sheet_dialog_view.findViewById(R.id.inputName);
            submit.setOnClickListener(v2 -> {
                if (String.valueOf(edit_text.getText()).isEmpty()){
                    Toast.makeText(this, "Please enter something", Toast.LENGTH_SHORT).show();
                }else {
                    bottomSheetDialog.dismiss();
                    ToSearchActivity(String.valueOf(edit_text.getText()));
                }
            });
        });

        settings_button.setOnClickListener(v -> ToSettings());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                    backgroundUpdateReceiver,
                    new IntentFilter("com.yurixahri.AHRIFY_UPDATE_BACKGROUND"),
                    Context.RECEIVER_NOT_EXPORTED
            );
        } else {
            registerReceiver(
                    backgroundUpdateReceiver,
                    new IntentFilter("com.yurixahri.AHRIFY_UPDATE_BACKGROUND")
            );
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onDestroy(){
        unregisterReceiver(backgroundUpdateReceiver);
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

            play_button.setOnClickListener(v -> {
                if (mediaplayer.isPlaying()){
                    mediaplayer.pausa();
                    play_button.setImageResource(R.drawable.baseline_play_arrow_24);
                }else{
                    mediaplayer.play();
                    play_button.setImageResource(R.drawable.pause);
                }
            });

            // Optional: Attach ExoPlayer Listener
            Mediaplayer.setPlayerListener(playerListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };


    private void setupSeekBar(ExoPlayer player) {
        seekBar.setMax((int) player.getDuration());

        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (!userSeeking && player.isPlaying() && player.getPlaybackState() == Player.STATE_READY && player.getPlayWhenReady()) {
                    long current = mediaplayer.getAccurateCurrentPosition();
                    seekBar.setProgress((int) current);
                }
                handler.postDelayed(this, 500); // Update every 500ms
            }
        };

        // Start updates
        handler.post(updateSeekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userSeeking = false;
                player.seekTo(seekBar.getProgress());
            }
        });
    }


    private void playNext(){
        if (mediaplayer.nextIndex()){
            mediaplayer.playSong();
        }
    }

    private void playPrevious(){
        if (mediaplayer.previousIndex()){
            mediaplayer.playSong();
        }
    }

    @Override
    public Mediaplayer getMediaService() {
        return mediaplayer;
    }

    @Override
    public void goToSongControlActivity(){
        Intent intent = new Intent(MainActivity.this, SongControlPanelActivity.class);
        startActivity(intent);
    }


    public void ToActivity2() {
        Intent intent = new Intent(MainActivity.this, SongControlPanelActivity.class);
        startActivity(intent);
    }

    public void ToSearchActivity(String search) {
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        intent.putExtra("search", search);
        startActivity(intent);
    }

    public void ToSettings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private final BroadcastReceiver backgroundUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.yurixahri.AHRIFY_UPDATE_BACKGROUND")) {
                updateBackgroundOnStart();
            }
        }
    };


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