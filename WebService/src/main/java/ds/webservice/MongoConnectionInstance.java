package ds.webservice;

// MongoDB client packages for connection and settings.
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import java.util.concurrent.TimeUnit;

/**
 * Singleton class for managing MongoDB connections efficiently within the application.
 * Ensures that only one MongoClient instance is active at any time, providing a shared
 * connection pool to be used across different parts of the application.
 *
 * Author: Aditya Aayush
 */
public class MongoConnectionInstance {

    // Singleton instance of MongoConnectionInstance for application-wide use.
    private static MongoConnectionInstance mongoConnectionInstance = null;
    // MongoClient instance, shared across the application.
    private static MongoClient mongoClient = null;

    /**
     * Private constructor to prevent external instantiation.
     * Initializes MongoClient with a connection string and tailored settings,
     * including timeouts for connections and socket read operations.
     */
    private MongoConnectionInstance() {
        // Connection string for MongoDB, including credentials and cluster address.
        String connectionString = "mongodb+srv://aaayush:QH3OrDlWx008btyY@cluster0.z9pcbas.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

        // Configuration settings for the MongoClient, including connection and read timeouts.
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .applyToSocketSettings(builder ->
                        builder.connectTimeout(30, TimeUnit.SECONDS) // Connection timeout set to 30 seconds.
                                .readTimeout(30, TimeUnit.SECONDS)) // Read timeout set to 30 seconds.
                .build();

        // Creation of the MongoClient with the specified settings.
        mongoClient = MongoClients.create(settings);
    }

    /**
     * Thread-safe method to obtain the single instance of MongoClient for the application.
     * If no instance exists, initializes the MongoClient and MongoConnectionInstance.
     *
     * @return The shared MongoClient instance for database operations.
     */
    public static synchronized MongoClient getInstance() {
        if (mongoConnectionInstance == null) {
            mongoConnectionInstance = new MongoConnectionInstance();
        }
        return mongoClient;
    }
}
