package com.abdullahradwan.chartsfinder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.constraint.Group;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText icaoEdit;

    private Group pdfGroup;

    private ConfigEditor config;

    static ArrayAdapter<String> spinnerAdapter;

    static ArrayList<String> spinnerItems = new ArrayList<>();

    static String[] icaoCode;

    static String path;

    static PDFView pdfView;

    static ArrayList<String> resources = new ArrayList<>();

    static ArrayList<File> files = new ArrayList<>();

    static boolean showNotify;

    static boolean openChart;

    static boolean internalPDF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        config = new ConfigEditor(getPreferences(Context.MODE_PRIVATE));

        config.readConfig();

        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar) findViewById(R.id.toolBar));

        icaoEdit = findViewById(R.id.icaoEdit);

        pdfGroup = findViewById(R.id.pdfGroup);

        pdfView = findViewById(R.id.pdfView);

        Spinner fileSpinner = findViewById(R.id.fileSpinner);

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerItems);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        fileSpinner.setAdapter(spinnerAdapter);

        fileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {pdfView.fromFile(files.get(position)).load();}

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

        }

        setPdfGroup();

    }

    @Override
    protected void onStart() {super.onStart(); setPdfGroup();}

    @Override
    protected void onStop() {super.onStop(); config.writeConfig();}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {getMenuInflater().inflate(R.menu.menu_main,menu); return true;}

    public void getChart(View view) {icaoCode = icaoEdit.getText().toString().toUpperCase().split("\\s+"); new Downloader(this).execute();}

    public void showSettings(MenuItem item) {startActivity(new Intent(this, SettingsActivity.class)); overridePendingTransition(R.anim.enter, R.anim.exit);}

    private void setPdfGroup(){if(internalPDF){pdfGroup.setVisibility(View.VISIBLE);} else {pdfGroup.setVisibility(View.GONE);}}

    static public void resetRes(){

        resources.clear();

        resources.add("http://www.armats.com/arm/aviation/products/eAIP/pdf/UD-AD-2.%s-en-GB.pdf");

        String airac = new SimpleDateFormat("yyMM", Locale.US).format(Calendar.getInstance().getTime());

        String url = String.format("http://imageserver.fltplan.com/merge/merge%s/", airac);

        resources.add(url + "%s.pdf");

        resources.add("http://vau.aero/navdb/chart/%s.pdf");

        resources.add("http://ottomanva.com/lib/charts/%s.pdf");

        resources.add("http://sa-ivao.net/charts_file/CHART-%s.PDF");

        resources.add("http://www.fly-sea.com/charts/%s.pdf");

        resources.add("http://uvairlines.com/admin/resources/charts/%s.pdf");

        resources.add("https://www.virtualairlines.eu/charts/%s.pdf");

    }

}
