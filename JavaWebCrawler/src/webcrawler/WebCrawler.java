package webcrawler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {

    private final String rootUrl;
    private final String keyword;
    private final Set<String> visitedUrls = new HashSet<>();
    private final Set<String> foundArticles = new HashSet<>();
    private final ExecutorService executorService;

    public WebCrawler(String rootUrl, String keyword, int numThreads) {
        this.rootUrl = rootUrl;
        this.keyword = keyword;
        this.executorService = Executors.newFixedThreadPool(numThreads);
    }

    public void start() {
        crawl(rootUrl);
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Found articles: " + foundArticles);
    }

    private void crawl(String url) {
        if (visitedUrls.contains(url)) {
            return;
        }
        visitedUrls.add(url);
        executorService.submit(() -> {
            try {
                Document doc = Jsoup.connect(url).get();
                if (doc.text().contains(keyword)) {
                    synchronized (foundArticles) {
                        foundArticles.add(url);
                    }
                }
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String absHref = link.attr("abs:href");
                    if (absHref.startsWith(rootUrl) && !visitedUrls.contains(absHref)) {
                        crawl(absHref);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        // Example usage
        WebCrawler crawler = new WebCrawler("https://www.gov.uk/", "climate", 10);
        crawler.start();
    }
}

