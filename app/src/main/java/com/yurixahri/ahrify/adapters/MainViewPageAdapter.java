package com.yurixahri.ahrify.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.yurixahri.ahrify.AlbumsFragment;
import com.yurixahri.ahrify.ArtistsFragment;
import com.yurixahri.ahrify.PlaylistsFragment;
import com.yurixahri.ahrify.FoldersFragment;

public class MainViewPageAdapter extends FragmentStatePagerAdapter {
    public MainViewPageAdapter(@NonNull FragmentManager fm, int behavior){
        super(fm, behavior);
    }
    @NonNull
    @Override
    public Fragment getItem(int pos) {
        switch (pos) {
            case 0:
                return new FoldersFragment();
            case 1:
                return new AlbumsFragment();
            case 2:
                return new ArtistsFragment();
            case 3:
                return new PlaylistsFragment();
        }
        return null;
    }
    @Override
    public int getCount() {
        return 4;
    }
}
