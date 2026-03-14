package com.taskflow.tests.base;

import com.taskflow.tests.utils.ApiClient;
import com.taskflow.tests.utils.ConfigManager;
import io.qameta.allure.Step;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all API tests.
 * Provides a shared ApiClient, config, and common setup/teardown hooks.
 */
public abstract class BaseApiTest {

    protected static final Logger log = LoggerFactory.getLogger(BaseApiTest.class);
    protected ApiClient api;
    protected ConfigManager config;

    @BeforeClass(alwaysRun = true)
    public void setUpClass() {
        config = ConfigManager.getInstance();
        api = new ApiClient();
        log.info("API tests targeting: {}", config.getApiBaseUrl());
    }

    @BeforeMethod(alwaysRun = true)
    public void logTestStart(java.lang.reflect.Method method) {
        log.info("▶  Starting test: {}", method.getName());
    }

    @Step("Verify application is healthy")
    protected void verifyAppIsRunning() {
        api.getAllTasks().statusCode(200);
    }
}
