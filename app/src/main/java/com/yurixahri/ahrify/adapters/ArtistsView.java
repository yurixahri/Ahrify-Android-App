package com.yurixahri.ahrify.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yurixahri.ahrify.R;
import com.yurixahri.ahrify.models.artist;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class ArtistsView extends BaseAdapter {
    private Context context;
    private int layout;
    private List<artist> list;
    private OnItemClickListener listener;;

    public ArtistsView(Context context, int layout, List<artist> list) {
        this.context = context;
        this.layout = layout;
        this.list = list;
    }

    public interface OnItemClickListener {
        void onItemClick(artist item, int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
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

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate( layout,null);
        TextView text;
        ShapeableImageView img;

        artist item = list.get(position);
        text= convertView.findViewById(R.id.listview_item_text);
        img = convertView.findViewById(R.id.listview_item_icon);




        text.setText(item.text);
        img.setImageResource(item.drawable);


        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item, position);
            }
        });
        return convertView;
    }
}
