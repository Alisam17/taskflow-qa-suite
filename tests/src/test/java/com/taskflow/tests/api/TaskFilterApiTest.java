package com.taskflow.tests.api;

import com.taskflow.tests.base.BaseApiTest;
import com.taskflow.tests.models.TaskRequest;
import io.qameta.allure.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.*;

/**
 * Data-driven filter and search test suite.
 * Demonstrates: DataProvider pattern, query parameter testing, search functionality.
 */
@Epic("Task Management API")
@Feature("Task Filtering and Search")
public class TaskFilterApiTest extends BaseApiTest {

    @BeforeClass(alwaysRun = true)
    @Override
    public void setUpClass() {
        super.setUpClass();
        seedTestData();
    }

    private void seedTestData() {
        // Seed known tasks so filter assertions are reliable
        api.createTask(TaskRequest.builder().title("Filter test TODO Low").status("TODO").priority("LOW").build());
        api.createTask(TaskRequest.builder().title("Filter test TODO High").status("TODO").priority("HIGH").build());
        api.createTask(TaskRequest.builder().title("Filter test IN_PROGRESS High").status("IN_PROGRESS").priority("HIGH").build());
        api.createTask(TaskRequest.builder().title("Filter test DONE Critical").status("DONE").priority("CRITICAL").build());
        api.createTask(TaskRequest.builder().title("Searchable unique xyz987").status("TODO").priority("MEDIUM").build());
    }

    // ─── Status filter ───────────────────────────────────────────────────────

    @DataProvider(name = "validStatuses")
    public Object[][] validStatuses() {
        return new Object[][] {
                {"TODO"},
                {"IN_PROGRESS"},
                {"DONE"},
                {"CANCELLED"}
        };
    }

    @Test(dataProvider = "validStatuses",
          description = "Filter by valid status returns only tasks with that status")
    @Story("Filter by Status")
    @Severity(SeverityLevel.CRITICAL)
    public void filterByStatus_returnsOnlyMatchingTasks(String status) {
        api.getTasksByStatus(status)
                .statusCode(200)
                .body("$", everyItem(hasEntry("status", status)));
    }

    // ─── Priority filter ─────────────────────────────────────────────────────

    @DataProvider(name = "validPriorities")
    public Object[][] validPriorities() {
        return new Object[][] {
                {"LOW"},
                {"MEDIUM"},
                {"HIGH"},
                {"CRITICAL"}
        };
    }

    @Test(dataProvider = "validPriorities",
          description = "Filter by valid priority returns only tasks with that priority")
    @Story("Filter by Priority")
    @Severity(SeverityLevel.CRITICAL)
    public void filterByPriority_returnsOnlyMatchingTasks(String priority) {
        api.getTasksByPriority(priority)
                .statusCode(200)
                .body("$", everyItem(hasEntry("priority", priority)));
    }

    // ─── Search ──────────────────────────────────────────────────────────────

    @Test(description = "Search by existing keyword returns matching tasks")
    @Story("Search Tasks")
    @Severity(SeverityLevel.NORMAL)
    public void searchTasks_byExistingKeyword_returnsMatches() {
        api.searchTasks("xyz987")
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("[0].title", containsStringIgnoringCase("xyz987"));
    }

    @Test(description = "Search is case-insensitive")
    @Story("Search Tasks")
    @Severity(SeverityLevel.NORMAL)
    public void searchTasks_caseInsensitive_returnsMatches() {
        api.searchTasks("XYZ987")
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test(description = "Search with no matches returns empty array (not 404)")
    @Story("Search Tasks")
    @Severity(SeverityLevel.NORMAL)
    public void searchTasks_noMatches_returnsEmptyArray() {
        api.searchTasks("zzz_nonexistent_keyword_zzz")
                .statusCode(200)
                .body("$", empty());
    }

    // ─── Stats ───────────────────────────────────────────────────────────────

    @Test(description = "GET /tasks/stats returns all expected counters")
    @Story("Task Statistics")
    @Severity(SeverityLevel.NORMAL)
    public void getStats_returnsAllCounterKeys() {
        api.getTaskStats()
                .statusCode(200)
                .body("total", greaterThanOrEqualTo(0))
                .body("todo", greaterThanOrEqualTo(0))
                .body("inProgress", greaterThanOrEqualTo(0))
                .body("done", greaterThanOrEqualTo(0))
                .body("cancelled", greaterThanOrEqualTo(0));
    }

    @Test(description = "Stats total equals sum of individual status counts")
    @Story("Task Statistics")
    @Severity(SeverityLevel.NORMAL)
    public void getStats_totalEqualsSum() {
        var stats = api.getTaskStats()
                .statusCode(200)
                .extract().jsonPath();

        int total = stats.getInt("total");
        int todo = stats.getInt("todo");
        int inProgress = stats.getInt("inProgress");
        int done = stats.getInt("done");
        int cancelled = stats.getInt("cancelled");

        org.assertj.core.api.Assertions.assertThat(total)
                .as("Stats total should equal sum of all statuses")
                .isEqualTo(todo + inProgress + done + cancelled);
    }
}
