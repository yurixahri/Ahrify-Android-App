package com.yurixahri.ahrify;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.yurixahri.ahrify.adapters.PlaylistListView;
import com.yurixahri.ahrify.adapters.PlaylistSongListView;
import com.yurixahri.ahrify.interfaces.MainIterface;
import com.yurixahri.ahrify.models.playlist;
import com.yurixahri.ahrify.models.playlistSong;
import com.yurixahri.ahrify.notSingleton.DBSingleton;
import com.yurixahri.ahrify.notSingleton.Mediaplayer;
import com.yurixahri.ahrify.sqlite.PlaylistDAO;
import com.yurixahri.ahrify.sqlite.PlaylistSongDAO;
import com.yurixahri.ahrify.utils.BitmapCompressor;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlaylistsFragment extends Fragment {
    DBSingleton db = DBSingleton.getInstance();
    ImageView place_holder;
    ImageButton add_playlist;
    ImageButton back_button;
    ListView list_view;

    Mediaplayer mediaplayer;
    MainIterface mainIterface;

    boolean isClickable = true;
    String current_playlist_name;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.playlists_fragment, container, false);

        if (requireContext() instanceof MainIterface) {
            mainIterface = (MainIterface) requireContext();
        }

        place_holder = v.findViewById(R.id.place_holder);
        add_playlist = v.findViewById(R.id.add_playlist);
        list_view   = v.findViewById(R.id.list_view);
        back_button = v.findViewById(R.id.back_button);

        PlaylistDAO playlistDAO = new PlaylistDAO(requireContext());

        back_button.setOnClickListener(v1 -> goBack());

        add_playlist.setOnClickListener(v1 -> {
            View bottom_sheet_dialog_view = LayoutInflater.from(requireContext()).inflate(R.layout.add_playlist_form, null);
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
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
                    Toast.makeText(getContext(), "Please enter a name", Toast.LENGTH_SHORT).show();
                }else {
                    playlist playlist = new playlist();
                    playlist.title = String.valueOf(edit_text.getText());
                    playlistDAO.insert(playlist);
                    updateListView();
                    Toast.makeText(requireContext(), "Added a new playlist", Toast.LENGTH_SHORT).show();
                    bottomSheetDialog.dismiss();
                }
            });
        });

        updateListView();

        return v;
    }

    private void updateListView(){
        PlaylistDAO playlistDAO = new PlaylistDAO(requireContext());
        list_view.setAdapter(db.adapter);
        db.updatePlaylists(playlistDAO.getAll());

        db.adapter.setOnItemClickListener(new PlaylistListView.OnItemClickListener() {
            @Override
            public void onItemClick(playlist item, int position) {
                if (isClickable) {
                    isClickable = false;
                    current_playlist_name = item.title;
                    getSongs(item);
                }
            }
            @Override
            public void onItemLongClick(playlist item, int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setMessage("Do you want to delete "+item.title+" playlist ?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            playlistDAO.delete(item.id);
                            db.playlists.remove(position);
                            db.adapter.notifyDataSetChanged();
                            checkVisibility();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                    });

                AlertDialog confirm = builder.create();
                confirm.show();


            }
        });

        checkVisibility();


        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                backgroundUpdateReceiver,
                new IntentFilter("com.yurixahri.AHRIFY_UPDATE_PLAYLIST")
        );

    }

    private void getSongs(playlist playlist){
        db.playlistSongs.clear();
        Context context = requireContext();

        PlaylistSongDAO playlistSongDAO = new PlaylistSongDAO(context);
        db.updatePlaylistSongs(playlistSongDAO.getAll(playlist.id));

        ListView list_view = new ListView(context);
        list_view.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        TextView header = new TextView(context);
        header.setText(playlist.title); // Or "Songs", or any title
        header.setTextSize(18f);
        header.setPadding(20, 20, 10, 20);
        header.setTextColor(getResources().getColor(R. color. aliceblue));
        list_view.addHeaderView(header);

        FrameLayout container = requireView().findViewById(R.id.view_container);

        if (container.getChildCount() > 0) {
            View currentTop = container.getChildAt(container.getChildCount() - 1);
            currentTop.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(200)
                    .start();
        }

        list_view.setAdapter(db.adapter_playlist_song);

        db.adapter_playlist_song.setOnItemClickListener(new PlaylistSongListView.OnItemClickListener() {
            @Override
            public void onItemClick(playlistSong item, int position) {
                    if (mainIterface.getMediaService() != null) {
                        mediaplayer = mainIterface.getMediaService();
                        if (!mediaplayer.isLoading){
                            mediaplayer.isLoading = true;
                            JSONArray playlist = new JSONArray();
                            for (int i = 0; i < db.playlistSongs.size(); i++){
                                JSONObject object = new JSONObject();
                                try {
                                    object.put("file_name", db.playlistSongs.get(i).file);
                                    object.put("song_name", db.playlistSongs.get(i).title);
                                    object.put("folder", db.playlistSongs.get(i).folder);
                                    if (db.playlistSongs.get(i).cover != null) object.put("cover", BitmapCompressor.blobToBitmap(db.playlistSongs.get(i).cover));
                                    playlist.put(object);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            mediaplayer.playlist = playlist;
                            mediaplayer.index = position;
                            mediaplayer.playlist_title = current_playlist_name;

                            mediaplayer.playSong();
                            mainIterface.goToSongControlActivity();
                        }
                    }

            }

            @Override
            public void onItemLongClick(playlistSong item, int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setMessage("Do you want to remove this song ?")
                        .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                playlistSongDAO.delete(item);
                                db.playlistSongs.remove(position);
                                db.adapter_playlist_song.notifyDataSetChanged();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                AlertDialog confirm = builder.create();
                confirm.show();
            }
        });

        list_view.setAlpha(0f);
        list_view.setTranslationY(requireView().getHeight());

        container.addView(list_view);
        list_view.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(200)
                .withEndAction(()->{isClickable = true;})
                .start();

    }

    private void goBack() {
        FrameLayout container = requireView().findViewById(R.id.view_container);
        if (container.getChildCount() > 1) {
            View top = container.getChildAt(container.getChildCount() - 1);
            View below = container.getChildAt(container.getChildCount() - 2);

            // Fade out top view
            top.animate()
                    .alpha(0f)
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        container.removeView(top); // Remove after fade
                    }).start();

            // Fade in the one underneath
            below.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200).start();
        }
    }

    private void checkVisibility(){
        if (!db.playlists.isEmpty()){
            place_holder.setVisibility(View.GONE);
            list_view.setVisibility(View.VISIBLE);
        }else {
            place_holder.setVisibility(View.VISIBLE);
            list_view.setVisibility(View.GONE);
        }
    }


    private final BroadcastReceiver backgroundUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.yurixahri.AHRIFY_UPDATE_PLAYLIST")) {
                checkVisibility();
            }
        }
    };
}