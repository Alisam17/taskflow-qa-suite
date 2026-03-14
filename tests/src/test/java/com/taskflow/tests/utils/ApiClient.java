package com.taskflow.tests.utils;

import com.taskflow.tests.models.TaskRequest;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

// Wraps REST Assured so tests read like plain English.
// Every request is automatically logged in the Allure report.
public class ApiClient {

    private static final ConfigManager config = ConfigManager.getInstance();
    private static final String TASKS_ENDPOINT = "/tasks";

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public ApiClient() {
        RestAssured.baseURI = config.getApiBaseUrl();

        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();

        responseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .build();
    }

    public ValidatableResponse getAllTasks() {
        return given(requestSpec)
                .when().get(TASKS_ENDPOINT)
                .then();
    }

    public ValidatableResponse getTasksByStatus(String status) {
        return given(requestSpec)
                .queryParam("status", status)
                .when().get(TASKS_ENDPOINT)
                .then();
    }

    public ValidatableResponse getTasksByPriority(String priority) {
        return given(requestSpec)
                .queryParam("priority", priority)
                .when().get(TASKS_ENDPOINT)
                .then();
    }

    public ValidatableResponse searchTasks(String keyword) {
        return given(requestSpec)
                .queryParam("search", keyword)
                .when().get(TASKS_ENDPOINT)
                .then();
    }

    public ValidatableResponse getTaskById(long id) {
        return given(requestSpec)
                .pathParam("id", id)
                .when().get(TASKS_ENDPOINT + "/{id}")
                .then();
    }

    public ValidatableResponse createTask(TaskRequest task) {
        return given(requestSpec)
                .body(task)
                .when().post(TASKS_ENDPOINT)
                .then();
    }

    public ValidatableResponse updateTask(long id, TaskRequest task) {
        return given(requestSpec)
                .pathParam("id", id)
                .body(task)
                .when().put(TASKS_ENDPOINT + "/{id}")
                .then();
    }

    public ValidatableResponse patchTaskStatus(long id, String status) {
        return given(requestSpec)
                .pathParam("id", id)
                .body(Map.of("status", status))
                .when().patch(TASKS_ENDPOINT + "/{id}/status")
                .then();
    }

    public ValidatableResponse deleteTask(long id) {
        return given(requestSpec)
                .pathParam("id", id)
                .when().delete(TASKS_ENDPOINT + "/{id}")
                .then();
    }

    public ValidatableResponse getTaskStats() {
        return given(requestSpec)
                .when().get(TASKS_ENDPOINT + "/stats")
                .then();
    }

    public long createAndGetId(TaskRequest task) {
        return createTask(task)
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }
}
