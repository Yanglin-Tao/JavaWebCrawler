package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import webcrawler.SeleniumCrawler;

public class WebCrawlerInterface extends JFrame{
	private static final int FRAME_WIDTH = 1000;
	private static final int FRAME_HEIGHT = 800;
	private JButton crawlButton;
	private ChartPanel chartPanel;
	private JTextArea textArea;
	private static String selectedCountry = "UK";
	private DefaultCategoryDataset dataset;
	
	public WebCrawlerInterface() {
		dataset = createBarChartDataset();
		createMainPanel();
		createTextArea();
	}
	
	private void crawlData() {
        SeleniumCrawler crawler = new SeleniumCrawler("https://www.gov.uk/search/news-and-communications", "climate", 1000, 150);
        crawler.start();
        updateDatasetWithCrawledData(crawler);
        chartPanel.getChart().fireChartChanged();
        displayInformation(crawler);
    }
	
	public void createMainPanel() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		chartPanel = createBarChartPanel();
        mainPanel.add(chartPanel, BorderLayout.CENTER);
		
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		crawlButton = new JButton("Crawl");
		crawlButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                crawlData();
                chartPanel.updateUI();
            }
        });
		controlPanel.add(crawlButton);
		mainPanel.add(controlPanel, BorderLayout.SOUTH);
		add(mainPanel);
	}
	
	private void createTextArea() {
        textArea = new JTextArea(15, 40); 
        textArea.setEditable(false); 
        JScrollPane scrollPane = new JScrollPane(textArea);
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(scrollPane, BorderLayout.CENTER);
        add(textPanel, BorderLayout.SOUTH);
    }
	
	private void displayInformation(SeleniumCrawler crawler) {
        StringBuilder info = new StringBuilder();
        info.append("DONE. Finished parsing ").append(crawler.getMaxPages()).append(" pages\n");
        info.append("Total execution time: ").append(crawler.getTotalTime() / 1000).append(" s\n");

        info.append("Total articles:\n");
        appendArticlesInfo(info, crawler.getTotalArticles());

        info.append("Strong Relationship Articles:\n");
        appendArticlesInfo(info, crawler.getStrongRelationshipArticles());

        info.append("Weak Relationship Articles:\n");
        appendArticlesInfo(info, crawler.getWeakRelationshipArticles());

        textArea.setText(info.toString());
    }

    private void appendArticlesInfo(StringBuilder info, Map<String, String> articles) {
        for (Map.Entry<String, String> entry : articles.entrySet()) {
            info.append(entry.getKey()).append(" - Updated on: ").append(entry.getValue()).append("\n");
        }
        info.append("Number of articles found: ").append(articles.size()).append("\n");
        info.append("---------------------------------------------------------------------------------------\n");
    }
	
	private DefaultCategoryDataset createBarChartDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(0, "Strong", "1 month");
        dataset.addValue(0, "Weak", "1 month");
        dataset.addValue(0, "Either", "1 month");

        dataset.addValue(0, "Strong", "3 months");
        dataset.addValue(0, "Weak", "3 months");
        dataset.addValue(0, "Either", "3 months");

        dataset.addValue(0, "Strong", "6 months");
        dataset.addValue(0, "Weak", "6 months");
        dataset.addValue(0, "Either", "6 months");

        dataset.addValue(0, "Strong", "12 months");
        dataset.addValue(0, "Weak", "12 months");
        dataset.addValue(0, "Either", "12 months");

        return dataset;
    }
	
	private ChartPanel createBarChartPanel() {
		String chartTitle = "Article Count Over Time in " + selectedCountry;
	    JFreeChart chart = ChartFactory.createBarChart(
	    		chartTitle, 
	            "Time Period", 
	            "Number of Articles", 
	            dataset, 
	            PlotOrientation.VERTICAL, 
	            true, 
	            true, 
	            false
	    );

	    chartPanel = new ChartPanel(chart);
	    return chartPanel;
	}	

	private void updateDatasetWithCrawledData(SeleniumCrawler crawler) {
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
	    Calendar currentDate = Calendar.getInstance();

	    int[] intervals = {1, 3, 6, 12};

	    for (Map.Entry<String, String> entry : crawler.getTotalArticles().entrySet()) {
	        String articleTitle = entry.getKey();
	        String articleDateStr = entry.getValue();
	        try {
	            Date articleDate = dateFormat.parse(articleDateStr);
	            Calendar articleCalendar = new GregorianCalendar();
	            articleCalendar.setTime(articleDate);
	            int monthsDiff = monthsBetween(articleCalendar, currentDate);

	            String relationshipType = "Either";
	            for (int interval : intervals) {
	                if (monthsDiff <= interval) {
	                    String intervalLabel = interval + " month" + (interval > 1 ? "s" : "");
	                    dataset.incrementValue(1, relationshipType, intervalLabel);
	                    break;
	                }
	            }

	            if (crawler.getStrongRelationshipArticles().containsKey(articleTitle)) {
	                relationshipType = "Strong";
	            } else if (crawler.getWeakRelationshipArticles().containsKey(articleTitle)) {
	                relationshipType = "Weak";
	            }

	            for (int interval : intervals) {
	                if (monthsDiff <= interval) {
	                    String intervalLabel = interval + " month" + (interval > 1 ? "s" : "");
	                    dataset.incrementValue(1, relationshipType, intervalLabel);
	                    break;
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    chartPanel.getChart().fireChartChanged();
	}
    
    private int monthsBetween(Calendar startDate, Calendar endDate) {
        int monthsBetween = 0;
        while (startDate.before(endDate)) {
            startDate.add(Calendar.MONTH, 1);
            monthsBetween++;
        }
        return monthsBetween;
    }
	
	public static JMenu createMenu(WebCrawlerInterface frame) {
      JMenu menu = new JMenu("Select Country");
      JMenuItem c1 = new JMenuItem("UK");   
      c1.addActionListener(e -> {
          selectedCountry = "UK";
      });
      JMenuItem c2 = new JMenuItem("Germany");    
      c2.addActionListener(e -> {
          selectedCountry = "Germany";
      });
      JMenuItem c3 = new JMenuItem("France");    
      c3.addActionListener(e -> {
          selectedCountry = "France";
      });
      menu.add(c1);
      menu.add(c2);
      menu.add(c3);
      return menu;
    }
	
	public static void main(String[] args) {
		JFrame frame = new WebCrawlerInterface(); 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		JMenuBar menuBar = new JMenuBar();     
	    frame.setJMenuBar(menuBar);
	    menuBar.add(createMenu((WebCrawlerInterface) frame));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setVisible(true);
	}
}
