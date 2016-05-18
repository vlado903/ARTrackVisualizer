package bp.uhk.arapp.view.tabs;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;

import bp.uhk.arapp.R;
import bp.uhk.arapp.ele.ElevationManager;
import bp.uhk.arapp.geo.GeoTools;
import bp.uhk.arapp.view.MainActivity;

public class HomeTab extends Fragment {

    private View view;

    private MainActivity mainActivity;

    private Button btRefreshOverview;
    private TextView tvLatitude;
    private TextView tvLongitude;
    private TextView tvElevation;
    private TextView tvAccuracy;
    private TextView tvNumOfPoints;
    private TextView tvDistance;
    private LineChart chart;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.tab_home, container, false);

        mainActivity = (MainActivity) getActivity();
        initViews();

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser){
            if(mainActivity != null) refreshOverview();
        }
    }


    private void initViews() {
        btRefreshOverview = (Button) view.findViewById(R.id.button_refresh_overview);
        btRefreshOverview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshOverview();
            }
        });

        tvLatitude = (TextView) view.findViewById(R.id.tv_latitude);
        tvLongitude = (TextView) view.findViewById(R.id.tv_longitude);
        tvElevation = (TextView) view.findViewById(R.id.tv_elevation);
        tvAccuracy = (TextView) view.findViewById(R.id.tv_accuracy);
        tvNumOfPoints = (TextView) view.findViewById(R.id.tv_number_of_points);
        tvDistance = (TextView) view.findViewById(R.id.tv_distance);

        chart = (LineChart) view.findViewById(R.id.chart);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(true);

        chart.setDescription("");    // Hide the description
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setEnabled(false);

        chart.getLegend().setEnabled(false);   // Hide the legend

        refreshChart();
    }

    public void refreshOverview(){
        final Location[] currentLocation = {mainActivity.getCurrentLocation()};
        final ElevationManager em = mainActivity.getElevationManager();

        if(currentLocation[0] != null){

            tvLatitude.setText(Double.toString(currentLocation[0].getLatitude()));
            tvLongitude.setText(Double.toString(currentLocation[0].getLongitude()));

            if (em != null){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        currentLocation[0] = em.getNearestElevation(currentLocation[0]);

                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvElevation.setText(String.format("%.1f m", currentLocation[0].getAltitude()));
                                tvAccuracy.setText(String.format("%.1f m", currentLocation[0].getAccuracy()));
                            }
                        });
                    }
                }).start();
            }
        }

        tvDistance.setText(String.format("%.0f m", GeoTools.getTotalDistance(mainActivity.getPoints())*1000)); //převod na metry
        tvNumOfPoints.setText(String.valueOf(mainActivity.getPoints().size()));

        refreshChart();
    }

    private void refreshChart() {
        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<Entry> yVals = new ArrayList<>();

        for (int i = 0; i < MainActivity.points.size(); i++){
            xVals.add(i + "");
            yVals.add(new Entry((float) MainActivity.points.get(i).getAltitude(), i));
        }

        LineDataSet yValsSet = new LineDataSet(yVals, "Points");
        yValsSet.setLineWidth(3f);
        yValsSet.setCircleRadius(6f);
        yValsSet.setValueTextSize(10);
        yValsSet.setValueTextColor(-1);

        yValsSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return Math.round(value) + "";
            }
        });

        yValsSet.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        yValsSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.colorAccent));

        chart.setData(new LineData(xVals, yValsSet));
        chart.invalidate();
    }
}
