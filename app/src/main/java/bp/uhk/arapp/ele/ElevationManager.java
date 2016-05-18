package bp.uhk.arapp.ele;

import android.location.Location;

import java.util.List;

/**
 * Created by vlado on 17.01.2016.
 */
public interface ElevationManager {

    Location getNearestElevation(double latitude, double longitude);

    Location getNearestElevation(Location location);

    List<Location> getNearestElevation(List<Location> locationList);
}
