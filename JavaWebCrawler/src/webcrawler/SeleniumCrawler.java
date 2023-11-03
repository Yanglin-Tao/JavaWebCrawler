package webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SeleniumCrawler {

    private final String rootUrl;
    private final String keyword;
    private final int maxPages;
    private final Set<String> visitedUrls = new HashSet<>();
    private final Set<String> totalArticleTitles = new HashSet<>();
    private final Set<String> strongRelationshipArticleTitles = new HashSet<>();
    private final Set<String> weakRelationshipArticleTitles = new HashSet<>();
    private final ExecutorService executorService;
    private ArrayList<String> strongRelationKeywordList = new ArrayList<String>();
    private ArrayList<String> weakRelationKeywordList = new ArrayList<String>();

    public SeleniumCrawler(String rootUrl, String keyword, int numThreads, int maxPages) {
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
        System.out.println("DONE. Finished parsing " + maxPages + " pages");
        for (String title : totalArticleTitles) {
        	System.out.println(title);
        }
        System.out.println("Number of articles with weak or strong relationship found: " + totalArticleTitles.size());
        System.out.println("---------------------------------------------------------------------------------------");
        for (String title : strongRelationshipArticleTitles) {
        	System.out.println(title);
        }
        System.out.println("Number of articles with strong relationship found: " + strongRelationshipArticleTitles.size());
        System.out.println("---------------------------------------------------------------------------------------");
        for (String title : weakRelationshipArticleTitles) {
        	System.out.println(title);
        }
        System.out.println("Number of articles with weak relationship found: " + weakRelationshipArticleTitles.size());
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

                // System.out.println(doc.title());

                Elements newsTitles = doc.select("a[data-ga4-ecommerce-path^=/government/news/]");
                for (Element title : newsTitles) {
                    synchronized (totalArticleTitles) {
                    	if (title.text().toLowerCase().contains(keyword) && (isStrongRelationship(title.text().toLowerCase()) || (isWeakRelationship(title.text().toLowerCase())))) {
                    		totalArticleTitles.add(title.text());
                    	} 
                    	if (title.text().toLowerCase().contains(keyword) && isStrongRelationship(title.text().toLowerCase())) {
                    		strongRelationshipArticleTitles.add(title.text());
                    	} 
                    	if (title.text().toLowerCase().contains(keyword) && isWeakRelationship(title.text().toLowerCase())) {
                    		weakRelationshipArticleTitles.add(title.text());
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

    public static void main(String[] args) {
        SeleniumCrawler crawler = new SeleniumCrawler("https://www.gov.uk/search/news-and-communications", "climate", 1000, 150);
        crawler.start();
    }
}




