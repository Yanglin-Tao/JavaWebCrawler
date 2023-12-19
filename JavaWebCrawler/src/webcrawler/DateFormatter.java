package webcrawler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.jsoup.nodes.Element;

/**
 * 
 * Format dates from retrieved metadata
 *
 */

public class DateFormatter {
	private CountryConfiguration countryConfig;
	private Element item;

	public DateFormatter(CountryConfiguration countryConfig, Element item) {
		this.countryConfig = countryConfig;
		this.item = item;
	}
	
	public String formatDate() throws ParseException {
		String articleDate = "";
		String metadataSelector = countryConfig.getMetadataSelector();
		String dateFormat = countryConfig.getDateFormat();
		if (metadataSelector.equals("time")) {
			// UK, EU, Belgium, Germany
            articleDate = item.select(metadataSelector).attr("datetime");

            SimpleDateFormat inputFormat = new SimpleDateFormat(dateFormat);
            Date date = inputFormat.parse(articleDate);

            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            articleDate = outputFormat.format(date);
        } else if (metadataSelector.equals("div.h6.clearfix.dataleft")) {
        	// Italy
        	articleDate = item.select(metadataSelector).text();
          
        	SimpleDateFormat inputFormat = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        	Date parsedDate = inputFormat.parse(articleDate);
        	SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
        	articleDate  = outputFormat.format(parsedDate);
        } else if (metadataSelector.equals("span.date")) {
        	// Norway
        	articleDate = item.select(metadataSelector).text();
        	SimpleDateFormat inputFormat = new SimpleDateFormat(dateFormat);
        	Date date = inputFormat.parse(articleDate);

        	SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
        	articleDate = outputFormat.format(date);
        } else if (metadataSelector.equals("p.meta")) {
        	// Netherland
        	String metaData = item.select(metadataSelector).text();
        	String[] parts = metaData.split(" \\| ");
        	if (parts.length > 1) {
        		String[] dateParts = parts[1].split("[–-]");
        		if (dateParts.length == 3) {
        			String dateString = dateParts[1] + "-" + dateParts[0] + "-" + dateParts[2];
        			SimpleDateFormat inputFormat = new SimpleDateFormat(dateFormat);
        			Date date = inputFormat.parse(dateString);

        			SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
        			articleDate = outputFormat.format(date);
        		} 
        	}
        } 
		return articleDate;
	}
}
