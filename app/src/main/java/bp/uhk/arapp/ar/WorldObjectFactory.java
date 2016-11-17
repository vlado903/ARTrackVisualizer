package bp.uhk.arapp.ar;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vlado on 06.03.2016.
 */
public final class WorldObjectFactory
{

    public static List<WorldObject> buildPoints(List<Location> locations)
    {

        List<WorldObject> objects = new ArrayList<>();

        for (Location l : locations)
        {
            objects.add(new WorldObject(l, false));
        }

        return objects;
    }

    public static List<WorldObject> buildMidPoints(List<Location> locations)
    {

        List<WorldObject> objects = new ArrayList<>();

        for (Location l : locations)
        {
            objects.add(new WorldObject(l, true));
        }

        return objects;
    }

}
