package com.yurixahri.ahrify.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.yurixahri.ahrify.R;
import com.yurixahri.ahrify.models.defaultListItem;

import java.util.List;

public class DefaultListView extends BaseAdapter {
    private Context context;
    private int layout;
    private List<defaultListItem> list;
    private DefaultListView.OnItemClickListener listener;;
    private int highlightIndex = -1;


    public interface OnItemClickListener {
        void onItemClick(defaultListItem item, int position);
        void onItemLongClick(defaultListItem item, int position);
    }


    public void setOnItemClickListener(DefaultListView.OnItemClickListener listener) {
        this.listener = listener;
    }



    public DefaultListView(Context context, int layout, List<defaultListItem> list) {
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

    public void setHighlightIndex(int index) {
        highlightIndex = index;
        notifyDataSetChanged();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate( layout,null);

        TextView text = convertView.findViewById(R.id.listview_item_text);
        ImageView image = convertView.findViewById(R.id.listview_item_icon);

        defaultListItem item = list.get(position);
        text.setText(item.text);
        if (item.cover != null){
            image.setImageBitmap(item.cover);
        }else{
            image.setImageResource(item.drawable);
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

        if (position == highlightIndex) {
            text.setTextColor(ContextCompat.getColor(context, R.color.sapphire_blue));
        } else {
            text.setTextColor(ContextCompat.getColor(context,R.color.aliceblue));; // Default color
        }

        return convertView;
    }
}
