package webcrawler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CountryConfiguration {

    private int id;
    private String countryName;
    private String rootUrl;
    private String listItemsSelector;
    private String newsTitleSelector;
    private String newsTeaserSelector;
    private String metadataSelector;
    private String dateFormat;
    private int numberOfThreads;

    public CountryConfiguration() {
    	
    }

    public CountryConfiguration(int id, String countryName, String rootUrl, String listItemsSelector,
                                String newsTitleSelector, String newsTeaserSelector, String metadataSelector,
                                String dateFormat, int numberOfThreads) {
        this.id = id;
        this.countryName = countryName;
        this.rootUrl = rootUrl;
        this.listItemsSelector = listItemsSelector;
        this.newsTitleSelector = newsTitleSelector;
        this.newsTeaserSelector = newsTeaserSelector;
        this.metadataSelector = metadataSelector;
        this.dateFormat = dateFormat;
        this.numberOfThreads = numberOfThreads;
    }

    public int getId() {
        return id;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getRootUrl() {
        return rootUrl;
    }

    public String getListItemsSelector() {
        return listItemsSelector;
    }

    public String getNewsTitleSelector() {
        return newsTitleSelector;
    }

    public String getNewsTeaserSelector() {
        return newsTeaserSelector;
    }

    public String getMetadataSelector() {
        return metadataSelector;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
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

    public List<String> fetchKeywordsForRelation(String relation) {
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


    public int getRelationId(String relation) {
        String query = "SELECT id FROM Relation WHERE relation = ?";
        try (Connection connection = DatabaseHandler.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, relation);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve relation ID from the database.", e);
        }
        return -1;
    }
}