package com.taskflow.tests.ui.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

// Page Object for the dashboard. Tests call methods here instead of
// touching selectors or WebDriver directly.
public class DashboardPage {

    private static final Logger log = LoggerFactory.getLogger(DashboardPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(id = "title")
    private WebElement titleInput;

    @FindBy(id = "status")
    private WebElement statusDropdown;

    @FindBy(id = "priority")
    private WebElement priorityDropdown;

    @FindBy(id = "add-task-btn")
    private WebElement addTaskButton;

    @FindBy(id = "stat-total")
    private WebElement statTotal;

    @FindBy(id = "stat-todo")
    private WebElement statTodo;

    @FindBy(id = "stat-inprogress")
    private WebElement statInProgress;

    @FindBy(id = "stat-done")
    private WebElement statDone;

    @FindBy(id = "tasks-table")
    private WebElement tasksTable;

    @FindBy(css = "#tasks-table tbody tr")
    private List<WebElement> taskRows;

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    @Step("Verify dashboard page is loaded")
    public DashboardPage verifyPageLoaded() {
        wait.until(ExpectedConditions.titleContains("TaskFlow"));
        log.info("Dashboard page loaded: {}", driver.getTitle());
        return this;
    }

    @Step("Enter task title: {title}")
    public DashboardPage enterTitle(String title) {
        wait.until(ExpectedConditions.visibilityOf(titleInput));
        titleInput.clear();
        titleInput.sendKeys(title);
        return this;
    }

    @Step("Select status: {status}")
    public DashboardPage selectStatus(String status) {
        new Select(statusDropdown).selectByValue(status);
        return this;
    }

    @Step("Select priority: {priority}")
    public DashboardPage selectPriority(String priority) {
        new Select(priorityDropdown).selectByValue(priority);
        return this;
    }

    @Step("Click 'Add Task' button")
    public DashboardPage clickAddTask() {
        // Resolve to a real element reference before clicking.
        // PageFactory proxies re-find elements on every access, so stalenessOf()
        // on a proxy never returns true (the proxy just finds the element again
        // on the reloaded page). Using driver.findElement() gives us the actual
        // underlying reference that goes stale after the POST redirect.
        WebElement btn = driver.findElement(By.id("add-task-btn"));
        btn.click();
        wait.until(ExpectedConditions.stalenessOf(btn));
        return new DashboardPage(driver).verifyPageLoaded();
    }

    @Step("Create task with title '{title}', status '{status}', priority '{priority}'")
    public DashboardPage createTask(String title, String status, String priority) {
        return enterTitle(title)
                .selectStatus(status)
                .selectPriority(priority)
                .clickAddTask();
    }

    // ─── Assertions / Getters ────────────────────────────────────────────────

    @Step("Get total task count from stats")
    public int getTotalCount() {
        return Integer.parseInt(statTotal.getText().trim());
    }

    @Step("Get TODO task count from stats")
    public int getTodoCount() {
        return Integer.parseInt(statTodo.getText().trim());
    }

    @Step("Check if task with title '{title}' is visible in the table")
    public boolean isTaskVisible(String title) {
        return taskRows.stream()
                .anyMatch(row -> row.getText().contains(title));
    }

    @Step("Get the status badge text for task '{title}'")
    public String getTaskStatus(String title) {
        return taskRows.stream()
                .filter(row -> row.getText().contains(title))
                .findFirst()
                .map(row -> row.findElement(By.cssSelector(".badge-TODO, .badge-IN_PROGRESS, .badge-DONE, .badge-CANCELLED")).getText())
                .orElseThrow(() -> new NoSuchElementException("Task not found: " + title));
    }

    @Step("Get number of rows in task table")
    public int getTaskRowCount() {
        try {
            wait.until(ExpectedConditions.visibilityOf(tasksTable));
            return taskRows.size();
        } catch (TimeoutException e) {
            return 0; // Table not present = 0 tasks
        }
    }

    @Step("Delete the first task matching title '{title}'")
    public DashboardPage deleteTask(String title) {
        WebElement deleteButton = taskRows.stream()
                .filter(row -> row.getText().contains(title))
                .findFirst()
                .map(row -> row.findElement(By.cssSelector(".btn-delete")))
                .orElseThrow(() -> new NoSuchElementException("Task not found: " + title));

        deleteButton.click();
        wait.until(ExpectedConditions.stalenessOf(deleteButton));
        return new DashboardPage(driver).verifyPageLoaded();
    }

    @Step("Get page title")
    public String getPageTitle() {
        return driver.getTitle();
    }
}
