package bp.uhk.arapp.ele;

import android.location.Location;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vlado on 17.01.2016.
 */
public class ElevationManagerASC implements ElevationManager
{

    double X_MIN;
    double Y_MIN;  //levý dolní roh
    double cellSize;
    String noDataValue;

    int rowsX;
    int columnsY;
    long dataStart;

    String filepath;

    RandomAccessFile randomAccessFile;

    public ElevationManagerASC(String filePathToASC)
    {

        filepath = filePathToASC;

        try
        {
            randomAccessFile = new RandomAccessFile(filepath, "r");

            columnsY = Integer.parseInt(randomAccessFile.readLine().substring(13)) - 1;
            rowsX = Integer.parseInt(randomAccessFile.readLine().substring(13)) - 1;
            X_MIN = Double.parseDouble(randomAccessFile.readLine().substring(13));
            Y_MIN = Double.parseDouble(randomAccessFile.readLine().substring(13));
            cellSize = Double.parseDouble(randomAccessFile.readLine().substring(13));
            noDataValue = randomAccessFile.readLine().substring(13);

            dataStart = randomAccessFile.getChannel().position();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public Location getNearestElevation(double lat, double lon)
    {

        Location locationResult = new Location("");
        locationResult.setLatitude(lat);
        locationResult.setLongitude(lon);
        locationResult.setAltitude(0);
        locationResult.setAccuracy(0);

        long startTime = System.currentTimeMillis();

        int row = rowsX;
        int column = 0;

        if (lat < Y_MIN || lon < X_MIN) return locationResult;

        while (lat > Y_MIN)
        {
            lat -= cellSize;
            row--;
        }

        if (row < 0) return locationResult;

        while (lon > X_MIN)
        {
            lon -= cellSize;
            column++;
        }

        if (column > columnsY) return locationResult;

        String elevation = "";
        try
        {
            if (randomAccessFile == null) new RandomAccessFile(filepath, "r");

            randomAccessFile.seek(dataStart + row * 128192 + column * 5);

            for (int i = 0; i < 5; i++)
            {
                elevation += (char) randomAccessFile.read();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("Total getNearestElevation elapsed time - " + String.valueOf(System.currentTimeMillis() - startTime) + "ms");

        locationResult.setAltitude(Double.valueOf(elevation));
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
        List<Location> locationResultList = new ArrayList<>();
        for (Location l : locationList) locationResultList.add(getNearestElevation(l));
        return locationResultList;
    }

}
