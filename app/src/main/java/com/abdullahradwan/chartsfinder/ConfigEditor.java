package com.abdullahradwan.chartsfinder;

import android.content.SharedPreferences;
import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class ConfigEditor {

    // Define preferences and Gson
    private final SharedPreferences sharedPref;

    private final Gson gson;

    // Get shared preferences and set Gson
    ConfigEditor(SharedPreferences sharedPref){this.sharedPref = sharedPref; gson = new Gson();}

    void readConfig(){

        // Set variables
        MainActivity.showNotify = sharedPref.getBoolean("showNotify", true);

        MainActivity.openChart = sharedPref.getBoolean("openChart", false);

        MainActivity.internalPDF = sharedPref.getBoolean("interiorPDF",true);

        // Set default value to downloads folder
        MainActivity.path = sharedPref.getString("path",
                String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));

        // Get array as string and convert it to ArrayList
        ArrayList<ResourcesItem> resItems = gson.fromJson(sharedPref.getString("resItems",""),
                new TypeToken<List<ResourcesItem>>(){}.getType());

        // If there is a list
        if(resItems!=null){MainActivity.resources  = resItems;} else{MainActivity.resetRes();}

        ArrayList<FilesItems> fileItems = gson.fromJson(sharedPref.getString("fileItems",""),
                new TypeToken<List<FilesItems>>(){}.getType());

        if(fileItems!=null){MainActivity.files  = fileItems;}

        ArrayList<String> fileSpinnerItems = gson.fromJson(sharedPref.getString("fileSpinnerItems",""),
                new TypeToken<List<String>>(){}.getType());

        if(fileSpinnerItems!=null){MainActivity.fileSpinnerItems  = fileSpinnerItems;}

        ArrayList<String> chartSpinnerItems = gson.fromJson(sharedPref.getString("chartSpinnerItems",""),
                new TypeToken<List<String>>(){}.getType());

        if(chartSpinnerItems!=null){MainActivity.chartSpinnerItems  = chartSpinnerItems;}

        // Get HashMap as string and convert it to HashMap
        HashMap<String, ArrayList<FilesItems>> filesMap = gson.fromJson(sharedPref.getString("filesMap",""),
                new TypeToken<HashMap<String, ArrayList<FilesItems>>>(){}.getType());

        if(filesMap!=null){MainActivity.filesMap = filesMap;}

    }

    void writeConfig(){

        // Set preferences editor
        SharedPreferences.Editor prefsEdit = sharedPref.edit();

        // Put values
        prefsEdit.putBoolean("showNotify",MainActivity.showNotify);

        prefsEdit.putBoolean("openChart", MainActivity.openChart);

        prefsEdit.putBoolean("interiorPDF", MainActivity.internalPDF);

        prefsEdit.putString("path", MainActivity.path);

        // Convert lists to string
        prefsEdit.putString("resItems", gson.toJson(MainActivity.resources));

        prefsEdit.putString("fileItems", gson.toJson(MainActivity.files));

        prefsEdit.putString("fileSpinnerItems", gson.toJson(MainActivity.fileSpinnerItems));

        prefsEdit.putString("chartSpinnerItems", gson.toJson(MainActivity.chartSpinnerItems));

        // Convert HashMap to string
        prefsEdit.putString("filesMap", gson.toJson(MainActivity.filesMap));

        // Save file
        prefsEdit.apply();

    }

}
