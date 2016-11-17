package bp.uhk.arapp.view.tabs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import bp.uhk.arapp.R;
import bp.uhk.arapp.view.MainActivity;


public class MapTab extends SupportMapFragment implements OnMapReadyCallback
{

    private GoogleMap map;
    private LinkedList<Marker> markerList = new LinkedList<>();
    private Polyline line;

    private Map<Marker, Double> markerOrigLat = new HashMap<>();

    private MainActivity mainActivity;

    private Location draggedLocation;
    private int draggedLocationPosition;

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        mainActivity = (MainActivity) getActivity();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (map == null)
        {
            getMapAsync(this);
        }
    }

    // vytvoření/smazání bodu při dlouhém podržení
    private void setUpMap()
    {
        //noinspection MissingPermission
        map.setMyLocationEnabled(true);
        useTerrainMap(mainActivity.isTerrainMap());

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener()
        {
            @Override
            public void onMapLongClick(final LatLng latLng)
            {
                final Marker marker = map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .draggable(true));

                markerOrigLat.put(marker, latLng.latitude);
                markerList.add(marker);

                refreshLine();

                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Location location = new Location("");

                        location.setLatitude(latLng.latitude);
                        location.setLongitude(latLng.longitude);
                        location = mainActivity.getElevationManager().getNearestElevation(location);

                        mainActivity.getPoints().add(location);
                        setMarkerTitle(marker, location);
                    }
                }).start();
            }
        });

        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener()
        {
            @Override
            public void onMarkerDragStart(Marker marker)
            {
                double latitude = markerOrigLat.get(marker);  //potřebuju načíst originální zeměpisnou délku, drag totiž marker posune!

                for (draggedLocationPosition = 0; draggedLocationPosition < mainActivity.getPoints().size(); draggedLocationPosition++)
                {
                    if (mainActivity.getPoints().get(draggedLocationPosition).getLatitude() == latitude)
                    {
                        draggedLocation = mainActivity.getPoints().get(draggedLocationPosition);
                        mainActivity.getPoints().remove(draggedLocationPosition);
                        return;
                    }
                }
            }

            @Override
            public void onMarkerDrag(Marker marker)
            {

            }

            @Override
            public void onMarkerDragEnd(Marker marker)
            {
                draggedLocation.setLatitude(marker.getPosition().latitude);
                draggedLocation.setLongitude(marker.getPosition().longitude);

                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        draggedLocation = mainActivity.getElevationManager().getNearestElevation(draggedLocation);
                        mainActivity.getPoints().add(draggedLocationPosition, draggedLocation);
                        mainActivity.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                refreshMarkersAndLine();
                            }
                        });
                    }
                }).start();
            }
        });

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener()
        {
            @Override
            public void onInfoWindowClick(Marker marker)
            {
                showRemoveMarkerDialog(marker);
            }
        });
    }

    public void clearMap()
    {
        for (Marker m : markerList)
        {
            m.remove();
        }
        markerList.clear();
        markerOrigLat.clear();

        if (line != null)
        {
            line.remove();
            line = null;
        }
    }

    public void refreshMarkersAndLine()
    {
        clearMap();

        if (map != null)
        {
            for (final Location location : mainActivity.getPoints())
            {
                final Marker marker = map.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .draggable(true));

                markerOrigLat.put(marker, location.getLatitude());
                markerList.add(marker);

                setMarkerTitle(marker, location);
            }
            refreshLine();
        }
    }

    public void useTerrainMap(boolean terrainMap)
    {
        if (map != null)
        {
            if (terrainMap)
            {
                map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }
            else
            {
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
        setUpMap();
        refreshMarkersAndLine();
    }

    private void setMarkerTitle(final Marker marker, final Location l)
    {
        mainActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                marker.setTitle(String.format(getString(R.string.marker_title), markerList.size()));
                if (mainActivity.isShowAccuracy())
                {
                    marker.setSnippet(String.format(getString(R.string.marker_snippet_accuracy), l.getAltitude(), l.getAccuracy()));
                }
                else
                {
                    marker.setSnippet(String.format(getString(R.string.marker_snippet), l.getAltitude()));
                }
            }
        });
    }

    private void refreshLine()
    {
        LinkedList<LatLng> markerLocations = new LinkedList<>();
        for (Marker m : markerList)
        {
            markerLocations.add(m.getPosition());
        }

        if (markerList != null && markerList.size() > 0)
        {
            if (line == null)
                line = map.addPolyline(new PolylineOptions().add(markerList.getFirst().getPosition()));
            line.setWidth(6);
            line.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            line.setPoints(markerLocations);
        }
    }

    private void showRemoveMarkerDialog(final Marker marker)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setMessage(getString(R.string.remove_marker));

        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                removeMarkerAndPoint(marker);
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
            }
        });
        builder.show();
    }

    private void removeMarkerAndPoint(Marker marker)
    {
        for (Location l : mainActivity.getPoints())
        {
            if (l.getLatitude() == marker.getPosition().latitude && l.getLongitude() == marker.getPosition().longitude)
            {
                mainActivity.getPoints().remove(l);
                marker.remove();

                markerList.remove(marker);
                refreshLine();
                return;
            }
        }
    }
}

