package com.yurixahri.ahrify;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yurixahri.ahrify.adapters.PlaylistListView;
import com.yurixahri.ahrify.models.playlist;
import com.yurixahri.ahrify.models.playlistSong;
import com.yurixahri.ahrify.notSingleton.DBSingleton;
import com.yurixahri.ahrify.notSingleton.Mediaplayer;
import com.yurixahri.ahrify.sqlite.PlaylistDAO;
import com.yurixahri.ahrify.sqlite.PlaylistSongDAO;
import com.yurixahri.ahrify.utils.BitmapCompressor;
import com.yurixahri.ahrify.utils.CustomVolley;
import com.yurixahri.ahrify.utils.TimeFormat;
import com.yurixahri.ahrify.utils.UnitConverter;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;

public class SongControlPanelActivity extends AppCompatActivity {
    ImageButton button;
    ImageButton song_details;
    ImageButton current_playlist;
    ShapeableImageView song_image;
    TextView song_title;
    ImageButton play_button;
    ImageButton next_button;
    ImageButton previous_button;

    ImageButton play_mode;
    ImageButton playlist_select;
    Mediaplayer mediaplayer;
    boolean isBound = false;

    SeekBar seekBar;
    TextView current_time;
    TextView duration;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBar;
    private boolean userSeeking = false;

    CustomVolley volley;

    SharedPreferences prefs;

    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            if (mediaplayer.isPlaying()){
                play_button.setImageResource(R.drawable.pause);
            }else{
                //
                play_button.setImageResource(R.drawable.baseline_play_arrow_24);
            }
            Log.d("MusicService", "Playing: " + isPlaying);
        }

        @Override
        public void onMediaItemTransition(MediaItem mediaItem, int reason){
//            if (mediaplayer.song_cover != null) song_image.setImageBitmap(mediaplayer.song_cover);
//            song_title.setText(mediaplayer.song_title);
        }

        @Override
        public void onPlaybackStateChanged(int state) {
            if (state == Player.STATE_READY) {
                handler.removeCallbacks(updateSeekBar);
                setupSeekBar(mediaplayer.player);
                if (mediaplayer.song_cover != null){
                    song_image.setImageBitmap(mediaplayer.song_cover);
                }else{
                    song_image.setImageResource(R.drawable.default_icon);
                }
                song_title.setText(mediaplayer.song_title);
            }
        }


    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.song_control_panel_activity);

        Intent intent = new Intent(this, Mediaplayer.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        updateBackgroundOnStart();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().setNavigationBarColor(Color.BLACK);
            getWindow().setNavigationBarDividerColor(Color.BLACK);
        }

        volley = new CustomVolley(this);

        button = findViewById(R.id.back_button);
        song_title = findViewById(R.id.song_title);
        play_button = findViewById(R.id.play_button);
        next_button = findViewById(R.id.next_song);
        previous_button = findViewById(R.id.previous_song);
        seekBar = findViewById(R.id.seekBar);
        current_time = findViewById(R.id.current_time);
        duration = findViewById(R.id.duration);
        play_mode = findViewById(R.id.play_mode);
        playlist_select = findViewById(R.id.playlist_select);


        button.setOnClickListener(this::endAcitvity);

        song_image = findViewById(R.id.image);
        song_image.post(() -> {
            int width = song_image.getWidth();
            ViewGroup.LayoutParams params = song_image.getLayoutParams();
            params.height = width - (int) UnitConverter.dpToPx(150, this);
            params.width = width - (int) UnitConverter.dpToPx(150, this);
            song_image.setLayoutParams(params);
        });

        next_button.setOnClickListener(v -> playNext());
        previous_button.setOnClickListener(v -> playPrevious());

        song_details = findViewById(R.id.song_details);
        song_details.setOnClickListener(this::ToSongDetailsActivity);

        current_playlist  = findViewById(R.id.current_playlist);
        current_playlist.setOnClickListener(this::ToPlaylist);



        play_mode.setOnClickListener(v -> {
            mediaplayer.play_mode = (mediaplayer.play_mode + 1 <= 4) ? (byte) (mediaplayer.play_mode + 1) : 0;
            updatePlayMode();

        });

        playlist_select.setOnClickListener(v -> {
            Context context = this;
            View bottom_sheet_dialog_view = LayoutInflater.from(context).inflate(R.layout.playlist_select, null);
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
            bottomSheetDialog.setContentView(bottom_sheet_dialog_view);
            bottomSheetDialog.show();

            ListView list_view = bottom_sheet_dialog_view.findViewById(R.id.list_view);

            PlaylistDAO playlistDAO = new PlaylistDAO(context);
            PlaylistSongDAO playlistSongDAO = new PlaylistSongDAO(context);

            DBSingleton db = DBSingleton.getInstance();

            list_view.setAdapter(db.adapter_add_song);

            db.updatePlaylists(playlistDAO.getAll());

            ImageButton add_playlist = bottom_sheet_dialog_view.findViewById(R.id.add_playlist);

            add_playlist.setOnClickListener(v1 -> {
                View add_view = LayoutInflater.from(context).inflate(R.layout.add_playlist_form, null);
                BottomSheetDialog add_view_dialog = new BottomSheetDialog(context);
                add_view_dialog.setContentView(add_view);
                add_view_dialog.show();

                Button submit = add_view.findViewById(R.id.submit);
                EditText edit_text = add_view.findViewById(R.id.inputName);
                submit.setOnClickListener(v2 -> {
                    playlist playlist = new playlist();
                    if (String.valueOf(edit_text.getText()).isEmpty()){
                        Toast.makeText(context, "Please enter a name", Toast.LENGTH_SHORT).show();
                    }else {
                        playlist.title = String.valueOf(edit_text.getText());
                        playlistDAO.insert(playlist);
                        db.updatePlaylists(playlistDAO.getAll());

                        Toast.makeText(context, "Added a new playlist", Toast.LENGTH_SHORT).show();
                        add_view_dialog.dismiss();
                    }
                });
            });

            db.adapter_add_song.setOnItemClickListener(new PlaylistListView.OnItemClickListener() {
                @Override
                public void onItemClick(playlist item, int position) {
                    if (!mediaplayer.song_file.isEmpty()){
                        playlistSong playlistSong = new playlistSong(
                                mediaplayer.song_file,
                                mediaplayer.song_folder,
                                mediaplayer.song_title,
                                BitmapCompressor.bitmapToByteArray(mediaplayer.song_cover),
                                item.id);

                        if (playlistSongDAO.insert(playlistSong) == -1){
                            Toast.makeText(context, "Song already exists in playlist", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(context, "Song added to playlist", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onItemLongClick(playlist item, int position) {

                }
            });

        });



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
        handler.removeCallbacks(updateSeekBar);
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
            updatePlayMode();

            if (mediaplayer.player.getCurrentMediaItem() != null) setupSeekBar(mediaplayer.player);
            if (mediaplayer.song_cover != null) song_image.setImageBitmap(mediaplayer.song_cover);
            song_title.setText(mediaplayer.song_title);

            if (mediaplayer.isPlaying()){
                play_button.setImageResource(R.drawable.pause);
            }else{
                play_button.setImageResource(R.drawable.baseline_play_arrow_24);
            }

            play_button.setOnClickListener(v -> {
                if (mediaplayer.isPlaying()){
                    mediaplayer.pausa();
                    play_button.setImageResource(R.drawable.baseline_play_arrow_24);
                }else{
                    mediaplayer.play();
                    play_button.setImageResource(R.drawable.pause);
                }
            });


            Mediaplayer.setPlayerListener(playerListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private void setupSeekBar(ExoPlayer player) {
        seekBar.setMax((int) player.getDuration());
        if (player.getDuration() >= 0) duration.setText(TimeFormat.msToString(player.getDuration()));

        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (!userSeeking && player.isPlaying() && player.getPlaybackState() == Player.STATE_READY && player.getPlayWhenReady()) {
                    long current = mediaplayer.getAccurateCurrentPosition();
                    seekBar.setProgress((int) current);
                    current_time.setText(TimeFormat.msToString(current));
                }

                handler.postAtTime(this, SystemClock.uptimeMillis() + 500);
            }
        };


        handler.post(updateSeekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    current_time.setText(TimeFormat.msToString(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userSeeking = false;
                player.seekTo(seekBar.getProgress());
                mediaplayer.updateSeekPosition(seekBar.getProgress());
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

    private void updatePlayMode(){
        switch (mediaplayer.play_mode){
            case 0:
                play_mode.setImageResource(R.drawable.baseline_stop_24);
                break;
            case 1:
                play_mode.setImageResource(R.drawable.right_double_arrow);
                break;
            case 2:
                play_mode.setImageResource(R.drawable.loop_current);
                break;
            case 3:
                play_mode.setImageResource(R.drawable.loop);
                break;
            case 4:
                play_mode.setImageResource(R.drawable.random);
                break;
        }
    }


    public void ToSongDetailsActivity(View view) {
        Intent intent = new Intent(this, SongDetailsActivity.class);
        startActivity(intent);
    }

    public void ToPlaylist(View view) {
        Intent intent = new Intent(this, PlaylistActivity.class);
        startActivity(intent);
    }

    public void endAcitvity(View view) {
        handler.removeCallbacks(updateSeekBar);
        Mediaplayer.removePlayerListener(playerListener);
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
