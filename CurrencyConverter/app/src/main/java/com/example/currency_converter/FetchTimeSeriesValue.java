package com.example.currency_converter;

import android.app.Activity;
import android.util.Log;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;


/**
 * Fetches time series currency values from an external API and updates the UI
 * with the fetched data by plotting it on a graph. This class initiates a background
 * task to retrieve the data without blocking the UI thread.
 *
 * Author: Aditya Aayush
 */
public class FetchTimeSeriesValue {

    private TimeSeriesActivity timeSeriesActivity;
    private String to;
    private String from;
    private String date;
    private List<Double> result;

    /**
     * Constructor initializes the class with the necessary parameters for the API call and
     * starts the background task to fetch the currency rates.
     *
     * @param mainActivity The context of the TimeSeriesActivity where the graph will be created.
     * @param activity The activity from which this constructor is called, used to run tasks on the UI thread.
     * @param to The target currency code.
     * @param from The base currency code.
     * @param date The specific date for fetching historical data.
     */
    public FetchTimeSeriesValue(TimeSeriesActivity mainActivity, Activity activity, String to, String from, String date) {
        this.timeSeriesActivity = mainActivity;
        this.date = date;
        this.to = to;
        this.from = from;
        new BackgroundTask(activity).execute();
    }

    /**
     * Encapsulates background operations for fetching currency rates without blocking the UI thread.
     */
    private class BackgroundTask {
        private final Activity activity;

        public BackgroundTask(Activity activity) {
            this.activity = activity;
        }

        /**
         * Starts the execution of the background task.
         */
        private void execute() {
            startBackground();
        }

        /**
         * Initiates a new Thread to perform network operations in the background.
         */
        private void startBackground() {
            new Thread(new Runnable() {
                public void run() {
                    doInBackground();
                    // Once background task is done, execute the UI update on the main thread.
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            onPostExecute();
                        }
                    });
                }
            }).start();
        }

        /**
         * Invoked on the UI thread after the background computation finishes.
         * Calls the TimeSeriesActivity method to update the UI with the result.
         */
        private void onPostExecute() {
            timeSeriesActivity.createGraph(result);
        }

        /**
         * Performs the network operation to fetch currency rates in the background.
         */
        private void doInBackground() {
            result = fetchCurrencyRates(to, from, date);
        }

        /**
         * Fetches currency rates from the specified URL and parses the JSON response.
         *
         * @param toCurrency The target currency code.
         * @param fromCurrency The base currency code.
         * @param toDate The date for which to fetch historical rates.
         * @return A list of currency rates as doubles.
         */
        public List<Double> fetchCurrencyRates(String toCurrency, String fromCurrency, String toDate) {
            List<Double> rates = new ArrayList<>();
            try {
                // Construct the URL for the API request.
                String urlString = String.format("https://cuddly-lamp-p594qvpr427xrv-8080.app.github.dev/api/historical?toDate=%s&to=%s&from=%s",
                        toDate, toCurrency, fromCurrency);
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                }

                // Read the response.
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    response.append(output);
                }
                conn.disconnect();

                // Parse the response and extract rates.
                JSONArray ratesArray = new JSONArray(response.toString());
                for (int i = 0; i < ratesArray.length(); i++) {
                    try {
                        rates.add(Double.parseDouble(ratesArray.getString(i)));
                    } catch (NumberFormatException e) {
                        Log.e("Parsing Error", "Error parsing rate as double: " + ratesArray.getString(i));
                    }
                }

            } catch (IOException | JSONException e) {
                Log.e("Network Error", "Error fetching currency rates", e);
            }
            return rates;
        }
    }
}