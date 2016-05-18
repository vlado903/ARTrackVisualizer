package bp.uhk.arapp.view.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by vlado on 15.04.2016.
 */
public final class PermissionInterpreter {

    private PermissionInterpreter(){}

    public static boolean evaluateGrantResults(int[] grantResults){
        int grantedSum = 0;
        for (int i : grantResults) if (i == PackageManager.PERMISSION_GRANTED) grantedSum++;{
            return grantedSum == grantResults.length;
        }
    }

    public static boolean isCameraGranted(Context context){
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isReadStorageGranted(Context context){
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isLocationGranted(Context context){
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}