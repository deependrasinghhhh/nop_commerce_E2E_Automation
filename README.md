# nopCommerce E2E QA Engineering Automation Framework

> **Portfolio Engineering Notice**: This repository contains an enterprise-grade, production-ready **Quality Assurance & Test Automation Framework** engineered for **nopCommerce 4.90.8** (.NET 9 / ASP.NET Core). It is strictly built with technical transparency: every metric, test case count, pass rate, and execution log is directly backed by machine-generated artifacts retained in `evidence/`.

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Playwright](https://img.shields.io/badge/Playwright%20Java-1.48.0-green.svg)](https://playwright.dev/java/)
[![TestNG](https://img.shields.io/badge/TestNG-7.10.2-red.svg)](https://testng.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9.6-blue.svg)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-nopCommerce%204.90.8-2496ED.svg)](https://hub.docker.com/r/nopcommerceteam/nopcommerce)
[![MSSQL](https://img.shields.io/badge/MSSQL-2022%20LTS-CC292B.svg)](https://www.microsoft.com/sql-server)
[![Jenkins](https://img.shields.io/badge/Jenkins-CI%2FPipeline%20Passing-D24939.svg)](http://localhost:8080)
[![Test Suite](https://img.shields.io/badge/Test%20Suite-55%2F55%20Passed%20(100%25)-brightgreen.svg)](#2-evidence-contract--real-execution-statistics)
[![Accessibility](https://img.shields.io/badge/A11y-WCAG%202.1%20AA-blueviolet.svg)](#5-multi-layer-test-architecture)

---

## 1. Executive Summary & Architecture

This project delivers a multi-layered, end-to-end automated test engineering solution for **nopCommerce 4.90.8** (.NET 9 / ASP.NET Core). Rebuilt from the ground up onto a modern Java ecosystem, the framework incorporates industry best practices for high-velocity continuous integration:

* **Core Test Automation Engine:** **Java 21 LTS** + **Microsoft Playwright Java 1.48.0**
* **Test Runner & Orchestration:** **TestNG 7.10.2** with parallel class execution and XML suite decoupling
* **Build & Dependency System:** **Apache Maven 3.9.6** with environment profiles (`smoke`, `ui`, `api`, `db`, `a11y`, `regression`)
* **Design Pattern:** Industry-standard **Page Object Model (POM)** across 12 page classes with encapsulation and fluent chaining
* **Concurrency & Resource Management:** `ThreadLocal<Playwright>`, `ThreadLocal<Browser>`, and `ThreadLocal<Page>` pooling paired with lightweight isolated `BrowserContext` lifecycles
* **Database Verification:** High-performance **HikariCP (v5.1.0)** connection pooling with automatic fallback to standard MSSQL JDBC drivers
* **Accessibility Auditing:** In-sprint WCAG 2.1 AA automated auditing powered by **Deque axe-core**
* **CI/CD Integration:** Dual pipeline strategy featuring a Declarative **Jenkins CI Pipeline** (`jenkins/Jenkinsfile`) and **GitHub Actions Smoke Gate** (`.github/workflows/smoke-gate.yml`)
* **Traceability & Governance:** Machine-reconciled **Requirements Traceability Matrix (RTM)** linking custom `@TestCase("TC-...")` code annotations to 104 specified requirements

```
                       nopCommerce QA Engineering Architecture
┌──────────────────────────────────────────────────────────────────────────────────┐
│                             TestNG Suite Orchestrator                            │
│  (suites/smoke.xml · suites/ui.xml · suites/api.xml · suites/db.xml · regression)│
└────────────────────────────────────────┬─────────────────────────────────────────┘
                                         │
        ┌────────────────────────────────┼────────────────────────────────┐
        ▼                                ▼                                ▼
┌─────────────────────────┐   ┌─────────────────────┐   ┌──────────────────────────┐
│     Storefront & UI     │   │   API Integration   │   │  Database Persistence   │
│ (12 POM Classes · A11y) │   │(Playwright Route API│   │ (MSSQL HikariCP Pool)    │
│  ThreadLocal Playwright │   │ REST HTTP Probing)  │   │  Direct SQL Assertions   │
└────────────┬────────────┘   └──────────┬──────────┘   └─────────────┬────────────┘
             │                           │                            │
             └───────────────────────────┼────────────────────────────┘
                                         ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│                           Application Under Test (AUT)                           │
│   • Local Primary: Docker Compose (nopCommerce 4.90.8 @ :5000 + MSSQL 2022)      │
│   • Network Routing: Explicit IPv4 Loopback (127.0.0.1) & Container Bridge Host  │
└────────────────────────────────────────┬─────────────────────────────────────────┘
                                         ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│                              Machine Retained Evidence                           │
│   • Surefire XML Reports: target/surefire-reports/ · evidence/surefire-reports/  │
│   • Visual Reports: Allure 2 Report · TestNG HTML Report · Jenkins Stage View    │
│   • Screenshots & Execution Traces: evidence/failures/*.png, *.zip               │
│   • Machine-Reconciled RTM: docs/traceability-matrix.md · execution-summary.md   │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Evidence Contract & Real Execution Statistics

In strict adherence to the project's **Evidence Contract**, all metrics reported below originate directly from machine-generated test execution logs:

| Metric Category | Machine Value | Artifact Reference |
| :--- | :--- | :--- |
| **Total Test Cases Specified** | **104** | [`docs/test-cases.csv`](docs/test-cases.csv) |
| **Automated Test Methods** | **47** (100% of automated scope) | [`docs/traceability-matrix.md`](docs/traceability-matrix.md) |
| **Regression Suite Invocations** | **55 Tests** (Data-driven parameterized) | `target/surefire-reports/TEST-TestSuite.xml` |
| **Overall Pass Rate** | **100.0% (55 / 55 Passed, 0 Failures)** | [`docs/execution-summary.md`](docs/execution-summary.md) |
| **Storefront UI & Cart Coverage** | 19 / 19 Passed (100%) | `evidence/surefire-reports/TEST-*.ui.*.xml` |
| **Admin Operations & Moderation** | 7 / 7 Passed (100%) | `evidence/surefire-reports/TEST-*.AdminTest.xml` |
| **API Route Probing** | 6 / 6 Passed (100%) | `evidence/surefire-reports/TEST-*.ApiEndpointTest.xml` |
| **Accessibility Compliance (WCAG AA)** | 5 / 5 Passed (100%) | `evidence/surefire-reports/TEST-*.AccessibilityTest.xml` |
| **Database Persistence (MSSQL)** | 4 / 4 Passed (100%) | `evidence/surefire-reports/TEST-*.DatabaseVerificationTest.xml` |
| **Non-Functional & SLA Gates** | 4 / 4 Passed (100%) | `evidence/surefire-reports/TEST-*.NonFunctionalTest.xml` |
| **Execution Duration (Headless CI)** | **1m 49s** (~65% speedup via thread pooling) | Jenkins Build Console Log |
| **Execution Duration (Live Headed)** | **2m 41s** (All windows visible on desktop) | Maven Surefire Execution Summary |

---

## 3. Engineering Challenges & Technical Solutions

During development, real-world distributed systems challenges emerged. Each was addressed with clean architectural solutions:

### 3.1 Docker Desktop Windows WSL2 IPv6 Proxy Stall
* **Issue:** Under Windows 11 with Docker Desktop WSL2, network calls to `http://localhost:5000` resolve first to IPv6 loopback (`::1:5000`). Docker Desktop's port proxy accepts the TCP SYN handshake on `::1`, but the ASP.NET Core container (`dotnet Nop.Web.dll`) listens strictly on IPv4 `0.0.0.0:80`. This caused Playwright navigations to stall for 30+ seconds waiting for packet transmission.
* **Resolution:** Re-routed all test execution configurations ([`config.properties`](src/test/resources/config/config.properties) and [`ConfigReader.java`](src/test/java/com/qa/nopcommerce/config/ConfigReader.java)) to explicit IPv4 loopback (`http://127.0.0.1:5000`). Time-to-first-byte dropped from 30+ seconds to **under 200 milliseconds**.

### 3.2 ASP.NET Core JIT & DOM Synchronization
* **Issue:** On initial cold hits, ASP.NET Core compiles Razor views and loads localized resource strings, during which external CDN assets (fonts, gravatars) can intermittently delay the browser's `window.onload` event.
* **Resolution:** Standardized [`BaseTest.java`](src/test/java/com/qa/nopcommerce/base/BaseTest.java) navigation synchronization on `WaitUntilState.DOMCONTENTLOADED` and increased navigation guard timeouts to 30,000ms. All interactive DOM locators render immediately without waiting on external third-party assets.

### 3.3 Asynchronous Post-Login Session Transitions
* **Issue:** In `HomePage.java`, `clickLogout()` previously used an instant `isVisible(logoutLink)` condition. Because Playwright's `isVisible()` evaluates with a 0ms wait, it checked the DOM while the login POST redirect was still in-flight, skipping logout and corrupting subsequent session assertions.
* **Resolution:** Added explicit `logoutLink.waitFor()` in [`HomePage.java`](src/test/java/com/qa/nopcommerce/pages/HomePage.java#L70-L77), ensuring the authentication state transition completes before triggering the logout action.

### 3.4 Playwright Browser Context Reuse vs Process Isolation
* **Issue:** Re-launching a brand new browser process (`playwright.chromium().launch()`) for every test method added significant process-spawn overhead (~5 minutes total run).
* **Resolution:** Re-engineered [`PlaywrightFactory.java`](src/test/java/com/qa/nopcommerce/factory/PlaywrightFactory.java) to reuse the underlying `Browser` process per thread while strictly isolating test state via ephemeral `BrowserContext` and `Page` lifecycles torn down in `@AfterMethod`. Execution time for 55 tests dropped from **5m 12s to 1m 49s (~65% reduction)**.

---

## 4. Multi-Layer Test Architecture

The framework implements a complete testing pyramid covering all critical architectural tiers:

```
               ▲
              / \
             / A11y \         WCAG 2.1 AA (axe-core Scans)
            /─────────\
           /  Admin &  \       Data Grids, Products, Customers, Orders
          /  Storefront \     12 Page Object Model Classes (Playwright POM)
         /───────────────\
        /   Integration   \    APIRequestContext HTTP Probing (Status, SLA, Auth)
       /───────────────────\
      /  DB Persistence &   \  MSSQL JDBC (HikariCP Pool), Direct SQL Schema Queries
     /     Non-Functional    \ Breakpoint Viewports (Mobile/Desktop), Latency SLA Gates
    ───────────────────────────
```

### 4.1 Storefront UI & E-Commerce Flow
* **Authentication (`AuthTest.java`):** Mandatory field registration, unique email collision detection, valid/invalid authentication, password masking, remember-me persistence, and session teardown.
* **Catalog Browsing (`CatalogTest.java`):** Category hierarchy navigation (Computers -> Desktops), price/name sorting verification, grid/list view toggling, and page-size changes.
* **Search Engine (`CatalogTest.java`):** Header search input, wildcards, auto-suggest dropdowns, and empty query validation.
* **Shopping Cart & Mini-Cart (`CartTest.java`):** Add-to-cart from product cards, quantity modification with subtotal recalculation, and line item removal.
* **Checkout Flow (`CheckoutTest.java`):** End-to-end guest and registered checkout workflows: Billing address entry, Shipping method selection, Payment method options, and Order confirmation.

### 4.2 Administration Portal
* **Moderation & Grid Data (`AdminTest.java`):** Backoffice admin authentication, dashboard metrics rendering, product catalog data grid (`#products-grid`), order history grid (`#orders-grid`), customer search grid (`#customers-grid`), and category management.

### 4.3 Headless API Route Probing
* **HTTP Route Auditing (`ApiEndpointTest.java`):** Utilizes Playwright `APIRequestContext` to validate core application endpoints (`/`, `/login`, `/register`, `/cart`, `/search`, `/contactus`) without browser rendering overhead:
  * HTTP status codes (200 OK / 302 Found)
  * Header validation (`Content-Type: text/html; charset=utf-8`)
  * Response latency SLA (< 2000ms TTFB)

### 4.4 Enterprise Database Persistence
* **MSSQL Verification (`DatabaseVerificationTest.java`):** Connects directly to the underlying SQL Server instance via **HikariCP** connection pooling:
  * Customer record persistence (`SELECT Email FROM Customer WHERE Email = ?`)
  * Order status and grand total integrity (`SELECT OrderStatusId, OrderTotal FROM [Order]`)
  * Shopping cart item mapping (`SELECT Quantity, ShoppingCartTypeId FROM ShoppingCartItem`)

### 4.5 Automated Accessibility Audits (WCAG 2.1 AA)
* **Axe-Core Auditing (`AccessibilityTest.java`):** Injects Deque `axe-core` into live pages to audit accessibility across 5 core user views:
  * Storefront Homepage
  * User Login Portal
  * Customer Registration Page
  * Category Browsing (Desktops)
  * Shopping Cart View
  * Enforces zero critical or serious WCAG accessibility violations.

### 4.6 Non-Functional & Responsive Breakpoints
* **Viewport Testing (`NonFunctionalTest.java`):** Dynamically resizes browser viewports to validate responsive UI stability:
  * Mobile Viewport: `390 x 844` (iPhone 12/13/14 profile)
  * Tablet Viewport: `768 x 1024` (iPad Mini profile)
  * Desktop Viewport: `1920 x 1080` (Full HD standard)
  * Server performance & First Byte response time verification.

---

## 5. Repository Directory Structure

```
E2E_Automation_nopcommerce/
├── .github/
│   └── workflows/
│       ├── smoke-gate.yml                     # GitHub Actions CI smoke gate & binary cache
│       └── jenkins-trigger.yml                # Push trigger dispatching to local Jenkins
├── database/
│   └── queries/                               # SQL persistence verification queries
│       ├── verify_cart_items.sql
│       ├── verify_customer_registration.sql
│       └── verify_order_placement.sql
├── docker/
│   ├── docker-compose.yml                     # Pinned nopCommerce 4.90.8 + MSSQL 2022
│   └── README.md                              # Docker container setup and networking guide
├── docs/                                      # QA Artifacts & Project Documentation
│   ├── application-overview.md                # nopCommerce architecture and tech stack
│   ├── project-charter.md                     # QA engagement scope and charter
│   ├── requirements.md                        # Immutable SRS requirement definitions
│   ├── risk-register.md                       # Risk register (RSK-001 through RSK-011)
│   ├── test-strategy.md                       # Comprehensive testing strategy
│   ├── test-plan.md                           # Master test plan with quality gates
│   ├── test-cases.csv                         # Master register of 104 specified test cases
│   ├── traceability-matrix.md                 # Machine-reconciled RTM (automated)
│   ├── execution-summary.md                   # Real-time execution summary report
│   ├── defect-log.md                          # Defect triage register (DEF-001 to DEF-003)
│   ├── qa-sign-off.md                         # Formal quality sign-off assessment
│   └── test-closure-report.md                 # Comprehensive project closure report
├── evidence/                                  # Machine-Generated Execution Artifacts
│   ├── a11y/                                  # Deque axe-core raw JSON audit outputs
│   ├── failures/                              # Failure screenshots and Playwright trace files
│   ├── performance/                           # k6 load testing summary JSON
│   ├── reports/                               # Standalone TestNG HTML reports
│   └── surefire-reports/                      # Surefire XML test execution outputs
├── jenkins/
│   ├── Jenkinsfile                            # Declarative parameterized CI/CD pipeline
│   ├── job-config.xml                         # Jenkins job XML definition
│   └── README.md                              # Jenkins architecture and configuration
├── performance/
│   └── k6/
│       └── storefront-load.js                 # k6 load testing script (SLA thresholds)
├── postman/
│   └── nopCommerce-exploratory-collection.json# Postman HTTP route exploration collection
├── qa-management/                             # Jira & Xray QA management export templates
├── scripts/
│   └── reconcile_rtm.py                       # Automated machine RTM reconciliation script
├── src/
│   └── test/
│       ├── java/com/qa/nopcommerce/
│       │   ├── annotation/                    # Custom @TestCase annotation definition
│       │   ├── base/                          # BaseTest lifecycle and navigation
│       │   ├── config/                        # ConfigReader cascading configuration
│       │   ├── database/                      # DatabaseManager (HikariCP JDBC pool)
│       │   ├── factory/                       # ThreadLocal PlaywrightFactory
│       │   ├── listeners/                     # TestListener (screenshot/trace on failure)
│       │   ├── pages/                         # 12 Page Object Model classes
│       │   │   ├── BasePage.java
│       │   │   ├── HomePage.java
│       │   │   ├── LoginPage.java
│       │   │   ├── RegisterPage.java
│       │   │   ├── CategoryPage.java
│       │   │   ├── ProductDetailsPage.java
│       │   │   ├── CartPage.java
│       │   │   ├── CheckoutPage.java
│       │   │   ├── CustomerInfoPage.java
│       │   │   ├── SearchPage.java
│       │   │   ├── AdminLoginPage.java
│       │   │   └── AdminDashboardPage.java
│       │   └── tests/                         # Multi-layer test classes
│       │       ├── a11y/AccessibilityTest.java
│       │       ├── api/ApiEndpointTest.java
│       │       ├── db/DatabaseVerificationTest.java
│       │       ├── nfr/NonFunctionalTest.java
│       │       ├── smoke/SmokeTest.java
│       │       └── ui/
│       │           ├── AdminTest.java
│       │           ├── AuthTest.java
│       │           ├── CartTest.java
│       │           ├── CatalogTest.java
│       │           └── CheckoutTest.java
│       └── resources/
│           ├── config/config.properties       # Baseline execution properties
│           └── suites/                        # TestNG XML suite files
│               ├── smoke.xml
│               ├── ui.xml
│               ├── api.xml
│               ├── db.xml
│               ├── a11y.xml
│               └── regression.xml             # Master regression suite (55 tests)
└── pom.xml                                    # Apache Maven project configuration
```

---

## 6. CI/CD Pipelines & DevOps Integration

### 6.1 Declarative Jenkins CI Pipeline
The primary CI pipeline is defined in [`jenkins/Jenkinsfile`](jenkins/Jenkinsfile) and runs on the Jenkins controller under the dedicated job **`nopCommerce-E2E-Automation-Pipeline`**:

* **Container Toolchain:** Uses native OpenJDK 21 LTS (`/usr/lib/jvm/java-21-openjdk-amd64`) and Maven 3.9.9 (`/usr/share/maven`).
* **Container Bridge Networking:** Connects to the host-hosted AUT via `http://host.docker.internal:5000`.
* **Parameterization:** Enables on-demand execution with dynamic parameters:
  * `SUITE`: Choice of `regression.xml`, `smoke.xml`, `ui.xml`, `api.xml`, `db.xml`
  * `BROWSER`: Choice of `chromium`, `firefox`, `webkit`
  * `HEADLESS`: Boolean (`true` / `false`)
  * `THREAD_COUNT`: Thread count across parallel classes (default: `2`)
* **Automated Pipeline Stages:**
  1. `Checkout & Toolchain Verification`
  2. `AUT Environment Verification` (Awaits port 5000 health)
  3. `Playwright Driver Setup` (Ensures browser binaries)
  4. `Execute Test Suite` (Maven TestNG execution)
  5. `RTM Traceability Reconciliation` (Executes `reconcile_rtm.py`)
  6. `Declarative: Post Actions` (JUnit results, HTML Publisher, evidence archiving)

### 6.2 Triggering Jenkins Pipeline via cURL
```powershell
curl.exe -X POST "http://localhost:8080/job/nopCommerce-E2E-Automation-Pipeline/buildWithParameters?token=nopcommerce-qa-trigger-token" -u "Deependra:1118dcb75992ada5baac019bfacd67a691"
```

### 6.3 GitHub Actions Smoke Gate (`.github/workflows/smoke-gate.yml`)
* Executes on every pull request and push to `main`.
* Spins up headless Ubuntu 22.04 runners.
* Caches Playwright browser binaries (`~/.cache/ms-playwright`) for ultra-fast CI startup.
* Archives Allure results and test failure screenshots automatically on completion.

---

## 7. Quickstart & Execution Guide

### 7.1 Prerequisites
* **Java 21 LTS**: Verify with `java -version`
* **Apache Maven 3.9+**: Verify with `mvn -version`
* **Docker Desktop**: Running with containerized AUT on port 5000
* **Python 3.10+**: For automated RTM reconciliation

### 7.2 Running Test Suites Locally

```powershell
# 1. Full Regression Suite - Headless Mode (Fast CI standard)
mvn test -Pregression

# 2. Full Regression Suite - Live Headed Browser Mode (Watch tests on screen)
mvn test -Pregression -Dheadless=false

# 3. Storefront Smoke Gate
mvn test -Psmoke

# 4. Storefront UI Core (Auth, Catalog, Cart, Checkout)
mvn test -Pui

# 5. API Route Integration Tests (100% Pass)
mvn test -Papi

# 6. Database Persistence Verification (MSSQL JDBC)
mvn test -Pdb

# 7. WCAG 2.1 AA Accessibility Scans (axe-core)
mvn test -Pa11y

# 8. Single Test Class Execution
mvn test -Dtest=SmokeTest
mvn test -Dtest=AdminTest -Dheadless=false
```

### 7.3 Managing the Docker Container AUT

```powershell
# Start local nopCommerce 4.90.8 and MSSQL 2022 containers
cd docker
docker compose up -d

# Verify containers are healthy
docker ps

# Tear down containers and volumes
docker compose down -v
```

### 7.4 Generating Allure & RTM Reports

```powershell
# Reconcile Requirements Traceability Matrix (RTM)
python scripts/reconcile_rtm.py

# Generate and view Allure Test Report
mvn allure:report
mvn allure:serve
```

---

## 8. Traceability Annotation Example

Every automated test method links directly to requirement identifiers using custom `@TestCase` annotations:

```java
@Test(groups = {"ui", "auth", "smoke"})
@TestCase("TC-AUTH-001")
@Severity(SeverityLevel.BLOCKER)
@Description("Verify valid customer registration with all mandatory fields completed")
public void testValidCustomerRegistration() {
    navigateTo("/register");
    RegisterPage registerPage = new RegisterPage(getPage());

    String uniqueEmail = "nop_qa_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    registerPage.selectGender("male");
    registerPage.fillRegistrationForm("Alex", "QA Tester", uniqueEmail, "QA Corp", "Test@123456", "Test@123456");
    registerPage.clickRegisterButton();

    PlaywrightAssertions.assertThat(getPage().locator(".result"))
            .hasText(Pattern.compile(".*Your registration completed.*"));
}
```

The reconciliation engine ([`scripts/reconcile_rtm.py`](scripts/reconcile_rtm.py)) scans source code AST and Surefire XML execution logs to dynamically refresh [`docs/traceability-matrix.md`](docs/traceability-matrix.md) after every run.

---

## 9. Comprehensive QA Documentation Index

| Document | Purpose & Description |
| :--- | :--- |
| **[Project Charter](docs/project-charter.md)** | Engagement vision, scope boundaries, stakeholder alignment, and testing mandate |
| **[Application Overview](docs/application-overview.md)** | Technical analysis of nopCommerce 4.90.8 architecture, routing, and data models |
| **[Requirements Specification (SRS)](docs/requirements.md)** | Granular functional and non-functional requirements with immutable IDs |
| **[Risk Register](docs/risk-register.md)** | Risk assessment matrix covering 11 critical technical risks (RSK-001 - RSK-011) |
| **[Test Strategy](docs/test-strategy.md)** | Quality philosophy, pyramid distribution, tooling justification, and test types |
| **[Master Test Plan](docs/test-plan.md)** | Milestone schedule, environment topology, entry/exit criteria, and defect SLAs |
| **[Master Test Cases Register](docs/test-cases.csv)** | Exhaustive inventory of 104 granular test cases with steps and expected results |
| **[Requirements Traceability Matrix](docs/traceability-matrix.md)** | Machine-reconciled RTM mapping requirements to automated test methods |
| **[Execution Summary](docs/execution-summary.md)** | Machine-generated execution report capturing pass rates and evidence artifacts |
| **[Defect Log](docs/defect-log.md)** | Defect triage register documenting findings, root causes, and remediation |
| **[QA Sign-Off Assessment](docs/qa-sign-off.md)** | Objective quality assessment and release readiness recommendation |
| **[Test Closure Report](docs/test-closure-report.md)** | Final engagement retrospective, test efficiency analysis, and lessons learned |

---

## 10. License & Attribution

This project is open-source software licensed under the [MIT License](LICENSE).
* **Application Under Test:** [nopCommerce](https://www.nopcommerce.com/) open-source e-commerce platform.
* **Framework Author & QA Engineering Lead:** **Deependra Singh**