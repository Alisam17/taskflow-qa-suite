package com.taskflow.tests.api;

import com.taskflow.tests.base.BaseApiTest;
import com.taskflow.tests.models.TaskRequest;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.greaterThan;

// Validates that the API response shape matches our JSON schema contract.
// If a field gets renamed or dropped, these tests catch it before it hits production.
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

    @Test(description = "Task list response matches the expected array schema")
    @Story("Schema Validation")
    @Severity(SeverityLevel.NORMAL)
    public void getTaskList_itemsMatchJsonSchema() {
        api.createAndGetId(TaskRequest.validTask("Schema list test"));

        // Validate the full response body against an array schema.
        // Using path extraction like [0] returns a deserialized Map rather than
        // a JSON string, which breaks the json-schema-validator null handling.
        // Validating the raw body avoids that entirely.
        api.getAllTasks()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/task-list-schema.json"));
    }
}
