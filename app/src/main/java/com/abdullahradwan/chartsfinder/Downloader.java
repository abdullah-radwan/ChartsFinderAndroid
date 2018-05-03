package com.abdullahradwan.chartsfinder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class Downloader extends AsyncTask<Void, Integer, Void> {

    private final Activity activity;

    private final ProgressBar bar;

    private final TextView downloadView;

    private final Button getButton;

    private String appName;

    private File file;

    private boolean cont = true;

    private PowerManager.WakeLock wakeLock;

    private InputStream input;

    private OutputStream output;

    private HttpURLConnection connection;

    Downloader(Activity activity){

        this.activity = activity;

        bar = activity.findViewById(R.id.progressBar);

        downloadView = activity.findViewById(R.id.downloadView);

        getButton = activity.findViewById(R.id.getButton);

    }

    @Override
    protected void onPreExecute() {

        PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);

        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());

        wakeLock.acquire(10 * 60 * 300L /*3 minutes*/);
    }

    @Override
    protected Void doInBackground(Void... voids) {

        for (String anIcaoCode : MainActivity.icaoCode) {

            if(!anIcaoCode.equals("")) {

                String fileName = String.format("/%s.pdf", anIcaoCode);

                editActivity("downStart", anIcaoCode);

                for (int i = 0; i < MainActivity.resources.size(); i++) {

                        try {

                            URL url = new URL(String.format(MainActivity.resources.get(i), anIcaoCode));

                            connection = (HttpURLConnection) url.openConnection();

                            connection.connect();

                            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                                input = connection.getInputStream();

                                if (!MainActivity.path.equals("")) {
                                    file = new File(MainActivity.path + fileName);
                                } else {
                                    file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + fileName);
                                }

                                if (!file.exists()) {

                                    output = new FileOutputStream(file);

                                    byte data[] = new byte[4096];

                                    int count;

                                    int length = connection.getContentLength();

                                    long total = 0;

                                    while ((count = input.read(data)) != -1) {

                                        total += count;

                                        if (length > 0) {
                                            publishProgress((int) (total * 100 / length));
                                        }

                                        output.write(data, 0, count);

                                    }

                                    input.close();

                                    output.close();

                                    connection.disconnect();

                                    cont = false;

                                    editActivity("downComp", anIcaoCode);

                                    break;

                                } else{editActivity("fileExist", anIcaoCode); cont=false; break;}

                            }

                        } catch (Exception ex) {
                            editActivity("writeFail", anIcaoCode);
                            cont = false;
                            break;
                        } finally {
                            try {
                                input.close();
                                output.close();
                                connection.disconnect();
                            } catch (Exception ignored) {
                            }
                        }

                    }

                if (cont) {
                    editActivity("downFail", anIcaoCode);
                }

                try {
                    Thread.sleep(5000);
                } catch (Exception ignored) {
                }

            } else{editActivity("fieldEmpty","");}


        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {

        super.onProgressUpdate(values);

        bar.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {super.onPostExecute(aVoid); wakeLock.release();}

    private void editActivity(final String op, final String icaoCode){

        appName = activity.getResources().getString(R.string.app_name);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (op){

                    case "fieldEmpty":

                        downloadView.setText(activity.getResources().getString(R.string.fieldempty_message));

                        break;

                    case "downStart":

                        getButton.setEnabled(false);

                        downloadView.setText(String.format(activity.getResources().getString(R.string.downstart_message),icaoCode));

                        bar.setProgress(0);

                        bar.setVisibility(View.VISIBLE);

                        if(MainActivity.showNotify){Notify.notify(activity,appName,
                                String.format(activity.getResources().getString(R.string.downstart_message),icaoCode));}

                        break;

                    case "downComp":

                        downloadView.setText(String.format(activity.getResources().getString(R.string.downcomp_message),icaoCode));

                        bar.setVisibility(View.GONE);

                        getButton.setEnabled(true);

                        if(MainActivity.showNotify){Notify.notify(activity,appName,
                                String.format(activity.getResources().getString(R.string.downcomp_message),icaoCode));}

                        if(MainActivity.interiorPDF){

                            MainActivity.files.add(0, file);

                            MainActivity.spinnerItems.add(0, icaoCode);

                            MainActivity.spinnerAdapter.notifyDataSetChanged();

                            MainActivity.pdfView.fromFile(MainActivity.files.get(0)).load();

                        }

                        if(MainActivity.openChart){openChart();}

                        break;

                    case "fileExist":

                        downloadView.setText(String.format(activity.getResources().getString(R.string.fileexist_message),icaoCode));

                        bar.setVisibility(View.GONE);

                        getButton.setEnabled(true);

                        if(MainActivity.showNotify){Notify.notify(activity,appName,
                                String.format(activity.getResources().getString(R.string.fileexist_message),icaoCode));}

                        break;

                    case "downFail":

                        downloadView.setText(String.format(activity.getResources().getString(R.string.downfail_message),icaoCode));

                        bar.setVisibility(View.GONE);

                        getButton.setEnabled(true);

                        if(MainActivity.showNotify){Notify.notify(activity,appName,
                                String.format(activity.getResources().getString(R.string.downfail_message),icaoCode));}

                        break;

                    case "writeFail":

                        downloadView.setText(String.format(activity.getResources().getString(R.string.writefail_message),icaoCode));

                        bar.setVisibility(View.GONE);

                        getButton.setEnabled(true);

                        if(MainActivity.showNotify){Notify.notify(activity,appName,
                                String.format(activity.getResources().getString(R.string.writefail_message),icaoCode));}

                        break;
                }
            }
        });

    }

    private void openChart(){

        Intent target = new Intent(Intent.ACTION_VIEW);

        target.setDataAndType(Uri.fromFile(file),"application/pdf");

        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        Intent intent = Intent.createChooser(target, "Open File");

        try {activity.startActivity(intent);} catch (Exception e) {
            Toast.makeText(activity,activity.getResources().getString(R.string.pdfreader_message), Toast.LENGTH_SHORT).show();}

    }

}
