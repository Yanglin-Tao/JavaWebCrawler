# JavaWebCrawler
A multi-threaded Java web crawler project to extract data from government websites and analyze the overall trend on climate change and national security.

#### _Yanglin Tao, Oct 29, 2023_

### Project Setup
MAC OS:
1. Download chromedriver with Homebrew (MacOS) by running this command `brew install chromedriver`. 
2. If prompted that “chromedriver” can’t be opened because Apple cannot check it for malicious software, go to System Settings > Security & Privacy, and you should see a message about chromedriver being blocked. In that case, click `Open Anyway` to force open the application.
3. Run `which chromedriver` to verify its path. Copy and paste the path to SeleniumCrawler.

WINDOWS:
1. Make sure you are using the latest version of “Google Chrome”, if not, update it to the latest version before installing chromedriver.
2. Download chromedriver with Chocolatey by running the following command in PowerShell (run as administrator): choco install chromedriver
3. Run “chromedriver - -version” to verify that the chromedriver has been successfully installed.


### Database Setup
1. Download postgresql and pgAdmin tool.
2. Create a database named 'webCrawler_db', use password 'root'.
3. Use Query tool and run queries in WebCrawlerDatabase.sql to initialize the database.

### Troubleshooting
If you encountered error like `org.openqa.selenium.SessionNotCreatedException: session not created`, it's likely that your ChromeDriver version is not compactible with your current Chrome browser version. In that case, run `brew install chromedriver` again and update Chrome browser to the latest version.

### Guidelines on finding more websites to crawl
#### Finding URLs to parse
It's important to select and parse a suitable base URL, use URLs like `https://www.gov.uk/search/news-and-communications`, where a list of news titles and metadata about their update dates can be found. 

#### Finding the metric elements
The crawler counts the number of articles, i.e. article titles or teasers, containing keywords. Go to Inspect Elements on the webpage, then locate the title and metadata. 

#### Scope of the search.
The number of threads used for each each country vary based on how many pages will be parsed. We typically recommend using 150 threads for larger websites and 50 threads for smaller ones when creating new configuration to the Country table. The number of pages to be parsed is customizable through interface, and we recommend that the number of pages should be greater than number of threads.
