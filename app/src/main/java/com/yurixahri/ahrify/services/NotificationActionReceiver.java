package com.yurixahri.ahrify.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yurixahri.ahrify.notSingleton.Mediaplayer;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Mediaplayer service = ((Mediaplayer.LocalBinder) peekService(context, new Intent(context, Mediaplayer.class))).getService();
        switch (intent.getAction()) {
            case Mediaplayer.ACTION_PLAY_PAUSE:
                if (Mediaplayer.player.isPlaying()) {
                    service.pausa();
                } else {
                    service.play();
                }
                break;
            case Mediaplayer.ACTION_NEXT:
                if (service.nextIndex()) {
                    service.playSong();
                }
                break;
            case Mediaplayer.ACTION_PREV:
                if (service.previousIndex()) {
                    service.playSong();
                }
                break;
        }
    }
}
