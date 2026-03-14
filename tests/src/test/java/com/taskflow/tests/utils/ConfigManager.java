package com.taskflow.tests.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// Reads from config.properties. Env vars always take priority so the same
// tests work locally and in GitHub Actions without touching code.
public class ConfigManager {

    private static final Properties properties = new Properties();
    private static ConfigManager instance;

    private ConfigManager() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public String get(String key) {
        String envValue = System.getenv(key.toUpperCase().replace(".", "_"));
        if (envValue != null && !envValue.isBlank()) return envValue;

        String sysValue = System.getProperty(key);
        if (sysValue != null && !sysValue.isBlank()) return sysValue;

        return properties.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        String value = get(key);
        return (value != null) ? value : defaultValue;
    }

    public String getBaseUrl() {
        return get("app.base.url", "http://localhost:8080");
    }

    public String getApiBaseUrl() {
        return getBaseUrl() + "/api";
    }

    public String getBrowser() {
        return get("browser", "chrome");
    }

    public boolean isHeadless() {
        return Boolean.parseBoolean(get("browser.headless", "true"));
    }

    public boolean isRemoteDriver() {
        return Boolean.parseBoolean(get("selenium.remote", "false"));
    }

    public String getSeleniumGridUrl() {
        return get("selenium.grid.url", "http://localhost:4444");
    }

    public int getImplicitWaitSeconds() {
        return Integer.parseInt(get("browser.implicit.wait", "10"));
    }

    public int getExplicitWaitSeconds() {
        return Integer.parseInt(get("browser.explicit.wait", "15"));
    }
}
