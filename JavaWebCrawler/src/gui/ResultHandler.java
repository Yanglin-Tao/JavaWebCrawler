package gui;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ResultHandler {
    private static final String DB_URL = "jdbc:postgresql://hostname:port/dbname";
    private static final String USER = "username";
    private static final String PASS = "password";

    public static void main(String[] args) {
        String countryName = "UK"; 
        ResultHandler fetcher = new ResultHandler();
        fetcher.fetchStatistics(countryName);
    }

    public void fetchStatistics(String countryName) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            int countryId = getCountryId(conn, countryName);
            if (countryId == -1) {
                System.out.println("Country not found.");
                return;
            }

            Map<String, Map<String, Integer>> stats = new HashMap<>();
            stats.put("1 Month", getStatsForPeriod(conn, countryId, 1));
            stats.put("3 Months", getStatsForPeriod(conn, countryId, 3));
            stats.put("6 Months", getStatsForPeriod(conn, countryId, 6));
            stats.put("12 Months", getStatsForPeriod(conn, countryId, 12));
            stats.put("Over 12 Months", getStatsForPeriod(conn, countryId, -1));

            System.out.println("Statistics for country: " + countryName);
            stats.forEach((period, stat) -> {
                System.out.println(period + ": " + stat);
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getCountryId(Connection conn, String countryName) throws SQLException {
        String query = "SELECT id FROM Country WHERE country_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, countryName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    private Map<String, Integer> getStatsForPeriod(Connection conn, int countryId, int months) throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = months > 0 ? endDate.minusMonths(months) : LocalDate.MIN;

        String query = "SELECT r.id, COUNT(rr.relation_id) as relation_count, rel.relation " +
                       "FROM CrawlerResult r " +
                       "LEFT JOIN ResultRelation rr ON r.id = rr.result_id " +
                       "LEFT JOIN Relation rel ON rr.relation_id = rel.id " +
                       "WHERE r.country_id = ? AND r.updated_date BETWEEN ? AND ? " +
                       "GROUP BY r.id, rel.relation";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
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
