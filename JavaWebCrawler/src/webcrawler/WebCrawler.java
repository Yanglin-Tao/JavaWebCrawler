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
    private final Set<String> articleTitles = new HashSet<>();
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
        System.out.println("Article titles: " + articleTitles);
    }

    private void crawl(String url) {
        if (visitedUrls.contains(url)) {
            return;
        }
        visitedUrls.add(url);
        System.out.println(url);
        executorService.submit(() -> {
            try {
                Document doc = Jsoup.connect(url).get();
                System.out.println(doc.title());
                if (doc.text().toLowerCase().contains(keyword)) {
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
                Elements newsTitles = doc.select("a.govuk-link");
                for (Element title : newsTitles) {
                    synchronized (articleTitles) {
                        articleTitles.add(title.text());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        // Example usage
        WebCrawler crawler = new WebCrawler("https://www.gov.uk/search/news-and-communications", "pm", 2000);
        crawler.start();
    }
}

