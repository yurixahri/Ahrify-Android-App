package com.yurixahri.ahrify.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.yurixahri.ahrify.AlbumsFragment;
import com.yurixahri.ahrify.ArtistsFragment;

import com.yurixahri.ahrify.SongsFragment;

public class SearchViewPageAdapter extends FragmentStatePagerAdapter {
    private String search;
    public SearchViewPageAdapter(@NonNull FragmentManager fm, int behavior, String search){
        super(fm, behavior);
        this.search = search;
    }
    @NonNull
    @Override
    public Fragment getItem(int pos) {
        Bundle args = new Bundle();
        args.putString("search", search);

        Fragment fragment;

        switch (pos) {
            case 0:
                fragment =  new AlbumsFragment();
                break;
            case 1:
                fragment = new ArtistsFragment();
                break;
            case 2:
                fragment =  new SongsFragment();
                break;
            default: return null;
        }

        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}
