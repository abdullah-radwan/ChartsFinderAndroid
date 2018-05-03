package com.abdullahradwan.chartsfinder;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class ConfigEditor {

    private final SharedPreferences sharedPref;

    private final Gson gson;

    ConfigEditor(SharedPreferences sharedPref){this.sharedPref = sharedPref; gson = new Gson();}

    void readConfig(){

        MainActivity.showNotify = sharedPref.getBoolean("showNotify", true);

        MainActivity.openChart = sharedPref.getBoolean("openChart", false);

        MainActivity.interiorPDF = sharedPref.getBoolean("interiorPDF",true);

        MainActivity.path = sharedPref.getString("path", "");

        ArrayList<String> resItems = gson.fromJson(sharedPref.getString("resItems",""),
                new TypeToken<List<String>>(){}.getType());

        if(resItems!=null){MainActivity.resources  = resItems;} else{MainActivity.resetRes();}

        ArrayList<File> fileItems = gson.fromJson(sharedPref.getString("fileItems",""),
                new TypeToken<List<File>>(){}.getType());

        if(fileItems!=null){MainActivity.files  = fileItems;}

        ArrayList<String> spinnerItems = gson.fromJson(sharedPref.getString("spinnerItems",""),
                new TypeToken<List<String>>(){}.getType());

        if(spinnerItems!=null){MainActivity.spinnerItems  = spinnerItems;}

    }

    void writeConfig(){

        SharedPreferences.Editor prefsEdit = sharedPref.edit();

        prefsEdit.putBoolean("showNotify",MainActivity.showNotify);

        prefsEdit.putBoolean("openChart", MainActivity.openChart);

        prefsEdit.putBoolean("interiorPDF", MainActivity.interiorPDF);

        prefsEdit.putString("path", MainActivity.path);

        prefsEdit.putString("resItems", gson.toJson(MainActivity.resources));

        prefsEdit.putString("fileItems", gson.toJson(MainActivity.files));

        prefsEdit.putString("spinnerItems", gson.toJson(MainActivity.spinnerItems));

        prefsEdit.apply();

    }

}
