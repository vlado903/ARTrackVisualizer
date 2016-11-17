package bp.uhk.arapp.ele;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

/**
 * Created by vlado on 19.04.2016.
 */
public class ElevationManagerGoogle implements ElevationManager
{

    String url = "https://maps.googleapis.com/maps/api/elevation/json?locations=%s&key=%s";
    String key = "";

    public ElevationManagerGoogle(String key)
    {
        this.key = key;
    }

    @Override
    public Location getNearestElevation(double latitude, double longitude)
    {

        long startTime = System.currentTimeMillis();

        Location locationResult = new Location("");
        locationResult.setLatitude(latitude);
        locationResult.setLongitude(longitude);
        locationResult.setAltitude(0);
        locationResult.setAccuracy(-1);

        String location = String.valueOf(latitude) + "," + String.valueOf(longitude);

        try
        {
            JSONObject response = getJSONResponse(location);
            locationResult.setAltitude(response.getJSONArray("results").getJSONObject(0).getDouble("elevation"));
            locationResult.setAccuracy((float) response.getJSONArray("results").getJSONObject(0).getDouble("resolution"));
        }
        catch (JSONException | IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("Total getNearestElevation elapsed time - " + String.valueOf(System.currentTimeMillis() - startTime) + "ms");

        return locationResult;
    }

    @Override
    public Location getNearestElevation(Location l)
    {
        return getNearestElevation(l.getLatitude(), l.getLongitude());
    }

    @Override
    public List<Location> getNearestElevation(List<Location> locationList)
    {

        long startTime = System.currentTimeMillis();

        String locations = "";

        for (int i = 0; i < locationList.size(); i++)
        {
            if (i != 0) locations += "|";
            locations += locationList.get(i).getLatitude() + "," + locationList.get(i).getLongitude();
        }

        try
        {
            JSONObject response = getJSONResponse(locations);
            for (int i = 0; i < locationList.size(); i++)
            {
                locationList.get(i).setAltitude(response.getJSONArray("results").getJSONObject(i).getDouble("elevation"));
                locationList.get(i).setAccuracy((float) response.getJSONArray("results").getJSONObject(i).getDouble("resolution"));
            }
        }
        catch (IOException | JSONException e)
        {
            e.printStackTrace();
            for (Location l : locationList)
            {
                l.setAltitude(0);
                l.setAccuracy(-1);
            }
        }

        System.out.println("Total getNearestElevation elapsed time - " + String.valueOf(System.currentTimeMillis() - startTime) + "ms");

        return locationList;
    }

    private JSONObject getJSONResponse(String locations) throws IOException, JSONException
    {
        URL url = new URL(String.format(this.url, locations, key));

        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));

        String json = "";
        String line;
        while ((line = reader.readLine()) != null)
        {
            json += line;
        }

        return new JSONObject(json);
    }
}
