package webcrawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    public static List<String> getCountryNames() {
    	List<String> countryNames = new ArrayList<>();

        String query = "SELECT country_name FROM Country";
        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String countryName = resultSet.getString("country_name");
                countryNames.add(countryName);
            }

        } catch (SQLException e) {
            System.err.println("Failed to retrieve country names from the database.");
            e.printStackTrace();
        }

        return countryNames;
    }
    
    public void fetchStatistics(String countryName) {
        try (Connection connection = connect()) {
            int countryId = getCountryId(connection, countryName);
            if (countryId == -1) {
                System.out.println("Country not found.");
                return;
            }

            Map<String, Map<String, Integer>> stats = new HashMap<>();
            stats.put("1 Month", getStatsForPeriod(connection, countryId, 1));
            stats.put("3 Months", getStatsForPeriod(connection, countryId, 3));
            stats.put("6 Months", getStatsForPeriod(connection, countryId, 6));
            stats.put("12 Months", getStatsForPeriod(connection, countryId, 12));
            stats.put("Over 12 Months", getStatsForPeriod(connection, countryId, -1));

            System.out.println("Statistics for country: " + countryName);
            stats.forEach((period, stat) -> {
                System.out.println(period + ": " + stat);
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private int getCountryId(Connection connection, String countryName) throws SQLException {
        String query = "SELECT id FROM Country WHERE country_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, countryName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        }
        return -1;
    }
    
    private Map<String, Integer> getStatsForPeriod(Connection connection, int countryId, int months) throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = months > 0 ? endDate.minusMonths(months) : LocalDate.MIN;

        String query = "SELECT r.id, COUNT(rr.relation_id) as relation_count, rel.relation " +
                       "FROM CrawlerResult r " +
                       "LEFT JOIN ResultRelation rr ON r.id = rr.result_id " +
                       "LEFT JOIN Relation rel ON rr.relation_id = rel.id " +
                       "WHERE r.country_id = ? AND r.updated_date BETWEEN ? AND ? " +
                       "GROUP BY r.id, rel.relation";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, countryId);
            stmt.setDate(2, java.sql.Date.valueOf(startDate));
            stmt.setDate(3, java.sql.Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String relation = rs.getString("relation");
                int count = rs.getInt("relation_count");
                stats.put(relation, stats.getOrDefault(relation, 0) + count);
            }
        }
        return stats;
    }

}
