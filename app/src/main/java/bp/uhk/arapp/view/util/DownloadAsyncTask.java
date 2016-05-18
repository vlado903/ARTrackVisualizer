package bp.uhk.arapp.view.util;

import android.app.Activity;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import bp.uhk.arapp.view.components.DownloadNotificationBuilder;

/**
 * Created by vlado on 19.04.2016.
 */
public class DownloadAsyncTask extends AsyncTask<Void, Integer, Void> {


    private String stringUrl = "";
    private String filepath;
    private DownloadListener downloadListener;
    private DownloadNotificationBuilder notificationBuilder;

    private long filesize = 0, downloaded = 0;

    public DownloadAsyncTask(String filepath, String url, Activity activity){
        this.filepath = filepath;
        this.stringUrl = url;

        notificationBuilder = new DownloadNotificationBuilder(activity);
    }

    public DownloadAsyncTask(String filepath, String url){
        this.filepath = filepath;
        this.stringUrl = url;
    }


    @Override
    protected Void doInBackground(Void... params) {
        try {
            URL url = new URL(stringUrl);
            URLConnection connection = url.openConnection();
            filesize = connection.getContentLength();
            BufferedInputStream in = new BufferedInputStream(connection.getInputStream());

            File file = new File(filepath);
            if (!file.exists()) file.createNewFile();

            DataOutputStream out;
            FileOutputStream fos = new FileOutputStream(file);
            out = new DataOutputStream(fos);

            int count;
            int i = 0;
            byte[] buffer = new byte[1024];

            long millis = System.currentTimeMillis();

            while ((count = in.read(buffer)) != -1) {
                downloaded += count;
                out.write(buffer, 0, count);

                if (notificationBuilder != null){
                    long elapsedTime;
                    if (i % 500 == 0 && (elapsedTime = System.currentTimeMillis() - millis) > 1000){
                        notificationBuilder.updateProgress((int) (100*downloaded/filesize), (int) (500*1000/elapsedTime));
                        millis = System.currentTimeMillis();
                    }
                    i++;
                }
            }

            out.flush();
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            if (notificationBuilder != null)notificationBuilder.showDownloadingError();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (filesize == downloaded){
            if (notificationBuilder != null)notificationBuilder.finish();
            if (downloadListener != null) downloadListener.onDownloadFinished();
        }
    }

    public void setDownloadListener(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    public void stop() {
        cancel(true);

        File f = new File(filepath);
        if (f.exists()) f.delete();
        if (notificationBuilder != null){
            notificationBuilder.cancel();
            notificationBuilder = null;
        }
    }

    public abstract class DownloadListener {
        public abstract void onDownloadFinished();
    }
}
