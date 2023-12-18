package webcrawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseHandler {

    private static final String URL = "jdbc:postgresql://localhost:5432/webCrawler_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "root";

    public static Connection connect() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Failed to load PostgreSQL JDBC driver.", e);
        }
    }

    public static void close(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Failed to close the database connection.");
            e.printStackTrace();
        }
    }

    public static CountryConfiguration getCountryConfiguration(String countryName) {
        String query = "SELECT * FROM Country WHERE country_name = ?";
        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, countryName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new CountryConfiguration(
                            resultSet.getInt("id"),
                            resultSet.getString("country_name"),
                            resultSet.getString("root_URL"),
                            resultSet.getString("list_items_selector"),
                            resultSet.getString("news_title_selector"),
                            resultSet.getString("news_teaser_selector"),
                            resultSet.getString("metadata_selector"),
                            resultSet.getString("date_format"),
                            resultSet.getInt("number_of_threads")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve country configuration from the database.");
            e.printStackTrace();
        }
        return null;
    }
    
    public static int getRelationId(String relation) {
    	String query = "SELECT id FROM Relation WHERE relation = ?";
    	int relationId = -1; 

        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setString(1, relation);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    relationId = resultSet.getInt("id");
                } else {
                    System.err.println("No relation found for: " + relation);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve relation id from the database.");
            e.printStackTrace();
        }
        return relationId;
    }
    
    public static int getCrawlerResultId(Connection connection, int countryId, String newsTitle) {
        String query = "SELECT id FROM CrawlerResult WHERE country_id = ? AND news_title = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, countryId);
            preparedStatement.setString(2, newsTitle);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve result id from the database.");
            e.printStackTrace();
        }
        return -1; 
    }

}
