package webcrawler;

/**
 * 
 * Wrapper class for country configuration
 *
 */

public class CountryConfiguration {

    private int id;
    private String countryName;
    private String rootUrl;
    private String listItemsSelector;
    private String newsTitleSelector;
    private String newsTeaserSelector;
    private String metadataSelector;
    private String dateFormat;
    private int numberOfThreads;

    public CountryConfiguration() {
    	
    }

    public CountryConfiguration(int id, String countryName, String rootUrl, String listItemsSelector,
                                String newsTitleSelector, String newsTeaserSelector, String metadataSelector,
                                String dateFormat, int numberOfThreads) {
        this.id = id;
        this.countryName = countryName;
        this.rootUrl = rootUrl;
        this.listItemsSelector = listItemsSelector;
        this.newsTitleSelector = newsTitleSelector;
        this.newsTeaserSelector = newsTeaserSelector;
        this.metadataSelector = metadataSelector;
        this.dateFormat = dateFormat;
        this.numberOfThreads = numberOfThreads;
    }

    public int getId() {
        return id;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getRootUrl() {
        return rootUrl;
    }

    public String getListItemsSelector() {
        return listItemsSelector;
    }

    public String getNewsTitleSelector() {
        return newsTitleSelector;
    }

    public String getNewsTeaserSelector() {
        return newsTeaserSelector;
    }

    public String getMetadataSelector() {
        return metadataSelector;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }
}