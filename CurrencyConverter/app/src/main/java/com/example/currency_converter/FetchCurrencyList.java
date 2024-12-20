package com.example.currency_converter;

import android.app.Activity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetches a list of available currencies from an external API and updates the UI
 * with this data. This class is responsible for initiating a background task to
 * retrieve the currency list without blocking the UI thread.
 *
 * Author: Aditya Aayush
 */
public class FetchCurrencyList {

    // Reference to the MainActivity to update the UI with fetched currency list.
    MainActivity mainActivity = null;

    // List to hold the fetched currency names.
    List<String> result = null;

    /**
     * Constructor initializes the class and starts the background task to fetch the currency list.
     *
     * @param mainActivity The instance of MainActivity that will use the fetched currency list.
     * @param activity The activity context from which this constructor is called.
     */
    public FetchCurrencyList(MainActivity mainActivity, Activity activity) {
        this.mainActivity = mainActivity;
        new BackgroundTask(activity).execute();
    }

    /**
     * Encapsulates background operations for fetching the currency list.
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
         * Initiates a new Thread to perform the network operations in the background.
         */
        private void startBackground() {
            new Thread(new Runnable() {
                public void run() {
                    doInBackground();
                    // Once the background task is complete, post the result back to the UI thread.
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
         * Updates the MainActivity UI with the fetched currency list.
         */
        private void onPostExecute() {
            mainActivity.setSpinnerValues(result);
        }

        /**
         * Performs the network operation to fetch the currency list in the background.
         */
        private void doInBackground() {
            result = fetchCurrencyList();
        }

        /**
         * Fetches a list of currencies from the specified URL and parses the JSON response.
         *
         * @return A list of currency codes as strings.
         */
        private List<String> fetchCurrencyList() {
            String urlString = "https://cuddly-lamp-p594qvpr427xrv-8080.app.github.dev/api/latest"; // URL should be replaced with the actual API endpoint
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            List<String> currencies = new ArrayList<>();

            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder builder = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                if (builder.length() == 0) {
                    // If the response is empty, return an empty list.
                    return currencies;
                }
                JSONArray jsonArray = new JSONArray(builder.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    currencies.add(jsonArray.getString(i));
                }
                Log.i("Currencies", currencies.toString());
                return currencies;

            } catch (IOException | JSONException e) {
                Log.e("FetchCurrencyList", "Error fetching currency list", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("FetchCurrencyList", "Error closing stream", e);
                    }
                }
            }
            return null;
        }
    }
}