package com.example.currency_converter;

import android.app.Activity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Fetches the conversion rate between two currencies either for the latest date or for a specific historical date.
 * This class initiates a background task to retrieve the conversion rate without blocking the UI thread.
 * @author : Aditya Aayush
 */
public class FetchConversionValue {

    private MainActivity mainActivity;
    private double result;
    private String to;
    private String from;
    private String date;
    private String conversionType;

    /**
     * Constructor for FetchConversionValue. Initiates the fetching process.
     *
     * @param mainActivity The MainActivity instance to update with the conversion result.
     * @param activity The current activity context.
     * @param to The target currency code.
     * @param from The source currency code.
     * @param conversionType Specifies whether the conversion rate should be the latest or historical.
     * @param date The date for which the historical rate is to be fetched, if applicable.
     */
    public FetchConversionValue(MainActivity mainActivity, Activity activity, String to, String from, String conversionType, String date) {
        this.mainActivity = mainActivity;
        this.date = date;
        this.to = to;
        this.from = from;
        this.conversionType = conversionType;
        new BackgroundTask(activity).execute();
    }

    /**
     * Handles background operations to fetch currency conversion rates.
     */
    private class BackgroundTask {
        private final Activity activity;

        public BackgroundTask(Activity activity) {
            this.activity = activity;
        }

        /**
         * Executes the background task.
         */
        private void execute() {
            startBackground();
        }

        /**
         * Starts a new thread to perform the network request.
         */
        private void startBackground() {
            new Thread(new Runnable() {
                public void run() {
                    doInBackground();
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            onPostExecute();
                        }
                    });
                }
            }).start();
        }

        /**
         * Runs after the background work is completed to update the UI.
         */
        private void onPostExecute() {
            mainActivity.setConvertedAmount(result);
        }

        /**
         * Fetches the conversion rate.
         */
        private void doInBackground() {
            if ("Latest".equals(conversionType)) {
                result = getLatestConversionRate(from, to);
            } else {
                result = getHistoricalConversionRate(from, to, date);
            }
        }

        /**
         * Fetches the latest conversion rate between two currencies.
         *
         * @param from The source currency code.
         * @param to The target currency code.
         * @return The conversion rate as double.
         */
        public double getLatestConversionRate(String from, String to) {
            try {
                String urlString = "https://cuddly-lamp-p594qvpr427xrv-8080.app.github.dev/api/latest?to=" + to + "&from=" + from; // URL should be updated with the actual endpoint
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                if (connection.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    response.append(output);
                }

                connection.disconnect();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONObject rates = jsonResponse.getJSONObject("rates");
                return rates.getDouble(to);
            } catch (Exception e) {
                e.printStackTrace();
                return -1; // Indicates an error
            }
        }

        /**
         * Fetches the historical conversion rate for a given date between two currencies.
         *
         * @param from The source currency code.
         * @param to The target currency code.
         * @param date The date for the historical rate.
         * @return The conversion rate as double.
         */
        public double getHistoricalConversionRate(String from, String to, String date) {
            try {
                String urlString = String.format("https://cuddly-lamp-p594qvpr427xrv-8080.app.github.dev/api/historical?date=%s&to=%s&from=%s",
                        date, to, from); // URL should be updated with the actual endpoint
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                if (connection.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    response.append(output);
                }

                connection.disconnect();

                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.getDouble("rate");
            } catch (Exception e) {
                e.printStackTrace();
                return -1; // Indicate error
            }
        }
    }
}