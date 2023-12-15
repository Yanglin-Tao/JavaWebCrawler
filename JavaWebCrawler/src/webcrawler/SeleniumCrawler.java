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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

//    private void crawl(String url) {
//        if (visitedUrls.contains(url)) {
//            return;
//        }
//        visitedUrls.add(url);
//        executorService.submit(() -> {
//            ChromeOptions options = new ChromeOptions();
//            options.addArguments("--headless");
//            WebDriver driver = new ChromeDriver(options);
//            try {
//                driver.get(url);
//                String htmlContent = driver.getPageSource();
//                Document doc = Jsoup.parse(htmlContent);
//
//                Elements newsItems = doc.select(countryConfig.getListItemsSelector());
//
//                for (Element item : newsItems) {
//                    synchronized (weakAndStrongRelationshipArticles) {
//                        String articleTitle = item.select(countryConfig.getNewsTitleSelector()).text();
//                        String articleDate;
//
//                        // Fetch metadata selector dynamically from the database
//                        String metadataSelector = countryConfig.getMetadataSelector();
//                        
//                        // Check the country and select the appropriate date selector
//                        if (countryConfig.getCountryName().equals("Portugal") || countryConfig.getCountryName().equals("Italy") || countryConfig.getCountryName().equals("France") ) {
//                            articleDate = item.select(countryConfig.getMetadataSelector()).text();
//                        
//                        } else if (countryConfig.getCountryName().equals("Belgium") || countryConfig.getCountryName().equals("Germany") || countryConfig.getCountryName().equals("EU") || countryConfig.getCountryName().equals("UK")){
//                            articleDate = item.select(countryConfig.getMetadataSelector()).attr("datetime");
//                        } else if (countryConfig.getCountryName().equals("Netherland")) {
//                        	String metaData = item.select(countryConfig.getMetadataSelector()).text();
//                            String[] parts = metaData.split(" \\| ");
//                        	if (parts.length > 1) {
//                            	String[] dateParts = parts[1].split("[–-]");
//                            	if (dateParts.length == 3) {
//                                    articleDate = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];
//                                } else {
//                                	articleDate = "Date cannot be formated";
//                                }
//                            } else {
//                            	articleDate = "Date not found";
//                            }
//                        }
//
//                        handleResults(item, articleTitle, articleDate);
//                    }
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                driver.close();
//            }
//        });
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
                        if (countryConfig.getCountryName().equals("Portugal") || countryConfig.getCountryName().equals("Italy") || countryConfig.getCountryName().equals("France")) {
                            articleDate = item.select(metadataSelector).text();
                        } else if (countryConfig.getCountryName().equals("Belgium") || countryConfig.getCountryName().equals("Germany") || countryConfig.getCountryName().equals("EU") || countryConfig.getCountryName().equals("UK")) {
                            articleDate = item.select(metadataSelector).attr("datetime");
                        } else if (countryConfig.getCountryName().equals("Netherlands")) {
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


    

    private void handleResults(Element item, String articleTitle, String articleDate) {
    	if (countryConfig.getCountryName().equals("Italy") || countryConfig.getCountryName().equals("Portugal")) {
        // For Italy, update the selector to target "news_title_teaser" element
        String updatedArticleTitle = item.select(countryConfig.getNewsTeaserSelector()).text();

        if (articleTitle.toLowerCase().contains(keyword)) {
            containsKeywordArticles.put(updatedArticleTitle, articleDate);
        }
        
        // Fetch strong and weak relation keywords for the current country
        List<String> strongRelationKeywords = countryConfig.fetchKeywordsForRelation("Strong relationship");
        List<String> weakRelationKeywords = countryConfig.fetchKeywordsForRelation("Weak relationship");

        if (articleTitle.toLowerCase().contains(keyword) && (isStrongRelationship(articleTitle, strongRelationKeywords) || isWeakRelationship(articleTitle, weakRelationKeywords))) {
            weakAndStrongRelationshipArticles.put(updatedArticleTitle, articleDate);
        }

        if (articleTitle.toLowerCase().contains(keyword) && isStrongRelationship(articleTitle, strongRelationKeywords)) {
            strongRelationshipArticles.put(updatedArticleTitle, articleDate);
        }

        if (articleTitle.toLowerCase().contains(keyword) && isWeakRelationship(articleTitle, weakRelationKeywords)) {
            weakRelationshipArticles.put(updatedArticleTitle, articleDate);
        }
    	} else {
        if (articleTitle.toLowerCase().contains(keyword)) {
            containsKeywordArticles.put(articleTitle, articleDate);
        }

        // Fetch strong and weak relation keywords for the current country
        List<String> strongRelationKeywords = countryConfig.fetchKeywordsForRelation("Strong relationship");
        List<String> weakRelationKeywords = countryConfig.fetchKeywordsForRelation("Weak relationship");

        if (articleTitle.toLowerCase().contains(keyword) && (isStrongRelationship(articleTitle, strongRelationKeywords) || isWeakRelationship(articleTitle, weakRelationKeywords))) {
            weakAndStrongRelationshipArticles.put(articleTitle, articleDate);
        }

        if (articleTitle.toLowerCase().contains(keyword) && isStrongRelationship(articleTitle, strongRelationKeywords)) {
            strongRelationshipArticles.put(articleTitle, articleDate);
        }

        if (articleTitle.toLowerCase().contains(keyword) && isWeakRelationship(articleTitle, weakRelationKeywords)) {
            weakRelationshipArticles.put(articleTitle, articleDate);
        }
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
        CountryConfiguration countryConfig = CountryConfiguration.getCountryConfigurationFromDatabase("France");

        // Fetch the number of threads from the country configuration
        int numberOfThreads = countryConfig.getNumberOfThreads();

        // Create the SeleniumCrawler with the retrieved configurations
        SeleniumCrawler crawler = new SeleniumCrawler(countryConfig, "climate", numberOfThreads, 50);
        crawler.start();
    }

}
