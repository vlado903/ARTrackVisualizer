package bp.uhk.arapp.view.components;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import bp.uhk.arapp.R;
import bp.uhk.arapp.view.MainActivity;

/**
 * Created by vlado on 20.04.2016.
 */
public class DownloadNotificationBuilder
{

    public static final String CANCEL_DOWNLOAD = "CANCEL_DOWNLOAD";

    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private Activity activity;
    private int id = 0;

    public DownloadNotificationBuilder(Activity a)
    {
        this.activity = a;

        notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

        builder = new NotificationCompat.Builder(activity);

        builder.setContentTitle(activity.getString(R.string.downloading_elevation))
                .setContentText(activity.getString(R.string.download_in_progress))
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_stat_pin);
        builder.setProgress(100, 0, false);

        associateCancelIntent();
        notificationManager.notify(id, builder.build());

    }

    public void updateProgress(int progress, int speedKBs)
    {
        builder.setProgress(100, progress, false);
        builder.setContentText(activity.getString(R.string.download_in_progress) + " - " + speedKBs + " KB/s");
        notificationManager.notify(id, builder.build());
    }

    public void finish()
    {
        updateNotificationText(activity.getString(R.string.download_finished));
        removeCancelIntent();
        associateOpenIntent();
    }

    public void cancel()
    {
        notificationManager.cancel(id);
    }

    public void showDownloadingError()
    {
        updateNotificationText(activity.getString(R.string.download_error));
        removeCancelIntent();
        associateOpenIntent();
    }

    private void associateCancelIntent()
    {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setAction(CANCEL_DOWNLOAD);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        activity,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.setContentIntent(pendingIntent);
    }

    private void removeCancelIntent()
    {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setAction(CANCEL_DOWNLOAD);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent.getActivity(
                activity,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        ).cancel();
    }

    private void updateNotificationText(String text)
    {
        builder.setContentText(text)
                // Removes the progress bar
                .setProgress(0, 0, false)
                .setOngoing(false);
        notificationManager.notify(id, builder.build());
    }

    private void associateOpenIntent()
    {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        activity,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.setContentIntent(pendingIntent);
        notificationManager.notify(id, builder.build());
    }
}
