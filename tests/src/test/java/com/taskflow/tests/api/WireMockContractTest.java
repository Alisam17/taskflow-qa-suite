package com.taskflow.tests.api;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.taskflow.tests.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * WireMock contract tests.
 *
 * These tests demonstrate consumer-driven contract testing: they verify that
 * the test code correctly handles specific API responses (stubs) — isolating
 * test logic from the real server. Useful when:
 *   - The API is still under development
 *   - You want to test specific error/edge-case responses reliably
 *   - Running tests without a live server (pure unit-style API tests)
 */
@Epic("Task Management API")
@Feature("Contract Testing / WireMock Stubs")
public class WireMockContractTest extends BaseApiTest {

    private WireMockServer wireMockServer;
    private String mockBaseUrl;

    @BeforeClass(alwaysRun = true)
    @Override
    public void setUpClass() {
        super.setUpClass();
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        mockBaseUrl = "http://localhost:" + wireMockServer.port();
        log.info("WireMock server started on port {}", wireMockServer.port());
    }

    @AfterClass(alwaysRun = true)
    public void tearDownWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            log.info("WireMock server stopped");
        }
    }

    @Test(description = "GET /api/tasks returns stubbed 200 response")
    @Story("Contract - Happy Path")
    @Severity(SeverityLevel.CRITICAL)
    public void getTasksStub_returns200WithTaskArray() {
        wireMockServer.stubFor(get(urlEqualTo("/api/tasks"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            [
                              {
                                "id": 1,
                                "title": "Stubbed Task",
                                "status": "TODO",
                                "priority": "HIGH"
                              }
                            ]
                            """)));

        given()
                .baseUri(mockBaseUrl)
                .accept(ContentType.JSON)
                .when().get("/api/tasks")
                .then()
                .statusCode(200)
                .body("[0].title", equalTo("Stubbed Task"))
                .body("[0].status", equalTo("TODO"));

        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/api/tasks")));
    }

    @Test(description = "POST /api/tasks returns stubbed 201 with location header")
    @Story("Contract - Create")
    @Severity(SeverityLevel.CRITICAL)
    public void createTaskStub_returns201WithLocationHeader() {
        wireMockServer.stubFor(post(urlEqualTo("/api/tasks"))
                .withRequestBody(containing("title"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Location", "/api/tasks/42")
                        .withBody("""
                            {
                              "id": 42,
                              "title": "New Stubbed Task",
                              "status": "TODO",
                              "priority": "MEDIUM"
                            }
                            """)));

        given()
                .baseUri(mockBaseUrl)
                .contentType(ContentType.JSON)
                .body("{\"title\": \"New Stubbed Task\", \"status\": \"TODO\", \"priority\": \"MEDIUM\"}")
                .when().post("/api/tasks")
                .then()
                .statusCode(201)
                .header("Location", "/api/tasks/42")
                .body("id", equalTo(42));
    }

    @Test(description = "GET /api/tasks/{id} for unknown ID returns stubbed 404")
    @Story("Contract - Not Found")
    @Severity(SeverityLevel.NORMAL)
    public void getTaskByIdStub_whenNotFound_returns404() {
        wireMockServer.stubFor(get(urlEqualTo("/api/tasks/9999"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Task not found\"}")));

        given()
                .baseUri(mockBaseUrl)
                .accept(ContentType.JSON)
                .when().get("/api/tasks/9999")
                .then()
                .statusCode(404);
    }

    @Test(description = "Simulates server 500 error and validates client handles it gracefully")
    @Story("Contract - Error Handling")
    @Severity(SeverityLevel.NORMAL)
    public void getTasksStub_serverError_returns500() {
        wireMockServer.stubFor(get(urlPathEqualTo("/api/tasks/error"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Internal server error\"}")
                        .withFixedDelay(100)));

        given()
                .baseUri(mockBaseUrl)
                .accept(ContentType.JSON)
                .when().get("/api/tasks/error")
                .then()
                .statusCode(500);
    }
}
