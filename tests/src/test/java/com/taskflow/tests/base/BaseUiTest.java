package com.taskflow.tests.base;

import com.taskflow.tests.utils.ConfigManager;
import com.taskflow.tests.utils.DriverManager;
import com.taskflow.tests.utils.ScreenshotUtil;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Base class for all UI (Selenium) tests.
 * Handles driver lifecycle, screenshot-on-failure, and navigation helpers.
 */
public abstract class BaseUiTest {

    protected static final Logger log = LoggerFactory.getLogger(BaseUiTest.class);
    protected WebDriver driver;
    protected ConfigManager config;

    @BeforeMethod(alwaysRun = true)
    public void setUpBrowser(java.lang.reflect.Method method) {
        config = ConfigManager.getInstance();
        driver = DriverManager.getDriver();
        log.info("▶  UI Test: {} | Browser: {} | Headless: {}",
                method.getName(), config.getBrowser(), config.isHeadless());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownBrowser(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            ScreenshotUtil.captureOnFailure(driver, result.getName());
            log.error("✗  Test FAILED: {}", result.getName());
        } else {
            log.info("✓  Test PASSED: {}", result.getName());
        }
        DriverManager.quitDriver();
    }

    @Step("Navigate to: {url}")
    protected void navigateTo(String url) {
        driver.get(url);
        log.info("Navigated to: {}", url);
    }

    protected void navigateToHome() {
        navigateTo(config.getBaseUrl() + "/");
    }
}
