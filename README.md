# nopCommerce E2E Test Automation Framework

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Playwright](https://img.shields.io/badge/Playwright%20Java-1.48.0-green.svg)](https://playwright.dev/java/)
[![TestNG](https://img.shields.io/badge/TestNG-7.10.2-red.svg)](https://testng.org/)
[![Allure](https://img.shields.io/badge/Allure-2.29.0-yellow.svg)](https://allurereport.org/)
[![Axe-core](https://img.shields.io/badge/Axe--core-WCAG%202.1%20AA-blue.svg)](https://www.deque.com/axe/)
[![Docker](https://img.shields.io/badge/Docker-nopCommerce%204.90.8-blue.svg)](https://www.docker.com/)
[![Jenkins](https://img.shields.io/badge/Jenkins-CI%2FCD%20Pipeline-informational.svg)](https://www.jenkins.io/)
[![Test Suite](https://img.shields.io/badge/Tests-61%2F61%20Passed-brightgreen.svg)](docs/execution-summary.md)

Production-grade Quality Engineering and End-to-End Test Automation framework for **nopCommerce 4.90.8** (.NET 9 on containerized Kestrel + MS SQL Server 2022). Built with **Java 21 LTS**, **Playwright Java**, **TestNG**, **Allure Report**, and **Axe-Playwright**, engineered for zero-flakiness multi-threaded parallel execution, automated CI/CD gating, and direct database persistence validation.

---

## Reconciled Execution Summary (`2026-11-13 10:52:44 UTC`)

**Execution Window:** `2026-11-13T10:51:46Z to 2026-11-13T10:52:44Z (58.00s)`
**Target AUT:** nopCommerce 4.90.8, isolated Docker stack
**Run Result:** 61 passed, 0 failed, 0 skipped (100.0% of 61 executed automated methods).

| Metric Category | Value | Source Artifact |
|---|---|---|
| Total Test Cases Specified | 108 | `docs/test-cases.csv` |
| Automated Test Methods | 61 (56.5% of catalog) | `docs/traceability-matrix.md` |
| Executed Automated Methods | 61 (100.0%) | `evidence/surefire-reports/testng-results.xml` |
| Passed / Failed / Skipped | 61 / 0 / 0 | `docs/execution-summary.md` |
| k6 Storefront Load Benchmark | k6 v0.54.0 separate HTTP browse/search workload: 25 peak VUs for 110 seconds, 481 requests, p95 892.6 ms | `evidence/performance/k6-summary.json` |

This run covers the executed automated methods only. The 61 automated methods map to a 108-case catalog; it is not 100% coverage of all planned cases.
GitHub Actions runs smoke and API suites only. A green Jenkins dispatch confirms acceptance of the request, not the downstream test result.
The axe default threshold is critical impact; critical findings fail the scan. Strict serious-impact audits can be run on demand with -Da11y.failOnImpact=serious.

## Required Secrets & Environment Variables

No credentials or fallback passwords are stored in the repository. Supply secrets via `.env` (local) or CI secrets:
- `ADMIN_EMAIL` / `ADMIN_PASSWORD`: nopCommerce administrator credentials
- `DB_USER` / `DB_PASSWORD`: MSSQL database credentials (required by `docker/docker-compose.yml` and `DatabaseManager`)
- `BASE_URL` / `ADMIN_URL`: Isolated nopCommerce test instance URLs (defaults to `http://127.0.0.1:5000`; `demo.nopcommerce.com` is rejected)
- `IGNORE_HTTPS_ERRORS`: Opt-in boolean (`false` by default)

---

## Framework Architecture & Engineering Standards

```mermaid
flowchart TD
    subgraph Config_Layer ["Configuration & Credential Cascade"]
        CLI["Maven CLI (-D)"] --> CFG["ConfigReader.java"]
        ENV["System Environment Vars"] --> CFG
        DOTENV[".env Secrets File"] --> CFG
        PROPS["config.properties"] --> CFG
    end

    subgraph Concurrency_Engine ["Execution & Thread Management"]
        XML["TestNG XML Suites"] --> TLIST["ThreadCountListener.java"]
        TLIST --> RUNNER["TestNG Parallel Runner (thread-count=2)"]
        RUNNER --> BASE["BaseTest Lifecycle Hooks"]
        BASE --> TST_LIST["TestListener (Allure, Screenshots, Traces)"]
    end

    subgraph Playwright_Factory ["Thread-Safe Browser Automation"]
        BASE --> PF["PlaywrightFactory.java"]
        PF --> TL_PW["ThreadLocal&lt;Playwright&gt;"]
        PF --> TL_CTX["ThreadLocal&lt;BrowserContext&gt;"]
        PF --> TL_PAGE["ThreadLocal&lt;Page&gt;"]
        TL_PAGE --> POM["18 Page Object Model Classes"]
    end

    subgraph Verification_Layer ["Validation & Persistence"]
        POM --> STOREFRONT["nopCommerce Storefront & Admin (:5000)"]
        POM --> AXE["AxeScanner (WCAG 2.1 AA)"]
        BASE --> DBM["DatabaseManager.java (HikariCP / JDBC)"]
        DBM --> MSSQL["MS SQL Server (:1433)"]
    end

    subgraph Reporting_Pipeline ["Reporting & Artifacts"]
        TST_LIST --> TRACE["Playwright Traces (.zip on failure)"]
        TST_LIST --> SHOT["Failure Screenshots (.png)"]
        RUNNER --> SUREFIRE["Surefire & TestNG HTML Reports"]
        RUNNER --> ALLURE["Allure 2.29.0 Results & Trend History"]
    end
```

### Architectural Highlights

1. **ThreadLocal Browser Isolation**: Every parallel test thread receives an isolated `Playwright`, `BrowserContext`, and `Page` instance, eliminating cross-test session leaks, cookie contamination, or state bleed.
2. **Auto-Waiting Locators**: Page objects strictly wrap Playwright locators (`click()`, `fill()`, `selectOption()`, `waitForVisible()`) relying on native readiness checks (attached, visible, stable, enabled) with zero arbitrary `Thread.sleep()` pauses.
3. **Dynamic Concurrency Control (`ThreadCountListener`)**: Inspects parallel suite configurations at runtime and dynamically binds `-DthreadCount` overrides across TestNG runners.
4. **Hierarchical Configuration Cascade**: Resolves settings in strict priority: `CLI System Properties (-D)` > `Environment Variables` > `.env` file > `config.properties` > hardcoded fallbacks.
5. **Automated Test Data Lifecycle**: `DatabaseManager.java` manages transactional database assertions and executes cascade teardown (`deleteCustomerCascade`) for dynamic test customer accounts.
6. **Failure-Only Artifact Capture**: Traces and video are discarded on passing tests to prevent trace collisions and storage bloat; full Playwright trace archives (`.zip`) and screenshots (`.png`) are automatically captured and linked to Allure only on test failure.

---

## 3. Automated Test Coverage (61 Tests Across 10 Domains)

| Domain | Test Class | Methods | Automated Coverage & Key Business Flows |
| --- | --- | :---: | --- |
| **Smoke Gate** | `SmokeTest.java` | 3 | Storefront root health, search input interactivity, admin portal login page rendering. |
| **Authentication & Identity** | `AuthTest.java` | 8 | Valid registration, duplicate email rejection, required field validation, invalid email syntax, password confirmation mismatch, valid login, invalid password, empty credentials, session logout. |
| **Catalog Browsing & Search** | `CatalogTest.java` | 7 | Top menu navigation, category desktop rendering, product details display, image gallery triggers, non-existent search handling, 3-character search warning, price sorting (low-to-high). |
| **Shopping Cart & Wishlist** | `CartTest.java` | 8 | Add simple product, add with options/attributes, update cart quantity & recalculate total, remove cart item, add to wishlist, migrate wishlist item to cart, apply valid discount coupon code & verify total reduction, invalid coupon rejection. |
| **Checkout Flow** | `CheckoutTest.java` | 3 | Guest checkout initiation, billing address validation, full end-to-end checkout (billing address, Ground shipping, Check/Money Order payment method, order confirmation, order number generation). |
| **Customer Account Management** | `CustomerAccountTest.java` | 6 | Customer info view, address book management (create, edit, delete address), order history list display, registered customer review submission, guest review restrictions. |
| **Administration Portal** | `AdminTest.java` | 7 | Admin authentication, dashboard metrics & navigation, product catalog grid search, orders table display, categories list verification, customer list verification, admin invalid login validation. |
| **API Endpoint Probing** | `ApiEndpointTest.java` | 6 | Root endpoint HTTP 200, register endpoint health, login endpoint health, 404 route handling, admin portal endpoint status, anti-forgery CSRF token enforcement (HTTP 400 Bad Request). |
| **Database Persistence** | `DatabaseVerificationTest.java` | 4 | JDBC connectivity health check (`SELECT 1`), customer record persistence in `Customer` table, shopping cart row persistence in `ShoppingCartItem`, order & order-items persistence in `[Order]`. |
| **Accessibility Compliance** | `AccessibilityTest.java` | 5 | Automated WCAG 2.1 AA scans with axe-core across Homepage, Registration, Login, Shopping Cart, and Category pages with configurable impact thresholds. |
| **Non-Functional & SLA** | `NonFunctionalTest.java` | 4 | Navigation page load time SLA (< 3000ms), mobile responsive layout (390x844), mobile hamburger drawer expansion, zero HTTP 500 server errors on page assets. |

---

## 4. Known Upstream Findings & Accessibility Reality

In strict adherence to professional QA transparency, known upstream platform defects are documented rather than masked:

### Upstream Theme Color-Contrast Finding (`DEF-A11Y-001`)

- **Finding**: nopCommerce 4.90.8's stock storefront CSS contains a known WCAG 2.1 AA `serious` color-contrast violation: white text on `#4ab2f1` primary buttons and links produces a contrast ratio of **2.35:1** (below the **4.5:1** threshold required for normal body text).
- **Quality Gate Strategy**: Because this repository is an **automation framework** testing an external containerized AUT and does not contain nopCommerce's application CSS, the automated CI gate defaults to `critical` (`a11y.failOnImpact=critical`). This ensures continuous delivery pipelines are not permanently blocked by an external application defect while functional regression remains 100% green.
- **On-Demand Strict Audit**: Accessibility auditors can run the suite in strict mode at any time to fail on serious contrast findings:

  ```powershell
  mvn test -Pa11y -Da11y.failOnImpact=serious
  ```

---

## 5. CLI Execution Cheat Sheet

### 5.1 Run Test Suites by Profile

```powershell
# 1. Quick Smoke Suite (3 tests, ~10s)
mvn test -Psmoke

# 2. Dedicated Parallel Storefront UI Suite (39 tests, 2 browsers)
mvn test -Pui

# 3. Full End-to-End Regression Suite (61 tests, ~1 min)
mvn test -Pregression

# 4. API Integration Probing (6 tests, ~12s)
mvn test -Papi

# 5. Database Verification Suite (4 tests, ~25s)
mvn test -Pdb

# 6. Accessibility WCAG 2.1 AA Scans (5 tests, ~15s)
mvn test -Pa11y

# 7. Non-Functional & Responsive SLA Suite (4 tests, ~20s)
mvn test -Pnfr
```

### 5.2 Targeted Test Execution

```powershell
# Run a specific test class
mvn test -Dtest=CartTest

# Run a single test method
mvn test -Dtest=CartTest#testAddProductToCart

# Run multiple test classes
mvn test -Dtest=AuthTest,CartTest,CheckoutTest
```

### 5.3 Execution Modes & Debugging Flags

```powershell
# Run in Headed mode (opens visible browser windows)
mvn test -Pui -Dheadless=false

# Run in Slow Motion (500ms pause per action for live demonstration)
mvn test -Dtest=CartTest -DslowMo=500 -Dheadless=false

# Override thread count dynamically
mvn test -Pui -DthreadCount=4

# Run against a different browser engine (chromium, firefox, webkit)
mvn test -Psmoke -DbrowserName=firefox
```

### 5.4 Allure Reporting

```powershell
# Generate static Allure HTML report to target/site/allure-maven-plugin/
mvn allure:report

# Start local web server with interactive Allure dashboard
mvn allure:serve
```

---

## 6. Infrastructure, Docker & Local Environment

The framework runs against an isolated, reproducible Docker stack:

```mermaid
flowchart LR
    HOST["Host Machine (PowerShell / Maven)"] -->|HTTP :5000| WEB["nopcommerce_web (ASP.NET Core 9)"]
    HOST -->|JDBC :1433| DB["nopcommerce_mssql (SQL Server 2022)"]
    WEB -->|TCP :1433| DB
    JENKINS["nop_jenkins_ci (:8080)"] -->|Docker Network| WEB
```

### Starting the AUT Stack

```powershell
# Safe bootstrap: parses required secrets from .env and verifies health
bash docker/bootstrap-aut.sh

# Verify running container health
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

Required environment variables or `.env` entries:

- `BASE_URL`: Defaults to `http://127.0.0.1:5000` (public `demo.nopcommerce.com` is rejected at startup).
- `ADMIN_URL`: Defaults to `http://127.0.0.1:5000/Admin`.
- `DB_USER` / `DB_PASSWORD`: MSSQL database credentials (default: `sa` / password configured in `.env`).

---

## 7. Continuous Integration & Pipeline Architecture

### GitHub Actions CI (`.github/workflows/smoke-gate.yml`)

- Triggered automatically on push and pull requests to `main` and `refactor/**`.
- Spins up an ephemeral, isolated nopCommerce and MS SQL Docker stack on `ubuntu-latest`.
- Pre-seeds the database snapshot using `docker/seed/nopcommerce_baseline.bak`.
- Executes both the **Browser Smoke Suite** and **API Probe Suite**.
- Automatically reconciles RTM documentation and uploads Allure + Surefire build artifacts.

### Local Jenkins CI Pipeline (`jenkins/Jenkinsfile`)

- Enterprise Declarative Pipeline running on `http://localhost:8080/job/nopCommerce-E2E-Automation-Pipeline/`.
- Full parameterization support (`SUITE`, `BROWSER`, `HEADLESS`, `THREAD_COUNT`, `ENABLE_TREND_HISTORY`).
- Automatically injects Jenkins credentials (`nop-admin-email`, `nop-admin-password`, `nop-db-user`, `nop-db-password`).
- Verifies container health on `http://nopcommerce_web`, sets up Playwright drivers, runs the suite with 2 worker threads, syncs the RTM, and archives Allure trend history.

---

## 8. Test Reports & Evidence Location

| Report Type | Path / URL | Description |
| --- | --- | --- |
| **TestNG Full HTML Suite Report** | [evidence/surefire-reports/index.html](evidence/surefire-reports/index.html) | Complete test class breakdown and execution logs. |
| **TestNG Emailable Report** | [evidence/surefire-reports/emailable-report.html](evidence/surefire-reports/emailable-report.html) | Single-page summary table with timings and statuses. |
| **Allure HTML Report** | `target/site/allure-maven-plugin/index.html` | Interactive dashboard with step trees, charts, and history. |
| **Axe Accessibility Scans** | [evidence/a11y/](evidence/a11y/) | Raw JSON violation trees for all 5 scanned pages. |
| **Requirements Traceability Matrix** | [docs/traceability-matrix.md](docs/traceability-matrix.md) | Granular 1:1 mapping of requirements to test code. |
| **Test Execution Summary** | [docs/execution-summary.md](docs/execution-summary.md) | Reconciled pass/fail metrics and duration benchmarks. |
| **Formal QA Sign-Off** | [docs/qa-sign-off.md](docs/qa-sign-off.md) | Verified QA sign-off and risk assessment document. |
| **Failure Screenshots & Traces** | `evidence/failures/`, `evidence/traces/` | Retained zip traces and failure images for debugging. |

---

## 9. Repository Structure

```text
├── src/test/java/com/qa/nopcommerce/
│   ├── base/           # BaseTest lifecycle hooks, context management, tracing & teardown
│   ├── config/         # ConfigReader with 5-tier cascade and validation
│   ├── database/       # DatabaseManager for JDBC pooling, SQL queries & customer cleanup
│   ├── factory/        # PlaywrightFactory for ThreadLocal browser and page instances
│   ├── listeners/      # TestListener (Allure/screenshots) & ThreadCountListener (parallelism)
│   ├── models/         # Test data models (Customer, etc.)
│   ├── pages/          # 18 Page Object implementations with auto-wait actions
│   ├── tests/          # 11 Test classes across UI, API, DB, Smoke, A11y, and NFR
│   └── utils/          # AxeScanner, AllureAttachmentUtil, TestDataGenerator
├── src/test/resources/
│   ├── config/         # config.properties repository defaults
│   └── suites/         # 7 TestNG XML suite definitions
├── docker/             # Docker Compose, bootstrap script, and database baseline backup
├── jenkins/            # Jenkinsfile and declarative pipeline job configurations
├── .github/workflows/  # GitHub Actions CI gate definitions
├── docs/               # Requirements, test strategy, RTM, and sign-off documentation
├── evidence/           # Retained Surefire XML/HTML reports and Axe scan outputs
└── scripts/            # Python reconciliation script for RTM documentation
```

---

## 10. QA Lead & Author

- **Author & QA Automation Engineer:** Deependra Singh
- **Target Application:** nopCommerce 4.90.8 (.NET 9 + MS SQL Server)
- **Status:** **Active / Fully Verified — 61/61 Tests Passing (100% Pass Rate)**
