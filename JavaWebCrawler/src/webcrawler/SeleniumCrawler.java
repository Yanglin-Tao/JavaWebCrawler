package webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

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
    private final Set<String> articleTitles = new HashSet<>();
    private final ExecutorService executorService;

    public SeleniumCrawler(String rootUrl, String keyword, int numThreads, int maxPages) {
        this.rootUrl = rootUrl;
        this.keyword = keyword;
        this.executorService = Executors.newFixedThreadPool(numThreads);
        this.maxPages = maxPages;

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
        System.out.println("Article titles: " + articleTitles);
        for (String title : articleTitles) {
        	System.out.println(title);
        }
        System.out.println("DONE. Number of articles found in " + maxPages + " pages: " + articleTitles.size());
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

                System.out.println(doc.title());

                Elements newsTitles = doc.select("a[data-ga4-ecommerce-path^=/government/news/]");
                for (Element title : newsTitles) {
                    synchronized (articleTitles) {
                    	if (title.text().toLowerCase().contains(keyword)) {
                    		articleTitles.add(title.text());
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

    public static void main(String[] args) {
        SeleniumCrawler crawler = new SeleniumCrawler("https://www.gov.uk/search/news-and-communications", "climate", 1000, 40);
        crawler.start();
    }
}

