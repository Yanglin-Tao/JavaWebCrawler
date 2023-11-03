package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class WebCrawlerInterface extends JFrame{
	private static final int FRAME_WIDTH = 500;
	private static final int FRAME_HEIGHT = 300;
	private JButton crawlButton;
	private ChartPanel chartPanel;
	
	public WebCrawlerInterface() {
		createMainPanel();
	}
	
	public void createMainPanel() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		chartPanel = createBarChartPanel();
        mainPanel.add(chartPanel, BorderLayout.CENTER);
		
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		crawlButton = new JButton("Crawl");
		controlPanel.add(crawlButton);
		mainPanel.add(controlPanel, BorderLayout.SOUTH);
		add(mainPanel);
	}
	
	private ChartPanel createBarChartPanel() {
	    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	    dataset.addValue(120, "Articles", "1 month");
	    dataset.addValue(300, "Articles", "3 months");
	    dataset.addValue(450, "Articles", "6 months");
	    dataset.addValue(700, "Articles", "12 months");

	    JFreeChart chart = ChartFactory.createBarChart(
	            "Article Count Over Time", // Chart title
	            "Time Period", 
	            "Number of Articles", 
	            dataset, 
	            PlotOrientation.VERTICAL, 
	            false, 
	            true, 
	            false
	    );

	    chartPanel = new ChartPanel(chart);
	    return chartPanel;
	}	
	
	public static JMenu createMenu(WebCrawlerInterface frame) {
      JMenu menu = new JMenu("Select Country");
      JMenuItem c1 = new JMenuItem("UK");    
      JMenuItem c2 = new JMenuItem("Germany");    
      JMenuItem c3 = new JMenuItem("France");    
      menu.add(c1);
      menu.add(c2);
      menu.add(c3);
      return menu;
    }
	
	public static void main(String[] args) {
		JFrame frame = new WebCrawlerInterface(); 
		JMenuBar menuBar = new JMenuBar();     
	    frame.setJMenuBar(menuBar);
	    menuBar.add(createMenu((WebCrawlerInterface) frame));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setVisible(true);
	}
}
