package com.example.currency_converter;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.jakewharton.threetenabp.AndroidThreeTen;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Main Activity for a currency converter application. This activity allows users to select currencies,
 * enter an amount to convert, choose the type of conversion (latest or historical), and view the conversion result.
 * It also features navigation to a TimeSeriesActivity for viewing historical data trends.
 *
 * Author: Aditya Aayush
 */
public class MainActivity extends AppCompatActivity {
    // UI components
    private Spinner spinnerFrom; // Dropdown for selecting the source currency
    private Spinner spinnerTo; // Dropdown for selecting the target currency
    private FrameLayout progressOverlay; // Overlay used to indicate loading processes
    private Button buttonConvert; // Button to trigger the conversion process
    private EditText editTextAmount; // Input field for the amount to be converted
    private RadioGroup radioGroupConversionType; // Group for selecting conversion type (Latest or Historical)
    private TextView convertedAmount; // Display the result of the conversion
    private EditText dateEditText; // Input field for selecting the date for historical conversion
    private ImageButton buttonDatePicker; // Button to trigger the date picker dialog
    private TextView textViewResultLabel; // Label for the conversion result
    private Button timeSeriesButton; // Button to navigate to the time series activity
    public MainActivity mainActivity = this; // Reference to self used in inner classes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the Backport of the Java 8 time API and the edge-to-edge support library
        AndroidThreeTen.init(this);
        EdgeToEdge.enable(this);

        // Set the content view from the XML layout
        setContentView(R.layout.activity_main);

        // Initialize UI components by finding them in the layout
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        editTextAmount = findViewById(R.id.editTextAmount);
        progressOverlay = findViewById(R.id.progressOverlay);
        convertedAmount = findViewById(R.id.textViewConvertedAmount);
        radioGroupConversionType = findViewById(R.id.radioGroupConversionType);
        buttonConvert = findViewById(R.id.buttonConvert);
        dateEditText = findViewById(R.id.editTextDate);
        buttonDatePicker = findViewById(R.id.buttonDatePicker);
        textViewResultLabel = findViewById(R.id.textViewResultLabel);
        timeSeriesButton = findViewById(R.id.buttonShowTimeSeries);

        // Initially show the progress overlay
        progressOverlay.setVisibility(View.VISIBLE);

        // Fetch the list of available currencies to populate the spinner controls
        new FetchCurrencyList(this, this);

        // Listener for changes in the selected conversion type (Latest or Historical)
        radioGroupConversionType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Show or hide the date input fields based on conversion type selection
                if (checkedId == R.id.radioButtonHistorical) {
                    dateEditText.setVisibility(View.VISIBLE);
                    buttonDatePicker.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.radioButtonLatest) {
                    dateEditText.setVisibility(View.GONE);
                    buttonDatePicker.setVisibility(View.GONE);
                }
            }
        });

        // Set a click listener on the date picker button to show a date picker dialog.
        buttonDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtain the current date to use as the default selection in the date picker.
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                // Create and show a DatePickerDialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                // Format the chosen date and set it in the date EditText.
                                String selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day);
                                dateEditText.setText(selectedDate);
                            }
                        }, year, month, dayOfMonth);

                datePickerDialog.show(); // Display the date picker dialog to the user.
            }
        });

// Set a click listener on the convert button to initiate the currency conversion process.
        buttonConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Extract the selected currencies and the amount from the UI elements.
                String fromCurrency = spinnerFrom.getSelectedItem().toString();
                String toCurrency = spinnerTo.getSelectedItem().toString();
                String amountStr = editTextAmount.getText().toString();
                // Determine the selected conversion type (Latest or Historical) based on the radio group selection.
                int selectedId = radioGroupConversionType.getCheckedRadioButtonId();
                RadioButton selectedRadioButton = findViewById(selectedId);
                String conversionTypeText = selectedRadioButton.getText().toString();

                // Log the selected values for debugging purposes.
                Log.d("values are", fromCurrency + toCurrency + amountStr);

                // Proceed with the conversion if the selection is valid and currencies are not the same.
                if (!"Select a Currency".equals(fromCurrency) && !"Select a Currency".equals(toCurrency) && !amountStr.isEmpty() && !fromCurrency.equals(toCurrency)) {
                    Log.d("Conversion Text", conversionTypeText);
                    // Trigger the appropriate conversion process based on the conversion type.
                    if ("Latest".equals(conversionTypeText)) {
                        // Show the progress overlay and initiate the latest conversion rate fetch.
                        progressOverlay.setVisibility(View.VISIBLE);
                        new FetchConversionValue(mainActivity, mainActivity, toCurrency, fromCurrency, "Latest", "");
                    }
                    else if ("Historical".equals(conversionTypeText)) {
                        // Show the progress overlay and initiate the fetch for a historical conversion rate.
                        String date = dateEditText.getText().toString();
                        progressOverlay.setVisibility(View.VISIBLE);
                        new FetchConversionValue(mainActivity, mainActivity, toCurrency, fromCurrency, "Historical", date);
                    }
                }
            }
        });


        timeSeriesButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O) // Indicates this method requires Android Oreo (API level 26) or later.
            @Override
            public void onClick(View view) {
                // Retrieve the date from the dateEditText, if any.
                String pastDate = dateEditText.getText().toString();
                // Get the current date.
                LocalDate today = LocalDate.now();
                // Define a formatter to format the date in a 'yyyy-MM-dd' pattern.
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                // Format the current date.
                String formattedDate = today.format(formatter);
                // Create an intent to navigate to TimeSeriesActivity.
                Intent intent = new Intent(getApplicationContext(), TimeSeriesActivity.class);
                // Put extra data into the intent: fromCurrency, toCurrency, and toDate.
                intent.putExtra("fromCurrency", spinnerFrom.getSelectedItem().toString());
                intent.putExtra("toCurrency", spinnerTo.getSelectedItem().toString());
                intent.putExtra("toDate", pastDate.isEmpty() ? formattedDate : pastDate); // Use the selected date or today's date if none is selected.
                // Start TimeSeriesActivity with the intent.
                startActivity(intent);
            }
        });
    }

    /**
     * Updates the spinner dropdowns with the list of fetched currencies.
     * @param currencies List of currencies fetched from the API.
     */
    public void setSpinnerValues(List<String> currencies) {
        if (currencies != null) {
            // Add a prompt item at the beginning of the list.
            currencies.add(0, "Select a Currency");
            // Create an ArrayAdapter using the string array and a default spinner layout.
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to both spinners.
            spinnerFrom.setAdapter(adapter);
            spinnerTo.setAdapter(adapter);
        }
        // Hide the progress overlay once the currencies are set.
        progressOverlay.setVisibility(View.GONE);
    }

    /**
     * Updates the UI with the converted currency amount.
     * @param conversionFactor The conversion rate received from the API.
     */
    public void setConvertedAmount(double conversionFactor) {
        // Log the conversion factor for debugging.
        Log.d("conversionFactor", String.valueOf(conversionFactor));
        // Parse the amount entered by the user.
        double amount = Double.parseDouble(editTextAmount.getText().toString());
        // Calculate the result of the conversion.
        double result = conversionFactor * amount;
        // Update the UI elements with the result.
        textViewResultLabel.setText(R.string.result); // Set the label text to "Result".
        convertedAmount.setText(String.format("%.2f", result)); // Display the formatted conversion result.
        // Make the time series button visible.
        timeSeriesButton.setVisibility(View.VISIBLE);
        // Hide the progress overlay.
        progressOverlay.setVisibility(View.GONE);
    }
}