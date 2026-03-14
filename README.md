# TaskFlow QA Automation Suite

[![CI Pipeline](https://github.com/alisam17/taskflow-qa-suite/actions/workflows/ci.yml/badge.svg)](https://github.com/alisam17/taskflow-qa-suite/actions)
[![Java](https://img.shields.io/badge/Java-17-007396?logo=java)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?logo=spring)](https://spring.io/projects/spring-boot)
[![Selenium](https://img.shields.io/badge/Selenium-4.18-43B02A?logo=selenium)](https://selenium.dev)
[![REST Assured](https://img.shields.io/badge/REST_Assured-5.4-brightgreen)](https://rest-assured.io)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](https://docker.com)
[![Allure](https://img.shields.io/badge/Reports-Allure-FF6347)](https://docs.qameta.io/allure/)

A QA automation framework built around a simple Spring Boot task management app. It covers API testing with REST Assured, UI automation with Selenium, contract testing with WireMock, and runs everything through a GitHub Actions pipeline with Allure reports published to GitHub Pages.

---

## Project layout

```
taskflow-qa-suite/
├── app/                          # Spring Boot REST API (the thing being tested)
│   ├── src/main/java/            # Controllers, services, models
│   ├── src/main/resources/       # Config, Thymeleaf UI templates
│   └── Dockerfile
│
├── tests/                        # Test automation framework
│   └── src/test/java/
│       ├── api/
│       │   ├── TaskCrudApiTest           # CRUD happy path + negative cases
│       │   ├── TaskFilterApiTest         # Data-driven filter and search tests
│       │   ├── TaskSchemaValidationTest  # JSON schema contract tests
│       │   └── WireMockContractTest      # Stubbed contract tests
│       ├── ui/
│       │   ├── pages/DashboardPage       # Page Object Model
│       │   └── DashboardUiTest           # Selenium tests
│       ├── base/                 # Shared setup/teardown for API and UI tests
│       ├── utils/                # DriverManager, ApiClient, ConfigManager, ScreenshotUtil
│       └── models/               # Request/response POJOs
│
├── docker-compose.yml            # Spins up the app + Selenium Grid
├── .github/workflows/ci.yml      # 4-job CI pipeline
└── README.md
```

---

## Tech stack

| | Technology | What it's used for |
|---|---|---|
| Language | Java 17 | Core language |
| Build | Maven | Dependency management |
| Test runner | TestNG 7.9 | Test execution, parallel runs, DataProviders |
| API testing | REST Assured 5.4 | HTTP assertions |
| UI testing | Selenium WebDriver 4.18 | Browser automation |
| Driver setup | WebDriverManager | Handles driver binaries automatically |
| Contract testing | WireMock 3.4 | Mock server, API stubs |
| Assertions | AssertJ | Fluent assertions |
| Reporting | Allure 2.25 | HTML reports with screenshots |
| Containers | Docker + Compose | Full environment setup |
| Browser grid | Selenium Grid 4.18 | Parallel cross-browser execution |
| CI/CD | GitHub Actions | Automated pipeline |
| App | Spring Boot 3.2 | The REST API being tested |

---

## Getting started

You need Java 17+, Maven 3.8+, and Docker.

**Run locally:**

```bash
# Start the app
cd app && mvn spring-boot:run &

# Run all tests
cd tests && mvn test

# Open the Allure report
cd tests && mvn allure:serve
```

**Run with Docker (recommended):**

```bash
# Start everything - app, Selenium Hub, Chrome, Firefox
docker compose up -d

# Grid UI: http://localhost:4444/ui
# App:     http://localhost:8080

# Run tests against Docker
cd tests && mvn test \
  -Dapp.base.url=http://localhost:8080 \
  -Dselenium.remote=true \
  -Dselenium.grid.url=http://localhost:4444

# Clean up
docker compose down -v
```

**Run specific tests:**

```bash
# Just API tests (no browser needed)
mvn test -Dtest="TaskCrudApiTest,TaskFilterApiTest,TaskSchemaValidationTest,WireMockContractTest"

# Just UI tests
mvn test -Dtest="DashboardUiTest"

# One specific method
mvn test -Dtest="TaskCrudApiTest#createTask_withValidData_returns201AndTask"
```

---

## What's covered

**API tests** (`/api/tasks`):

- `TaskCrudApiTest` - 14 tests covering create, read, update, and delete with positive and negative cases
- `TaskFilterApiTest` - data-driven tests using `@DataProvider` that run across all valid status and priority values
- `TaskSchemaValidationTest` - validates the response shape against a JSON schema so silent contract breaks get caught
- `WireMockContractTest` - tests against a local mock server, including error simulation and request verification

**UI tests:**

- `DashboardUiTest` - 6 tests covering task creation, deletion, stat counter updates, and form validation

---

## Design notes

**Page Object Model** - `DashboardPage` handles all browser interactions. Tests call methods like `dashboardPage.createTask(title, status, priority)` and never touch selectors directly.

**Builder pattern** - `TaskRequest.builder()` makes test data readable. There are also factory methods like `TaskRequest.validTask("title")` for common cases.

**Thread-safe drivers** - `DriverManager` uses `ThreadLocal<WebDriver>` so parallel tests don't share browser instances.

**Config with env var overrides** - `ConfigManager` reads from `config.properties` but environment variables take priority. The same test code runs locally and in CI without any changes.

**Fluent API client** - `ApiClient` wraps REST Assured so test code reads like `api.createTask(request).statusCode(201)` rather than raw boilerplate.

**Screenshot on failure** - `ScreenshotUtil` captures and attaches a screenshot to the Allure report automatically when a UI test fails.

---

## CI/CD pipeline

Four jobs run on every push and pull request:

```
Push / PR
  |
  +-- build-app        Compiles and packages the Spring Boot JAR
  |
  +-- api-tests        Starts the app, runs REST Assured and WireMock tests
  |
  +-- ui-tests         Starts Docker Selenium Grid, runs Selenium tests
  |
  +-- allure-report    Merges results, generates the report, deploys to GitHub Pages
```

---

## Configuration

Settings live in `tests/src/test/resources/config.properties`. Any value can be overridden with an environment variable (uppercase, dots replaced with underscores).

| Property | Default | Description |
|---|---|---|
| `app.base.url` | `http://localhost:8080` | Where the app is running |
| `browser` | `chrome` | Browser to use (`chrome` or `firefox`) |
| `browser.headless` | `true` | Run headless |
| `selenium.remote` | `false` | Use Selenium Grid |
| `selenium.grid.url` | `http://localhost:4444` | Grid hub URL |

---

## Viewing the Allure report

```bash
cd tests
mvn allure:serve
```

This generates the report and opens it in your browser. After a CI run, the latest report is also published to GitHub Pages at `https://alisam17.github.io/taskflow-qa-suite/`.
