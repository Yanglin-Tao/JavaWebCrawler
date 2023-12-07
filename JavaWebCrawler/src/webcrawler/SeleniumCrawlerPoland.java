package webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SeleniumCrawlerPoland {

    private final String rootUrl;
    private final String keyword;
    private final int maxPages;
	private final Set<String> visitedUrls = new HashSet<>();
    private final Map<String, String> totalArticles = new HashMap<>();
	private final Map<String, String> strongRelationshipArticles = new HashMap<>();
    private final Map<String, String> weakRelationshipArticles = new HashMap<>();
    
    private final ExecutorService executorService;
    private ArrayList<String> strongRelationKeywordList = new ArrayList<String>();
    private ArrayList<String> weakRelationKeywordList = new ArrayList<String>();
    
    private long startTime;
    private long endTime;
    private long totalTime;

	public SeleniumCrawlerPoland(String rootUrl, String keyword, int numThreads, int maxPages) {
        this.rootUrl = rootUrl;
        this.keyword = keyword;
        this.executorService = Executors.newFixedThreadPool(numThreads);
        this.maxPages = maxPages;
        strongRelationKeywordList.add("security");
        strongRelationKeywordList.add("defense");
        weakRelationKeywordList.add("change");
        weakRelationKeywordList.add("risk");
        
        System.setProperty("webdriver.chrome.driver", "C:\\ProgramData\\chocolatey\\bin\\chromedriver.exe");

    }

    public void start() {
    	startTime = System.currentTimeMillis();
    	int pageNum = 1;
        while(pageNum <= maxPages) {
            crawl(rootUrl + "?page=" + pageNum);
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
        System.out.println("Total execution time: " + totalTime/1000 + " s");
        
        for (Map.Entry<String, String> entry : totalArticles.entrySet()) {
            System.out.println(entry.getKey() + " - Updated on: " + entry.getValue());
        }
        System.out.println("Number of articles with weak or strong relationship found: " + totalArticles.size());
        
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
                String htmlContent = driver.getPageSource();
                Document doc = Jsoup.parse(htmlContent);

                Elements newsTitles = doc.select("ul li");
                
                for (Element title : newsTitles) {
                    synchronized (totalArticles) {
                    	String articleTitle = title.text();
                    	// Print the title here
                    	System.out.println("Title: " + articleTitle);

                    	Element listItem = title.closest("div.title");
                    	String articleDate = listItem.select("time").attr("datetime");
                    	if (articleTitle.toLowerCase().contains(keyword) && (isStrongRelationship(articleTitle.toLowerCase()) || (isWeakRelationship(articleTitle.toLowerCase())))) {
                    		totalArticles.put(articleTitle, articleDate);
                    	} 
                    	if (articleTitle.toLowerCase().contains(keyword) && isStrongRelationship(articleTitle.toLowerCase())) {
                    		strongRelationshipArticles.put(articleTitle, articleDate);
                    	} 
                    	if (articleTitle.toLowerCase().contains(keyword) && isWeakRelationship(articleTitle.toLowerCase())) {
                    		weakRelationshipArticles.put(articleTitle, articleDate);
                    		
                    	}
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                driver.close();
            }
        });
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
    
    public Map<String, String> getTotalArticles() {
		return totalArticles;
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
        SeleniumCrawlerPoland crawler = new SeleniumCrawlerPoland("https://www.gov.pl/web/diplomacy/news-", "climate", 100, 50);
        crawler.start();
    }
}




