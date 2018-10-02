package com.ubb.webscraping.settings;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Hemed
 */
public class Settings {
    
 
    public static final int TIMEOUT_MILLIS = 500000;
    public static int LOCAL_COUNT = 0;
    public static String DOI_PREFIX = "http://dx.doi.org/";
    public static String GS_SEARCH_URL = "https://scholar.google.no/?hl=no";
    public static String GS_TEST_URL = "https://scholar.google.no/scholar?hl=en&q=The%20Scientific%20Basis.%20Intergovernmental%20Panel%20on%20Climate%20Change";
    public static final String OUTPUT_DIR_NAME = "output";
    public static final String OUTPUT_FILE_NAME_PREFIX = "Frie_Publikasjoner";

    //Generate todays date
    public static String getCurrentDate(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        return formatter.format(new Date());
    }

    //Generate todays date
    public static String getCurrentDate(String format){
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(new Date());
    }

    public static String getOutputFileName() {
        return OUTPUT_FILE_NAME_PREFIX + "_" + getCurrentDate();
    }


    /**
     * Check if the Operating system running this application is Windows
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    /**
     * Set system property if it does not exist
     * @param key a property key
     * @param value a property value
     */
    public static void setSystemProperty(String key, String value) {
        if(System.getProperty(key) == null) {
            System.setProperty(key, value);
        }

    }

}
