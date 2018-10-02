# Set up Google scholar search


## Description

A utility program to retrieve data from google scholar into excel stylesheets using Selenium WebDriver, to discover Open Access status.
The program first searches google for the doi as an uri, and then falls back to using title if no results are found.

Google Scholar links directly to paid resources if a "campus" ip is recognized, so run on an off-campus network.

When CAPTCHAS are met, the user takes control over the Browser while the application waits for completion.

If the browser is closed, or an error is met, the excel file is closed, and can be found in the output folder.

Retrieved results are:

```
DOI, LOCAL_ID, LOCAL_TITLE, GS_TITLE, GS_AUTHOR, GS_NUM_OF_CITATIONS, GS_LINK_FORMAT (pdf, html), GS_LINK_DOMAIN, LOCAL_COMMENT (from application; not found),GS_OPEN_ACCESS, DATESTAMP
```

## Install and run

## Dependencies
* Java
* Chrome (release contains chromedriver and is configured for chrome only)
* ChromeDriver
* Maven to build

## Run 

Make sure java is installed. Download OS specific binary from http://chromedriver.chromium.org/downloads (Download page lists supported version of Chrome as well). Unpack driver and release and place chromedriver binary in unzipped `lib` folder.
Releases are not OS specific (has been tested with IOS and Windows), but the chromedrivers are.

Create an input file, and place in data/input.txt
`input.txt` is a tab seperated format of DOI, LOCAL_ID (Cristin ID for us) and Title which is used to drive the query. The file is expected to be saved as UTF-8. An example file is found at
[input.txt](data/input.txt)

### To build:
```
git clone git@github.com:ubbdst/frie-publikasjoner-gs.git
mvn clean install
```

Or Open as maven-project in IDE of choice.

## Issues

The script runs as of 2018-10-02, but has been broken earlier by changes to google scholars web page structure. If that happens, look at updating the xpath in GoogleScholarSearch.java.

Sometimes if the web-page hangs on first page the google chromedriver and Chrome are out of sync. See http://chromedriver.chromium.org/downloads to check if versions are compatible.
