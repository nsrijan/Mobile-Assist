package com.example.nsrijan.virtualassitance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by nsrijan on 9/3/17.
 */

public class MyAdapter extends ArrayAdapter<String> {

    LayoutInflater inflater;
    Context myContext;
    List<String> newList;
    public MyAdapter(Context context, int resource, List<String> list) {
        super(context, resource, list);
        // TODO Auto-generated constructor stub
        myContext = context;
        newList = list;
        inflater = LayoutInflater.from(context);
    }
    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.custom_list, null);
            holder.tvSname = (TextView) view.findViewById(R.id.tvtext_item);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.tvSname.setText(newList.get(position).toString());

        return view;
    }
}
