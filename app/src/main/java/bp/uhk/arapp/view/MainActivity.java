package bp.uhk.arapp.view;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import bp.uhk.arapp.R;
import bp.uhk.arapp.ele.ElevationDataProvider;
import bp.uhk.arapp.ele.ElevationManager;
import bp.uhk.arapp.geo.GeoTools;
import bp.uhk.arapp.view.components.DownloadNotificationBuilder;
import bp.uhk.arapp.view.components.SampleFragmentPagerAdapter;
import bp.uhk.arapp.view.tabs.HomeTab;
import bp.uhk.arapp.view.tabs.MapTab;
import bp.uhk.arapp.view.util.LocaleHelper;
import bp.uhk.arapp.view.util.PermissionInterpreter;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener, OnRequestPermissionsResultCallback
{

    private static final int REQUEST_CAMERA = 0;
    private static final int WRITE_EXTERNAL_STORAGE = 1;
    private static final int ACCESS_FINE_LOCATION = 2;
    public static List<Location> points = new ArrayList<>();
    public static List<Location> midPoints = new ArrayList<>();
    public static Location userLocation;
    private boolean terrainMap, pointMode, showAccuracy;
    private int dataSource;

    private SampleFragmentPagerAdapter pagerAdapter;

    private GoogleApiClient googleApiClient;

    private ElevationDataProvider dataProvider;
    private ElevationManager elevationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        terrainMap = sharedPref.getBoolean("terrainMap", false);
        pointMode = sharedPref.getBoolean("pointMode", false);
        showAccuracy = sharedPref.getBoolean("showAccuracy", false);
        dataSource = sharedPref.getInt("dataSource", ElevationDataProvider.GOOGLE_ELEVATION_API);

        LocaleHelper.onCreate(this);

//      restoreInstanceState(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkPermissionsAndProceed();
    }

    private void proceedAfterPermissionsGranted()
    {
        loadViewPager();
        connectGoogleApi();

        loadElevationManager(dataSource);
    }

    private void loadViewPager()
    {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        pagerAdapter = new SampleFragmentPagerAdapter(getSupportFragmentManager(), MainActivity.this);
        viewPager.setAdapter(pagerAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void connectGoogleApi()
    {
        if (googleApiClient == null)
        {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        googleApiClient.connect(); //začít s hledáním pozice
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        outState.putParcelableArrayList("points", (ArrayList) points);
//        outState.putBoolean("terrainMap", terrainMap);
//        outState.putBoolean("pointMode", pointMode);
//        outState.putBoolean("showAccuracy", showAccuracy);
//
//        super.onSaveInstanceState(outState);
//    }
//
//    private void restoreInstanceState(Bundle savedInstanceState) {
//        if (savedInstanceState != null) {
//            points = savedInstanceState.getParcelableArrayList("points");
//            terrainMap = savedInstanceState.getBoolean("terrainMap");
//            pointMode = savedInstanceState.getBoolean("pointMode");
//            showAccuracy = savedInstanceState.getBoolean("showAccuracy");
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_terrain_map).setChecked(terrainMap);
        menu.findItem(R.id.action_show_accuracy).setChecked(showAccuracy);
        menu.findItem(R.id.action_point_mode).setChecked(pointMode);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        MapTab mapTab = (MapTab) pagerAdapter.getItem(1);

        switch (item.getItemId())
        {

            case R.id.action_terrain_map:
                item.setChecked(!item.isChecked());
                terrainMap = item.isChecked();
                mapTab.useTerrainMap(terrainMap);
                return true;

            case R.id.action_point_mode:
                item.setChecked(!item.isChecked());
                pointMode = item.isChecked();
                return true;

            case R.id.action_show_accuracy:
                item.setChecked(!item.isChecked());
                showAccuracy = item.isChecked();
                mapTab.refreshMarkersAndLine();
                return true;

            case R.id.action_clear:
                points.clear();
                mapTab.clearMap();
                ((HomeTab) pagerAdapter.getItem(0)).refreshOverview();
                return true;

            case R.id.action_load_data:
                showDataChoiceDialog();
                return true;

            case R.id.action_change_language:
                showChangeLanguageDialog();
                return true;

            case R.id.action_about:
                showAboutDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showAR(View view)
    {
        if (!points.isEmpty())
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    midPoints.clear();
                    if (!pointMode) calculateMidPoints();
                    Intent i = new Intent(MainActivity.this, ARActivity.class);
                    startActivity(i);
                }
            }).start();
        }
        else
        {
            Toast.makeText(this, getString(R.string.no_points), Toast.LENGTH_SHORT).show();
        }

    }

    private void calculateMidPoints()
    {

        for (int i = 0; i < points.size() - 1; i++)
        { //vypočítat body

            double averageDistanceToUser = (GeoTools.getDistance(points.get(i), userLocation) + GeoTools.getDistance(points.get(i + 1), userLocation)) / 2;

//            double distanceToUser1 = GeoTools.getDistance(points.get(i), userLocation);
//            double distanceToUser2 = GeoTools.getDistance(points.get(i + 1), userLocation);
//
//            double distanceToUser1CloseRation = distanceToUser1/(distanceToUser1 + distanceToUser2);
//            double distanceToUser2CloseRation = distanceToUser2/(distanceToUser1 + distanceToUser2);

            double distanceBetweenPoints = GeoTools.getDistance(points.get(i), points.get(i + 1));

            double numberOfMidPoints = 15 * distanceBetweenPoints / Math.sqrt(averageDistanceToUser);
            numberOfMidPoints = Math.min(numberOfMidPoints, 50);    //Google elevation API hodí při dlouhé URL error 400 + omezení se hodí

            List<Location> midPointsPart = GeoTools.getMidPoints(points.get(i), points.get(i + 1), Math.round(Math.round(numberOfMidPoints)));

            midPoints.addAll(elevationManager.getNearestElevation(midPointsPart));  //doplnit jim nadmořskou výšku
        }
    }

    public ElevationManager getElevationManager()
    {
        return elevationManager;
    }

    public Location getCurrentLocation()
    {
        return userLocation;
    }

    public List<Location> getPoints()
    {
        return points;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onRestart()
    {
        if (googleApiClient != null)
        {
            googleApiClient.connect();
        }
        else
        {
            checkPermissionsAndProceed();
        }
        super.onRestart();
    }

    @Override
    protected void onStop()
    {
        if (googleApiClient != null) googleApiClient.disconnect();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("pointMode", pointMode);
        editor.putBoolean("terrainMap", terrainMap);
        editor.putBoolean("showAccuracy", showAccuracy);
        editor.putInt("dataSource", dataSource);
        editor.clear();
        editor.apply();

        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (dataProvider != null) dataProvider.cancelDownload();
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //noinspection MissingPermission
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(final Location location)
    {

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if (elevationManager != null)
                {
                    userLocation = elevationManager.getNearestElevation(location);
                }
                else
                {
                    userLocation = location;
                }
            }
        }).start();
    }

    @Override
    public void onConnectionSuspended(int i)
    {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    private void checkPermissionsAndProceed()
    {
        boolean cameraGranted;
        if (PermissionInterpreter.isCameraGranted(this))
        {
            cameraGranted = true;
        }
        else
        {
            cameraGranted = false;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }

        boolean storageGranted;
        if (cameraGranted && PermissionInterpreter.isReadStorageGranted(this))
        {
            storageGranted = true;
        }
        else
        {
            storageGranted = false;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
        }

        if (cameraGranted && storageGranted && PermissionInterpreter.isLocationGranted(this))
        {
            proceedAfterPermissionsGranted();
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (grantResults.length > 0)
        {
            if (PermissionInterpreter.evaluateGrantResults(grantResults))
            {
                checkPermissionsAndProceed();
            }
            else
            {
                showPermissionDeniedAndCloseApp();
            }
        }
    }


    private void loadElevationManager(final int dataSource)
    {
        String rootPath = getFilesDir().getAbsolutePath();

        if (dataProvider != null) dataProvider.cancelDownload();
        dataProvider = new ElevationDataProvider(dataSource, rootPath, this);

        dataProvider.getAsyncManager(dataProvider.new OnDataReadyListener()
        {
            @Override
            public void onDataReady(ElevationManager em)
            {
                elevationManager = em;
                MainActivity.this.dataSource = dataSource;
            }

            @Override
            public void onDataFailed()
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(getString(R.string.extracting_error));
                builder.show();
            }
        });

        // elevationManager = new Elevation49ManagerASC("/sdcard/Download/ARCASCII-parsed.asc"); testovací ASC
    }

    public boolean isTerrainMap()
    {
        return terrainMap;
    }

    public boolean isShowAccuracy()
    {
        return showAccuracy;
    }

    private void showPermissionDeniedAndCloseApp()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.grant_permissions))
                .setTitle(getString(R.string.permissions_not_granted));

        AlertDialog dialog = builder.create();
        dialog.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            public void run()
            {
                finish();
            }
        }, 2000);
    }

    private void showDataChoiceDialog()
    {
        CharSequence dataSources[] = new CharSequence[]{"Google Elevation API", getString(R.string.czechia), /*getString(R.string.taiwan),*/ "EUDEM Hradec Králové"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.choose_data_source));
        builder.setSingleChoiceItems(dataSources, dataSource, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                int dataSourceToLoad = -1;

                switch (which)
                {
                    case (0):
                        dataSourceToLoad = ElevationDataProvider.GOOGLE_ELEVATION_API;
                        break;
                    case (1):
                        dataSourceToLoad = ElevationDataProvider.CZECHIA;
                        break;
                    //case (2): dataSourceToLoad = ElevationDataProvider.TAIWAN; break;
                    case (2):
                        dataSourceToLoad = ElevationDataProvider.HRADEC_KRALOVE;
                        break;
                }

                loadElevationManager(dataSourceToLoad);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showChangeLanguageDialog()
    {
        CharSequence languages[] = new CharSequence[]{getString(R.string.czech), getString(R.string.english)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        int previouslySelected;
        if (LocaleHelper.getLanguage(this).equals("cs"))
        {
            previouslySelected = 0;
        }
        else
        {
            previouslySelected = 1;
        }

        builder.setTitle(getString(R.string.select_language));
        builder.setSingleChoiceItems(languages, previouslySelected, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String language = "en";

                switch (which)
                {
                    case (0):
                        language = "cs";
                        break;
                    case (1):
                        language = "en";
                        break;
                }

                LocaleHelper.setLocale(MainActivity.this, language);
                dialog.dismiss();

                MainActivity.this.recreate();

                MapTab mapTab = (MapTab) pagerAdapter.getItem(1);       //title zůstaval ve stejném jazyku
                if (mapTab != null) mapTab.refreshMarkersAndLine();
            }
        });
        builder.show();
    }

    private void showAboutDialog()
    {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_about);
        dialog.show();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.cancel_download));

        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                if (dataProvider != null) dataProvider.cancelDownload();
            }
        });

        String intentAction = intent.getAction();
        if (intentAction != null && intentAction.equals(DownloadNotificationBuilder.CANCEL_DOWNLOAD))
            showCancelDialog();
    }

    private void showCancelDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.cancel_download));

        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                if (dataProvider != null) dataProvider.cancelDownload();
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

//    private void useFileManager() {
//        new FileChooser(this).setFileListener(new FileChooser.FileSelectedListener() {
//            @Override
//            public void fileSelected(File f) {
//                elevationManager = new ElevationManagerBMP(f.getAbsolutePath()); //naloadovat BMP elevationManagera
//            }
//        }).showDialog();
//    }

}

