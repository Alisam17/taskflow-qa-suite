# ⚡ TaskFlow QA Automation Suite

[![CI Pipeline](https://github.com/alisam17/taskflow-qa-suite/actions/workflows/ci.yml/badge.svg)](https://github.com/alisam17/taskflow-qa-suite/actions)
[![Java](https://img.shields.io/badge/Java-17-007396?logo=java)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?logo=spring)](https://spring.io/projects/spring-boot)
[![Selenium](https://img.shields.io/badge/Selenium-4.18-43B02A?logo=selenium)](https://selenium.dev)
[![REST Assured](https://img.shields.io/badge/REST_Assured-5.4-brightgreen)](https://rest-assured.io)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](https://docker.com)
[![Allure](https://img.shields.io/badge/Reports-Allure-FF6347)](https://docs.qameta.io/allure/)

A production-grade QA automation framework built to demonstrate end-to-end testing skills for modern web applications. Features API testing, UI automation with Selenium Grid, contract testing with WireMock, containerized environments, and automated CI/CD reporting.

---

## 📐 Architecture

```
taskflow-qa-suite/
├── app/                          # Spring Boot REST API (System Under Test)
│   ├── src/main/java/            # Controllers, Services, Models
│   ├── src/main/resources/       # Config, Thymeleaf templates
│   └── Dockerfile                # Multi-stage Docker build
│
├── tests/                        # Test Automation Framework
│   └── src/test/java/
│       ├── api/                  # REST Assured API tests
│       │   ├── TaskCrudApiTest       # Full CRUD with positive + negative cases
│       │   ├── TaskFilterApiTest     # Data-driven filter & search tests
│       │   ├── TaskSchemaValidationTest # JSON schema contract tests
│       │   └── WireMockContractTest  # Consumer-driven contract tests
│       ├── ui/
│       │   ├── pages/DashboardPage   # Page Object Model
│       │   └── DashboardUiTest       # Selenium WebDriver UI tests
│       ├── base/                 # BaseApiTest, BaseUiTest
│       ├── utils/                # DriverManager, ApiClient, ConfigManager, ScreenshotUtil
│       └── models/               # TaskRequest, TaskResponse (Builder pattern)
│
├── docker-compose.yml            # App + Selenium Grid (Hub + Chrome + Firefox)
├── .github/workflows/ci.yml      # 4-job GitHub Actions CI/CD pipeline
└── README.md
```

---

## 🛠 Tech Stack

| Layer | Technology | Purpose |
|---|---|---|
| Language | Java 17 | Core language |
| Build | Maven | Dependency management & build |
| Test Framework | TestNG 7.9 | Test runner, parallel execution, DataProviders |
| API Testing | REST Assured 5.4 | Fluent HTTP assertions |
| UI Testing | Selenium WebDriver 4.18 | Browser automation |
| Driver Management | WebDriverManager 5.8 | Automatic driver binaries |
| Contract Testing | WireMock 3.4 | API stubs & mock server |
| Assertions | AssertJ | Fluent, readable assertions |
| Reporting | Allure 2.25 | Rich HTML reports with screenshots |
| Containerization | Docker + Compose | Full environment in one command |
| Browser Grid | Selenium Grid 4.18 | Parallel cross-browser execution |
| CI/CD | GitHub Actions | Automated 4-stage pipeline |
| App Framework | Spring Boot 3.2 | REST API (system under test) |
| Test Data | JavaFaker | Realistic test data generation |

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Option 1: Run tests locally (requires running app)

```bash
# 1. Start the application
cd app && mvn spring-boot:run &

# 2. Run all tests
cd tests && mvn test

# 3. Generate & open Allure report
cd tests && mvn allure:report && mvn allure:serve
```

### Option 2: Run everything with Docker (recommended)

```bash
# Start the full environment (app + Selenium Grid)
docker compose up -d

# View Grid UI: http://localhost:4444/ui
# View App:     http://localhost:8080

# Run tests against Docker environment
cd tests && mvn test \
  -Dapp.base.url=http://localhost:8080 \
  -Dselenium.remote=true \
  -Dselenium.grid.url=http://localhost:4444

# Tear down
docker compose down -v
```

### Option 3: Run specific test groups

```bash
# API tests only (no browser required)
mvn test -Dgroups=api

# UI tests only
mvn test -Dgroups=ui

# Specific test class
mvn test -Dtest=TaskCrudApiTest

# Specific test method
mvn test -Dtest=TaskCrudApiTest#createTask_withValidData_returns201AndTask
```

---

## 📊 Test Coverage

### API Tests (`/api/tasks`)

| Test Class | Scenarios | Focus |
|---|---|---|
| `TaskCrudApiTest` | 14 tests | Create, Read, Update, Delete — positive & negative |
| `TaskFilterApiTest` | 10 tests | Data-driven filter/search with `@DataProvider` |
| `TaskSchemaValidationTest` | 2 tests | JSON schema contract validation |
| `WireMockContractTest` | 4 tests | Stubbed contract tests, error simulation |

### UI Tests (Selenium)

| Test Class | Scenarios | Focus |
|---|---|---|
| `DashboardUiTest` | 6 tests | Create/delete tasks, stat counters, form validation |

---

## 🏗 Key Design Patterns

**Page Object Model** — All UI interactions are encapsulated in `DashboardPage`. Tests never touch CSS selectors directly.

**Builder Pattern** — `TaskRequest.builder()` enables clean, readable test data construction with factory methods like `TaskRequest.validTask()` and `TaskRequest.highPriorityTask()`.

**Thread-Safe Driver Management** — `DriverManager` uses `ThreadLocal<WebDriver>` for safe parallel test execution.

**Centralized Config** — `ConfigManager` reads from `config.properties` with environment variable overrides, making the same tests work locally and in CI without code changes.

**Fluent API Client** — `ApiClient` wraps REST Assured with a clean DSL. Tests express intent: `api.createTask(request).statusCode(201)`.

**Data-Driven Testing** — `@DataProvider` in `TaskFilterApiTest` runs the same test across all valid status/priority values.

---

## 📈 Allure Report Features

- **Epics / Features / Stories** — Full test hierarchy
- **Step-by-step breakdowns** — Every `@Step` shows in the report
- **Automatic screenshots on failure** — `ScreenshotUtil` captures and attaches on any UI test failure
- **REST Assured request/response logging** — Full HTTP logs attached to each API test
- **GitHub Pages deployment** — Allure report published automatically on `main` merges

---

## ⚙️ CI/CD Pipeline (GitHub Actions)

The pipeline runs 4 parallel/sequential jobs on every push and pull request:

```
Push/PR
  │
  ├── Job 1: build-app         → Compiles & packages the Spring Boot JAR
  │
  ├── Job 2: api-tests         → Starts app, runs REST Assured + WireMock tests
  │
  ├── Job 3: ui-tests          → Starts Docker Selenium Grid, runs UI tests
  │
  └── Job 4: allure-report     → Merges results, generates report, deploys to GitHub Pages
```

---

## 🔧 Configuration

All settings are in `tests/src/test/resources/config.properties`. Every property can be overridden via environment variables (for CI/CD):

| Property | Env Var | Default | Description |
|---|---|---|---|
| `app.base.url` | `APP_BASE_URL` | `http://localhost:8080` | Application URL |
| `browser` | `BROWSER` | `chrome` | Browser (`chrome`/`firefox`) |
| `browser.headless` | `BROWSER_HEADLESS` | `true` | Run headless |
| `selenium.remote` | `SELENIUM_REMOTE` | `false` | Use Selenium Grid |
| `selenium.grid.url` | `SELENIUM_GRID_URL` | `http://localhost:4444` | Grid hub URL |

---

## 📁 Sample Allure Report

After running tests, generate the report:

```bash
cd tests
mvn allure:serve   # Generates and opens in browser
```

The report includes: overall pass/fail breakdown, suite hierarchy, per-test timelines, failure screenshots, and full request/response logs for every API call.

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/add-performance-tests`
3. Write tests with proper Allure annotations (`@Epic`, `@Feature`, `@Story`, `@Severity`)
4. Run locally: `mvn test`
5. Open a pull request — CI will run automatically

---

*Built as a QA portfolio project demonstrating industry-standard automation practices.*
