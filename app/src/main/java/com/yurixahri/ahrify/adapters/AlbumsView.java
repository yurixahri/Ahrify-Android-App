package com.yurixahri.ahrify.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.yurixahri.ahrify.R;
import com.yurixahri.ahrify.models.album;
import com.google.android.material.imageview.ShapeableImageView;


import java.util.List;

public class AlbumsView extends BaseAdapter {
    private Context context;
    private int layout;
    private List<album> list;
    private OnItemClickListener listener;;

    public AlbumsView(Context context, int layout, List<album> list) {
        this.context = context;
        this.layout = layout;
        this.list = list;
    }

    public interface OnItemClickListener {
        void onItemClick(album item, int position);
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

        album item = list.get(position);
        if (item.is_file){
            text= convertView.findViewById(R.id.listview_item_text);
            img = convertView.findViewById(R.id.listview_item_icon);
        }else{
            text= convertView.findViewById(R.id.album_text);
            img = convertView.findViewById(R.id.album_image);
        }




        text.setText(item.text);
        if (item.cover != null){
            img.setImageBitmap(item.cover);
        }else{
            img.setImageResource(item.drawable);
        }


        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item, position);
            }
        });
        return convertView;
    }
}
