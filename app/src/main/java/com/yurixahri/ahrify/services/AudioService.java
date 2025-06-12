package com.yurixahri.ahrify.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import com.yurixahri.ahrify.MainActivity;

public class AudioService extends MediaSessionService {
    private ExoPlayer player;
    private MediaSession mediaSession;

    @Override
    public void onCreate() {
        super.onCreate();
        player = new ExoPlayer.Builder(this).build();

        mediaSession = new MediaSession.Builder(this, player)
                .setSessionActivity(createSessionActivity())
                .build();

        player.setPlayWhenReady(true);
    }

    private PendingIntent createSessionActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        int flags = Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0;
        return PendingIntent.getActivity(this, 0, intent, flags);
    }

    @Nullable
    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    @Override
    public void onDestroy() {
        mediaSession.release();
        player.release();
        super.onDestroy();
    }
}