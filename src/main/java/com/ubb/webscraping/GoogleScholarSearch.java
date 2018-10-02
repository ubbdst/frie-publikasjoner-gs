package com.ubb.webscraping;

import com.ubb.webscraping.client.DriverFactory;
import com.ubb.webscraping.settings.Settings;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.ubb.webscraping.settings.Settings.LOCAL_COUNT;

/**
 * @author Hemed Ali, Øyvind
 * University of Bergen Library
 * <p>
 * The application needs to be run outside the UiB campus to be able to know whether the
 * articles are open accessible to all.
 */
public class GoogleScholarSearch {
    private static Logger logger = Logger.getLogger(GoogleScholarSearch.class.getName());

    public static void main(String[] args) throws IOException {
        //Using driver for firefox v45.0
        //For Mac, at home
        //System.setProperty(FirefoxDriver.SystemProperty.BROWSER_BINARY, "/usr/bin/firefox");
        //For windows, at work
        //System.setProperty(FirefoxDriver.SystemProperty.BROWSER_BINARY, "E:\\Firefox\\firefox.exe");
        //System.setProperty(FirefoxDriver.SystemProperty.BROWSER_BINARY, "C:\\Program Files\\Mozilla Firefox\\firefox.exe");
        //Download gecko from https://github.com/mozilla/geckodriver/releases/ and edit property
        //System.setProperty("webdriver.gecko.driver", "gecko\\geckodriver.exe");

        String absolutePath = new File("").getAbsolutePath() + File.separator;
        String webdriverPath = absolutePath + "lib";
        String chromeDriverProerty = "webdriver.chrome.driver";

        if (Settings.isWindows()) {
            Settings.setSystemProperty(chromeDriverProerty, webdriverPath + File.separator + "chromedriver.exe");

        } else {//Use Mac settings by default
            Settings.setSystemProperty(chromeDriverProerty, webdriverPath + File.separator + "chromedriver");
        }


        logger.info("Using driver path : " + webdriverPath);

        WebDriver driver = DriverFactory.getChromeWebDriver();
        driver.manage().timeouts().setScriptTimeout(5, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
        
        Workbook workbook = new HSSFWorkbook();
        Sheet workbookSheet = workbook.createSheet(Settings.OUTPUT_FILE_NAME_PREFIX);

        //Read the input file from data folder
        //BufferedReader reader = Files.newBufferedReader(Paths.get("data/input.txt"), StandardCharsets.ISO_8859_1);
        BufferedReader reader = new BufferedReader(new FileReader("data/input.txt"));
        //Prepare output file

        Boolean dir = new File(Settings.OUTPUT_DIR_NAME).mkdir();
        if (dir) {
            logger.info("Created output directory : " + absolutePath +  Settings.OUTPUT_DIR_NAME);
        }
        FileOutputStream fileOut = new FileOutputStream(Settings.OUTPUT_DIR_NAME + File.separator + Settings.getOutputFileName() + ".xls");

        try {
            String line;
            //Iterate through a file, line by line. Input file must be tab separated and
            //in the first column, should be DOI, second column, WOS ID and third column, WOS title respectively.
            driver.get(Settings.GS_SEARCH_URL);
            while ((line = reader.readLine()) != null) {

                //If we meet an empty line, skip it.
                if (line.isEmpty() || line.trim().equals("") || line.trim().equals("\n")) {
                    System.out.println("Skipping empty line...");
                    continue;
                }

                int randomNumber = (int) (Math.random() * 1100 + 1000);
                Thread.sleep(500 + randomNumber);

                boolean isOpenAccess = true;
                String formatAndDomain = "";
                String gsTitle;
                String gsAuthor;
                String citedBy;
                String comment = "";
                String doi = "", wosId = "", wosTitle = "";

                try {
                    //DOI are in the first column
                    doi = line.split("\\t", -1)[0];

                    //WOS Unique ID is in second colum
                    wosId = line.split("\\t", -1)[1];

                    //Titles are in the second colum
                    wosTitle = line.split("\\t", -1)[2];
                } catch (ArrayIndexOutOfBoundsException ex) {
                    //This means the input line is malformed. Either it doesn't contain tabs or there is
                    // another reason. Must be manually checked.
                    logger.warning("Cannot read input for line + [ " + line + " ]");
                    comment = "Cannot process input line: [ " + line + "]";
                }

                String searchByDOI = Settings.DOI_PREFIX + doi;
                driver.findElement(By.xpath("//input[@aria-label='Søk']")).clear();

                //Search using DOI if it exists
                if (!doi.isEmpty()) {
                    findAndClick(driver, searchByDOI, randomNumber);
                } else {//Else, search using title.
                    findAndClick(driver, wosTitle, randomNumber);
                    comment = "No DOI. Searched by title. ";
                }
                //Wait for at most one day if Capcha appears.
                //This is because someone will have enough time to deal with it.
              WebDriverWait wait = new WebDriverWait(driver, 60000);
              wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='gs_res_ccl']")));
                
                /*
                 If we didn't search using title and the result is "Sorry, no info on this DOI",
                 then try searching again using title. Susanne addressed this issue
                 */
                boolean isBeklagerFound = driver.findElements(By
                        .xpath("//p[@class='gs_med' and (contains(.,'Sorry') or (contains(.,'Beklager')))]"))
                        .size() > 0;

                if (isBeklagerFound && !doi.isEmpty()) {
                    comment = "DOI gave no results. Searched by title";
                    findAndClick(driver, wosTitle, randomNumber);
                }

                ////Xpath for lenkeliste div[@class=\"gs_fl\"][1]
                //The result is in the form of e.g "Cited by 10"

                citedBy = getFirstMatch(driver, "//div[@class=\"gs_fl\"][1]/a[contains(@href,'?cites=')]");

                gsTitle = getFirstMatch(driver, "//div[@class='gs_ri'][1]/h3[@class='gs_rt']/a[1]");

                //If the author name is a link, will this fail? - Needs some experimentation
                gsAuthor = getFirstMatch(driver, "//div[@class='gs_ri'][1]/div[@class='gs_a']");

                //The result is in the form of e.g "[PDF] researchgate.com"
                formatAndDomain = getFirstMatch(driver, "//div[@class='gs_ggsd'][1]/div[1]/a");

                //For every line, create it's own row.
                Row workbookSheetRow = workbookSheet.createRow(LOCAL_COUNT++);

                String numberCited = citedBy.startsWith("Sitert av") || citedBy.startsWith("Cited by") ?
                        citedBy.split(" ")[2] : "0";

                String format = formatAndDomain.isEmpty() || formatAndDomain.startsWith("Fulltext") ?
                        null : formatAndDomain.split(" ")[0];

                String domain = formatAndDomain.isEmpty() || formatAndDomain.startsWith("Fulltext") ?
                        null : formatAndDomain.split(" ")[1];

                if (formatAndDomain.isEmpty() || formatAndDomain.startsWith("Fulltext")) {
                    isOpenAccess = false;
                }

                //Remove square brackets around format.
                if (format != null)
                    format = format.substring(1, format.length() - 1);

                //Append DOI in the first cell
                workbookSheetRow.createCell(0).setCellValue(doi);
                //Append GS authors in the second cell
                workbookSheetRow.createCell(1).setCellValue(wosId);
                //Append WS title in the first cell
                workbookSheetRow.createCell(2).setCellValue(wosTitle);
                //Append GS title in the second cell
                workbookSheetRow.createCell(3).setCellValue(gsTitle);
                //Append GS authors in the second cell
                workbookSheetRow.createCell(4).setCellValue(gsAuthor);
                //Append number of times it was cited.
                workbookSheetRow.createCell(5).setCellValue(Integer.parseInt(numberCited));
                //Append publishing format
                workbookSheetRow.createCell(6).setCellValue(format);
                //Append publishing domain
                workbookSheetRow.createCell(7).setCellValue(domain);
                //Append true/false based on whether it is Open Access
                workbookSheetRow.createCell(8).setCellValue(isOpenAccess);
                //Add comments
                workbookSheetRow.createCell(9).setCellValue(comment);
                //Put date.
                workbookSheetRow.createCell(10).setCellValue(Settings.getCurrentDate("yyyy-MM-dd HH:mm"));

                //Log what has been queried
                System.out.println(Settings.LOCAL_COUNT + " DOI: " + doi );
            }
        } catch (Exception ex) {
            //Close file steams even if exception has occurred.
         
           logger.severe("Exception occurred. Details [" + ex.getLocalizedMessage() + "]");
            ex.printStackTrace();
            closeFile(workbook, fileOut);

            if(ex instanceof NoSuchWindowException || ex instanceof WebDriverException) {
                logger.severe("\nUnavailable Browser Window. Exiting the application ... ");
                System.exit(1);
            }
        }
        
        closeFile( workbook, fileOut);
      
        System.out.println("\n Reached end of the file. All data have been processed!");
        driver.quit();
    }

    private static void closeFile(Workbook workbook, FileOutputStream fileOut) throws IOException {
            workbook.write(fileOut);
            fileOut.close();
}
    /**
     * Send search query and then click the search box.
     **/
    private static void findAndClick(WebDriver driver, String queryString, int randomNo) throws InterruptedException {
        try {
            //Clear input field after search.
            driver.findElement(By.xpath("//input[@id=\"gs_hdr_tsi\"]")).clear();
            driver.findElement(By.xpath("//input[@aria-label='Søk']")).sendKeys(queryString);
            Thread.sleep(randomNo);
            driver.findElement(By.xpath("//button[@id=\"gs_hdr_tsb\"]")).click();
        } catch (Exception e) {
            logger.severe(e.getLocalizedMessage());
            e.printStackTrace();
        }

    }

    /**
     * Check if this element exists
     */
    private static String getFirstMatch(WebDriver driver, String xpath) {
        List<WebElement> list = driver.findElements(By.xpath(xpath));
        if (list.size() > 0) {
            return list.get(0).getText();
        }
        return "";
    }


}
