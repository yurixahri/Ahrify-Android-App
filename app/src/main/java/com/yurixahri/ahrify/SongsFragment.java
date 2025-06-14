package com.yurixahri.ahrify;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yurixahri.ahrify.adapters.DefaultListView;

import com.yurixahri.ahrify.interfaces.MainIterface;
import com.yurixahri.ahrify.models.album;
import com.yurixahri.ahrify.models.defaultListItem;

import com.yurixahri.ahrify.notSingleton.Mediaplayer;
import com.yurixahri.ahrify.notSingleton.Songs;
import com.yurixahri.ahrify.utils.BitmapCompressor;
import com.yurixahri.ahrify.utils.CustomVolley;
import com.yurixahri.ahrify.utils.GlideHelper;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Future;

public class SongsFragment extends Fragment {
    Songs songs = new Songs();
    ArrayList<defaultListItem> song_list = new ArrayList<>();

    CustomVolley volley;
    ProgressBar spinner;
    ListView list_view;
    Mediaplayer mediaplayer;
    MainIterface mainIterface;

    boolean isClickable = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.songs_fragment, container, false);
        if (getArguments() != null) {
            songs.search = getArguments().getString("search");
            // Use this query to filter content
        }

        if (requireContext() instanceof MainIterface) {
            mainIterface = (MainIterface) requireContext();
        }

        spinner = v.findViewById(R.id.loadingSpinner);
        list_view = v.findViewById(R.id.list_view);

        volley = new CustomVolley(v.getContext());

        setAdapter(v);

        return v;
    }

    @Override
    public void onStop() {
        super.onStop();
        volley.getRequestQueue().cancelAll("request");
    }

    private void setAdapter(View v){


        FrameLayout container = v.findViewById(R.id.view_container);

        DefaultListView adapter = new DefaultListView(getContext(), R.layout.default_listview_item, song_list);

        list_view.setAdapter(adapter);


        list_view.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (!songs.isLoading && (firstVisibleItem + visibleItemCount >= totalItemCount - 2)) {
                    songs.isLoading = true;
                    loadMoreAlbums(adapter, v); // load next batch
                }
            }
        });

    }

    private void loadMoreAlbums(DefaultListView adapter, View v) {

        spinner.setVisibility(View.VISIBLE);

        songs.getSongs(volley, new Songs.SongListCallback() {
            @Override
            public void onListLoaded() {
                try {
                    for (int i=(songs.currentPage-1)*songs.ITEMS_PER_PAGE; i < songs.songs.length(); i++){
                        try {
                            JSONObject item = songs.songs.getJSONObject(i);
                            int index = i;
                            String text = !item.getString("title").isEmpty() ? item.getString("title") : item.getString("file_url");
                            String cover = !item.getString("cover64").isEmpty() ? item.getString("cover64") :item.getString("cover");
                            Log.d("item", "name: "+text);
                            Log.d("item", "cover: "+cover);
                            if (cover.isEmpty()){
                                song_list.add(new defaultListItem(text, R.drawable.file_present_aliceblue, null, index));
                            }else{
                                if (item.getString("cover").startsWith("http://") || item.getString("cover").startsWith("https://")){

                                    Glide.with(requireContext())
                                            .asBitmap()
                                            .load(item.getString("cover"))
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .into(new CustomTarget<Bitmap>() {
                                                @Override
                                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                    Bitmap resizedBitmap = BitmapCompressor.resizeKeepRatio(resource, 500, 500);
                                                    Bitmap compressedBitmap = BitmapCompressor.compress(resizedBitmap, BitmapCompressor.Format.JPEG, 50);
                                                    song_list.add(new defaultListItem(text, R.drawable.file_present_aliceblue, compressedBitmap, index));
                                                    adapter.notifyDataSetChanged();
                                                }
                                                @Override
                                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                                }
                                            });
                                }else{
                                    String mBase64string = item.getString("cover").split("[,]")[1];
                                    byte[] decodedString = Base64.decode(mBase64string, Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0,decodedString.length);
                                    Bitmap resizedBitmap = BitmapCompressor.resizeKeepRatio(decodedByte, 500, 500);
                                    Bitmap compressedBitmap = BitmapCompressor.compress(resizedBitmap, BitmapCompressor.Format.JPEG, 50);
                                    song_list.add(new defaultListItem(text, R.drawable.file_present_aliceblue, compressedBitmap, index));
                                    adapter.notifyDataSetChanged();
                                }
                            }
                            //Log.d("item", item.getString("cover"));
                        }catch (Exception e){
                            Log.e("item", e.getMessage());
                        }

                    }

                    adapter.setOnItemClickListener(new DefaultListView.OnItemClickListener() {
                        @Override
                        public void onItemClick(defaultListItem item, int position) {

                                if (mainIterface.getMediaService() != null) {
                                    mediaplayer = mainIterface.getMediaService();
                                    if (!mediaplayer.isLoading){
                                        try {
                                            mediaplayer.isLoading = true;
                                            mediaplayer.index = 0;
                                            JSONArray array = new JSONArray();
                                            JSONObject object = new JSONObject();

                                            object.put("file_name", songs.songs.getJSONObject(item.index).getString("file_url"));
                                            object.put("song_name", songs.songs.getJSONObject(item.index).getString("title"));
                                            object.put("folder", songs.songs.getJSONObject(item.index).getString("folder_name"));
                                            object.put("cover", item.cover);

                                            array.put(object);
                                            mediaplayer.playlist = array;
                                            mediaplayer.playlist_title = "";
                                            mediaplayer.playSong();
                                            mainIterface.goToSongControlActivity();
                                        } catch (JSONException e) {
                                            mediaplayer.isLoading = false;
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }

                        }
                        @Override
                        public void onItemLongClick(defaultListItem item, int position) {
                        }
                    });


                    adapter.notifyDataSetChanged();
                    songs.currentPage++;
                } catch (Exception e) {
                    Log.e("lazyload", e.getMessage());
                } finally {
                    spinner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSongsLoaded() {

            }
        });
    }

}
