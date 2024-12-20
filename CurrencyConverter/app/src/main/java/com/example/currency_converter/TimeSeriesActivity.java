package com.example.currency_converter;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for displaying time series data on a line chart using the MPAndroidChart library.
 * This activity is responsible for fetching time series currency conversion data and plotting it.
 * @author: Aditya Aayush
 */
public class TimeSeriesActivity extends AppCompatActivity {

    private FrameLayout progressOverlay; // Overlay view for indicating data loading

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_series); // Set the layout for the activity
        progressOverlay = findViewById(R.id.progressOverlay); // Find the progress overlay by its ID

        // Extract passed data from the intent
        Bundle extras = getIntent().getExtras();
        String toCurrency = null;
        String fromCurrency = null;
        String toDate = null;
        if (extras != null) {
            fromCurrency = extras.getString("fromCurrency");
            toCurrency = extras.getString("toCurrency");
            toDate = extras.getString("toDate");
        }
        // Show the progress overlay when beginning to fetch data
        progressOverlay.setVisibility(View.VISIBLE);
        // Initiate fetching of time series data
        new FetchTimeSeriesValue(this, this, toCurrency, fromCurrency, toDate);
    }

    /**
     * Creates a graph based on the fetched currency values.
     * Uses MPAndroidChart to plot the data points on a line chart.
     *
     * @param values List of Double values representing currency rates over time.
     */
    public void createGraph(List<Double> values) {
        Log.d("currencyValues", values.toString()); // Debug log for verifying fetched values
        LineChart chart = findViewById(R.id.chart); // Ensure there's a LineChart view in your layout with this ID

        // Prepare data entries from the fetched values
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            // Each value is converted into an Entry object (Entry(x, y))
            entries.add(new Entry(i, values.get(i).floatValue()));
        }

        // Create a dataset from the entries with optional label
        LineDataSet dataSet = new LineDataSet(entries, "Label"); // "Label" here can be replaced with a dynamic label
        dataSet.setColor(Color.BLUE); // Line color
        dataSet.setValueTextColor(Color.BLACK); // Value text color

        // Additional styling options
        dataSet.setDrawCircles(false); // Do not draw circles for each data point
        dataSet.setDrawValues(false); // Do not draw value text next to each data point

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // Refresh the chart to display the data

        // Hide the progress overlay once the graph is ready
        progressOverlay.setVisibility(View.GONE);
    }

}