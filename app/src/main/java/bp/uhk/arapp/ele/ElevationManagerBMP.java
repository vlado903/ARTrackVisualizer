package bp.uhk.arapp.ele;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import bp.uhk.arapp.geo.GeoTools;

/**
 * Created by vlado on 28.02.2016.
 */
public class ElevationManagerBMP implements ElevationManager
{
    private static final String TAG = ElevationManagerBMP.class.getSimpleName();

    private ArrayList<String> paths = new ArrayList<>();
    private NavigableSet<Double> borders = new TreeSet<>();

    private Bitmap eleBitmap;
    private String filePath = "";

    private double X_MIN;
    private double Y_MIN;  //levý dolní roh
    private double X_MAX;
    private double Y_MAX;
    private double cellSize;
    private String noDataValue = "";

    private int rows;
    private int columns;
    private int pixelHeight;
    private int numOfFiles;

    public ElevationManagerBMP(String infoFilePath)
    {

        File infoFile = new File(infoFilePath);
        try
        {
            BufferedReader bfr = new BufferedReader(new InputStreamReader(new FileInputStream(infoFile)));
            columns = Integer.parseInt(bfr.readLine().substring(13)) - 1;
            rows = Integer.parseInt(bfr.readLine().substring(13)) - 1;
            X_MIN = Double.parseDouble(bfr.readLine().substring(13));
            Y_MIN = Double.parseDouble(bfr.readLine().substring(13));
            cellSize = Double.parseDouble(bfr.readLine().substring(13));
            noDataValue = bfr.readLine().substring(13);
            pixelHeight = Integer.parseInt(bfr.readLine().substring(13));
            numOfFiles = Integer.parseInt(bfr.readLine().substring(13));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Y_MAX = Y_MIN + cellSize * rows;
        X_MAX = X_MIN + cellSize * columns;

        int lastImgHeight = rows % pixelHeight;
        double firstBorder = Y_MIN + cellSize * lastImgHeight;
        borders.add(Y_MIN);
        borders.add(firstBorder);

        for (int i = 0; i < numOfFiles; i++)
        {
            String directoryPath = infoFile.getParent();
            String fileName = infoFile.getName().substring(0, infoFile.getName().indexOf("."));
            paths.add(directoryPath + "/" + fileName + i + ".png");

            if (i > 1) borders.add(firstBorder + (i - 1) * cellSize * pixelHeight);
        }
    }

    @Override
    public Location getNearestElevation(double lat, double lon)
    {

        Location locationResult = new Location("");
        locationResult.setLatitude(lat);
        locationResult.setLongitude(lon);
        locationResult.setAltitude(0);
        locationResult.setAccuracy(-1);

        long startTime = System.currentTimeMillis();

        if (lat < Y_MIN || lat > Y_MAX || lon < X_MIN || lon > X_MAX) return locationResult;

        double closestBorder = borders.floor(lat);
        ArrayList<Double> borderArray = new ArrayList<>(borders.descendingSet());

        int fileNo = borderArray.indexOf(closestBorder);

        if (!filePath.equals(paths.get(fileNo)) || eleBitmap == null)
        {
            filePath = paths.get(fileNo);
            eleBitmap = BitmapFactory.decodeFile(filePath);
        }

        double fileLatitude = closestBorder;
        int indexY = eleBitmap.getHeight() - 1;
        double halfCellSize = cellSize / 2;

        while (lat > fileLatitude && lat - fileLatitude > halfCellSize)
        {
            fileLatitude += cellSize;
            indexY--;
        }

        if (indexY == -1)
        { // CHECK FOR BORDER
            if (fileNo > 0)
            {
                fileNo--;
                eleBitmap = BitmapFactory.decodeFile(paths.get(fileNo));
                indexY = eleBitmap.getHeight() - 1;
            }
            else
            {
                return locationResult;
            }
        }

        double fileLongitude = X_MIN;
        int indexX = 0;

        while (lon - fileLongitude > halfCellSize)
        {
            fileLongitude += cellSize;
            indexX++;
        }

        //Log.d(TAG, String.format("fileNo: %s   indexY: %s   bitmapHeight: %s   latitude: %s   fileLatitude: %s   closestBorder: %s", fileNo, indexY, eleBitmap.getHeight(), latitude, fileLatitude, closestBorder));

        int i = eleBitmap.getPixel(indexX, indexY) << 8;
        double result = (double) i / 100;

        Log.d(TAG, "Total getNearestElevation elapsed time - " + (System.currentTimeMillis() - startTime) + "ms");

        locationResult.setAltitude(result);
        locationResult.setAccuracy(Math.round(GeoTools.getDistance(lat, lon, fileLatitude, fileLongitude) * 1000));

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
