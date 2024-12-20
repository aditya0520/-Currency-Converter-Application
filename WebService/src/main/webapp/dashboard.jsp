<%@ page import="org.bson.Document" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Dashboard</title>
    <style>
        table, th, td {
            border: 1px solid black;
            border-collapse: collapse;
        }
        th, td {
            padding: 10px;
        }
        th {
            text-align: left;
        }
    </style>
</head>
<body>
<h2>Average Response Time</h2>
<p><%= request.getAttribute("averageResponseTime") %></p>

<h2>Most Frequent Conversion</h2>
<p><%= request.getAttribute("mostFrequentConversion") %></p>

<h2>Top 5 Devices</h2>
<ul>
    <% List<String> topDevices = (List<String>) request.getAttribute("topDevices");
        if (topDevices != null && !topDevices.isEmpty()) {
            for (String deviceName : topDevices) {
    %>
    <li><%= deviceName %></li>
    <%
        }
    } else {
    %>
    <li>No data available.</li>
    <%
        }
    %>
</ul>

<h2>Client Request Data</h2>

<%
    List<Document> clientRequests = (List<Document>) request.getAttribute("clientRequests");
    if (clientRequests != null && !clientRequests.isEmpty()) {
%>
<table>
    <tr>
        <th>EndPoint</th>
        <th>HTTP Method</th>
        <th>Device Name</th>
        <th>Operating System</th>
        <th>IP Address</th>
        <th>From Currency</th>
        <th>To Currency</th>
        <th>Date</th>
        <th>To Date</th>
    </tr>
    <%
        for (Document clientRequest : clientRequests) {
    %>
    <tr>
        <td><%= clientRequest.getString("endPoint") %></td>
        <td><%= clientRequest.getString("httpMethod") %></td>
        <td><%= clientRequest.getString("deviceName") %></td>
        <td><%= clientRequest.getString("operatingSystem") %></td>
        <td><%= clientRequest.getString("ipAddress") %></td>
        <td><%= clientRequest.getString("fromCurrency") != null ? clientRequest.getString("fromCurrency") : "N/A" %></td>
        <td><%= clientRequest.getString("toCurrency") != null ? clientRequest.getString("toCurrency") : "N/A" %></td>
        <td><%= clientRequest.getString("date") != null ? clientRequest.getString("date") : "N/A" %></td>
        <td><%= clientRequest.getString("toDate") != null ? clientRequest.getString("toDate") : "N/A" %></td>
    </tr>
    <%
        }
    %>
</table>
<%
} else {
%>
<p>No client requests found.</p>
<%
    }
%>

<!-- Server Request Data Table Begins Here -->
<h2>Server Request Data</h2>

<%
    List<Document> serverRequests = (List<Document>) request.getAttribute("serverRequests");
    if (serverRequests != null && !serverRequests.isEmpty()) {
%>
<table>
    <tr>
        <th>Timestamp</th>
        <th>Endpoint</th>
        <th>HTTP Method</th>
        <th>To Date</th>
        <th>From</th>
        <th>To</th>
        <th>IP Address</th>
    </tr>
    <%
        for (Document requestDoc : serverRequests) {
            Document queryParameters = (Document) requestDoc.get("queryParameters");
    %>
    <tr>
        <td><%= requestDoc.getString("timestamp") %></td>
        <td><%= requestDoc.getString("endpoint") %></td>
        <td><%= requestDoc.getString("httpMethod") %></td>
        <td><%= queryParameters.getString("toDate") %></td>
        <td><%= queryParameters.getString("from") %></td>
        <td><%= queryParameters.getString("to") %></td>
        <td><%= requestDoc.getString("ipAddress") %></td>
    </tr>
    <%
        }
    %>
</table>
<%
} else {
%>
<p>No server requests found.</p>
<%
    }
%>

<h2>Server Response Data</h2>

<%
    List<Document> serverResponses = (List<Document>) request.getAttribute("serverResponses");
    if (serverResponses != null && !serverResponses.isEmpty()) {
%>
<table>
    <tr>
        <th>Response Time (ms)</th>
        <th>Status Code</th>
        <th>Payload Size</th>
        <th>Base</th>
        <th>Start Date</th>
        <th>End Date</th>
        <th>Number Of Values</th>
        <th>Average Rate</th>
        <th>To Currencies</th>
        <th>Currency Values</th>
    </tr>
    <%
        for (Document serverResponse : serverResponses) {
            Document responseData = (Document) serverResponse.get("responseData");
    %>
    <tr>
        <td><%= serverResponse.getString("responseTime") %></td>
        <td><%= serverResponse.getString("statusCode") %></td>
        <td><%= serverResponse.getString("payloadSize") %></td>
        <td><%= responseData.getString("base") %></td>
        <td><%= responseData.getString("start_Date") %></td>
        <td><%= responseData.getString("end_Date") %></td>
        <td><%= responseData.getString("NumberOfRateValues") %></td>
        <td><%= responseData.getString("AverageRate") %></td>
        <td><%= responseData.getString("toCurrencies") %></td>
        <td><%= responseData.getString("toCurrencyValues") %></td>
    </tr>
    <%
        }
    %>
</table>
<%
} else {
%>
<p>No server responses found.</p>
<%
    }
%>

<h2>Service Response Data</h2>

<%
    List<Document> serviceResponses = (List<Document>) request.getAttribute("serviceResponses");
    if (serviceResponses != null && !serviceResponses.isEmpty()) {
%>
<table>
    <tr>
        <th>Response Time (ms)</th>
        <th>Status Code</th>
        <th>Number Of Values</th>
        <th>Average Rate</th>
        <th>To Currencies</th>
        <th>Currency Values</th>
    </tr>
    <%
        for (Document serviceResponse : serviceResponses) {
            Document responseData = (Document) serviceResponse.get("responseData");
    %>
    <tr>
        <td><%= serviceResponse.getLong("responseTime").toString() %></td>
        <td><%= serviceResponse.getString("statusCode") %></td>
        <td><%= responseData.getString("NumberOfRateValues") %></td>
        <td><%= responseData.getString("AverageRate") %></td>
        <td><%= responseData.getString("toCurrencies") %></td>
        <td><%= responseData.getString("toCurrencyValues") %></td>
    </tr>
    <%
        }
    %>
</table>
<%
} else {
%>
<p>No service responses found.</p>
<%
    }
%>


</body>
</html>
