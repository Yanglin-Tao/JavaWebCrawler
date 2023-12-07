package webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AJAXCrawler {

    private final String rootUrl;
    private final String keyword;
    private final int maxPages;
	private final Set<String> visitedUrls = new HashSet<>();
	private final Map<String, String> containsKeywordArticles = new HashMap<>();
	private final Map<String, String> weakAndStrongRelationshipArticles = new HashMap<>();
	private final Map<String, String> strongRelationshipArticles = new HashMap<>();
    private final Map<String, String> weakRelationshipArticles = new HashMap<>();
    
    private final ExecutorService executorService;
    private ArrayList<String> strongRelationKeywordList = new ArrayList<String>();
    private ArrayList<String> weakRelationKeywordList = new ArrayList<String>();
    
    private long startTime;
    private long endTime;
    private long totalTime;

	public AJAXCrawler(String rootUrl, String keyword, int numThreads, int maxPages) {
        this.rootUrl = rootUrl;
        this.keyword = keyword;
        this.executorService = Executors.newFixedThreadPool(numThreads);
        this.maxPages = maxPages;
        strongRelationKeywordList.add("security");
        strongRelationKeywordList.add("defense");
        weakRelationKeywordList.add("change");
        weakRelationKeywordList.add("risk");
        
        System.setProperty("webdriver.chrome.driver", "/opt/homebrew/bin/chromedriver");

    }

    public void start() {
    	startTime = System.currentTimeMillis();
    	crawl(rootUrl);
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        endTime = System.currentTimeMillis();
        
        System.out.println("DONE. Finished parsing " + maxPages + " pages");
        totalTime = endTime - startTime;
        System.out.println("Total execution time: " + totalTime/1000 + " s");
        
        for (Map.Entry<String, String> entry : containsKeywordArticles.entrySet()) {
            System.out.println(entry.getKey() + " - Updated on: " + entry.getValue());
        }
        System.out.println("Number of articles conatining keyword: " + containsKeywordArticles.size());
        
        System.out.println("---------------------------------------------------------------------------------------");
        
        for (Map.Entry<String, String> entry : weakAndStrongRelationshipArticles.entrySet()) {
            System.out.println(entry.getKey() + " - Updated on: " + entry.getValue());
        }
        System.out.println("Number of articles with weak or strong relationship found: " + weakAndStrongRelationshipArticles.size());
        
        System.out.println("---------------------------------------------------------------------------------------");
        
        for (Map.Entry<String, String> entry : strongRelationshipArticles.entrySet()) {
            System.out.println(entry.getKey() + " - Updated on: " + entry.getValue());
        }
        System.out.println("Number of articles with strong relationship found: " + strongRelationshipArticles.size());
        
        System.out.println("---------------------------------------------------------------------------------------");
        
        for (Map.Entry<String, String> entry : weakRelationshipArticles.entrySet()) {
            System.out.println(entry.getKey() + " - Updated on: " + entry.getValue());
        }
        System.out.println("Number of articles with weak relationship found: " + weakRelationshipArticles.size());
        
        System.out.println("---------------------------------------------------------------------------------------");
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
                WebDriverWait wait = new WebDriverWait(driver, 10); // 10 seconds wait

                while (true) {
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".view-content"))); // Adjust CSS selector as needed
                    String htmlContent = driver.getPageSource();
                    Document doc = Jsoup.parse(htmlContent);

                    Elements newsItems = doc.select(".view-content .views-row");
                    for (Element item : newsItems) {
                    	synchronized (weakAndStrongRelationshipArticles) {
                            String articleTitle = item.select("h3").text();
                            System.out.println(articleTitle);
                            String articleDate = item.select("time").attr("datetime");
                            handleResults(articleTitle, articleDate);
                        }
                    }
                    
                    WebElement cookieComplianceBanner = new WebDriverWait(driver, 10)
                            .until(ExpectedConditions.elementToBeClickable(By.id("calibr8-cookie-compliance")));

                    cookieComplianceBanner.click();

                    new WebDriverWait(driver, 10)
                            .until(ExpectedConditions.invisibilityOfElementLocated(By.id("calibr8-cookie-compliance")));

                    WebElement nextButton = new WebDriverWait(driver, 10)
                            .until(ExpectedConditions.elementToBeClickable(By.cssSelector("li.pager-item--next")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextButton);

                }
                
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                driver.close();
            }
        });
    }
    
    private void handleResults(String articleTitle, String articleDate) {
    	if (articleTitle.toLowerCase().contains(keyword)) {
    		containsKeywordArticles.put(articleTitle, articleDate);
    	} 
    	if (articleTitle.toLowerCase().contains(keyword) && (isStrongRelationship(articleTitle.toLowerCase()) || (isWeakRelationship(articleTitle.toLowerCase())))) {
    		weakAndStrongRelationshipArticles.put(articleTitle, articleDate);
    	} 
    	if (articleTitle.toLowerCase().contains(keyword) && isStrongRelationship(articleTitle.toLowerCase())) {
    		strongRelationshipArticles.put(articleTitle, articleDate);
    	} 
    	if (articleTitle.toLowerCase().contains(keyword) && isWeakRelationship(articleTitle.toLowerCase())) {
    		weakRelationshipArticles.put(articleTitle, articleDate);
    	}
    }
    
    private Boolean isStrongRelationship(String titleText) {
    	for (String keyword : strongRelationKeywordList) {
    		if (titleText.contains(keyword)) {
    			return true;
    		}
    	}
    	return false;
    }
    
	private Boolean isWeakRelationship(String titleText) {
		for (String keyword : weakRelationKeywordList) {
    		if (titleText.contains(keyword)) {
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
    
    // comment out the code if you are connecting to gui
    public static void main(String[] args) {
        AJAXCrawler crawler = new AJAXCrawler("https://news.belgium.be/fr/rechercher-un-communique", "climate", 50, 50);
        crawler.start();
    }
}
