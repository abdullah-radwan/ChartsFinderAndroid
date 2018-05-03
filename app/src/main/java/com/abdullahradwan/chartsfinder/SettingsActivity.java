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

    private CheckBox openCheck;

    private TextView pathView;

    private Button removeButton;

    private static ListAdapter adapter;

    private int itemPos;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        setSupportActionBar((Toolbar) findViewById(R.id.toolBar));

        openCheck = findViewById(R.id.openCheck);

        CheckBox notifyCheck = findViewById(R.id.notfiyCheck);

        CheckBox interiorCheck = findViewById(R.id.interiorCheck);

        pathView = findViewById(R.id.pathView);

        ListView listView = findViewById(R.id.resList);

        removeButton = findViewById(R.id.removeButton);

        pathView = findViewById(R.id.pathView);

        pathView.setText(getResources().getString(R.string.path_textview) + MainActivity.path);

        adapter = new ListAdapter(this, MainActivity.resources);

        openCheck.setChecked(MainActivity.openChart);

        openCheck.setEnabled(!MainActivity.internalPDF);

        openCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.openChart = isChecked;}});

        notifyCheck.setChecked(MainActivity.showNotify);

        notifyCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {MainActivity.showNotify = isChecked;}});

        interiorCheck.setChecked(MainActivity.internalPDF);

        interiorCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {MainActivity.internalPDF = isChecked; openCheck.setEnabled(!isChecked);}});

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {removeButton.setEnabled(true);itemPos = position;}}); }

    public void changePath(View view) {

        DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                .newDirectoryName("")
                .allowNewDirectoryNameModification(true)
                .build();

        final DirectoryChooserFragment mDialog = DirectoryChooserFragment.newInstance(config);

        mDialog.show(getFragmentManager(), null);

        mDialog.setDirectoryChooserListener(new DirectoryChooserFragment.OnFragmentInteractionListener() {
            @Override
            public void onSelectDirectory(@NonNull String path) {

                MainActivity.path = path;

                pathView.setText(getResources().getString(R.string.path_textview) + path);

                mDialog.dismiss();

            }

            @Override
            public void onCancelChooser() {mDialog.dismiss();}
        });

    }

    @Override
    public void onBackPressed() {super.onBackPressed(); overridePendingTransition(R.anim.enter_b, R.anim.exit_b);}

    public void addRes(View view) {new AddResDialog().show(getFragmentManager(), "AddRes");}

    static public void addRes(int order, String url){

        MainActivity.resources.add(order, url);

        adapter.notifyDataSetChanged();

    }

    public void remRes(View view) {

        MainActivity.resources.remove(itemPos);

        adapter.notifyDataSetChanged();

        removeButton.setEnabled(false);

    }

    public void resetRes(View view) {

        MainActivity.resetRes();

        adapter.notifyDataSetChanged();

    }

}
