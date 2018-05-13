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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

    private final Spinner chartSpinner;

    private final ArrayList<FilesItems> chartItems = new ArrayList<>();

    private boolean cont = true;

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

        chartSpinner = activity.findViewById(R.id.chartSpinner);

    }

    // Before run the download process
    @Override
    protected void onPreExecute() {

        // Get access to power manager
        PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);

        // Lock the wakelock
        try {

            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());

            wakeLock.acquire(10 * 60 * 300L /*3 minutes*/);

        } catch (Exception ignored){}

    }

    // Download method
    @Override
    protected Void doInBackground(Void... voids) {

        // For each airport in the list
        for (String anIcaoCode : MainActivity.icaoCode) {

            // If the ICAO code isn't empty
            if(!anIcaoCode.equals("")) {

                // Call download start
                editActivity("downStart", anIcaoCode,null, null);

                // Try to download chart per each resource
                for (int i = 0; i < MainActivity.resources.size(); i++) {

                    // If resource is a folder resource
                    if (MainActivity.resources.get(i).urlType.equals("Folder")) {

                        try {

                            // Connect to Jsoup, well release an exception if the URL is wrong

                            Document doc = Jsoup.connect(String.format(MainActivity.resources.get(i).url, anIcaoCode)).get();

                            // Set folder on ICAO code
                            File folder = new File(MainActivity.path + "/" + anIcaoCode);

                            // If folder isn't exists and the chart isn't in the files list (maybe exists on another path)
                            if (!folder.exists() && check_avail(anIcaoCode)) {

                                // Make directory
                                folder.mkdir();

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
                            } else {editActivity("folderExist",anIcaoCode,null, null); cont = false; break;}

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

                        try {

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

                                // Set file
                                File file = new File(MainActivity.path + "/" + fileName);

                                // If file isn't exists in the path and files list
                                if (!file.exists() && check_avail(anIcaoCode)) {

                                    //Open output stream
                                    output = new FileOutputStream(file);

                                    byte data[] = new byte[4096];

                                    int count;

                                    // Get file size
                                    int length = connection.getContentLength();

                                    long total = 0;

                                    while ((count = input.read(data)) != -1) {

                                        // If canceled
                                        if(cancel){break;}

                                        total += count;

                                        // If file size is known
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
                                    editActivity("downComp", anIcaoCode,null, file);

                                    break;

                                } else {

                                    // File exists

                                    editActivity("fileExist", anIcaoCode,null, file);

                                    cont = false;

                                    break;

                                }

                            }

                        } catch (Exception ex) {

                            // Can't write file, maybe permission is denied
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
                    }

                }

                // Break the final loop if process canceled
                if(cancel){break;}

                // Resources over and chart not found
                if (cont) {editActivity("downFail", anIcaoCode, null, null);}

                // Wait 3 seconds before download the next ICAO code
                try {Thread.sleep(3000);} catch (Exception ignored) {}

            // ICAO code is empty
            } else{editActivity("fieldEmpty",null,null, null);}

        }

        editActivity("downFinish", null,null,null);

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
                        chartItems.clear();

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

                        downloadView.setText(String.format(activity.getResources().getString(R.string.downcomp_message),icaoCode));

                        if(MainActivity.showNotify){Notify.notify(activity,
                                String.format(activity.getResources().getString(R.string.downcomp_message),icaoCode));}

                        if(MainActivity.internalPDF){

                            // Add file to file menu
                            MainActivity.files.add(0, new FilesItems(icaoCode, "Normal", chartFile));

                            // Add to file spinner list
                            MainActivity.fileSpinnerItems.add(0, icaoCode);

                            // Update adapter
                            MainActivity.fileSpinnerAdapter.notifyDataSetChanged();

                            // Hide file spinner
                            chartSpinner.setVisibility(View.GONE);

                            // Show file in pdf viewer
                            MainActivity.pdfView.fromFile(chartFile).load();

                        }

                        if(MainActivity.openChart){openChart(chartFile);}

                        break;

                    // Finish download folder charts
                    case "folderFinish":

                        downloadView.setText(String.format(activity.getResources().getString(R.string.downcomp_message),icaoCode));

                        if(MainActivity.showNotify){Notify.notify(activity,
                                String.format(activity.getResources().getString(R.string.downcomp_message),icaoCode));}

                        if(MainActivity.internalPDF){

                            // Make instance of chartItems
                            ArrayList<FilesItems> list = chartItems;

                            // Put the ICAO code and files ArrayList
                            MainActivity.filesMap.put(icaoCode, list);

                            // Add chart to files and set type to folder
                            MainActivity.files.add(0,new FilesItems(icaoCode, "Folder", null));

                            // Add chart to spinner
                            MainActivity.fileSpinnerItems.add(0,icaoCode);

                            MainActivity.fileSpinnerAdapter.notifyDataSetChanged();

                        }

                        break;

                    // If chart is exists
                    case "fileExist":

                        if(MainActivity.internalPDF) {

                            // Check if it added to files list
                            if(check_avail(icaoCode)) {

                                // Add to file list
                                MainActivity.files.add(0, new FilesItems(icaoCode, "Normal", chartFile));

                                MainActivity.fileSpinnerItems.add(0, icaoCode);

                                MainActivity.fileSpinnerAdapter.notifyDataSetChanged();

                                chartSpinner.setVisibility(View.GONE);

                                MainActivity.pdfView.fromFile(chartFile).load();

                            }

                        }

                        if(MainActivity.openChart){

                            // Open chart
                            if(check_avail(icaoCode)){openChart(chartFile);}

                        }

                    // Charts exists for folder, it won't added to files spinner
                    case "folderExist":

                        // Set TextView
                        downloadView.setText(String.format(activity.getResources().getString(R.string.fileexist_message),icaoCode));

                        // If notification is enabled
                        if(MainActivity.showNotify){Notify.notify(activity,
                                String.format(activity.getResources().getString(R.string.fileexist_message),icaoCode));}

                        break;

                    // Charts not found
                    case "downFail":

                        downloadView.setText(String.format(activity.getResources().getString(R.string.downfail_message),icaoCode));

                        if(MainActivity.showNotify){Notify.notify(activity,
                                String.format(activity.getResources().getString(R.string.downfail_message),icaoCode));}

                        break;

                    // Error in write charts
                    case "writeFail":

                        downloadView.setText(String.format(activity.getResources().getString(R.string.writefail_message),icaoCode));

                        if(MainActivity.showNotify){Notify.notify(activity,
                                String.format(activity.getResources().getString(R.string.writefail_message),icaoCode));}

                        break;

                    // the whole download process finished
                    case "downFinish":

                        bar.setVisibility(View.GONE);

                        getButton.setEnabled(true);

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

    // Open chart after download
    private void openChart(File chartFile){

        // Set intent
        Intent target = new Intent(Intent.ACTION_VIEW);

        // Set action to view pdf and path
        target.setDataAndType(Uri.fromFile(chartFile),"application/pdf");

        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        // Set final intent
        Intent intent = Intent.createChooser(target, "Open File");

        // Start intent
        try {
            activity.startActivity(intent);
        // If no app installed
        } catch (Exception e) {
            Toast.makeText(activity,activity.getResources().getString(R.string.pdfreader_message),Toast.LENGTH_SHORT).show();}
    }

}
