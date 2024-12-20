/**
 * Servlet for fetching historical currency rates.
 * This servlet handles requests to retrieve historical exchange rates for a given date
 * or a time series of exchange rates between two dates.
 *
 * Author: Aditya Aayush
 */

package ds.webservice;

// Importing necessary classes for servlet and JSON handling
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

// Define servlet URL pattern for API endpoint
@WebServlet(urlPatterns = {"/api/historical"})
public class HistoricalRatesServlet extends HttpServlet {
    // Model for data processing and business logic
    Model model = null;

    // Servlet initialization method
    public void init() throws ServletException {
        super.init(); // Call to the superclass init method
        model = new Model(); // Initialize the Model instance
    }

    // Handle GET request for historical data
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Extract query parameters for date and currency conversion
        String date = request.getParameter("date");
        String toDate = request.getParameter("toDate");
        String fromCurrency = request.getParameter("from");
        String toCurrency = request.getParameter("to");

        // Set response content type to JSON
        response.setContentType("application/json");

        try {
            // Log client request
            model.inductClientRequest(request);

            // Variables for request processing
            String requestType = "";
            long startTime = System.currentTimeMillis();
            JSONObject historicalResult = null;
            JSONArray resultTimeSeries = null;

            // Single date historical rate request
            if (date != null && toDate == null) {
                requestType = "historical";
                historicalResult = model.fetchCurrencyRate(date, fromCurrency, toCurrency);
                response.getWriter().write(historicalResult.toString());
            }
            // Date range time series request
            else if (toDate != null && date == null) {
                requestType = "timeSeries";
                resultTimeSeries = model.fetchCurrencyRate("2005-01-31", toDate, fromCurrency, toCurrency);
                response.getWriter().write(resultTimeSeries.toString());
            } else {
                // Response for invalid request format
                response.getWriter().write("Invalid request format.");
            }

            long endTime = System.currentTimeMillis();

            // Log the service response for auditing or tracking
            if (requestType.equals("historical")) {
                model.inductServiceResponse((endTime - startTime), 200, "historical", historicalResult, null);
            }
            else {
                model.inductServiceResponse((endTime - startTime), 200, "timeSeries", null, resultTimeSeries);
            }

        } catch (Exception e) {
            // Handle exceptions and errors
            response.getWriter().write("Error processing request: " + e.getMessage());
        }
    }
}
