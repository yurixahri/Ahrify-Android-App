package com.yurixahri.ahrify;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yurixahri.ahrify.adapters.ArtistsView;
import com.yurixahri.ahrify.interfaces.MainIterface;
import com.yurixahri.ahrify.models.artist;
import com.yurixahri.ahrify.notSingleton.Artists;
import com.yurixahri.ahrify.notSingleton.Mediaplayer;
import com.yurixahri.ahrify.utils.CustomVolley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ArtistsFragment extends Fragment {
    Artists artists = new Artists();
    ArrayList<artist> artist_model_list = new ArrayList<>();
    ArrayList<artist> artist_song_model_list = new ArrayList<>();
    CustomVolley volley;
    ProgressBar spinner;
    ImageButton back_button;

    Mediaplayer mediaplayer;
    MainIterface mainIterface;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.artists_fragment, container, false);

        spinner = v.findViewById(R.id.loadingSpinner);
        volley = new CustomVolley(v.getContext());

        if (getArguments() != null) {
            artists.search = getArguments().getString("search");
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
        ListView view = new ListView(v.getContext());
        view.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        FrameLayout container = v.findViewById(R.id.view_container);

        ArtistsView adapter = new ArtistsView(getContext(), R.layout.default_listview_item, artist_model_list);

        view.setAdapter(adapter);


        view.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (!artists.isLoading && (firstVisibleItem + visibleItemCount >= totalItemCount - 2)) {
                    artists.isLoading = true;
                    loadMoreAlbums(adapter, v); // load next batch
                }
            }
        });
        container.addView(view);
    }

    private void loadMoreAlbums(ArtistsView adapter, View v) {

        spinner.setVisibility(View.VISIBLE);


        artists.getArtists(volley, new Artists.ArtistListCallback() {
            @Override
            public void onListLoaded() {
                try {
                    for (int i=(artists.currentPage-1)*artists.ITEMS_PER_PAGE; i < artists.artists.length(); i++){
                        try {
                            JSONObject item = artists.artists.getJSONObject(i);
                            String text = item.getString("name");
                            artist_model_list.add(new artist(R.drawable.person_aliceblue, text, false));
                            //Log.d("item", item.getString("cover"));
                        }catch (Exception e){
                            Log.e("item", e.getMessage());
                        }

                    }

                    adapter.setOnItemClickListener((item, position) -> {
                        if (!item.is_file && artists.isClickable) {
                            artists.isClickable = false;
                            artists.current_artist = item;
                            getSongs(item, v);
                        }
                    });

                    // Notify the adapter that data has changed
                    adapter.notifyDataSetChanged();
                    artists.currentPage++;
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

    private void getSongs(artist artist, View v){
        artist_song_model_list.clear();

        ListView list_view = new ListView(v.getContext());
        list_view.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        TextView header = new TextView(v.getContext());
        header.setText(artist.text); // Or "Songs", or any title
        header.setTextSize(18f);
        header.setPadding(20, 20, 10, 20);
        header.setTextColor(getResources().getColor(R. color. aliceblue));
        list_view.addHeaderView(header);




        FrameLayout container = v.findViewById(R.id.view_container);

        if (container.getChildCount() > 0) {
            View currentTop = container.getChildAt(container.getChildCount() - 1);
            currentTop.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(200)
                    .start();
        }

        artists.getSongs(volley, artist.text, new Artists.ArtistListCallback() {
            @Override
            public void onListLoaded() {}

            @Override
            public void onSongsLoaded() {
                for (short i = 0; i < artists.files.length(); i++){
                    try {
                        JSONObject item = artists.files.getJSONObject(i);
                        Log.d("song", item.getString("file_name"));
                        String text = (item.getString("song_name").isEmpty()) ? item.getString("file_name") : item.getString("song_name");
                        artist_song_model_list.add(new artist(R.drawable.music_file, text, true));


                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                Log.d("song length", String.valueOf(artists.files.length()));

                ArtistsView adapter = new ArtistsView(v.getContext(), R.layout.default_listview_item, artist_song_model_list);
                list_view.setAdapter(adapter);

                adapter.setOnItemClickListener((item, position) -> {
                    if (item.is_file) {
                        if (mainIterface.getMediaService() != null){
                            mediaplayer = mainIterface.getMediaService();
                            if (!mediaplayer.isLoading){
                                try {
                                    mediaplayer.isLoading = true;
                                    JSONObject object = artists.files.getJSONObject(position);
                                    mediaplayer.playlist = artists.files;
                                    mediaplayer.index = position;
                                    mediaplayer.cover = null;
                                    mediaplayer.playlist_title = artists.current_artist.text;
                                    mediaplayer.getSongInfo(getContext(), volley, object.getString("folder"), object.getString("file_name"), new Mediaplayer.callback() {
                                        @Override
                                        public void afterGetInfo(String url) {
                                            mediaplayer.setUrl(url, new Mediaplayer.OnMediaStartListener() {
                                                @Override
                                                public void onMediaStarted(String url) {
                                                    mediaplayer.isLoading = false;
                                                    mainIterface.goToSongControlActivity();
                                                }
                                            });

                                        }
                                    });
                                } catch (JSONException e) {
                                    mediaplayer.isLoading = false;
                                    throw new RuntimeException(e);
                                }
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
                        .withEndAction(()->{artists.isClickable = true;})
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
        }
    }
}
