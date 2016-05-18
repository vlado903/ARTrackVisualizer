package bp.uhk.arapp.geo;

import android.location.Location;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by vlado on 17.01.2016.
 */
public class GeoTools {

    private GeoTools(){}

    public static List<Location> getMidPoints(double lat1,double lon1,double lat2,double lon2, int numberOfPoints){

        LinkedList<Location> midPoints = new LinkedList<>();

        if (numberOfPoints > 0){
            double latDiff = lat2-lat1;
            double lonDiff = lon2-lon1;

            double latDiffDivided = latDiff/(numberOfPoints+1);
            double lonDiffDivided = lonDiff/(numberOfPoints+1);

            Location firstPoint = new Location("");
            firstPoint.setLatitude(lat1 + latDiffDivided);
            firstPoint.setLongitude(lon1 + lonDiffDivided);

            midPoints.add(firstPoint);

            for (int i = 1; i < numberOfPoints; i++){

                Location last = midPoints.getLast();

                double latitude = last.getLatitude() + latDiffDivided;
                double longitude = last.getLongitude() + lonDiffDivided;

                Location l = new Location("");
                l.setLatitude(latitude);
                l.setLongitude(longitude);

                midPoints.add(l);
            }
        }

        return midPoints;
    }

    public static List<Location> getMidPoints(Location l1, Location l2, int numberOfPoints){
        return getMidPoints(l1.getLatitude(), l1.getLongitude(), l2.getLatitude(), l2.getLongitude(), numberOfPoints);
    }


    //pomocná funkce na získání vzdálenost dvou bodů, vrací km - znatelně rychlejší než Location.distanceTo()
    public static double getDistance(double latitude1, double longitude1, double latitude2, double longitude2) {

        latitude1 = Math.toRadians(latitude1);
        longitude1 = Math.toRadians(longitude1);
        latitude2 = Math.toRadians(latitude2);
        longitude2 = Math.toRadians(longitude2);

        return 6371 * Math.acos(Math.sin(latitude1) * Math.sin(latitude2) + Math.cos(latitude1) * Math.cos(latitude2) * Math.cos(longitude2 - longitude1));
    }

    public static double getDistance(Location l1, Location l2){
        return getDistance(l1.getLatitude(), l1.getLongitude(), l2.getLatitude(), l2.getLongitude());
    }

    //vrací celkovou vzdálenost seřazených bodů v km
    public static double getTotalDistance(List<Location> points){
        double totalDistance = 0;
        for (int i = 1; i < points.size(); i++){
            totalDistance += getDistance(points.get(i-1), points.get(i));
        }
        return totalDistance;
    }

    public static float[] normalizeVector(float[] vector){

        double x = vector[0];
        double y = vector[1];
        double z = vector[2];

        double length = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

        if (length == 0) return new float[]{0,0,0};

        x = x/length;
        y = y/length;
        z = z/length;

        return new float[]{(float) x, (float) y, (float) z};
    }

}
