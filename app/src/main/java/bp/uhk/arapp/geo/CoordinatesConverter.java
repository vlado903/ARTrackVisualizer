package bp.uhk.arapp.geo;

import android.location.Location;

import bp.uhk.arapp.ar.WorldRenderer;

/**
 * Created by vlado on 18.03.2016.
 */
public class CoordinatesConverter {

    public static float MAX_FAR_VALUE = -1; //neboli přibližuj body neustále

    private double x, y, z;
    private double objectLat, objectLon, objectAlt, userLat, userLon, userAlt;
    private double elevationFix = 0;

    private boolean interpolationMode = true;

    public CoordinatesConverter(Location objectLocation, Location userLocation){
        loadObjectLocation(objectLocation);
        loadUserLocation(userLocation);
    }

    public float[] getCoordinates() {
        calculateCoordinates();
        return new float[]{(float) x, (float) y, (float) z};
    }

    public void setUserLocation(Location userLocation){
        loadUserLocation(userLocation);
    }

    public void setInterpolationMode(boolean interpolationMode) {
        this.interpolationMode = interpolationMode;
    }


    private synchronized void calculateCoordinates() {
        convertCoordinatesToMeters();
        if (interpolationMode) zoomIfFar();
    }

    private void convertCoordinatesToMeters() {

        double latSignum;
        if (userLat > 0){
            latSignum = Math.signum(objectLat - userLat);   //severní polokoule
        }else{
            latSignum = Math.signum(userLat - objectLat);   //jižní polokoule
        }

        double lonSignum;
        if (userLon > 0){
            lonSignum = Math.signum(objectLon - userLon);   //východní polokoule
        }else{
            lonSignum = Math.signum(userLon - objectLon);   //západní polokoule
        }

        x = lonSignum * 1000 * GeoTools.getDistance(userLat, objectLon, userLat, userLon);
        y = latSignum * 1000 * GeoTools.getDistance(objectLat, userLon, userLat, userLon);
        z = objectAlt - userAlt;

        System.out.println("x: " + x + " y: " + y + " z: " + z);
    }

    private void zoomIfFar() {

        double distanceXY = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));                        //vzdálenost bez ohledu na nadmořskou výšku v metrech
        double distance = (float) Math.sqrt(Math.pow(distanceXY, 2) + Math.pow(z, 2));        //celková vzdálenost v metrech

        double distanceOver = distance - MAX_FAR_VALUE;

        if (distanceOver > 1) {   // pro vzdálenost větší než MAX_FAR_VALUE (dále se nevykresluje) proveď přiblížení

            //pěkná funkce pro přiblížení; Math.min, protože pokud by bylo distance větší než Z_FAR_VALUE, bod by se nevykreslil
            distance = Math.min(MAX_FAR_VALUE + 20 + Math.pow(Math.log(distanceOver), 3) / 2, WorldRenderer.Z_FAR_VALUE);

            double slope3D = z/distanceXY;

            distanceXY = distance/Math.sqrt(Math.pow(slope3D, 2) + 1);

            double slope2D = y/x;

            x = (float) (Math.signum(x)*distanceXY / Math.sqrt(Math.pow(slope2D, 2) + 1));
            y = (float) (Math.signum(y)*Math.abs(slope2D*x));
            z = (float) (distanceXY*slope3D);
        }
    }

    private void loadUserLocation(Location userLocation) {
        userLat = userLocation.getLatitude();
        userLon = userLocation.getLongitude();
        userAlt = userLocation.getAltitude() + elevationFix;
    }

    private void loadObjectLocation(Location objectLocation) {
        objectLat = objectLocation.getLatitude();
        objectLon = objectLocation.getLongitude();
        objectAlt = objectLocation.getAltitude();
    }

    public void setElevationFix(double elevationFix) {
        this.elevationFix = elevationFix;

    }
}
