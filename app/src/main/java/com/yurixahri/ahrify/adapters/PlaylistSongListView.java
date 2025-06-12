package com.yurixahri.ahrify.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yurixahri.ahrify.R;
import com.yurixahri.ahrify.models.playlistSong;
import com.yurixahri.ahrify.utils.BitmapCompressor;

import java.util.List;

public class PlaylistSongListView extends BaseAdapter {
    private Context context;
    private int layout;
    private List<playlistSong> list;
    private PlaylistSongListView.OnItemClickListener listener;;


    public interface OnItemClickListener {
        void onItemClick(playlistSong item, int position);
        void onItemLongClick(playlistSong item, int position);
    }


    public void setOnItemClickListener(PlaylistSongListView.OnItemClickListener listener) {
        this.listener = listener;
    }



    public PlaylistSongListView(Context context, int layout, List<playlistSong> list) {
        this.layout = layout;
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }




    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate( layout,null);

        TextView text = convertView.findViewById(R.id.listview_item_text);
        ImageView image = convertView.findViewById(R.id.listview_item_icon);

        playlistSong item = list.get(position);
        text.setText(item.title);

        if (item.cover != null){
            image.setImageBitmap(BitmapCompressor.blobToBitmap(item.cover));
        }else{
            image.setImageResource(R.drawable.default_icon);
        }


        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item, position);
            }
        });

        convertView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onItemLongClick(item, position);
            }
            return false;
        });


        return convertView;
    }
}
