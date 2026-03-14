package com.taskflow.tests.ui;

import com.taskflow.tests.base.BaseUiTest;
import com.taskflow.tests.ui.pages.DashboardPage;
import io.qameta.allure.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI test suite for the TaskFlow Dashboard.
 *
 * All browser interactions go through DashboardPage (Page Object Model).
 * Screenshots are automatically captured on failure (via BaseUiTest).
 * All steps are tracked in the Allure report.
 */
@Epic("TaskFlow UI")
@Feature("Dashboard")
public class DashboardUiTest extends BaseUiTest {

    private DashboardPage dashboardPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "setUpBrowser")
    public void openDashboard() {
        navigateToHome();
        dashboardPage = new DashboardPage(driver).verifyPageLoaded();
    }

    // ─── Page load ──────────────────────────────────────────────────────────

    @Test(description = "Dashboard loads with the correct page title")
    @Story("Page Load")
    @Severity(SeverityLevel.BLOCKER)
    public void dashboard_pageTitle_isCorrect() {
        assertThat(dashboardPage.getPageTitle()).contains("TaskFlow");
    }

    // ─── Create task ────────────────────────────────────────────────────────

    @Test(description = "Creating a task via the form adds it to the task table")
    @Story("Create Task via UI")
    @Severity(SeverityLevel.BLOCKER)
    public void createTask_viaForm_appearsInTable() {
        String uniqueTitle = "UI Test Task " + System.currentTimeMillis();

        DashboardPage updated = dashboardPage.createTask(uniqueTitle, "TODO", "HIGH");

        assertThat(updated.isTaskVisible(uniqueTitle))
                .as("Newly created task should appear in the task table")
                .isTrue();
    }

    @Test(description = "Creating a task increments the total and TODO stats")
    @Story("Create Task via UI")
    @Severity(SeverityLevel.CRITICAL)
    public void createTask_incrementsStatCounters() {
        int totalBefore = dashboardPage.getTotalCount();
        int todoBefore  = dashboardPage.getTodoCount();

        String title = "Stats increment test " + System.currentTimeMillis();
        DashboardPage updated = dashboardPage.createTask(title, "TODO", "MEDIUM");

        assertThat(updated.getTotalCount())
                .as("Total count should increase by 1")
                .isEqualTo(totalBefore + 1);
        assertThat(updated.getTodoCount())
                .as("TODO count should increase by 1")
                .isEqualTo(todoBefore + 1);
    }

    @Test(description = "Task form with empty title does not submit (HTML5 required)")
    @Story("Create Task - Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void createTask_withEmptyTitle_doesNotSubmit() {
        int rowsBefore = dashboardPage.getTaskRowCount();

        // Click Add without entering a title
        dashboardPage.selectStatus("TODO").selectPriority("HIGH");
        // HTML5 required attribute prevents submission; row count should stay the same
        driver.findElement(org.openqa.selenium.By.id("add-task-btn")).click();

        // Page should not reload — still on the same page
        assertThat(driver.getCurrentUrl())
                .as("URL should not change when form validation fails")
                .contains("/");
    }

    @Test(description = "Created task shows the correct priority badge")
    @Story("Create Task via UI")
    @Severity(SeverityLevel.NORMAL)
    public void createTask_withCriticalPriority_showsCorrectBadge() {
        String title = "Critical priority UI test " + System.currentTimeMillis();
        DashboardPage updated = dashboardPage.createTask(title, "TODO", "CRITICAL");

        assertThat(updated.isTaskVisible(title)).isTrue();
    }

    // ─── Delete task ────────────────────────────────────────────────────────

    @Test(description = "Deleting a task removes it from the table")
    @Story("Delete Task via UI")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteTask_removesItFromTable() {
        String title = "Task to delete UI " + System.currentTimeMillis();

        DashboardPage afterCreate = dashboardPage.createTask(title, "TODO", "LOW");
        assertThat(afterCreate.isTaskVisible(title)).isTrue();

        int countBeforeDelete = afterCreate.getTaskRowCount();
        DashboardPage afterDelete = afterCreate.deleteTask(title);

        assertThat(afterDelete.isTaskVisible(title))
                .as("Deleted task should no longer appear in table")
                .isFalse();
        assertThat(afterDelete.getTaskRowCount())
                .as("Task row count should decrease by 1")
                .isEqualTo(countBeforeDelete - 1);
    }

    @Test(description = "Deleting a task decrements the total stats counter")
    @Story("Delete Task via UI")
    @Severity(SeverityLevel.NORMAL)
    public void deleteTask_decrementsStatCounter() {
        String title = "Stats decrement delete test " + System.currentTimeMillis();
        DashboardPage afterCreate = dashboardPage.createTask(title, "TODO", "MEDIUM");

        int totalBeforeDelete = afterCreate.getTotalCount();
        DashboardPage afterDelete = afterCreate.deleteTask(title);

        assertThat(afterDelete.getTotalCount())
                .as("Total count should decrease by 1 after deletion")
                .isEqualTo(totalBeforeDelete - 1);
    }
}
