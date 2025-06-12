package com.yurixahri.ahrify.notSingleton;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;



import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yurixahri.ahrify.R;
import com.yurixahri.ahrify.services.NotificationActionReceiver;
import com.yurixahri.ahrify.utils.BitmapCompressor;
import com.yurixahri.ahrify.utils.CustomVolley;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

public class Mediaplayer extends Service {
    public JSONArray playlist = new JSONArray();
    public String playlist_title;
    public Bitmap cover;

    public String song_title = "";
    public String song_artist = "";
    public String song_album = "";
    public String song_file = "";
    public String song_folder = "";
    public Bitmap song_cover;

    CustomVolley volley;

    public int index;
    public byte play_mode = 0;

    public short size;
    private String base_url = "https://server.yurixahri.net/";
    private String song_info_url = "https://ahrify.api.yurixahri.net/song_info";

    private long playbackStartTime = 0;
    private long positionAtStart = 0;
    private boolean isTrackingPosition = false;
    private long pausedTime = 0;
    private long totalPausedDuration = 0;


    public static final String CHANNEL_ID = "music_channel";
    public static final int NOTIFICATION_ID = 1;

    public static final String ACTION_PLAY_PAUSE = "com.yurixahri.ACTION_PLAY_PAUSE";
    public static final String ACTION_NEXT = "com.yurixahri.ACTION_NEXT";
    public static final String ACTION_PREV = "com.yurixahri.ACTION_PREV";
    private MediaSessionCompat mediaSession;


    public static ExoPlayer player;
    private final IBinder binder = new LocalBinder();

    public interface callback {
        void afterGetInfo(String url);
    }

    public class LocalBinder extends Binder {
        public Mediaplayer getService() {
            return Mediaplayer.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        volley = new CustomVolley(getApplicationContext());
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AhrifySession");
        mediaSession.setActive(true);

        RenderersFactory renderersFactory = new DefaultRenderersFactory(this)
                .setEnableAudioOffload(false) // <- disables clock drift from offloading
                .setEnableAudioTrackPlaybackParams(true); // <- uses real audio clock

        player = new ExoPlayer.Builder(this, renderersFactory).build();
        player.setPlaybackSpeed(1.0f);

        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    startPositionTracking();
                    if (isTrackingPosition) {

                    } else {
                        if (pausedTime > 0) {
                            totalPausedDuration += SystemClock.elapsedRealtime() - pausedTime;
                            pausedTime = 0;
                        }
                    }
                    isTrackingPosition = true;
                } else {
                    pausedTime = SystemClock.elapsedRealtime();
                    isTrackingPosition = false;
                }
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(NOTIFICATION_ID, buildNotification());
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    switch (play_mode) {
                        case 1:
                            if (nextIndex()) {
                                playSong();
                            }
                            break;
                        case 2:
                            play();
                            break;
                        case 3:
                            if (nextIndex()) {
                                playSong();
                            } else {
                                index = 0;
                                playSong();
                            }
                            break;
                        case 4:
                            Random r = new Random();
                            int i = r.nextInt(playlist.length());
                            index = i;
                            playSong();
                            break;
                    }
                }

            }
        });
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        // Stop player
        if (player != null) {
            player.stop();
            player.release();
        }
        stopSelf();
    }

    public interface OnMediaStartListener {
        void onMediaStarted(String url);
    }


    public void setUrl(String url, OnMediaStartListener callback) {
        Log.d("seturl", base_url + url);
        MediaItem item = MediaItem.fromUri(base_url + url);
        player.setMediaItem(item);
        player.prepare();
        player.play();
        callback.onMediaStarted(base_url + url);
    }

    public void play() {
        if (player.getPlaybackState() == Player.STATE_ENDED) {
            player.seekTo(0);
        }
        player.play();
        startPositionTracking();
    }

    public boolean previousIndex() {
        if (index > 0) {
            index--;
            return true;
        } else {
            return false;
        }
    }

    public boolean nextIndex() {
        if (index < playlist.length() - 1) {
            index++;
            return true;
        } else {
            return false;
        }
    }

    public void pausa() {
        player.pause();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public static void setPlayerListener(Player.Listener listener) {
        player.addListener(listener);
    }

    public static void removePlayerListener(Player.Listener listener) {
        if (player != null) {
            player.removeListener(listener);
        }
    }

    private Notification buildNotification() {
        int playPauseIcon = player.isPlaying() ? R.drawable.pause : R.drawable.baseline_play_arrow_24;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.music_note_aliceblue)
                .setContentTitle(song_title)
                .setContentText(song_artist)
                .setLargeIcon(song_cover != null ? song_cover :
                        BitmapFactory.decodeResource(getResources(), R.drawable.default_icon))
                .addAction(R.drawable.skip_previous, "Previous", getPendingIntent(ACTION_PREV))
                .addAction(playPauseIcon, "Play/Pause", getPendingIntent(ACTION_PLAY_PAUSE))
                .addAction(R.drawable.skip_next, "Next", getPendingIntent(ACTION_NEXT))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(player.isPlaying())
                .setOnlyAlertOnce(true);

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    public void getSongInfo(Context context, CustomVolley volley, String folder, String file, callback callback) {
        String encoded_folder = "";
        String encoded_file = "";
        try {
            encoded_folder = URLEncoder.encode(folder, "UTF-8");
            encoded_file = URLEncoder.encode(file, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //todo
        }

        String url = folder + "/" + file;
        String param = "?folder=" + encoded_folder + "&file=" + encoded_file;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, song_info_url + param, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    song_title = (response.getString("title").isEmpty()) ? file : response.getString("title");
                    song_album = (response.getString("album").isEmpty()) ? "" : response.getString("album");
                    song_artist = (response.getString("artist").isEmpty()) ? "" : response.getString("artist");
                    song_file = file;
                    song_folder = folder;
                    if (!response.getString("cover").isEmpty()) {
                        if (response.getString("cover").startsWith("http://") || response.getString("cover").startsWith("https://")) {
                            Glide.with(context)
                                    .asBitmap()
                                    .load(response.getString("cover"))
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(new CustomTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                            Bitmap resizedBitmap = BitmapCompressor.resizeKeepRatio(resource, 1000, 1000);
                                            Bitmap compressedBitmap = BitmapCompressor.compress(resizedBitmap, BitmapCompressor.Format.JPEG, 70);
                                            song_cover = compressedBitmap;
                                            callback.afterGetInfo(url);
                                        }

                                        @Override
                                        public void onLoadCleared(@Nullable Drawable placeholder) {
                                        }
                                    });
                        } else {
                            String mBase64string = response.getString("cover").split("[,]")[1];
                            byte[] decodedString = Base64.decode(mBase64string, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            Bitmap resizedBitmap = BitmapCompressor.resizeKeepRatio(decodedByte, 1000, 1000);
                            Bitmap compressedBitmap = BitmapCompressor.compress(resizedBitmap, BitmapCompressor.Format.JPEG, 70);
                            song_cover = compressedBitmap;
                            callback.afterGetInfo(url);
                        }

                    } else {
                        song_cover = null;
                        callback.afterGetInfo(url);
                    }
                } catch (JSONException e) {
                    callback.afterGetInfo(url);
                    throw new RuntimeException(e);

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                //Log.e("albums", "Main request failed: " + e.getMessage());
            }
        });

        volley.getRequestQueue().add(request);
    }

    public void playSong() {
        try {
            JSONObject object = playlist.getJSONObject(index);
            getSongInfo(this, volley, object.getString("folder"), object.getString("file_name"), new Mediaplayer.callback() {
                @Override
                public void afterGetInfo(String url) {
                    setUrl(url, new Mediaplayer.OnMediaStartListener() {
                        @Override
                        public void onMediaStarted(String url) {
                            startForeground(NOTIFICATION_ID, buildNotification());
                        }
                    });

                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void startPositionTracking() {
        positionAtStart = player.getCurrentPosition();
        playbackStartTime = SystemClock.elapsedRealtime();
        pausedTime = 0;
        totalPausedDuration = 0;
        isTrackingPosition = true;
    }

    public long getAccurateCurrentPosition() {
        if (!isTrackingPosition) {
            return positionAtStart;
        }
        long elapsed = SystemClock.elapsedRealtime() - playbackStartTime - totalPausedDuration;
        return Math.max(positionAtStart + elapsed, 0);
    }

    public void updateSeekPosition(long newPositionMs) {
        positionAtStart = newPositionMs;
        playbackStartTime = SystemClock.elapsedRealtime();
    }

    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(this, NotificationActionReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(this, action.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE);
    }

}
