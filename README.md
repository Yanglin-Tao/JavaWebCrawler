# JavaWebCrawler
A multi-threaded Java web crawler project to extract data from government websites and analyze the overall trend on climate change and national security.

#### _Yanglin Tao, Oct 29, 2023_

### Project Setup
1. Download chromedriver with Homebrew (MacOS) by running this command `brew install chromedriver`. 
2. If prompted that “chromedriver” can’t be opened because Apple cannot check it for malicious software, go to System Settings > Security & Privacy, and you should see a message about chromedriver being blocked. In that case, click `Open Anyway` to force open the application.
3. Run `which chromedriver` to verify its path. Copy and paste the path to SeleniumCrawler.

### Database Setup
1. Download postgresql and pgAdmin tool.
2. Create a database named 'webCrawler_db', use password 'root'.
3. Use Query tool and run queries in WebCrawlerDatabase.sql to initialize the database.

### Finding URLs to parse
It's important to parse the correct base URL, use URLs like `https://www.gov.uk/search/news-and-communications` instead of generic ones for better results. 

### Finding the metric elements
In the current phase, the crawler should count the number of articles, i.e. article titles, containing keywords. Therefore the title must be located first. Go to Inspect Elements on the webpage, then locate the title element that share a same path or class selector. 

### Scope of the search
Both number of threads and number of webpages to crawl can be specified in the crawler.

### Troubleshooting
If you encountered error like `org.openqa.selenium.SessionNotCreatedException: session not created`, it's likely that your ChromeDriver version is not compactible with your current Chrome browser version. In that case, run `brew install chromedriver` again and update Chrome browser to the latest version.
