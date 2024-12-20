/**
 * Servlet responsible for generating the dashboard data.
 * This servlet fetches data for the highest currency pair, top devices, and various request/response metrics,
 * and forwards this data to a JSP page for rendering the dashboard view.
 *
 * Author: Aditya Aayush
 */

package ds.webservice;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

// Define the servlet URL pattern for the root path
@WebServlet("/")
public class DashboardServlet extends HttpServlet{
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Create a new instance of the Model to fetch the necessary data
        Model model = new Model();

        // Fetch the highest currency pair from the Model
        List<String> highestCurrencyPair = model.fetchHighestCurrencyPair();

        // Prepare the text to be displayed. Default to "No data available" if no data is fetched
        String displayText = "No data available";
        if (highestCurrencyPair != null) {
            // Format the display text with the highest currency pair
            displayText = String.format("%s to %s", highestCurrencyPair.get(0), highestCurrencyPair.get(1));
        }

        // Set attributes for the request scope to be accessed in the JSP
        request.setAttribute("mostFrequentConversion", displayText);
        request.setAttribute("topDevices", model.getTop5Devices());
        request.setAttribute("clientRequests", model.getAllClientRequests());
        request.setAttribute("serverRequests", model.getAllServerRequests());
        request.setAttribute("serverResponses", model.getAllServerResponses());
        request.setAttribute("serviceResponses", model.getAllServiceResponses());
        request.setAttribute("averageResponseTime", model.getAverageResponseTime());

        // Forward the request to the dashboard.jsp page for rendering the dashboard view
        request.getRequestDispatcher("dashboard.jsp").forward(request, response);
    }
}
