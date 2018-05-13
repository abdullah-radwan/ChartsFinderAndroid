package com.abdullahradwan.chartsfinder;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

public class SettingsActivity extends AppCompatActivity {

    // Define variables
    private CheckBox openCheck;

    private TextView pathView;

    private Button removeButton;

    private static ListAdapter adapter;

    private int itemPos;

    // On start
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Show activity
        setContentView(R.layout.activity_settings);

        setSupportActionBar((Toolbar) findViewById(R.id.toolBar));

        // Set variables
        openCheck = findViewById(R.id.openCheck);

        CheckBox notifyCheck = findViewById(R.id.notfiyCheck);

        CheckBox interiorCheck = findViewById(R.id.interiorCheck);

        pathView = findViewById(R.id.pathView);

        ListView listView = findViewById(R.id.resList);

        removeButton = findViewById(R.id.removeButton);

        pathView = findViewById(R.id.pathView);

        // Set path TextView
        pathView.setText(getResources().getString(R.string.path_textview) + MainActivity.path);

        // Set ListView Adapter
        adapter = new ListAdapter(this, MainActivity.resources);

        // Set checked
        openCheck.setChecked(MainActivity.openChart);

        // Will disable if use interior pdf is active
        openCheck.setEnabled(!MainActivity.internalPDF);

        openCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.openChart = isChecked;}});

        // Set checked
        notifyCheck.setChecked(MainActivity.showNotify);

        notifyCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {MainActivity.showNotify = isChecked;}});

        // Set checked
        interiorCheck.setChecked(MainActivity.internalPDF);

        // Set checked and open check enable
        interiorCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {MainActivity.internalPDF = isChecked;
            openCheck.setEnabled(!isChecked);}});

        // Link ListView to adapter
        listView.setAdapter(adapter);

        // Enable the remove button and set resource position
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {removeButton.setEnabled(true);itemPos = position;}});

    }

    // Path button on click
    public void changePath(View view) {

        // Set directory chooser dialog
        DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                .newDirectoryName("")
                .allowNewDirectoryNameModification(true)
                .build();

        // Make it fragment
        final DirectoryChooserFragment mDialog = DirectoryChooserFragment.newInstance(config);

        // Show dialog
        mDialog.show(getFragmentManager(), null);

        mDialog.setDirectoryChooserListener(new DirectoryChooserFragment.OnFragmentInteractionListener() {
            @Override
            public void onSelectDirectory(@NonNull String path) {

                // Set path
                MainActivity.path = path;

                pathView.setText(getResources().getString(R.string.path_textview) + path);

                // Hide dialog
                mDialog.dismiss();

            }

            // Hide dialog on cancel clicked
            @Override
            public void onCancelChooser() {mDialog.dismiss();}
        });

    }

    // Set animations on back pressed
    @Override
    public void onBackPressed() {super.onBackPressed(); overridePendingTransition(R.anim.enter_b, R.anim.exit_b);}

    // Show add resource dialog
    public void addRes(View view) {new AddResDialog().show(getFragmentManager(), "AddRes");}

    // Add resource
    static public void addRes(int order, String url, String type){

        MainActivity.resources.add(order, new ResourcesItem(url, type));

        // Notify list data changed
        adapter.notifyDataSetChanged();

    }

    // Remove resource
    public void remRes(View view) {

        // Remove item from click
        MainActivity.resources.remove(itemPos);

        // Notify list changed
        adapter.notifyDataSetChanged();

        // Disable remove button
        removeButton.setEnabled(false);

    }

    // Reset resources
    public void resetRes(View view) {

        MainActivity.resetRes();

        adapter.notifyDataSetChanged();

    }

}
