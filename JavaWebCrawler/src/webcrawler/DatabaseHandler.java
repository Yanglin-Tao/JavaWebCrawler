package webcrawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Handle database operations
 *
 */

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
    
    public static CountryConfiguration getCountryConfigurationFromDatabase(String countryName) {
        String query = "SELECT * FROM Country WHERE country_name = ?";
        try (Connection connection = DatabaseHandler.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, countryName);
            ResultSet resultSet = preparedStatement.executeQuery();
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
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve country configuration from the database.", e);
        }
        return null;
    }

    public static List<String> fetchKeywordsForRelation(String relation) {
        List<String> keywords = new ArrayList<>();
        String query = "SELECT combination_keyword FROM CombinationKeyword " +
                "WHERE relation_id = (SELECT id FROM Relation WHERE relation = ?)";
        try (Connection connection = DatabaseHandler.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, relation);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    keywords.add(resultSet.getString("combination_keyword"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch keywords from the database.", e);
        }
        return keywords;
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
            throw new RuntimeException("Failed to retrieve relation ID from the database.", e);
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
    
    public static Map<String, Map<String, Integer>> fetchStatistics(String countryName) {
        Map<String, Map<String, Integer>> stats = new LinkedHashMap<>(); 
        
        try (Connection connection = connect()) {
            int countryId = getCountryId(connection, countryName);
            if (countryId == -1) {
                System.out.println("Country not found.");
                return stats;
            }
            
            List<Period> periods = Arrays.asList(
                new Period(0, 1),
                new Period(1, 3),
                new Period(3, 6),
                new Period(6, 12),
                new Period(12, -1)
            );

            for (Period period : periods) {
                int startMonths = period.getStartMonths();
                int endMonths = period.getEndMonths();
                
                String periodLabel;
                if (endMonths == -1) {
                    periodLabel = "Over 12 Months";
                } else {
                    periodLabel = startMonths + "-" + endMonths + " months";
                }

                LocalDate startDate;
                LocalDate endDate;

                if (endMonths == -1) {
                    endDate = LocalDate.now().minusMonths(12);
                    startDate = getEarliestDate(connection, countryId);
                } else {
                    endDate = LocalDate.now().minusMonths(startMonths);
                    startDate = LocalDate.now().minusMonths(endMonths);
                }

                System.out.println("Statistics for period: " + periodLabel);
                
                Map<String, Integer> periodStats = getStatsForPeriod(connection, countryId, startDate, endDate);

                stats.put(periodLabel, periodStats);
            }

            System.out.println("Statistics for country: " + countryName);
            stats.forEach((period, stat) -> {
                System.out.println(period + ": " + stat);
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
    
    private static int getCountryId(Connection connection, String countryName) throws SQLException {
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
    
    private static Map<String, Integer> getStatsForPeriod(Connection connection, int countryId, LocalDate startDate, LocalDate endDate) throws SQLException {
        Map<String, Integer> stats = new HashMap<>();

        String query = "SELECT r.id, COUNT(rr.relation_id) as relation_count, rel.relation " +
                       "FROM CrawlerResult r " +
                       "LEFT JOIN ResultRelation rr ON r.id = rr.result_id " +
                       "LEFT JOIN Relation rel ON rr.relation_id = rel.id " +
                       "WHERE r.country_id = ? AND r.updated_date BETWEEN ? AND ? " +
                       "GROUP BY r.id, rel.relation";
        
        if (startDate.isBefore(endDate)) {
	        try (PreparedStatement statement = connection.prepareStatement(query)) {
	            statement.setInt(1, countryId);
	            statement.setDate(2, java.sql.Date.valueOf(startDate));
	            statement.setDate(3, java.sql.Date.valueOf(endDate));
	            ResultSet resultSet = statement.executeQuery();
	            while (resultSet.next()) {
	                String relation = resultSet.getString("relation");
	                int count = resultSet.getInt("relation_count");
	                stats.put(relation, stats.getOrDefault(relation, 0) + count);
	            }
	        }
	
	        System.out.println("Stats for period: " + startDate + " to " + endDate);
	        stats.forEach((relation, count) -> {
	            System.out.println(relation + ": " + count);
	        });
        }

        return stats;
    }
    
    private static LocalDate getEarliestDate(Connection connection, int countryId) throws SQLException {
        String query = "SELECT MIN(updated_date) AS earliest_date FROM CrawlerResult WHERE country_id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, countryId);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                LocalDate earliestDate = resultSet.getDate("earliest_date").toLocalDate();
                System.out.println("Earliest record date: " + earliestDate);
                return earliestDate;
            }
        }
        
        return LocalDate.MIN;
    }
}
