package com.abdullahradwan.chartsfinder;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class ListAdapter extends BaseAdapter {

    private final ArrayList<String> items;

    private final Activity activity;

    ListAdapter(Activity activity, ArrayList<String> items){this.activity = activity; this.items = items;}

    @Override
    public int getCount() {return items.size();}

    @Override
    public Object getItem(int position) {return items.get(position);}

    @Override
    public long getItemId(int position) {return 0;}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = activity.getLayoutInflater();

        View view = inflater.inflate(R.layout.list_item, null);

        TextView urlView = view.findViewById(R.id.resView);

        urlView.setText(items.get(position));

        return view;

    }
}
