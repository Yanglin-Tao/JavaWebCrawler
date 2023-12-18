-- table to store crawler configuration for each country
CREATE TABLE Country (
    id INT PRIMARY KEY,
    country_name VARCHAR(255),
    root_URL VARCHAR(255),
    list_items_selector VARCHAR(255),
    news_title_selector VARCHAR(255),
    news_teaser_selector VARCHAR(255),
    metadata_selector VARCHAR(255),
    date_format VARCHAR(255),
    number_of_threads INT
);

-- table to store result returned by crawler
CREATE TABLE CrawlerResult (
    id INT PRIMARY KEY,
    country_id INT,
    news_title VARCHAR(255),
    updated_date DATE,
    FOREIGN KEY (country_id) REFERENCES Country(id)
);

-- table to store relation levels
CREATE TABLE Relation (
    id INT PRIMARY KEY,
    relation VARCHAR(255)
);

-- table to store combination keywords; only one relation can be represented by each combination keyword
CREATE TABLE CombinationKeyword (
    id INT PRIMARY KEY,
    combination_keyword VARCHAR(255),
    relation_id INT,
    FOREIGN KEY (relation_id) REFERENCES Relation(id)
);

-- table to store relations represented in each result; each result can have several relations
CREATE TABLE ResultRelation (
    id INT PRIMARY KEY,
    result_id INT,
    relation_id INT,
    FOREIGN KEY (result_id) REFERENCES CrawlerResult(id),
    FOREIGN KEY (relation_id) REFERENCES Relation(id)
);

INSERT INTO Relation (
    id,
    relation
) VALUES (
    1,
    'Strong relationship'
);

INSERT INTO Relation (
    id,
    relation
) VALUES (
    2,
    'Weak relationship'
);

INSERT INTO Relation (
    id,
    relation
) VALUES (
    3,
    'Contains keyword'
);

INSERT INTO CombinationKeyword (
    id,
    combination_keyword,
    relation_id
) VALUES (
    1,
    'security',
    1
);

INSERT INTO CombinationKeyword (
    id,
    combination_keyword,
    relation_id
) VALUES (
    2,
    'defense',
    1
);

INSERT INTO CombinationKeyword (
    id,
    combination_keyword,
    relation_id
) VALUES (
    3,
    'change',
    2
);

INSERT INTO CombinationKeyword (
    id,
    combination_keyword,
    relation_id
) VALUES (
    4,
    'risk',
    2
);

INSERT INTO Country (
    id,
    country_name,
    root_URL,
    list_items_selector,
    news_title_selector,
    metadata_selector,
    date_format,
    number_of_threads
) VALUES (
    1,
    'UK',
    'https://www.gov.uk/search/news-and-communications',
    'ul.gem-c-document-list li.gem-c-document-list__item',
    'a.govuk-link', 
    'time', 
    'yyyy-MM-dd', 
    150
);

INSERT INTO Country (
    id,
    country_name,
    root_URL,
    list_items_selector,
    news_title_selector,
    metadata_selector,
    date_format,
    number_of_threads
) VALUES (
    2,
    'EU',
    'https://european-union.europa.eu/news-and-events/news-and-stories_en',
    '.ecl-row .ecl-content-item-block__item',
    'h1', 
    'time', 
    'yyyy-MM-dd''T''HH:mm:ss''Z''', 
    50
);

INSERT INTO Country (
    id,
    country_name,
    root_URL,
    list_items_selector,
    news_title_selector,
    metadata_selector,
    date_format,
    number_of_threads
) VALUES (
    3,
    'Belgium',
    'https://www.belgium.be/en/news/overview',
    '.view__content .view__row',
    'h3.node__title > a', 
    'time', 
    'yyyy-MM-dd', 
    50
);

INSERT INTO Country (
    id,
    country_name,
    root_URL,
    list_items_selector,
    news_title_selector,
    metadata_selector,
    date_format,
    number_of_threads
) VALUES (
    4,
    'Netherland',
    'https://www.government.nl/latest/news',
    'ol.results li.results__item',
    'h3', 
    'p.meta', 
    'MM-dd-yyyy', 
    50
);

INSERT INTO Country (
    id,
    country_name,
    root_URL,
    list_items_selector,
    news_title_selector,
    metadata_selector,
    date_format,
    number_of_threads
) VALUES (
    5,
    'Germany',
    'https://www.bundesregierung.de/breg-en/news',
    'ol.bpa-search-result-list li.bpa-search-result-full',
    'span.bpa-teaser-title-text-inner', 
    'time', 
    'yyyy-MM-dd''T''HH:mm:ss''Z''', 
    50
);

INSERT INTO Country (
    id,
    country_name,
    root_URL,
    list_items_selector,
    news_title_selector,
    metadata_selector,
    date_format,
    number_of_threads
) VALUES (
    6,
    'France',
    'https://www.gouvernement.fr/toute-l-actualite',
    '.fr-grid-row.fr-grid-row--gutters.list.fr-my-0 .fr-card.fr-enlarge-link.fr-card--horizontal.fr-card--sm',
    'h4', 
    'p.fr-card__detail', 
    'dd/MM/yyyy', 
    150
);

INSERT INTO Country (
    id,
    country_name,
    root_URL,
    list_items_selector,
    news_title_selector,
    news_teaser_selector,
    metadata_selector,
    date_format,
    number_of_threads
) VALUES (
    7,
    'Italy',
    'https://www.governo.it/en/notizie-chigi-en',
    '.row.half_base .box_text.box_text_small.clearfix',
    'p', 
    'h2.h4',
    'div.h6.clearfix.dataleft', 
    'dd MMMM yyyy', 
    100
);

INSERT INTO Country (
    id,
    country_name,
    root_URL,
    list_items_selector,
    news_title_selector,
    news_teaser_selector,
    metadata_selector,
    date_format,
    number_of_threads
) VALUES (
    8,
    'Portugal',
    'https://www.portugal.gov.pt/en/gc23/communication/news',
    '.row .itemList',
    '.gov-texts-list', 
    'a.text:not(:has(*))',
    '.dateItem', 
    'yyyy-MM-dd ''at'' HH''h''mm', 
    100
);

INSERT INTO Country (
    id,
    country_name,
    root_URL,
    list_items_selector,
    news_title_selector,
    news_teaser_selector,
    metadata_selector,
    date_format,
    number_of_threads
) VALUES (
    9,
    'Sweden',
    'https://www.government.se/press-releases/',
    'ul.list--block li:has(div.sortcompact)',
    'div.sortcompact > a', 
    '',
    'time', 
    'yyyy-MM-dd', 
    5
);

INSERT INTO Country (
    id,
    country_name,
    root_URL,
    list_items_selector,
    news_title_selector,
    news_teaser_selector,
    metadata_selector,
    date_format,
    number_of_threads
) VALUES (
    10,
    'Norway',
    'https://www.regjeringen.no/en/whatsnew/news-and-press-releases/id2006120/',
    'ul.listing > li.listItem',
    'h2.title > a', 
    '',
    'span.date', 
    'dd/MM/yyyy', 
    50
);