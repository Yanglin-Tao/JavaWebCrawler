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
import java.text.SimpleDateFormat;
import java.util.Date;


// Parsing through the paragraph part of each article as titles don't contain the keyword

public class SeleniumCrawlerItaly {

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

	public SeleniumCrawlerItaly(String rootUrl, String keyword, int numThreads, int maxPages) {
        this.rootUrl = rootUrl;
        this.keyword = keyword;
        this.executorService = Executors.newFixedThreadPool(numThreads);
        this.maxPages = maxPages;
        strongRelationKeywordList.add("security");
        strongRelationKeywordList.add("defense");
        weakRelationKeywordList.add("change");
        weakRelationKeywordList.add("risk");
        
         System.setProperty("webdriver.chrome.driver", "C:\\ProgramData\\chocolatey\\bin\\chromedriver.exe");
        // System.setProperty("webdriver.chrome.driver", "/opt/homebrew/bin/chromedriver");

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
                String htmlContent = driver.getPageSource();
                Document doc = Jsoup.parse(htmlContent);

                Elements newsItems = doc.select(".row.half_base .box_text.box_text_small.clearfix");

                for (Element item : newsItems) {
                    synchronized (weakAndStrongRelationshipArticles) {
                        String articleTitle = item.select("p").text();
                        String articleDate = item.select("div.h6.clearfix.dataleft").text();
                        
                     // Convert the date format
                        SimpleDateFormat originalFormat = new SimpleDateFormat("d MMMM yyyy");
                        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = originalFormat.parse(articleDate);
                        articleDate = targetFormat.format(date);
                        
                        handleResults(item,articleTitle, articleDate);
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
    	if (articleTitle.toLowerCase().contains(keyword)) {
    		String updatedArticleTitle = item.select("h2.h4").text();

    		containsKeywordArticles.put(updatedArticleTitle, articleDate);
    	} 
    	if (articleTitle.toLowerCase().contains(keyword) && (isStrongRelationship(articleTitle.toLowerCase()) || (isWeakRelationship(articleTitle.toLowerCase())))) {
    		String updatedArticleTitle = item.select("h2.h4").text();

    		weakAndStrongRelationshipArticles.put(updatedArticleTitle, articleDate);
    	} 
    	if (articleTitle.toLowerCase().contains(keyword) && isStrongRelationship(articleTitle.toLowerCase())) {
            // Update the selector here to target "h2" and "h4" tags
            String updatedArticleTitle = item.select("h2.h4").text();
            strongRelationshipArticles.put(updatedArticleTitle, articleDate);
        } 
        if (articleTitle.toLowerCase().contains(keyword) && isWeakRelationship(articleTitle.toLowerCase())) {
            // Update the selector here to target "h2" and "h4" tags
            String updatedArticleTitle = item.select("h2.h4").text();
            weakRelationshipArticles.put(updatedArticleTitle, articleDate);
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
        //EUCrawler crawler = new EUCrawler("https://european-union.europa.eu/news-and-events/news-and-stories_en", "climate", 50, 50);
    	SeleniumCrawlerItaly crawler = new SeleniumCrawlerItaly("https://www.governo.it/en/notizie-chigi-en", "climate", 50, 50);
        crawler.start();
    }
}


