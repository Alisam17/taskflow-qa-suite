package com.taskflow.tests.utils;

import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

/**
 * Utility for capturing and attaching screenshots to Allure reports.
 * Called automatically on test failures via the base UI test class.
 */
public class ScreenshotUtil {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotUtil.class);

    private ScreenshotUtil() {}

    /**
     * Captures a screenshot and attaches it directly to the current Allure step.
     *
     * @param driver  the WebDriver instance
     * @param name    the attachment name shown in the Allure report
     */
    public static void captureAndAttach(WebDriver driver, String name) {
        if (driver == null) {
            log.warn("Cannot capture screenshot: WebDriver is null");
            return;
        }
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshot), ".png");
            log.info("Screenshot captured: {}", name);
        } catch (Exception e) {
            log.error("Failed to capture screenshot: {}", e.getMessage());
        }
    }

    public static void captureOnFailure(WebDriver driver, String testName) {
        captureAndAttach(driver, "FAILURE - " + testName);
    }
}
