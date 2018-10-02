package com.ubb.webscraping.client;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * A static factory for creating web drivers.
 */
public class DriverFactory {
    private static WebDriver driver;

    private static WebDriver createFirefoxWebDriver(){
        return new FirefoxDriver();
    }
     
    private static WebDriver createChromeWebDriver(){
        return new ChromeDriver();
    }

    public static WebDriver getFirefoxWebDriver(){
        if(driver == null){
            driver = createFirefoxWebDriver();
        }
        return driver;
    }
     public static WebDriver getChromeWebDriver(){
        if(driver == null){
            driver = createChromeWebDriver();
        }
        return driver;
    }
}
