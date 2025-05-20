import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/game.db"; // Change with your DB URL
    private static final String DB_USER = "root"; // Your DB username
    private static final String DB_PASSWORD = ""; // Your DB password

    // Method to get a connection to the database
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            System.out.println("Error while connecting to database: " + e.getMessage());
            throw e; // Propagate the exception
        }
    }

    // Close the connection safely
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println("Error while closing connection: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Testing DatabaseConnection...");

        try {
            // Test database connection
            Connection connection = getConnection();
            if (connection != null) {
                System.out.println("Database connection successful!");
                closeConnection(connection);
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }
}


