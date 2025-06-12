package com.yurixahri.ahrify;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.yurixahri.ahrify.adapters.SearchViewPageAdapter;
import com.yurixahri.ahrify.interfaces.MainIterface;
import com.yurixahri.ahrify.notSingleton.Mediaplayer;
import com.yurixahri.ahrify.utils.BitmapCompressor;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class SearchActivity extends AppCompatActivity implements MainIterface {
    AnimatedBottomBar top_nav;
    ViewPager search_view_pager;
    TextView mode_text;

    ImageButton back_button;
    Mediaplayer mediaplayer;
    boolean isBound = false;

    byte current_page = 0;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.search_activity);

        prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        updateBackgroundOnStart();

        top_nav = findViewById(R.id.top_navigation);
        search_view_pager = findViewById(R.id.search_view_pager);
        mode_text = findViewById(R.id.mode_text);
        back_button = findViewById(R.id.back_button);

        String search = getIntent().getStringExtra("search");

        SearchViewPageAdapter searchViewPageAdapter = new SearchViewPageAdapter (getSupportFragmentManager()
                , FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, search);
        search_view_pager.setAdapter(searchViewPageAdapter);
        search_view_pager.setCurrentItem(0);
        search_view_pager.setOffscreenPageLimit(2);

        search_view_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                current_page = (byte) position;
                switch (position) {
                    case 0:
                        top_nav.selectTabById(R.id.albums, true);
                        mode_text.setText("Albums");
                        break;
                    case 1:
                        top_nav.selectTabById(R.id.artists, true);
                        mode_text.setText("Artists");
                        break;
                    case 2:
                        top_nav.selectTabById(R.id.songs, true);
                        mode_text.setText("Songs");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        top_nav.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {
            @Override
            public void onTabSelected(int i, @Nullable AnimatedBottomBar.Tab tab, int i1, @NonNull AnimatedBottomBar.Tab tab1) {
                current_page = (byte) i1;
                switch (i1){
                    case 0:
                        search_view_pager.setCurrentItem(0);
                        mode_text.setText("Albums");
                        break;
                    case 1:
                        search_view_pager.setCurrentItem(1);
                        mode_text.setText("Artists");
                        break;
                    case 2:
                        search_view_pager.setCurrentItem(2);
                        mode_text.setText("Songs");
                        break;
                }
            }
            @Override
            public void onTabReselected(int i, @NonNull AnimatedBottomBar.Tab tab) {}
        });

        back_button.setOnClickListener(v -> endActivity());

        Intent intent = new Intent(this, Mediaplayer.class);
        startForegroundService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Mediaplayer.LocalBinder binder = (Mediaplayer.LocalBinder) service;
            mediaplayer = binder.getService();
            isBound = true;

        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    public Mediaplayer getMediaService() {
        return mediaplayer;
    }

    @Override
    public void goToSongControlActivity(){
        Intent intent = new Intent(SearchActivity.this, SongControlPanelActivity.class);
        startActivity(intent);
    }

    private void endActivity(){
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