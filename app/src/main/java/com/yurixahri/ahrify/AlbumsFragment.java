package com.yurixahri.ahrify;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yurixahri.ahrify.adapters.AlbumsView;

import com.yurixahri.ahrify.interfaces.MainIterface;
import com.yurixahri.ahrify.models.album;

import com.yurixahri.ahrify.notSingleton.Albums;

import com.yurixahri.ahrify.notSingleton.Mediaplayer;
import com.yurixahri.ahrify.utils.BitmapCompressor;
import com.yurixahri.ahrify.utils.CustomVolley;
import com.yurixahri.ahrify.utils.GlideHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Future;

public class AlbumsFragment extends Fragment {
    Albums albums = new Albums();
    ArrayList<album> album_model_list = new ArrayList<>();
    ArrayList<album> album_song_model_list = new ArrayList<>();
    CustomVolley volley;
    ProgressBar spinner;
    ImageButton back_button;

    Mediaplayer mediaplayer;
    MainIterface mainIterface;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v;
        v = inflater.inflate(R.layout.albums_fragment, container, false);
        spinner = v.findViewById(R.id.loadingSpinner);
        volley = new CustomVolley(v.getContext());

        if (getArguments() != null) {
            albums.search = getArguments().getString("search");
            // Use this query to filter content
        }

        if (requireContext() instanceof MainIterface) {
            mainIterface = (MainIterface) requireContext();
        }



        setAdapter(v);

        back_button = v.findViewById(R.id.back_button);
        back_button.setOnClickListener(v1 -> {
                goBack();
        });

        return v;
    }

    @Override
    public void onStop() {
        super.onStop();
        volley.getRequestQueue().cancelAll("request");
    }

    private void setAdapter(View v){
        GridView view = new GridView(v.getContext());
        view.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        view.setGravity(Gravity.CENTER);
        view.setHorizontalSpacing(10);
        view.setVerticalSpacing(24);

        view.setNumColumns(3);
        //view.setColumnWidth(view.getWidth()/4);

        FrameLayout container = v.findViewById(R.id.view_container);

        AlbumsView adapter = new AlbumsView(getContext(), R.layout.album_item, album_model_list);

        view.setAdapter(adapter);


        view.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (!albums.isLoading && (firstVisibleItem + visibleItemCount >= totalItemCount - 2)) {

                    loadMoreAlbums(adapter, v);
                }
            }
        });
        container.addView(view);


        //loadMoreAlbums(adapter);


    }

    private void loadMoreAlbums(AlbumsView adapter, View v) {

        spinner.setVisibility(View.VISIBLE);
        albums.isLoading = true;

        albums.getAlbums(volley, new Albums.AlbumListCallback() {
            @Override
            public void onListLoaded() {
                try {
                    for (int i=(albums.currentPage-1)*albums.ITEMS_PER_PAGE; i < albums.albums.length(); i++){
                        try {
                            JSONObject item = albums.albums.getJSONObject(i);
                            String text = item.getString("name");
                            if (item.getString("cover").isEmpty()){
                                album_model_list.add(new album(R.drawable.default_icon, null, text,false));
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
                                                    album_model_list.add(new album(R.drawable.default_icon, compressedBitmap, text, false));
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
                                    album_model_list.add(new album(R.drawable.default_icon, compressedBitmap, text, false));

                                }
                            }

                            //Log.d("item", item.getString("cover"));
                        }catch (Exception e){
                            Log.e("item", e.getMessage());
                        }

                    }

                    adapter.setOnItemClickListener((item, position) -> {
                        if (!item.is_file) {
                            albums.current_album = item;
                            getSongs(item, v);
                        }
                    });

                    // Notify the adapter that data has changed
                    adapter.notifyDataSetChanged();
                    albums.currentPage++;
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


    private void getSongs(album folder, View v){
        album_song_model_list.clear();

        ListView list_view = new ListView(v.getContext());
        list_view.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        TextView header = new TextView(v.getContext());
        header.setText(folder.text); // Or "Songs", or any title
        header.setTextSize(18f);
        header.setPadding(20, 20, 10, 20);
        header.setTextColor(getResources().getColor(R. color. aliceblue));
        list_view.addHeaderView(header);




        FrameLayout container = requireView().findViewById(R.id.view_container);

        if (container.getChildCount() > 0) {
            View currentTop = container.getChildAt(container.getChildCount() - 1);
            currentTop.setClickable(false);
            currentTop.setEnabled(false);
            currentTop.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(200)
                    .start();
        }

        albums.getSongs(volley, folder.text, new Albums.AlbumListCallback() {
            @Override
            public void onListLoaded() {}

            @Override
            public void onSongsLoaded() {
                for (short i = 0; i < albums.files.length(); i++){
                    try {
                        JSONObject item = albums.files.getJSONObject(i);
                        Log.d("song", item.getString("file_name"));
                        String text = (item.getString("song_name").isEmpty()) ? item.getString("file_name") : item.getString("song_name");

                        if (folder.cover != null){
                            album_song_model_list.add(new album(R.drawable.default_icon, folder.cover, text, true));
                        }else{
                            album_song_model_list.add(new album(R.drawable.default_icon, null, text, true));
                        }


                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                Log.d("song length", String.valueOf(albums.files.length()));

                AlbumsView adapter = new AlbumsView(v.getContext(), R.layout.default_listview_item, album_song_model_list);
                list_view.setAdapter(adapter);

                adapter.setOnItemClickListener((item, position) -> {
                    if (item.is_file) {
                        if (mainIterface.getMediaService() != null){
                            mediaplayer = mainIterface.getMediaService();
                            try {
                                JSONObject object = albums.files.getJSONObject(position);
                                mediaplayer.playlist = albums.files;
                                mediaplayer.cover = item.cover;
                                mediaplayer.index = position;
                                mediaplayer.getSongInfo(getContext(), volley, object.getString("folder"), object.getString("file_name"), new Mediaplayer.callback() {
                                    @Override
                                    public void afterGetInfo(String url) {
                                        mediaplayer.setUrl(url, new Mediaplayer.OnMediaStartListener() {
                                            @Override
                                            public void onMediaStarted(String url) {
                                                mainIterface.goToSongControlActivity();
                                            }
                                        });
                                    }
                                });
                                Log.d("play", "onSongsLoaded: ");
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    }
                });

                list_view.setAlpha(0f);
                list_view.setTranslationY(requireView().getHeight());

                container.addView(list_view);
                list_view.animate()
                        .alpha(1f)
                        .translationY(0)
                        .setDuration(200)
                        .start();

            }
        });
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
            below.setClickable(true);
            below.setEnabled(true);
        }
    }


}
