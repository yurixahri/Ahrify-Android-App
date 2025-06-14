package com.yurixahri.ahrify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.yurixahri.ahrify.adapters.FoldersListView;
import com.yurixahri.ahrify.interfaces.MainIterface;
import com.yurixahri.ahrify.models.folder;
import com.yurixahri.ahrify.notSingleton.Folders;
import com.yurixahri.ahrify.notSingleton.Mediaplayer;
import com.yurixahri.ahrify.utils.CustomVolley;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class FoldersFragment extends Fragment {

    Folders folders = Folders.getInstance();

    ArrayList<ArrayList<folder>> folders_list = new ArrayList<>();
    //FoldersListView adapter;
    //ListView list_view;
    CustomVolley volley;
    ImageButton back_button;
    Mediaplayer mediaplayer;
    MainIterface mainIterface;


//    int fadeDuration = 100;
//    AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
//    AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        folders_list.clear();
        folders.folders.clear();
        folders.files.clear();
        folders.current_path.clear();
        folders.previous_path.clear();

        View v;
        v = inflater.inflate(R.layout.folders_fragment, container, false);

        volley = new CustomVolley(v.getContext());

        if (requireContext() instanceof MainIterface) {
            mainIterface = (MainIterface) requireContext();
        }


        openFolder(v);

        back_button = v.findViewById(R.id.folders_back_button);
        back_button.setOnClickListener(v1 -> {
            if(!folders.current_path.isEmpty()){
                folders.current_path.remove(folders.current_path.size()-1);
                folders_list.remove(folders_list.size()-1);
                goBack();
            }
        });


        return v;
    }

    @Override
    public void onStop() {
        super.onStop();
        volley.getRequestQueue().cancelAll("request");
    }

    private void openFolder(View v) {
        ArrayList<folder> list = new ArrayList<>();

        ListView list_view = new ListView(getContext());
        list_view.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        FrameLayout container = v.findViewById(R.id.folders_listview_container);

        if (container.getChildCount() > 0) {
            View currentTop = container.getChildAt(container.getChildCount() - 1);
            currentTop.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(200)
                    .start();
        }
//        list_view.startAnimation(fadeOut);
//        list_view.setVisibility(View.GONE);
        folders.getList(volley, new Folders.FolderListCallback() {
            @Override
            public void onListLoaded() {
                for (String folder : folders.folders){
                    list.add(new folder(R.drawable.folder_aliceblue, folder, true));
                }

                for (String file : folders.files){
                    list.add(new folder(R.drawable.file_present_aliceblue, file, false));
                }

                FoldersListView adapter = new FoldersListView(getContext(), R.layout.default_listview_item, list);
                list_view.setAdapter(adapter);

                adapter.setOnItemClickListener((item, position) -> {
                    if (item.is_folder && folders.isClickable) {
                        folders.isClickable = false;
                        folders.current_path.add(item.text);
                        openFolder(v);
                    } else if (!item.is_folder){

                            JSONArray playlist = new JSONArray();
                            for (short i = 0; i < folders.files.size(); i++){
                                try {
                                    JSONObject song = new JSONObject();
                                    song.put("folder", String.join("/", folders.current_path));
                                    song.put("file_name", folders.files.get(i));
                                    playlist.put(song);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }


                            if (mainIterface.getMediaService() != null){
                                mediaplayer = mainIterface.getMediaService();
                                if (!mediaplayer.isLoading){
                                    mediaplayer.isLoading = true;
                                    mediaplayer.playlist = playlist;
                                    mediaplayer.cover = null;
                                    mediaplayer.index = position - folders.folders.size();
                                    mediaplayer.playlist_title = folders.current_path.get(folders.current_path.size() - 1);
                                    mediaplayer.getSongInfo(getContext(), volley, String.join("/", folders.current_path), item.text, new Mediaplayer.callback() {
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
                        .withEndAction(()->{folders.isClickable = true;})
                        .start();


                folders_list.add(list);
            }
        });
    }

    private void goBack() {
        FrameLayout container = requireView().findViewById(R.id.folders_listview_container);
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
