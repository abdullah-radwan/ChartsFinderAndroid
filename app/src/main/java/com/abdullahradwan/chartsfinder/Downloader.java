package com.abdullahradwan.chartsfinder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

class Downloader extends AsyncTask<Void, Integer, Void> {

    // Define main variables
    private final Activity activity;

    private final ProgressBar bar;

    private final TextView downloadView;

    private final Button getButton;

    private ArrayList<FilesItems> chartItems = new ArrayList<>();

    private boolean cont = true;

    private String message;

    private PowerManager.WakeLock wakeLock;

    private InputStream input;

    private OutputStream output;

    private HttpURLConnection connection;

    boolean cancel = false;

    // Receive activity and get widgets
    Downloader(Activity activity){

        this.activity = activity;

        bar = activity.findViewById(R.id.progressBar);

        downloadView = activity.findViewById(R.id.downloadView);

        getButton = activity.findViewById(R.id.getButton);

    }

    // Before run the download process
    @Override
    protected void onPreExecute() {

        // Get access to power manager
        PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);

        // Lock the wakelock
        if(pm != null) {

            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());

            wakeLock.acquire(10 * 60 * 300L /*3 minutes*/);

        }

    }

    // Download method
    @Override
    protected Void doInBackground(Void... voids) {

        int x = 0;

        // For each airport in the list
        for (String anIcaoCode :  MainActivity.icaoCode) {

            // If the ICAO code isn't empty
            if(!anIcaoCode.equals("")) {

                // Try to download chart per each resource
                for (int i = 0; i < MainActivity.resources.size(); i++) {

                    // If resource is a folder resource
                    if (MainActivity.resources.get(i).urlType.equals("Folder")) {

                        try {

                            // Set folder on ICAO code
                            File folder = new File(MainActivity.path + "/" + anIcaoCode);

                            // If folder isn't exists
                            if (!folder.exists()) {

                                // Connect to Jsoup, wll release an exception if the URL is wrong
                                Document doc = Jsoup.connect(String.format(MainActivity.resources.get(i).url, anIcaoCode)).get();

                                // Make directory
                                folder.mkdir();

                                // Call download start
                                editActivity("downStart", anIcaoCode,null, null);

                                // For every thing in the HTML page
                                for (Element element : doc.select("a")) {

                                    // If it's a URL (href)
                                    String fileName = element.attr("href");

                                    // If it ends with .pdf
                                    if (fileName.endsWith(".pdf")) {

                                        // Set chart name by remove '.pdf'
                                        String chartName = fileName.substring(0, fileName.length() - 4);

                                        // Start folder download, one per chart
                                        editActivity("downFolder", anIcaoCode, chartName, null);

                                        // Make URL from full link 'abs'
                                        URL url = new URL(element.attr("abs:href"));

                                        // Set connection
                                        connection = (HttpURLConnection) url.openConnection();

                                        // Connect to server
                                        connection.connect();

                                        // File is exists and ready to download
                                        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                                            // Open input stream
                                            input = connection.getInputStream();

                                            // Set file inside the ICAO code folder
                                            File folderFile = new File(folder + "/" + fileName);

                                            // Open output stream
                                            output = new FileOutputStream(folderFile);

                                            // Make byte array
                                            byte data[] = new byte[4096];

                                            int count;

                                            // Get file size
                                            int length = connection.getContentLength();

                                            // Used to update progress bar
                                            long total = 0;

                                            // While any data is available
                                            while ((count = input.read(data)) != -1) {

                                                // If canceled
                                                if (cancel){break;}

                                                // Set total
                                                total += count;

                                                // If file size is known, update progress bar
                                                if (length > 0) {
                                                    publishProgress((int) (total * 100 / length));
                                                }

                                                // Write data
                                                output.write(data, 0, count);

                                            }

                                            if(cancel){

                                                // Close streams
                                                input.close();

                                                output.close();

                                                connection.disconnect();

                                                // Delete all files in directory
                                                File[] files = folder.listFiles();

                                                if(files!=null) {for(File f: files) {f.delete();}}

                                                // Delete directory
                                                folder.delete();

                                                // Clear chart items
                                                chartItems.clear();

                                                break;

                                            }

                                            // Close streams
                                            input.close();

                                            output.close();

                                            connection.disconnect();

                                            // Finish downloading a file
                                            editActivity("folderComp", anIcaoCode, chartName, folderFile);

                                        }
                                    }
                                }

                                // After all charts is downloaded

                                // Set continue to false
                                cont = false;

                                // Download process over
                                editActivity("folderFinish", anIcaoCode, null, null);

                                // Get out from resources loop, move to the next ICAO code
                                break;

                            // Chart exists
                            } else {editActivity("folderExist",anIcaoCode,null, folder); cont = false; break;}

                        // Problem in URL, will pass to the next one
                        } catch (Exception ignored){

                        // Confirm streams closed
                        } finally {
                            try {
                                input.close();
                                output.close();
                                connection.disconnect();
                            } catch (Exception ignored) {}
                        }

                    // If resource is normal
                    } else {

                        // Set file name
                        String fileName = String.format("/%s.pdf", anIcaoCode);

                        // Set file
                        File file = new File(MainActivity.path + "/" + fileName);

                        // If file isn't exists in the path
                        if (!file.exists()) {

                            try {

                                // Call download start
                                editActivity("downStart", anIcaoCode,null, null);

                                // Make URL from resource
                                URL url = new URL(String.format(MainActivity.resources.get(i).url, anIcaoCode));

                                // Open connection
                                connection = (HttpURLConnection) url.openConnection();

                                // Connect to server
                                connection.connect();

                                // If file ready to download
                                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                                    // Open input stream
                                    input = connection.getInputStream();

                                    //Open output stream
                                    output = new FileOutputStream(file);

                                    byte data[] = new byte[4096];

                                    int count;

                                    // Get file size
                                    int length = connection.getContentLength();

                                    long total = 0;

                                    while ((count = input.read(data)) != -1) {

                                        // If canceled
                                        if (cancel) {break;}

                                        total += count;

                                        // If file size is known
                                        if (length > 0) {
                                            publishProgress((int) (total * 100 / length));
                                        }

                                        // Write data
                                        output.write(data, 0, count);

                                    }

                                    if (cancel) {

                                        // Close streams
                                        input.close();

                                        output.close();

                                        connection.disconnect();

                                        // Delete file
                                        file.delete();

                                        break;

                                    }

                                    // Close streams
                                    input.close();

                                    output.close();

                                    connection.disconnect();

                                    cont = false;

                                    // Finish download
                                    editActivity("downComp", anIcaoCode, null, file);

                                    break;

                                }

                            // Can't write file, maybe permission is denied
                            } catch (Exception ex) {

                                editActivity("writeFail", anIcaoCode, null, null);

                                cont = false;

                                break;

                            // Confirm streams is closed
                            } finally {
                                try {

                                    input.close();

                                    output.close();

                                    connection.disconnect();

                                } catch (Exception ignored) {}

                            }

                        // File exists
                        } else {

                            editActivity("fileExist", anIcaoCode,null, file);

                            cont = false;

                            break;

                        }

                    }

                }

                // Break the airports loop if process canceled
                if(cancel){break;}

                // Resources over and chart not found
                if (cont) {editActivity("downFail", anIcaoCode, null, null);}

                ++x;

                try {
                    // Check if there is another airport, if so; no need to delay
                    String a = MainActivity.icaoCode[x];

                    // Wait 2 seconds before download the next ICAO code
                    Thread.sleep(2000);

                } catch (Exception ignored) {}

                editActivity("downFinish", null, null, null);

            // ICAO code is empty
            } else{editActivity("fieldEmpty",null,null, null);}

        }

        return null;

    }

    // Update progress bar
    @Override
    protected void onProgressUpdate(Integer... values) {

        super.onProgressUpdate(values);

        // Set progress bar to value received
        bar.setProgress(values[0]);

    }

    // After download, release wakelock
    @Override
    protected void onPostExecute(Void aVoid) {super.onPostExecute(aVoid); try{wakeLock.release();} catch (Exception ignored){}}

    // Edit activity, operations on GUI
    private void editActivity(final String op, final String icaoCode, final String chartName, final File chartFile){

        // Run on GUI thread
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                switch (op){

                    // If field is empty
                    case "fieldEmpty":

                        downloadView.setText(activity.getResources().getString(R.string.fieldempty_message));

                        break;

                    // Download started
                    case "downStart":

                        // Clear file items list
                        chartItems = new ArrayList<>();

                        // Disable 'Get charts' button
                        getButton.setEnabled(false);

                        downloadView.setText(String.format(activity.getResources().getString(R.string.downstart_message),icaoCode));

                        // Reset progress
                        bar.setProgress(0);

                        // Show bar
                        bar.setVisibility(View.VISIBLE);

                        if(MainActivity.showNotify){Notify.notify(activity,
                                String.format(activity.getResources().getString(R.string.downstart_message),icaoCode));}

                        break;

                    // Start download for each folder resource file
                    case "downFolder":

                        bar.setProgress(0);

                        downloadView.setText(String.format(activity.getResources().getString(R.string.downfolder_message),icaoCode,chartName));

                        break;

                    // Finish each file in folder resource
                    case "folderComp":

                        // Add chart to chart list
                        chartItems.add(0, new FilesItems(chartName,null, chartFile));

                        break;

                    // Normal resource download finished
                    case "downComp":

                        message = String.format(activity.getResources().getString(R.string.downcomp_message),icaoCode);

                        // Add file to file menu
                        if(MainActivity.internalPDF){addFile(icaoCode, "Normal", chartFile);}

                        if(MainActivity.openChart){openPath(chartFile, "openFile");}

                        break;

                    case "downFinish":

                        bar.setVisibility(View.GONE);

                        getButton.setEnabled(true);

                        downloadView.setText(message);

                        if(MainActivity.showNotify){Notify.notify(activity, message);}

                        break;

                    // Finish download folder charts
                    case "folderFinish":

                        message = String.format(activity.getResources().getString(R.string.downcomp_message),icaoCode);

                        if(MainActivity.internalPDF){

                            // Make instance of chartItems
                            ArrayList<FilesItems> list = chartItems;

                            // Put the ICAO code and files ArrayList
                            MainActivity.filesMap.put(icaoCode, list);

                            // Add chart to files and set type to folder
                            addFile(icaoCode, "Folder", null);

                        }

                        if(MainActivity.openChart){openPath(chartFile, "openPath");}

                        break;

                    // If chart is exists
                    case "fileExist":

                        message = String.format(activity.getResources().getString(R.string.fileexist_message),icaoCode);

                        if(MainActivity.internalPDF) {

                            // Check if it added to files list
                            if(check_avail(icaoCode)) {

                                // Add to file list
                                addFile(icaoCode, "Normal", chartFile);

                            }

                        }

                        // Open chart
                        if(MainActivity.openChart){openPath(chartFile, "openFile");}

                        break;

                    // Charts exists for folder, it won't added to files spinner
                    case "folderExist":

                        // Set TextView
                        message = String.format(activity.getResources().getString(R.string.fileexist_message),icaoCode);

                        if(MainActivity.internalPDF) {

                            if (check_avail(icaoCode)) {

                                File[] files = chartFile.listFiles();

                                for (File file : files) {

                                    if (file.toString().endsWith(".pdf")) {

                                        String chartName = file.getName().substring(0, file.getName().length() - 4);

                                        chartItems.add(new FilesItems(chartName, null, file));

                                    }

                                }

                                if(!chartItems.isEmpty()) {

                                    // Make instance of chartItems
                                    ArrayList<FilesItems> listFolder = chartItems;

                                    // Put the ICAO code and files ArrayList
                                    MainActivity.filesMap.put(icaoCode, listFolder);

                                    addFile(icaoCode, "Folder", null);

                                }

                            }

                        }

                        if(MainActivity.openChart){openPath(chartFile, "openPath");}

                        break;

                    // Charts not found
                    case "downFail":

                        message = String.format(activity.getResources().getString(R.string.downfail_message),icaoCode);

                        break;

                    // Error in write charts
                    case "writeFail":

                        message = String.format(activity.getResources().getString(R.string.writefail_message),icaoCode);

                        break;

                }

            }

        });

    }

    // Check if chart is available
    private boolean check_avail(String chartName){

        boolean avail = true;

        // Check if chart exists in every entry of files list
        for(int i = 0; i < MainActivity.files.size(); i++){

            // If chart exists
            if(chartName.equals(MainActivity.files.get(i).chartName)){

                avail = false;

                break;

            }

        }

        return avail;

    }

    private static void addFile(String icaoCode, String type, File file){

        // Add chart to files and set type to folder
        MainActivity.files.add(0, new FilesItems(icaoCode, type, file));

        // Add chart to spinner
        MainActivity.fileSpinnerItems.add(0, icaoCode);

        MainActivity.fileSpinnerAdapter.notifyDataSetChanged();

    }

    // Open chart after download
    private void openPath(File chartFile, String op){

        // Set intent
        Intent target = new Intent(Intent.ACTION_VIEW);

        String type;

        String title;

        if(op.equals("openFile")) {

            type = "application/pdf";

            title = activity.getResources().getString(R.string.openfile_title);

        } else {

            type = "resource/folder";

            title = activity.getResources().getString(R.string.openpath_title);

        }

        // Set action and path
        target.setDataAndType(Uri.fromFile(chartFile), type);

        // Set final intent
        Intent intent = Intent.createChooser(target, title);

        // Start intent
        activity.startActivity(intent);

    }

}
