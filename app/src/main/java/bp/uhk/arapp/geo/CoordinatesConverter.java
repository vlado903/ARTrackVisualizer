package bp.uhk.arapp.geo;

import android.location.Location;

import bp.uhk.arapp.ar.WorldRenderer;

/**
 * Created by vlado on 18.03.2016.
 */
public class CoordinatesConverter {

    public static float MAX_FAR_VALUE = -1; //neboli interpoluj neustále

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
        calculateLocalCoordinates();
        convertCoordinatesToMeters();
        if (interpolationMode) zoomCoordinatesIfFar();
    }

    private void calculateLocalCoordinates() {
        if (userLon > 0){
            x = objectLon - userLon;  //východní polokoule (neošetřuji situaci na greenwichi/rovníku)
        }else{
            x = userLon - objectLon;  //západní polokoule
        }

        if (userLat > 0){
            y = objectLat - userLat;   //severní polokoule
        }else{
            y = userLat - objectLat;   //jižní polokoule
        }
    }

    private void convertCoordinatesToMeters() {
        x = Math.signum(x) * 1000 * GeoTools.getDistance(0, x, 0, 0);
        y = Math.signum(y) * 1000 * GeoTools.getDistance(y, 0, 0, 0);
        z = objectAlt - userAlt;
    }

    private void zoomCoordinatesIfFar() {

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
