package com.yurixahri.ahrify.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yurixahri.ahrify.R;
import com.yurixahri.ahrify.models.playlist;

import java.util.List;

public class PlaylistListView extends BaseAdapter {
    private Context context;
    private int layout;
    private List<playlist> list;
    private PlaylistListView.OnItemClickListener listener;;


    public interface OnItemClickListener {
        void onItemClick(playlist item, int position);
        void onItemLongClick(playlist item, int position);
    }


    public void setOnItemClickListener(PlaylistListView.OnItemClickListener listener) {
        this.listener = listener;
    }



    public PlaylistListView(Context context, int layout, List<playlist> list) {
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

        playlist item = list.get(position);
        text.setText(item.title);

        image.setImageResource(R.drawable.playlist);


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
