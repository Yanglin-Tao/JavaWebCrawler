package webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.sql.PreparedStatement;
import java.util.Locale;
import java.sql.ResultSet;
import java.text.ParseException;





public class SeleniumCrawler {

    private final CountryConfiguration countryConfig;
    private final String keyword;
    private final int maxPages;

    private final Set<String> visitedUrls;
    private final Map<String, String> containsKeywordArticles;
    private final Map<String, String> weakAndStrongRelationshipArticles;
    private final Map<String, String> strongRelationshipArticles;
    private final Map<String, String> weakRelationshipArticles;

    private final ExecutorService executorService;
    private final List<String> strongRelationKeywordList;
    private final List<String> weakRelationKeywordList;

    private long startTime;
    private long endTime;
    private long totalTime;

    public SeleniumCrawler(CountryConfiguration countryConfig, String keyword, int numThreads, int maxPages) {
        this.countryConfig = countryConfig;
        this.keyword = keyword;
        this.maxPages = maxPages;
        this.visitedUrls = new HashSet<>();
        this.containsKeywordArticles = new HashMap<>();
        this.weakAndStrongRelationshipArticles = new HashMap<>();
        this.strongRelationshipArticles = new HashMap<>();
        this.weakRelationshipArticles = new HashMap<>();
        this.executorService = Executors.newFixedThreadPool(numThreads);
        this.strongRelationKeywordList = countryConfig.fetchKeywordsForRelation("Strong relationship");
        this.weakRelationKeywordList = countryConfig.fetchKeywordsForRelation("Weak relationship");

        System.setProperty("webdriver.chrome.driver", "C:\\ProgramData\\chocolatey\\bin\\chromedriver.exe");
        // System.setProperty("webdriver.chrome.driver", "/opt/homebrew/bin/chromedriver");
    }

    public void start() {
        startTime = System.currentTimeMillis();
        int pageNum = 1;
        while (pageNum <= maxPages) {
            crawl(countryConfig.getRootUrl() + "?page=" + pageNum);
            pageNum++;
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        endTime = System.currentTimeMillis();

        System.out.println("DONE. Finished parsing " + maxPages + " pages");
        totalTime = endTime - startTime;
        System.out.println("Total execution time: " + totalTime / 1000 + " s");

//        printResults(containsKeywordArticles, "Number of articles containing keyword");
//        System.out.println("---------------------------------------------------------------------------------------");
//
//        printResults(weakAndStrongRelationshipArticles, "Number of articles with weak or strong relationship found");
//        System.out.println("---------------------------------------------------------------------------------------");
//
//        printResults(strongRelationshipArticles, "Number of articles with strong relationship found");
//        System.out.println("---------------------------------------------------------------------------------------");
//
//        printResults(weakRelationshipArticles, "Number of articles with weak relationship found");
//        System.out.println("---------------------------------------------------------------------------------------");
    }

//    private void printResults(Map<String, String> resultMap, String message) {
//        for (Map.Entry<String, String> entry : resultMap.entrySet()) {
//            System.out.println(entry.getKey() + " - Updated on: " + entry.getValue());
//        }
//        System.out.println(message + ": " + resultMap.size());
//    }

    private void crawl(String url) {
        if (visitedUrls.contains(url)) {
            return;
        }
        visitedUrls.add(url);
        executorService.submit(() -> {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            WebDriver driver = new ChromeDriver(options);
            try {
                driver.get(url);
                String htmlContent = driver.getPageSource();
                Document doc = Jsoup.parse(htmlContent);

                Elements newsItems = doc.select(countryConfig.getListItemsSelector());

                for (Element item : newsItems) {
                    synchronized (weakAndStrongRelationshipArticles) {
                        String articleTitle = item.select(countryConfig.getNewsTitleSelector()).text();
                        String articleDate;

                        // Fetch metadata selector dynamically from the database
                        String metadataSelector = countryConfig.getMetadataSelector();

                        // Check the country and select the appropriate date selector
                        if (countryConfig.getCountryName().equals("Italy")) { 
                            articleDate = item.select(metadataSelector).text();
                            
                         // Assuming articleDate is in "15 December 2023" format
                            SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
                            Date parsedDate = inputFormat.parse(articleDate);

                            // Convert the date to "MM-dd-yyyy" format
                            SimpleDateFormat outputFormat = new SimpleDateFormat("MM-dd-yyyy");
                            articleDate  = outputFormat.format(parsedDate);

                            
                        } else if (countryConfig.getCountryName().equals("Portugal")) {
                        	articleDate = item.select(metadataSelector).text();
                        	// Convert the date format
                            SimpleDateFormat inputFormat = new SimpleDateFormat("YYYY-MM-DD 'at' HH'h'mm");
                            Date date = inputFormat.parse(articleDate);

                            SimpleDateFormat outputFormat = new SimpleDateFormat("MM-DD-YYYY");
                            articleDate = outputFormat.format(date);
                            
                      
                        } else if (countryConfig.getCountryName().equals("France")) {
                        	articleDate = item.select(metadataSelector).text();
                        	
                        	if (!articleDate.isEmpty()) {
                                // Parse the date from "Publié 15/12/2023" format
                                SimpleDateFormat inputFormat = new SimpleDateFormat("'Publié' MM/DD/YYYY", Locale.FRENCH);
                                Date date = inputFormat.parse(articleDate);

                                // Format the date as "MM-dd-yyyy"
                                SimpleDateFormat outputFormat = new SimpleDateFormat("MM-DD-YYYY");
                                articleDate = outputFormat.format(date);
                        	}
                      
                        } else if (countryConfig.getCountryName().equals("Belgium") || countryConfig.getCountryName().equals("Germany") || countryConfig.getCountryName().equals("EU") || countryConfig.getCountryName().equals("UK")) {
                            articleDate = item.select(metadataSelector).attr("datetime");
                        } else if (countryConfig.getCountryName().equals("Netherland")) {
                            String metaData = item.select(metadataSelector).text();
                            String[] parts = metaData.split(" \\| ");
                            if (parts.length > 1) {
                                String[] dateParts = parts[1].split("[–-]");
                                if (dateParts.length == 3) {
                                    articleDate = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];
                                } else {
                                    articleDate = "Date cannot be formatted";
                                }
                            } else {
                                articleDate = "Date not found";
                            }
                        } else {
                            // Handle the case for other countries if needed
                            articleDate = "Date handling not implemented for this country";
                        }

                        handleResults(item, articleTitle, articleDate);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                driver.close();
            }
        });
    }

    private boolean newsTitleExists(Connection connection, int countryId, String newsTitle) throws SQLException {
        String query = "SELECT COUNT(*) FROM crawlerresult WHERE country_id = ? AND news_title = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, countryId);
            preparedStatement.setString(2, newsTitle);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }
    


 
    private void handleResults(Element item, String articleTitle, String articleDate) {
        if (countryConfig.getCountryName().equals("Italy") || countryConfig.getCountryName().equals("Portugal")) {
            String updatedArticleTitle = item.select(countryConfig.getNewsTeaserSelector()).text();

            if (articleTitle.toLowerCase().contains(keyword)) {
                containsKeywordArticles.put(updatedArticleTitle, articleDate);
                insertOrUpdateCrawlerResult(updatedArticleTitle, articleDate);
            }

            // Fetch strong and weak relation keywords for the current country
            List<String> strongRelationKeywords = countryConfig.fetchKeywordsForRelation("Strong relationship");
            List<String> weakRelationKeywords = countryConfig.fetchKeywordsForRelation("Weak relationship");

            if (articleTitle.toLowerCase().contains(keyword) && (isStrongRelationship(articleTitle, strongRelationKeywords) || isWeakRelationship(articleTitle, weakRelationKeywords))) {
                weakAndStrongRelationshipArticles.put(updatedArticleTitle, articleDate);
                insertOrUpdateCrawlerResult(updatedArticleTitle, articleDate);
            }

            if (articleTitle.toLowerCase().contains(keyword) && isStrongRelationship(articleTitle, strongRelationKeywords)) {
                strongRelationshipArticles.put(updatedArticleTitle, articleDate);
                insertOrUpdateCrawlerResult(updatedArticleTitle, articleDate);
            }

            if (articleTitle.toLowerCase().contains(keyword) && isWeakRelationship(articleTitle, weakRelationKeywords)) {
                weakRelationshipArticles.put(updatedArticleTitle, articleDate);
                insertOrUpdateCrawlerResult(updatedArticleTitle, articleDate);
            }
        } else {
            if (articleTitle.toLowerCase().contains(keyword)) {
                containsKeywordArticles.put(articleTitle, articleDate);
                insertOrUpdateCrawlerResult(articleTitle, articleDate);

            }

            // Fetch strong and weak relation keywords for the current country
            List<String> strongRelationKeywords = countryConfig.fetchKeywordsForRelation("Strong relationship");
            List<String> weakRelationKeywords = countryConfig.fetchKeywordsForRelation("Weak relationship");

            if (articleTitle.toLowerCase().contains(keyword) && (isStrongRelationship(articleTitle, strongRelationKeywords) || isWeakRelationship(articleTitle, weakRelationKeywords))) {
                weakAndStrongRelationshipArticles.put(articleTitle, articleDate);
                insertOrUpdateCrawlerResult(articleTitle, articleDate);

            }

            if (articleTitle.toLowerCase().contains(keyword) && isStrongRelationship(articleTitle, strongRelationKeywords)) {
                strongRelationshipArticles.put(articleTitle, articleDate);
                insertOrUpdateCrawlerResult(articleTitle, articleDate);

            }

            if (articleTitle.toLowerCase().contains(keyword) && isWeakRelationship(articleTitle, weakRelationKeywords)) {
                weakRelationshipArticles.put(articleTitle, articleDate);
                insertOrUpdateCrawlerResult(articleTitle, articleDate);

            }
        	}
        }

    private void insertOrUpdateCrawlerResult(String newsTitle, String articleDate) {
        try (Connection connection = DatabaseHandler.connect()) {
            // Assuming you have a method getId() in CountryConfiguration
            int countryId = countryConfig.getId();

            // Manually incrementing the id value
            int nextId = 0;

            // Query the maximum id value from the table and increment it
            String getMaxIdQuery = "SELECT MAX(id) FROM CrawlerResult";
            try (PreparedStatement preparedStatement = connection.prepareStatement(getMaxIdQuery);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    nextId = resultSet.getInt(1) + 1;
                }
            }

            // Check if the news_title already exists
            if (newsTitleExists(connection, countryId, newsTitle)) {
                // Update query
                String updateQuery = "UPDATE crawlerresult SET updated_date = ? WHERE country_id = ? AND news_title = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                    // Parse the articleDate and set it as a Date parameter
                    preparedStatement.setDate(1, java.sql.Date.valueOf(articleDate));
                    preparedStatement.setInt(2, countryId);
                    preparedStatement.setString(3, newsTitle);

                    // Execute the update query
                    preparedStatement.executeUpdate();
                }
            } else {
                // Insert query with specifying the id column
                String insertQuery = "INSERT INTO crawlerresult (id, country_id, news_title, updated_date) VALUES (?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    preparedStatement.setInt(1, nextId);
                    preparedStatement.setInt(2, countryId);
                    preparedStatement.setString(3, newsTitle);

                    // Parse the articleDate and set it as a Date parameter
                    preparedStatement.setDate(4, java.sql.Date.valueOf(articleDate));

                    // Execute the insert query
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            // Log the exception or perform custom exception handling
            System.err.println("An error occurred while processing the database: " + e.getMessage());
        }
    }
    private Boolean isStrongRelationship(String titleText, List<String> strongRelationKeywords) {
        for (String keyword : strongRelationKeywords) {
            if (titleText.toLowerCase().contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private Boolean isWeakRelationship(String titleText, List<String> weakRelationKeywords) {
        for (String keyword : weakRelationKeywords) {
            if (titleText.toLowerCase().contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }


    
    public Map<String, String> getContainsKeywordArticles() {
        return containsKeywordArticles;
    }

    public Map<String, String> getWeakAndStrongRelationshipArticles() {
        return weakAndStrongRelationshipArticles;
    }

    public Map<String, String> getStrongRelationshipArticles() {
        return strongRelationshipArticles;
    }

    public Map<String, String> getWeakRelationshipArticles() {
        return weakRelationshipArticles;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public int getMaxPages() {
        return maxPages;
    }
    public static void main(String[] args) {
        // Retrieve configurations from the database for each country
        CountryConfiguration countryConfig = CountryConfiguration.getCountryConfigurationFromDatabase("Germany");

        // Fetch the number of threads from the country configuration
        int numberOfThreads = countryConfig.getNumberOfThreads();

        // Create the SeleniumCrawler with the retrieved configurations
        SeleniumCrawler crawler = new SeleniumCrawler(countryConfig, "climate", numberOfThreads, 50);
        crawler.start();
    }

}
