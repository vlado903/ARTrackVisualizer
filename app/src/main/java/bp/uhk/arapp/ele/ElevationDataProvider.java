package bp.uhk.arapp.ele;

import android.app.Activity;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import bp.uhk.arapp.R;
import bp.uhk.arapp.view.util.DownloadAsyncTask;

/**
 * Created by vlado on 20.04.2016.
 */
public class ElevationDataProvider
{

    public final static int GOOGLE_ELEVATION_API = 0;
    public final static int CZECHIA = 1;
    public final static int TAIWAN = 2;
    public final static int HRADEC_KRALOVE = 3;

    static String CzURL = "https://dl.dropbox.com/s/hrfdawfhr7jravu/CZ500.zip?dl=1";
    static String TwURL = "https://dl.dropbox.com/s/skuuxqcj4y5p4a7/TW100.zip?dl=1";
    static String HkURL = "https://dl.dropbox.com/s/df2d7yvy1ed6hlu/HK28.zip?dl=1";

    private String filepath = "";
    private String infoFilePath = "";
    private String url = "";

    private int dataSource;
    private DownloadAsyncTask downloadAsyncTask;
    private Activity activity;

    public ElevationDataProvider(int region, String rootPath)
    {
        filepath = rootPath;
        loadMatchingUrlAndFilepaths(region);
    }

    public ElevationDataProvider(int dataSource, String rootPath, Activity activity)
    {
        this.dataSource = dataSource;
        if (dataSource != GOOGLE_ELEVATION_API)
        {
            filepath = rootPath;
            loadMatchingUrlAndFilepaths(dataSource);
        }
        this.activity = activity;
    }

    private boolean isDownloadedAlready()
    {
        File f = new File(infoFilePath);
        return f.exists();
    }

    private void loadMatchingUrlAndFilepaths(int dataSource)
    {
        switch (dataSource)
        {
            case CZECHIA:
                url = CzURL;
                filepath += "/CZ.zip";
                infoFilePath = filepath.substring(0, filepath.lastIndexOf('.')) + "/ele.info";
                break;
            case TAIWAN:
                url = TwURL;
                filepath += "/TW.zip";
                infoFilePath = filepath.substring(0, filepath.lastIndexOf('.')) + "/taiwanWhole.info";
                break;
            case HRADEC_KRALOVE:
                url = HkURL;
                filepath += "/HK.zip";
                infoFilePath = filepath.substring(0, filepath.lastIndexOf('.')) + "/ele.info";
                break;
        }

    }

    public void getAsyncManager(final OnDataReadyListener listener)
    {
        if (dataSource == GOOGLE_ELEVATION_API)
        {
            listener.onDataReady(new ElevationManagerGoogle(activity.getString(R.string.google_elevation_key)));
            return;
        }
        if (!isDownloadedAlready())
        {

            if (activity != null)
            {
                downloadAsyncTask = new DownloadAsyncTask(filepath, url, activity);
                Toast.makeText(activity, activity.getString(R.string.temporary_mode), Toast.LENGTH_LONG).show();
            }
            else
            {
                downloadAsyncTask = new DownloadAsyncTask(filepath, url);
            }

            downloadAsyncTask.setDownloadListener(downloadAsyncTask.new DownloadListener()
            {
                @Override
                public void onDownloadFinished()
                {
                    extractData(listener);
                }
            });

            downloadAsyncTask.execute();
        }
        else
        {
            listener.onDataReady(new ElevationManagerBMP(infoFilePath));
        }
    }

    private void extractData(final OnDataReadyListener listener)
    {
        downloadAsyncTask = null;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    FileInputStream fis = new FileInputStream(filepath);
                    ZipInputStream zis = new ZipInputStream(fis);
                    ZipEntry zipEntry;

                    File directory = new File(filepath.substring(0, filepath.lastIndexOf('.')));
                    if (!directory.exists()) directory.mkdirs();

                    while ((zipEntry = zis.getNextEntry()) != null)
                    {

                        FileOutputStream fout = new FileOutputStream(directory.getAbsolutePath() + "/" + zipEntry.getName(), false);

                        byte[] buffer = new byte[8192];

                        int count;
                        while ((count = zis.read(buffer)) != -1)
                        {
                            fout.write(buffer, 0, count);
                        }
                        fout.close();
                        zis.closeEntry();
                    }

                    new File(filepath).delete();

                    listener.onDataReady(new ElevationManagerBMP(infoFilePath));
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    listener.onDataFailed();
                }
            }
        }).start();
    }

    public void cancelDownload()
    {
        if (downloadAsyncTask != null) downloadAsyncTask.stop();
    }

    public abstract class OnDataReadyListener
    {
        public abstract void onDataReady(ElevationManager em);

        public abstract void onDataFailed();
    }

}
