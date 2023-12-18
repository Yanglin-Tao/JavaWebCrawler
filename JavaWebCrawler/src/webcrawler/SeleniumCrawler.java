package webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

        // System.setProperty("webdriver.chrome.driver", "C:\\ProgramData\\chocolatey\\bin\\chromedriver.exe");
        System.setProperty("webdriver.chrome.driver", "/opt/homebrew/bin/chromedriver");
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
        
        for (Map.Entry<String, String> entry : containsKeywordArticles.entrySet()) {
        	if (strongRelationshipArticles.containsKey(entry.getKey())) {
        		insertOrUpdateCrawlerResult(entry.getKey(), entry.getValue(), "Strong relationship");
        	} 
        	if (weakRelationshipArticles.containsKey(entry.getKey())) {
        		insertOrUpdateCrawlerResult(entry.getKey(), entry.getValue(), "Weak relationship");
        	} 
        	insertOrUpdateCrawlerResult(entry.getKey(), entry.getValue(), "Contains keyword");
        }

        System.out.println("DONE. Finished parsing " + maxPages + " pages");
        totalTime = endTime - startTime;
        System.out.println("Total execution time: " + totalTime / 1000 + " s");

        printResults(containsKeywordArticles, "Number of articles containing keyword");
        System.out.println("---------------------------------------------------------------------------------------");

        printResults(weakAndStrongRelationshipArticles, "Number of articles with weak or strong relationship found");
        System.out.println("---------------------------------------------------------------------------------------");

        printResults(strongRelationshipArticles, "Number of articles with strong relationship found");
        System.out.println("---------------------------------------------------------------------------------------");

        printResults(weakRelationshipArticles, "Number of articles with weak relationship found");
        System.out.println("---------------------------------------------------------------------------------------");
    }

    private void printResults(Map<String, String> resultMap, String message) {
        for (Map.Entry<String, String> entry : resultMap.entrySet()) {
            System.out.println(entry.getKey() + " - Updated on: " + entry.getValue());
        }
        System.out.println(message + ": " + resultMap.size());
    }

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
                        DateFormatter dateFormatter = new DateFormatter(countryConfig, item);
                        String articleDate = dateFormatter.formatDate();
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
            }
            if (articleTitle.toLowerCase().contains(keyword) && (isStrongRelationship(articleTitle) || isWeakRelationship(articleTitle))) {
                weakAndStrongRelationshipArticles.put(updatedArticleTitle, articleDate);
            }

            if (articleTitle.toLowerCase().contains(keyword) && isStrongRelationship(articleTitle)) {
                strongRelationshipArticles.put(updatedArticleTitle, articleDate);
            }

            if (articleTitle.toLowerCase().contains(keyword) && isWeakRelationship(articleTitle)) {
                weakRelationshipArticles.put(updatedArticleTitle, articleDate);
            }
        } else {
            if (articleTitle.toLowerCase().contains(keyword)) {
                containsKeywordArticles.put(articleTitle, articleDate);
            }

            if (articleTitle.toLowerCase().contains(keyword) && (isStrongRelationship(articleTitle) || isWeakRelationship(articleTitle))) {
                weakAndStrongRelationshipArticles.put(articleTitle, articleDate);

            }

            if (articleTitle.toLowerCase().contains(keyword) && isStrongRelationship(articleTitle)) {
                strongRelationshipArticles.put(articleTitle, articleDate);
            }

            if (articleTitle.toLowerCase().contains(keyword) && isWeakRelationship(articleTitle)) {
                weakRelationshipArticles.put(articleTitle, articleDate);
            }
        }
    }

    private void insertOrUpdateCrawlerResult(String newsTitle, String articleDate, String relation) {
        try (Connection connection = DatabaseHandler.connect()) {
            int countryId = countryConfig.getId();

            int nextId = 0;
            int resultId = 0;

            String getMaxIdQuery = "SELECT MAX(id) FROM CrawlerResult";
            try (PreparedStatement preparedStatement = connection.prepareStatement(getMaxIdQuery);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    nextId = resultSet.getInt(1) + 1;
                }
            }

            if (newsTitleExists(connection, countryId, newsTitle)) {
                String updateQuery = "UPDATE crawlerresult SET updated_date = ? WHERE country_id = ? AND news_title = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                    preparedStatement.setDate(1, java.sql.Date.valueOf(articleDate));
                    preparedStatement.setInt(2, countryId);
                    preparedStatement.setString(3, newsTitle);

                    preparedStatement.executeUpdate();
                    resultId = DatabaseHandler.getCrawlerResultId(connection, countryId, newsTitle);
                }
            } else {
                String insertQuery = "INSERT INTO crawlerresult (id, country_id, news_title, updated_date) VALUES (?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    preparedStatement.setInt(1, nextId);
                    preparedStatement.setInt(2, countryId);
                    preparedStatement.setString(3, newsTitle);
                    preparedStatement.setDate(4, java.sql.Date.valueOf(articleDate));

                    preparedStatement.executeUpdate();
                    resultId = nextId;
                }
            }
            
            int relationId = DatabaseHandler.getRelationId(relation);
            ResultRelationHandler resultRelationHandler = new ResultRelationHandler(resultId, relationId, connection);
            resultRelationHandler.insertResultRelation();
            
        } catch (SQLException e) {
            System.err.println("An error occurred while processing the database: " + e.getMessage());
        }
    }
    private Boolean isStrongRelationship(String titleText) {
        for (String keyword : strongRelationKeywordList) {
            if (titleText.toLowerCase().contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private Boolean isWeakRelationship(String titleText) {
        for (String keyword : weakRelationKeywordList) {
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
        CountryConfiguration countryConfig = CountryConfiguration.getCountryConfigurationFromDatabase("EU");

        int numberOfThreads = countryConfig.getNumberOfThreads();
        SeleniumCrawler crawler;

        if ("France".equals(countryConfig.getCountryName())) {
            crawler = new SeleniumCrawler(countryConfig, "climat", numberOfThreads, 150);
        } else {
            crawler = new SeleniumCrawler(countryConfig, "climate", numberOfThreads, 50);
        }

        crawler.start();
    }
}
