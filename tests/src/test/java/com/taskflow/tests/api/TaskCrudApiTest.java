package com.taskflow.tests.api;

import com.taskflow.tests.base.BaseApiTest;
import com.taskflow.tests.models.TaskRequest;
import com.taskflow.tests.models.TaskResponse;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * CRUD API test suite for /api/tasks.
 * Covers: Create, Read, Update, Delete with positive and negative scenarios.
 */
@Epic("Task Management API")
@Feature("Task CRUD Operations")
public class TaskCrudApiTest extends BaseApiTest {

    // ═══════════════════════════════════════════════════════════════════════
    // CREATE TESTS
    // ═══════════════════════════════════════════════════════════════════════

    @Test(description = "Create a task with all valid fields")
    @Story("Create Task")
    @Severity(SeverityLevel.BLOCKER)
    public void createTask_withValidData_returns201AndTask() {
        TaskRequest request = TaskRequest.builder()
                .title("Automate regression suite")
                .description("Set up full regression using Selenium Grid")
                .status("TODO")
                .priority("HIGH")
                .build();

        TaskResponse response = api.createTask(request)
                .statusCode(201)
                .body("id", notNullValue())
                .body("title", equalTo("Automate regression suite"))
                .body("status", equalTo("TODO"))
                .body("priority", equalTo("HIGH"))
                .body("createdAt", notNullValue())
                .extract().as(TaskResponse.class);

        assertThat(response.getId()).isPositive();
        assertThat(response.getTitle()).isEqualTo("Automate regression suite");
    }

    @Test(description = "Create task with only required fields (title, status, priority)")
    @Story("Create Task")
    @Severity(SeverityLevel.CRITICAL)
    public void createTask_withMinimalFields_returns201() {
        TaskRequest request = TaskRequest.validTask("Minimal task");

        api.createTask(request)
                .statusCode(201)
                .body("title", equalTo("Minimal task"))
                .body("id", greaterThan(0));
    }

    @Test(description = "Create task with a blank title returns 400")
    @Story("Create Task - Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void createTask_withBlankTitle_returns400() {
        TaskRequest request = TaskRequest.builder()
                .title("")
                .status("TODO")
                .priority("MEDIUM")
                .build();

        api.createTask(request).statusCode(400);
    }

    @Test(description = "Create task with null title returns 400")
    @Story("Create Task - Validation")
    @Severity(SeverityLevel.NORMAL)
    public void createTask_withNullTitle_returns400() {
        TaskRequest request = TaskRequest.builder()
                .status("TODO")
                .priority("MEDIUM")
                .build();

        api.createTask(request).statusCode(400);
    }

    @Test(description = "Create task with title exceeding 200 characters returns 400")
    @Story("Create Task - Validation")
    @Severity(SeverityLevel.NORMAL)
    public void createTask_withTitleOver200Chars_returns400() {
        TaskRequest request = TaskRequest.builder()
                .title("A".repeat(201))
                .status("TODO")
                .priority("MEDIUM")
                .build();

        api.createTask(request).statusCode(400);
    }

    @Test(description = "Create task with invalid status returns 400")
    @Story("Create Task - Validation")
    @Severity(SeverityLevel.NORMAL)
    public void createTask_withInvalidStatus_returns400() {
        TaskRequest request = TaskRequest.builder()
                .title("Task with bad status")
                .status("INVALID_STATUS")
                .priority("MEDIUM")
                .build();

        api.createTask(request).statusCode(400);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // READ TESTS
    // ═══════════════════════════════════════════════════════════════════════

    @Test(description = "GET /tasks returns HTTP 200 and a JSON array")
    @Story("Read Tasks")
    @Severity(SeverityLevel.BLOCKER)
    public void getAllTasks_returnsOkAndArray() {
        api.getAllTasks()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test(description = "GET /tasks/{id} returns a single task when it exists")
    @Story("Read Tasks")
    @Severity(SeverityLevel.CRITICAL)
    public void getTaskById_whenExists_returnsTask() {
        long id = api.createAndGetId(TaskRequest.validTask("Fetch by ID test"));

        api.getTaskById(id)
                .statusCode(200)
                .body("id", equalTo((int) id))
                .body("title", equalTo("Fetch by ID test"));
    }

    @Test(description = "GET /tasks/{id} returns 404 for non-existent task")
    @Story("Read Tasks")
    @Severity(SeverityLevel.CRITICAL)
    public void getTaskById_whenNotExists_returns404() {
        api.getTaskById(999999L).statusCode(404);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UPDATE TESTS
    // ═══════════════════════════════════════════════════════════════════════

    @Test(description = "PUT /tasks/{id} updates all fields successfully")
    @Story("Update Task")
    @Severity(SeverityLevel.CRITICAL)
    public void updateTask_withValidData_returns200WithUpdatedFields() {
        long id = api.createAndGetId(TaskRequest.validTask("Original title"));

        TaskRequest updated = TaskRequest.builder()
                .title("Updated title")
                .description("Updated description")
                .status("IN_PROGRESS")
                .priority("HIGH")
                .build();

        api.updateTask(id, updated)
                .statusCode(200)
                .body("title", equalTo("Updated title"))
                .body("status", equalTo("IN_PROGRESS"))
                .body("priority", equalTo("HIGH"));
    }

    @Test(description = "PATCH /tasks/{id}/status updates only the status")
    @Story("Update Task")
    @Severity(SeverityLevel.NORMAL)
    public void patchTaskStatus_toInProgress_updatesStatusOnly() {
        long id = api.createAndGetId(TaskRequest.validTask("Status patch test"));

        api.patchTaskStatus(id, "IN_PROGRESS")
                .statusCode(200)
                .body("status", equalTo("IN_PROGRESS"))
                .body("title", equalTo("Status patch test")); // title unchanged
    }

    @Test(description = "PATCH /tasks/{id}/status with invalid value returns 400")
    @Story("Update Task - Validation")
    @Severity(SeverityLevel.NORMAL)
    public void patchTaskStatus_withInvalidStatus_returns400() {
        long id = api.createAndGetId(TaskRequest.validTask("Invalid status patch"));
        api.patchTaskStatus(id, "NOT_A_STATUS").statusCode(400);
    }

    @Test(description = "PUT /tasks/{id} returns 404 for non-existent task")
    @Story("Update Task")
    @Severity(SeverityLevel.NORMAL)
    public void updateTask_whenNotExists_returns404() {
        api.updateTask(999999L, TaskRequest.validTask("Ghost task")).statusCode(404);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DELETE TESTS
    // ═══════════════════════════════════════════════════════════════════════

    @Test(description = "DELETE /tasks/{id} removes the task and returns 204")
    @Story("Delete Task")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteTask_whenExists_returns204AndTaskIsGone() {
        long id = api.createAndGetId(TaskRequest.validTask("Task to delete"));

        api.deleteTask(id).statusCode(204);

        // Verify it's actually gone
        api.getTaskById(id).statusCode(404);
    }

    @Test(description = "DELETE /tasks/{id} returns 404 for non-existent task")
    @Story("Delete Task")
    @Severity(SeverityLevel.NORMAL)
    public void deleteTask_whenNotExists_returns404() {
        api.deleteTask(999999L).statusCode(404);
    }
}
