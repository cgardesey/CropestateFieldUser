package com.cropestate.fielduser.util;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadFileAsync extends AsyncTask<String, Integer, String> {

    OnTaskCompletedInterface onTaskCompletedInterface;
    OnTaskProgressUpdateInterface onTaskProgressUpdateInterface;
    OnTaskCancelledInterface onTaskCancelledInterface;

    public DownloadFileAsync(OnTaskCompletedInterface onTaskCompletedInterface, OnTaskProgressUpdateInterface onTaskProgressUpdateInterface, OnTaskCancelledInterface onTaskCancelledInterface) {
        this.onTaskCompletedInterface = onTaskCompletedInterface;
        this.onTaskProgressUpdateInterface = onTaskProgressUpdateInterface;
        this.onTaskCancelledInterface = onTaskCancelledInterface;
    }

    @Override
    protected String doInBackground(String... f_url) {
        int count;
        try {
            URL url = new URL(f_url[0]);
            URLConnection conection = url.openConnection();
            conection.connect();

            // this will be useful so that you can show a tipical 0-100%
            // progress bar
            int lenghtOfFile = conection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);

            // Output stream
            OutputStream output = new FileOutputStream(f_url[1]);

            byte[] data = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress((int) ((total * 100) / lenghtOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
            return e.toString();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        onTaskCompletedInterface.onTaskCompleted(response);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        onTaskProgressUpdateInterface.onTaskProgressUpdate(values[0]);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        onTaskCancelledInterface.onTaskCancelled();
    }

    public interface OnTaskCompletedInterface {
        void onTaskCompleted(String file_url);
    }

    public interface OnTaskProgressUpdateInterface {
        void onTaskProgressUpdate(int progress);
    }

    public interface OnTaskCancelledInterface {
        void onTaskCancelled();
    }
}
