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

public class WikiCrawler {
    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect("https://www.gov.uk/search/news-and-communications").get();
        System.out.println(doc.title());

        Elements allElements = doc.select("*");

        for (Element element : allElements) {
        	System.out.println(element.toString());
//            if (element.toString().toLowerCase().contains("climate")) {
//                System.out.println(element);
//            }
        }
    }
}
