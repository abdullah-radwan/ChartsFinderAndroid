package com.abdullahradwan.chartsfinder;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class ListAdapter extends BaseAdapter {

    // Define variables
    private final ArrayList<ResourcesItem> items;

    private final Activity activity;

    // Get activity and resources list
    ListAdapter(Activity activity, ArrayList<ResourcesItem> items){this.activity = activity;
        this.items = items;}

    @Override
    public int getCount() {return items.size();}

    @Override
    public Object getItem(int position) {return items.get(position);}

    @Override
    public long getItemId(int position) {return 0;}

    // Set ListView items
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Inflate list item resource
        LayoutInflater inflater = activity.getLayoutInflater();

        // Set view
        View view = inflater.inflate(R.layout.list_item, null);

        // Get TextViews
        TextView urlView = view.findViewById(R.id.resView);

        TextView typeView = view.findViewById(R.id.typeView);

        // Set text
        urlView.setText(items.get(position).url);

        typeView.setText(items.get(position).urlType);

        return view;

    }
}
