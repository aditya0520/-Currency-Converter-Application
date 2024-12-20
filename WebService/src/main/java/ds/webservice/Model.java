/**
 * Handles data model interactions, including fetching currency rates, logging requests and responses,
 * and aggregating data for reporting. Utilizes MongoDB for storage and retrieval of data.
 * Interacts with external currency rate APIs for current and historical data.
 *
 * Author: Aditya Aayush
 */

package ds.webservice;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Sorts;
import jakarta.servlet.http.HttpServletRequest;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

import static com.mongodb.client.model.Indexes.descending;

public final class Model {

    private MongoDatabase database;

    /**
     * Constructor initializes the MongoDB client and selects the specified database.
     * Utilizes a singleton pattern for the MongoDB client instance to ensure efficient resource usage.
     */
    public Model() {
        // Obtain a singleton MongoDB client instance from MongoConnectionInstance
        MongoClient mongoClient = MongoConnectionInstance.getInstance();
        // Select the "Project4" database for use in this model
        database = mongoClient.getDatabase("Project4");
    }

    /**
     * Asynchronously logs an incoming client HTTP request. Extracts various request parameters
     * and user agent details, then logs these details into a MongoDB collection.
     *
     * @param request The HttpServletRequest object containing details of the incoming request.
     */
    public void inductClientRequest(HttpServletRequest request) {
        // Extract request parameters for logging
        String date = request.getParameter("date");
        String toDate = request.getParameter("toDate");
        String fromCurrency = request.getParameter("from");
        String toCurrency = request.getParameter("to");

        // Extract additional request metadata
        String endPoint = request.getRequestURI();
        String httpMethod = request.getMethod();
        String ipAddress = request.getRemoteAddr();

        // Extract user agent details using UserAgentAnalyzer
        String userAgent = request.getHeader("User-Agent");
        UserAgentAnalyzer userAgentAnalyzer = UserAgentAnalyzer.newBuilder().build();
        UserAgent agent = userAgentAnalyzer.parse(userAgent);
        String deviceName = agent.getValue("DeviceName");
        String operatingSystem = agent.getValue("OperatingSystemNameVersionMajor");

        // Perform logging in a separate thread to not block the main request processing thread
        new Thread(() -> {
            // Log currency conversion pairs if applicable
            if (toCurrency != null && fromCurrency != null) {
                addConversionPair(fromCurrency, toCurrency);
            }
            // Register the client request details in MongoDB
            registerClientRequest(endPoint, httpMethod, fromCurrency, toCurrency, date, toDate, deviceName, operatingSystem, ipAddress);
        }).start();
    }

    /**
     * Logs details of the client request into the MongoDB collection "client_request".
     * This method is called within a separate thread initiated by inductClientRequest.
     *
     * @param endPoint The request endpoint.
     * @param httpMethod The HTTP method used for the request (e.g., GET, POST).
     * @param fromCurrency The source currency code, if applicable.
     * @param toCurrency The target currency code, if applicable.
     * @param date The date parameter from the request, if applicable.
     * @param toDate The end date parameter for time-bound queries, if applicable.
     * @param deviceName The name of the device extracted from the user agent.
     * @param operatingSystem The operating system extracted from the user agent.
     * @param ipAddress The IP address from which the request originated.
     */
    private void registerClientRequest(String endPoint, String httpMethod, String fromCurrency, String toCurrency, String date, String toDate, String deviceName, String operatingSystem, String ipAddress) {
        // Access the "client_request" collection
        MongoCollection<Document> collection = database.getCollection("client_request");

        // Create and populate the document to log
        Document doc = new Document()
                .append("endPoint", endPoint)
                .append("httpMethod", httpMethod)
                .append("deviceName", deviceName)
                .append("operatingSystem", operatingSystem)
                .append("ipAddress", ipAddress)
                .append("requestData", new Document()
                        .append("fromCurrency", fromCurrency)
                        .append("toCurrency", toCurrency)
                        .append("date", date)
                        .append("toDate", toDate));

        // Insert the document into the collection
        collection.insertOne(doc);
    }

    /**
     * Retrieves all client requests logged in the MongoDB collection "client_request".
     *
     * @return A list of Document objects, each representing a logged client request.
     */
    public List<Document> getAllClientRequests() {
        // Access the "client_request" collection
        MongoCollection<Document> collection = database.getCollection("client_request");
        List<Document> clientRequests = new ArrayList<>();

        // Iterate over the collection, extracting and transforming each document
        for (Document doc : collection.find()) {
            Document requestData = (Document) doc.get("requestData");
            Document row = new Document()
                    .append("endPoint", doc.getString("endPoint"))
                    .append("httpMethod", doc.getString("httpMethod"))
                    .append("deviceName", doc.getString("deviceName"))
                    .append("operatingSystem", doc.getString("operatingSystem"))
                    .append("ipAddress", doc.getString("ipAddress"))
                    .append("fromCurrency", requestData != null ? requestData.getString("fromCurrency") : null)
                    .append("toCurrency", requestData != null ? requestData.getString("toCurrency") : null)
                    .append("date", requestData != null ? requestData.getString("date") : null)
                    .append("toDate", requestData != null ? requestData.getString("toDate") : null);

            // Add the transformed document to the results list
            clientRequests.add(row);
        }

        return clientRequests;
    }


    /**
     * Registers server request details in a MongoDB collection synchronously.
     * This method logs various aspects of the server request such as the HTTP method used,
     * the endpoint accessed, timestamps, query parameters including any currency conversion
     * parameters, and the IP address of the requester.
     *
     * @param httpRequest The HTTP method of the request (e.g., GET, POST).
     * @param endPoint The endpoint URI accessed by the request.
     * @param timeStamp The timestamp when the request was received.
     * @param toDate The end date parameter for queries that require a date range, if applicable.
     * @param from The source currency code for currency conversion requests, if applicable.
     * @param to The target currency code for currency conversion requests, if applicable.
     * @param ipAddress The IP address from which the request originated.
     */
    private void registerServerRequest(String httpRequest, String endPoint, String timeStamp, String toDate, String from, String to, String ipAddress) {
        MongoCollection<Document> collection = database.getCollection("server_request");
        Document doc = new Document("timestamp", timeStamp)
                .append("endpoint", endPoint)
                .append("httpMethod", httpRequest)
                .append("queryParameters", new Document()
                        .append("toDate", toDate)
                        .append("from", from)
                        .append("to", to))
                .append("ipAddress", ipAddress);
        collection.insertOne(doc);
    }

    /**
     * Retrieves all server requests logged in the MongoDB collection. This can be used for
     * analytical purposes or to generate reports on server activity.
     *
     * @return A list of Document objects where each Document represents a logged server request.
     */
    public List<Document> getAllServerRequests() {
        MongoCollection<Document> collection = database.getCollection("server_request");
        List<Document> serverRequests = new ArrayList<>();
        collection.find().into(serverRequests);
        return serverRequests;
    }

    /**
     * Logs details about the server's response to a request into MongoDB. This is particularly
     * useful for monitoring server performance, troubleshooting errors, and keeping track of
     * the responses generated by the server, including response time, HTTP status codes, and
     * payload sizes among other data.
     *
     * @param responseTime The time taken by the server to respond, measured in milliseconds.
     * @param statusCode The HTTP status code of the response.
     * @param payloadSize The size of the response payload, in bytes.
     * @param base The base currency for requests involving currency conversion.
     * @param startDate The starting date for requests that involve a range of dates.
     * @param endDate The ending date for date range requests.
     * @param numberOfValues The number of currency values returned in the response, applicable to currency conversion requests.
     * @param averageRate The average currency conversion rate, if applicable.
     * @param toCurrencies A list of target currencies in the response, for conversion requests.
     * @param currencyValues A list of conversion values corresponding to the 'toCurrencies' list.
     */
    private void registerServerResponse(String responseTime, String statusCode, String payloadSize, String base, String startDate, String endDate, String numberOfValues, String averageRate, List<String> toCurrencies, List<String> currencyValues) {
        MongoCollection<Document> collection = database.getCollection("server_response");
        Document responseData = new Document("base", base)
                .append("start_Date", startDate)
                .append("end_Date", endDate)
                .append("NumberOfRateValues", numberOfValues)
                .append("AverageRate", averageRate)
                .append("toCurrencies", toCurrencies)
                .append("toCurrencyValues", currencyValues);
        Document doc = new Document("responseTime", responseTime)
                .append("statusCode", statusCode)
                .append("payloadSize", payloadSize)
                .append("responseData", responseData);
        collection.insertOne(doc);
    }


    /**
     * Retrieves all server responses stored in MongoDB. This method is key for analyzing
     * the server's response patterns, efficiency, and to detect any anomalies or issues
     * in server behavior and performance.
     *
     * @return A List of Document objects, each encapsulating a server response.
     */
    public List<Document> getAllServerResponses() {
        // Access the 'server_response' collection within the database.
        MongoCollection<Document> collection = database.getCollection("server_response");
        List<Document> serverResponses = new ArrayList<>();

        // Iterate over each document (server response) in the collection.
        for (Document doc : collection.find()) {
            // Extract the 'responseData' sub-document, which contains detailed response metrics.
            Document responseData = doc.get("responseData", Document.class);
            if (responseData != null) {
                // Extract lists of 'toCurrencies' and 'currencyValues' from the responseData.
                List<String> toCurrencies = responseData.getList("toCurrencies", String.class);
                List<String> currencyValues = responseData.getList("toCurrencyValues", String.class);

                // Join the lists into comma-separated strings for easier presentation and analysis.
                String toCurrenciesStr = String.join(", ", toCurrencies);
                String currencyValuesStr = String.join(", ", currencyValues);

                // Update the 'responseData' document with the comma-separated strings.
                responseData.append("toCurrencies", toCurrenciesStr)
                        .append("toCurrencyValues", currencyValuesStr);
            }
            // Add the enhanced document to the list of server responses.
            serverResponses.add(doc);
        }

        return serverResponses;
    }

    /**
     * Processes and logs the details of service responses into MongoDB asynchronously.
     * This includes a variety of metrics such as response time, status code, and specifics
     * of the request type (e.g., rate queries, currency list queries).
     *
     * @param responseTime The duration (in milliseconds) it took to process the request and generate a response.
     * @param statusCode The HTTP status code of the response.
     * @param requestType The type of request being processed (e.g., "getRate", "getCurrencies").
     * @param jsonObject The response data in JSON format for single response scenarios.
     * @param jsonArray The response data in JSON format for multiple response scenarios, such as time series data.
     */
    public void inductServiceResponse(long responseTime, int statusCode, String requestType, JSONObject jsonObject, JSONArray jsonArray) {
        // Initialize metrics for analysis.
        int numberOfValues = 0;
        double averageValue = 0;
        List<String> toCurrencies = new ArrayList<>();
        List<String> currencyValues = new ArrayList<>();

        // Process the response based on the request type.
        switch (requestType) {
            case "getRate":
                // For single rate queries, extract and compute the necessary details.
                numberOfValues = 1;
                JSONObject rates = jsonObject.getJSONObject("rates");
                String firstKey = rates.keys().next();
                averageValue = rates.getDouble(firstKey);
                toCurrencies.add(firstKey);
                currencyValues.add(String.valueOf(rates.getDouble(firstKey)));
                break;
            case "getCurrencies":
                // For currency list queries, populate the 'toCurrencies' list.
                for (int i = 0; i < jsonArray.length(); i++) {
                    toCurrencies.add(jsonArray.getString(i));
                }
                // No specific currency values to log for this request type.
                currencyValues = null;
                break;
            case "timeSeries":
                // For time series data, aggregate and compute the average of the rates provided.
                double sum = 0;
                for (int i = 0; i < jsonArray.length(); i++) {
                    double rate = jsonArray.getDouble(i);
                    sum += rate;
                    currencyValues.add(String.valueOf(rate));
                }
                numberOfValues = jsonArray.length();
                averageValue = sum / numberOfValues;
                break;
            case "historical":
                // For historical rate queries, simply log the rate provided.
                numberOfValues = 1;
                averageValue = jsonObject.getDouble("rate");
                currencyValues.add(String.valueOf(averageValue));
                // No specific currencies to log for this request type.
                toCurrencies = null;
                break;
        }

        // Asynchronously log the processed response details into MongoDB for future analysis.
        int finalNumberOfValues = numberOfValues;
        double finalAverageValue = averageValue;
        List<String> finalToCurrencies = toCurrencies;
        List<String> finalCurrencyValues = currencyValues;
        new Thread(() -> {
            registerServiceResponse(responseTime, String.valueOf(statusCode), String.valueOf(finalNumberOfValues),
                    String.valueOf(finalAverageValue), finalToCurrencies, finalCurrencyValues);
        }).start();
    }

    /**
     * Asynchronously logs detailed metrics about a service response into MongoDB.
     * This method captures a comprehensive set of data about each response, including
     * the response time, status code, and detailed currency conversion data when applicable.
     * This is valuable for analytics and auditing of the service's performance over time.
     *
     * @param responseTime The time taken by the server to respond, in milliseconds.
     * @param statusCode The HTTP status code of the response.
     * @param numberOfValues The number of currency conversion rates returned, if applicable.
     * @param averageRate The average rate of currency conversion calculated, if applicable.
     * @param toCurrencies A list of target currencies for the request, useful for currency conversion requests.
     * @param currencyValues A list of currency conversion values corresponding to each target currency, if applicable.
     */
    private void registerServiceResponse(long responseTime, String statusCode, String numberOfValues, String averageRate, List<String> toCurrencies, List<String> currencyValues) {
        MongoCollection<Document> collection = database.getCollection("service_response");
        Document responseData = new Document("NumberOfRateValues", numberOfValues)
                .append("AverageRate", averageRate)
                .append("toCurrencies", toCurrencies)
                .append("toCurrencyValues", currencyValues);
        Document doc = new Document("responseTime", responseTime)
                .append("statusCode", statusCode)
                .append("responseData", responseData);
        collection.insertOne(doc);
    }

    /**
     * Fetches and compiles a list of all service response logs stored in MongoDB.
     * This is essential for performing detailed performance analysis and for conducting
     * audits of the service's historical response data. Each document retrieved represents
     * a unique response log entry, including metrics such as response time, status code, and
     * specific data related to currency conversion responses.
     *
     * @return A List of Document objects, each representing a detailed record of a service response.
     */
    public List<Document> getAllServiceResponses() {
        MongoCollection<Document> collection = database.getCollection("service_response");
        List<Document> serviceResponses = new ArrayList<>();

        for (Document doc : collection.find()) {
            Document responseData = doc.get("responseData", Document.class);
            if (responseData != null) {
                // Prepare and format lists of currencies and conversion values for readability
                List<String> toCurrencies = responseData.getList("toCurrencies", String.class, Collections.emptyList());
                List<String> currencyValues = responseData.getList("toCurrencyValues", String.class, Collections.emptyList());

                String toCurrenciesStr = String.join(", ", toCurrencies);
                String currencyValuesStr = String.join(", ", currencyValues);

                // Append formatted strings back into the responseData Document
                responseData.append("toCurrencies", toCurrenciesStr)
                        .append("toCurrencyValues", currencyValuesStr);
            }
            serviceResponses.add(doc);
        }

        return serviceResponses;
    }


    /**
     * Aggregates data to identify the top 5 most frequently used devices from client requests.
     * This method leverages MongoDB's aggregation pipeline to group, sort, and limit the results
     * to achieve the desired outcome without parameters.
     *
     * @return A list of Strings representing the top 5 devices by request count.
     */
    public List<String> getTop5Devices() {
        MongoCollection<Document> collection = database.getCollection("client_request");
        // Aggregate the data with a pipeline: group by deviceName, count occurrences, sort by count descendingly, and limit to top 5
        List<Document> topDevices = collection.aggregate(
                Arrays.asList(
                        Aggregates.group("$deviceName", Accumulators.sum("count", 1)),
                        Aggregates.sort(Sorts.descending("count")),
                        Aggregates.limit(5)
                )
        ).into(new ArrayList<>());

        // Extract the device names from the aggregation result
        List<String> topDeviceNames = new ArrayList<>();
        for (Document doc : topDevices) {
            topDeviceNames.add(doc.getString("_id")); // "_id" is the grouped field, here it's the deviceName
        }
        return topDeviceNames;
    }

    /**
     * Calculates the average response time of all service responses logged in MongoDB.
     * This is used for performance monitoring by providing insights into the overall speed
     * of the server's response times.
     *
     * @return A formatted string representing the average response time in milliseconds.
     */
    public String getAverageResponseTime() {
        MongoCollection<Document> collection = database.getCollection("service_response");
        // Aggregate to calculate the average response time
        Bson groupStage = Aggregates.group(null, Accumulators.avg("averageResponseTime", "$responseTime"));
        Document result = collection.aggregate(Arrays.asList(groupStage)).first();

        if (result == null) {
            return "No data available";
        } else {
            Double averageResponseTimeMs = result.getDouble("averageResponseTime");
            return String.format(Locale.US, "%.2f ms", averageResponseTimeMs);
        }
    }

    /**
     * Increments the count for a specific currency conversion pair in MongoDB. This helps
     * in tracking the frequency of requests for each currency pair conversion.
     *
     * @param fromCurrency The source currency code.
     * @param toCurrency The target currency code.
     */
    private void addConversionPair(String fromCurrency, String toCurrency) {
        MongoCollection<Document> collection = database.getCollection("conversion_requests");
        Document filter = new Document("fromCurrency", fromCurrency).append("toCurrency", toCurrency);
        Document update = new Document("$inc", new Document("Count", 1));
        collection.updateOne(filter, update, new com.mongodb.client.model.UpdateOptions().upsert(true));
    }

    /**
     * Identifies and returns the currency conversion pair with the highest number of requests.
     * This method sorts the conversion request counts in descending order and retrieves the
     * pair with the highest count, providing insight into the most popular conversion requests.
     *
     * @return A list containing the source and target currency codes of the most requested conversion pair.
     */
    public List<String> fetchHighestCurrencyPair() {
        MongoCollection<Document> collection = database.getCollection("conversion_requests");
        Document highestPair = collection.find().sort(descending("Count")).first();
        if (highestPair != null) {
            return Arrays.asList(highestPair.getString("fromCurrency"), highestPair.getString("toCurrency"));
        }
        return null; // Return null if no conversion pairs are found
    }

    /**
     * Fetches the latest currency rates from an external API and logs the request and response.
     * This method makes a GET request to the Frankfurter API to retrieve current exchange rates
     * for all available currencies against a base currency. The response, including the rates and
     * currency names, is logged asynchronously for performance monitoring and analysis.
     *
     * @return A JSONArray containing the names of the currencies for which rates were fetched.
     * @throws UnknownHostException If the DNS lookup fails for the API's hostname.
     */
    public JSONArray fetchCurrencies() throws UnknownHostException {
        List<String> toCurrencies = new ArrayList<>();
        List<String> currencyValues = new ArrayList<>();
        double currencySum = 0;
        JSONArray currencyNames = new JSONArray();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.frankfurter.app/latest"))
                .build();
        try {
            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.currentTimeMillis();

            String responseBody = response.body();
            JSONObject jsonObject = new JSONObject(responseBody);
            String baseCurrency = jsonObject.getString("base");
            String date = jsonObject.getString("date");

            JSONObject rates = jsonObject.getJSONObject("rates");
            Iterator<String> keys = rates.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                currencyNames.put(key);
                toCurrencies.add(key);
                double rate = rates.getDouble(key);
                currencySum += rate;
                currencyValues.add(String.valueOf(rate));
            }

            double finalCurrencySum = currencySum;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        registerServerRequest("GET", "latest", String.valueOf(startTime), null, null, null, InetAddress.getLocalHost().getHostAddress());
                        registerServerResponse(String.valueOf(endTime - startTime), String.valueOf(response.statusCode()), String.valueOf(responseBody.length()), baseCurrency, null, date, String.valueOf(rates.length()), String.valueOf(finalCurrencySum / rates.length()), toCurrencies, currencyValues);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currencyNames;
    }


    /**
     * Fetches current exchange rates for a specific currency pair from an external API and logs
     * the request/response details. This involves making a GET request to the Frankfurter API,
     * specifying the source and target currencies. The method logs the interaction asynchronously,
     * including metrics such as request duration and the size of the response payload.
     *
     * @param fromCurrency The ISO currency code for the source currency.
     * @param toCurrency The ISO currency code for the target currency.
     * @return A JSONObject containing the fetched currency rate between the specified pair.
     * @throws UnknownHostException If the DNS lookup fails for the API's hostname.
     */
    public JSONObject fetchCurrencies(String fromCurrency, String toCurrency) throws UnknownHostException {
        HttpClient client = HttpClient.newHttpClient();
        JSONObject result = null;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.frankfurter.app/latest?from=" + fromCurrency + "&to=" + toCurrency))
                    .build();
            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.currentTimeMillis();

            JSONObject jsonObject = new JSONObject(response.body());
            String baseCurrency = jsonObject.getString("base");
            JSONObject rates = jsonObject.getJSONObject("rates");
            String firstKey = rates.keys().next();
            double firstRate = rates.getDouble(firstKey);

            result = new JSONObject()
                    .put("base", baseCurrency)
                    .put("rates", rates);

            new Thread(() -> {
                try {
                    registerServerRequest("GET", "latest", String.valueOf(startTime), null, fromCurrency, toCurrency, InetAddress.getLocalHost().getHostAddress());
                    registerServerResponse(String.valueOf(endTime - startTime), String.valueOf(response.statusCode()), String.valueOf(response.body().length()), baseCurrency, null, jsonObject.getString("date"), "1", String.valueOf(firstRate), List.of(firstKey), List.of(String.valueOf(firstRate)));
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Fetches the historical currency exchange rate for a given date and currency pair from an external API.
     * This method also logs the request and response details asynchronously for performance monitoring and analysis.
     *
     * @param date The specific date for which the currency rate is requested.
     * @param fromCurrency The ISO currency code for the source currency.
     * @param toCurrency The ISO currency code for the target currency.
     * @return A JSONObject containing the exchange rate for the specified currency pair on the specified date.
     * @throws UnknownHostException If the API's hostname could not be resolved.
     */
    public JSONObject fetchCurrencyRate(String date, String fromCurrency, String toCurrency) throws UnknownHostException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.frankfurter.app/" + date + "?from=" + fromCurrency + "&to=" + toCurrency))
                .build();
        JSONObject rateObject = new JSONObject();
        try {
            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.currentTimeMillis();

            JSONObject jsonObject = new JSONObject(response.body());
            if (jsonObject.has("rates")) {
                double rate = jsonObject.getJSONObject("rates").getDouble(toCurrency);
                rateObject.put("rate", rate);
            }

            // Logging the request and response details asynchronously
            new Thread(() -> {
                try {
                    registerServerRequest("GET", "historical", String.valueOf(startTime), date, fromCurrency, toCurrency, InetAddress.getLocalHost().getHostAddress());
                    registerServerResponse(String.valueOf(endTime - startTime), String.valueOf(response.statusCode()), String.valueOf(response.body().length()), jsonObject.getString("base"), date, date, "1", String.valueOf(rateObject.getDouble("rate")), List.of(toCurrency), List.of(String.valueOf(rateObject.getDouble("rate"))));
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rateObject;
    }

    /**
     * Fetches a time series of currency exchange rates between two dates for a specified currency pair from an external API.
     * The request and response details are logged asynchronously. This method is useful for analyzing currency value trends over a period.
     *
     * @param fromDate The start date of the period for which currency rates are requested.
     * @param toDate The end date of the period.
     * @param fromCurrency The ISO currency code for the source currency.
     * @param toCurrency The ISO currency code for the target currency.
     * @return A JSONArray containing exchange rates for each day in the specified period.
     * @throws Exception If an error occurs during the request or processing the response.
     */
    public JSONArray fetchCurrencyRate(String fromDate, String toDate, String fromCurrency, String toCurrency) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String url = String.format("https://api.frankfurter.app/%s..%s?from=%s&to=%s", fromDate, toDate, fromCurrency, toCurrency);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        JSONArray resultArray = new JSONArray();
        try {
            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.currentTimeMillis();

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONObject rates = jsonResponse.getJSONObject("rates");
                rates.keys().forEachRemaining(date -> {
                    double rate = rates.getJSONObject(date).getDouble(toCurrency);
                    resultArray.put(new JSONObject().put(date, rate));
                });

                // Asynchronously log the request and response details
                new Thread(() -> {
                    try {
                        registerServerRequest("GET", "historical", String.valueOf(startTime), toDate, fromCurrency, toCurrency, InetAddress.getLocalHost().getHostAddress());
                        registerServerResponse(String.valueOf(endTime - startTime), String.valueOf(response.statusCode()), String.valueOf(response.body().length()), jsonResponse.getString("base"), fromDate, toDate, String.valueOf(rates.length()), "N/A", List.of(toCurrency), null); // Note: Average rate and currency values might be adjusted based on actual needs
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                return resultArray;
            } else {
                throw new Exception("Failed to fetch currency rates: HTTP error code : " + response.statusCode());
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new Exception("Error fetching currency rates", e);
        }
    }
}
