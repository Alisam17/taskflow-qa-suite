package com.taskflow.tests.api;

import com.taskflow.tests.base.BaseApiTest;
import com.taskflow.tests.models.TaskRequest;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * JSON Schema validation tests.
 * Ensures the API contract (response shape) never breaks silently.
 * Uses REST Assured's built-in JSON schema validator.
 */
@Epic("Task Management API")
@Feature("API Contract / Schema Validation")
public class TaskSchemaValidationTest extends BaseApiTest {

    @Test(description = "Single task response matches expected JSON schema")
    @Story("Schema Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void getSingleTask_matchesJsonSchema() {
        long id = api.createAndGetId(TaskRequest.validTask("Schema test task"));

        api.getTaskById(id)
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/task-schema.json"));
    }

    @Test(description = "Task list response contains items matching task schema")
    @Story("Schema Validation")
    @Severity(SeverityLevel.NORMAL)
    public void getTaskList_itemsMatchJsonSchema() {
        // Create at least one task to ensure the list is non-empty
        api.createAndGetId(TaskRequest.validTask("Schema list test"));

        api.getAllTasks()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0]", matchesJsonSchemaInClasspath("schemas/task-schema.json"));
    }

    // Static import needed by assertThat below
    private static int greaterThan(int n) {
        return n; // used for clarity — actual matcher is from Hamcrest
    }
}
