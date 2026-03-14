package com.taskflow.tests.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Duration;

// WebDriver factory using ThreadLocal so parallel tests don't share a browser.
// Supports local Chrome/Firefox and remote Selenium Grid.
public class DriverManager {

    private static final Logger log = LoggerFactory.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ConfigManager config = ConfigManager.getInstance();

    private DriverManager() {}

    public static WebDriver getDriver() {
        if (driverThreadLocal.get() == null) {
            driverThreadLocal.set(createDriver());
            configureDriver(driverThreadLocal.get());
        }
        return driverThreadLocal.get();
    }

    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                log.info("WebDriver quit successfully");
            } catch (Exception e) {
                log.warn("Error quitting WebDriver: {}", e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    private static WebDriver createDriver() {
        String browser = config.getBrowser().toLowerCase();
        boolean isRemote = config.isRemoteDriver();
        boolean isHeadless = config.isHeadless();

        log.info("Creating {} driver | remote={} | headless={}", browser, isRemote, isHeadless);

        try {
            if (isRemote) {
                return createRemoteDriver(browser, isHeadless);
            }
            return switch (browser) {
                case "firefox" -> createFirefoxDriver(isHeadless);
                default -> createChromeDriver(isHeadless);
            };
        } catch (Exception e) {
            throw new RuntimeException("Failed to create WebDriver: " + e.getMessage(), e);
        }
    }

    private static WebDriver createChromeDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=1920,1080",
                "--disable-extensions",
                "--disable-infobars"
        );
        return new ChromeDriver(options);
    }

    private static WebDriver createFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("--headless");
        }
        return new FirefoxDriver(options);
    }

    private static WebDriver createRemoteDriver(String browser, boolean headless) throws Exception {
        String gridUrl = config.getSeleniumGridUrl() + "/wd/hub";
        log.info("Connecting to Selenium Grid at: {}", gridUrl);

        return switch (browser) {
            case "firefox" -> {
                FirefoxOptions opts = new FirefoxOptions();
                if (headless) opts.addArguments("--headless");
                yield new RemoteWebDriver(new URL(gridUrl), opts);
            }
            default -> {
                ChromeOptions opts = new ChromeOptions();
                if (headless) opts.addArguments("--headless=new");
                opts.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--window-size=1920,1080");
                yield new RemoteWebDriver(new URL(gridUrl), opts);
            }
        };
    }

    private static void configureDriver(WebDriver driver) {
        driver.manage().timeouts()
                .implicitlyWait(Duration.ofSeconds(config.getImplicitWaitSeconds()))
                .pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().window().maximize();
    }
}
