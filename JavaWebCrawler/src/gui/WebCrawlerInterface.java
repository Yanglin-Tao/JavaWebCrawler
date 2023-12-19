package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import webcrawler.CountryConfiguration;
import webcrawler.DatabaseHandler;
import webcrawler.SeleniumCrawler;

/**
 * 
 * Interface for web crawler
 *
 */

public class WebCrawlerInterface extends JFrame {
    private static final int FRAME_WIDTH = 1000;
    private static final int FRAME_HEIGHT = 800;
    private JButton crawlButton;
    private ChartPanel chartPanel;
    private JTextArea textArea;
    private static String selectedCountry = "UK";
    private int maxPages = 300;
    private JTextField maxPagesTextField;
    private DefaultCategoryDataset dataset;

    public WebCrawlerInterface() {
    	dataset = createBarChartDataset(); 
        chartPanel = createBarChartPanel(dataset); 
        createMainPanel();
        createTextArea();
    }

    private void crawlData() {
        int maxPagesValue = Integer.parseInt(maxPagesTextField.getText());

        CountryConfiguration countryConfig = DatabaseHandler.getCountryConfigurationFromDatabase(selectedCountry);

        int numberOfThreads = countryConfig.getNumberOfThreads();
        SeleniumCrawler crawler;

        crawler = new SeleniumCrawler(countryConfig, "climate", numberOfThreads, maxPagesValue);
        crawler.start();

        Map<String, Map<String, Integer>> statisticsMap = DatabaseHandler.fetchStatistics(countryConfig.getCountryName());

        DefaultCategoryDataset statisticsDataset = convertToDataset(statisticsMap);

        JFreeChart updatedChart = createBarChartPanel(statisticsDataset).getChart();

        chartPanel.setChart(updatedChart);

        chartPanel.getChart().fireChartChanged();
        displayInformation(crawler);
    }

    private ChartPanel createBarChartPanel(DefaultCategoryDataset dataset) {
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

        return new ChartPanel(chart);
    }


    public void createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        mainPanel.add(chartPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

        JLabel maxPagesLabel = new JLabel("Max Pages:");
        controlPanel.add(maxPagesLabel);

        maxPagesTextField = new JTextField(String.valueOf(maxPages), 5);
        controlPanel.add(maxPagesTextField);

        crawlButton = new JButton("Crawl");
        crawlButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                crawlData();
            }
        });
        JLabel countryLabel = new JLabel("Selected Country: ");
        controlPanel.add(countryLabel);
        controlPanel.add(createCountryComboBox());
        controlPanel.add(crawlButton);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        add(mainPanel);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu(this));
        setJMenuBar(menuBar);
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
        
        info.append("Contains keyword articles:\n");
        appendArticlesInfo(info, crawler.getContainsKeywordArticles());

        info.append("Weak and strong relationship articles:\n");
        appendArticlesInfo(info, crawler.getWeakAndStrongRelationshipArticles());

        info.append("Strong relationship articles:\n");
        appendArticlesInfo(info, crawler.getStrongRelationshipArticles());

        info.append("Weak relationship articles:\n");
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
        
        dataset.addValue(0, "Contains keyword", "0-1 months");
        dataset.addValue(0, "Strong", "0-1 months");
        dataset.addValue(0, "Weak", "0-1 months");

        dataset.addValue(0, "Contains keyword", "1-3 months");
        dataset.addValue(0, "Strong", "1-3 months");
        dataset.addValue(0, "Weak", "1-3 months");
        
        dataset.addValue(0, "Contains keyword", "3-6 months");
        dataset.addValue(0, "Strong", "3-6 months");
        dataset.addValue(0, "Weak", "3-6 months");
        
        dataset.addValue(0, "Contains keyword", "6-12 months");
        dataset.addValue(0, "Strong", "6-12 months");
        dataset.addValue(0, "Weak", "6-12 months");
        
        dataset.addValue(0, "Contains keyword", "Over 12 months");
        dataset.addValue(0, "Strong", "Over 12 months");
        dataset.addValue(0, "Weak", "Over 12 months");
        
        return dataset;
    }

    private DefaultCategoryDataset convertToDataset(Map<String, Map<String, Integer>> stats) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, Map<String, Integer>> entry : stats.entrySet()) {
            String period = entry.getKey();
            Map<String, Integer> relationships = entry.getValue();

            for (Map.Entry<String, Integer> relationshipEntry : relationships.entrySet()) {
                String relationship = relationshipEntry.getKey();
                Integer count = relationshipEntry.getValue();

                dataset.addValue(count, relationship, period);
            }
        }

        return dataset;
    }

    private JComboBox<String> createCountryComboBox() {
        List<String> countries = DatabaseHandler.getCountryNames();
        JComboBox<String> comboBox = new JComboBox<>(countries.toArray(new String[0]));
        comboBox.setSelectedItem(selectedCountry);
        comboBox.addActionListener(e -> {
            selectedCountry = (String) comboBox.getSelectedItem();
            crawlButton.setEnabled(true); 
        });
        return comboBox;
    }

    public static JMenu createFileMenu(WebCrawlerInterface frame) {
        JMenu menu = new JMenu("File");
        JMenuItem item = new JMenuItem("Exit");
        item.addActionListener((e) -> System.exit(0));
        menu.add(item);
        return menu;
    }

    public static void main(String[] args) {
        JFrame frame = new WebCrawlerInterface();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setVisible(true);
    }
}
