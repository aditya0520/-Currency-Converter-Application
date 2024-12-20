# Currency Converter Application

This project is a comprehensive solution for real-time and historical currency conversion, consisting of two main components: a **Native Android Application** for user interaction and a **Scalable Web Service** for data processing and storage.

---

## Features

### 1. **Native Android Application**

- **Name:** `Currency_Converter`
  
- **Functionalities:**
  - Convert amounts between currencies using live and historical exchange rates.
  - Access time-series data for exchange rates.
  - Reusable app design without needing restarts.
    
- **Technologies Used:**
  - Android Studio with 11 views including `ConstraintLayout`, `Spinner`, `EditText`, `RadioGroup`, and more.
  - MPAndroidChart library for graphical representation of time-series data.
    
- **Sample API Usage:**
  - **Latest Rate Conversion:**  
    URL: `https://cuddly-lamp-p594qvpr427xrv-8080.app.github.dev/api/latest?to={TO}&from={FROM}`
    ```json
    {
      "rates": {
        "INR": 83.4
      },
      "base": "USD"
    }
    ```
  - **Historical Rate Conversion:**  
    URL: `https://cuddly-lamp-p594qvpr427xrv-8080.app.github.dev/api/historical?date={DATE}&to={TO}&from={FROM}`
    ```json
    {
      "rate": 62.182
    }
    ```

---

### 2. **Web Service**
- **Deployed on:** Codespace
  
- **Components:**
  - **DashboardServlet:** Fetches data from MongoDB to populate dashboards.
  - **HistoricalRatesServlet:** Handles GET requests for historical conversion rates.
  - **LatestRatesServlet:** Handles GET requests for the latest conversion rates.
    
- **Business Logic:**
  - Communicates between clients and servers.
  - Logs data entries to MongoDB for analysis.
    
- **MongoDB Connection:**
  - Connection string: `mongodb+srv://[username]:[password]@cluster0.mongodb.net/`
  - Hosted on a cluster with three shards for high availability.

---

## Logging and Analytics
The application logs user and system data to MongoDB for:
- **Performance Monitoring:** Tracks response time, status codes, and payload sizes.
- **User Analytics:** Monitors popular currency pairs, top devices, and frequent conversion requests.
- **Geographic Analysis:** Logs IP addresses for location-based insights.

---

## Project Structure

### `WebService`
- **Key Files:**
  - `DashboardServlet.java`: Backend logic for dashboards.
  - `HistoricalRatesServlet.java`: Handles historical rate requests.
  - `LatestRatesServlet.java`: Handles latest rate requests.
  - `Model.java`: Business logic and MongoDB interactions.

### `CurrencyConverter`
- **Key Features:**
  - Android app with an intuitive UI for currency conversion.
  - Real-time and historical data integration.
  - Graphical representation of trends using MPAndroidChart.

---

## Highlights
- **Scalability:**  
  Integrated **Java Message Service (JMS)** for efficient load management.
- **Data Insights:**  
  MongoDB stores and analyzes user behavior to optimize services.
- **Stability:**  
  Backend hosted on **Apache** ensures high availability and robustness.

---

## Screenshots

### Android Application
- **Conversion View:**  
  Displays real-time currency conversion.
    
  ![Displays real-time currency conversion.](Screenshots/ConversionView.jpg)

- **Time-Series View:**  
  Graphical representation of currency trends.
    
  ![Graphical representation of currency trends.](Screenshots/TImeSeriesView.jpg)
---

## Future Enhancements
- Improve the efficiency of historical data fetching.
- Implement AI-driven insights for predictive currency trends.

---

