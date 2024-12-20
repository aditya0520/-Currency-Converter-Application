/**
 * Servlet for fetching the latest currency rates.
 * Handles requests for converting between currencies and retrieving available currencies.
 *
 * Author: Aditya Aayush
 */

package ds.webservice;

// Import necessary classes for handling servlets and JSON
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

// Define the servlet and its mapping URL for API endpoint
@WebServlet("/api/latest/*")
public class LatestRatesServlet extends HttpServlet {

    // Define a model variable to interact with the data layer or business logic
    Model model = null;

    // Initialize the servlet and the model object
    public void init() throws ServletException {
        super.init(); // Call the parent's init method
        model = new Model(); // Initialize the model object
    }

    // Handle GET requests to this servlet
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set the response content type to JSON
        response.setContentType("application/json");

        // Retrieve 'from' and 'to' parameters from the request query string
        String fromCurrency = request.getParameter("from");
        String toCurrency = request.getParameter("to");

        // Log the incoming request for tracking or debugging purposes
        model.inductClientRequest(request);

        // Variables to track the type of request and its duration
        String requestType = "";
        long startTime = System.currentTimeMillis();
        JSONObject currencyValue = null;
        JSONArray currencies = null;

        // Check if both 'from' and 'to' currency parameters are provided
        if (fromCurrency != null && toCurrency != null) {
            requestType = "getRate"; // This is a currency conversion rate request
            // Fetch the conversion rate between the specified currencies
            currencyValue = model.fetchCurrencies(fromCurrency, toCurrency);
            // Respond with a 200 OK status and the conversion rate in JSON format
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(currencyValue.toString());
        } else {
            // If one or both currency parameters are missing, return a list of available currencies
            requestType = "getCurrencies"; // This is a request for available currencies
            currencies = model.fetchCurrencies();
            // Respond with a 200 OK status and the list of currencies in JSON format
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(currencies.toString());
        }

        // Calculate the time taken to process the request
        long endTime = System.currentTimeMillis();

        // Log the response details for tracking or auditing purposes
        if (requestType.equals("getRate")) {
            model.inductServiceResponse((endTime - startTime), HttpServletResponse.SC_OK, "getRate", currencyValue, null);
        } else {
            model.inductServiceResponse((endTime - startTime), HttpServletResponse.SC_OK, "getCurrencies", null, currencies);
        }
    }
}
