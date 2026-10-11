# nopCommerce E2E QA Engineering Automation Framework

> **Portfolio Simulation Notice**: This repository is an advanced, production-grade **QA Engineering Portfolio Project** simulating a comprehensive quality assurance engagement on **nopCommerce 4.90.8**. It is deliberately designed and documented with technical honesty: there are zero fabricated corporate stakeholders, made-up client sign-offs, or invented pass rates. Every metric, execution log, pass/fail count, and screenshot is backed by machine-generated retained artifacts.

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Playwright](https://img.shields.io/badge/Playwright%20Java-1.48.0-green.svg)](https://playwright.dev/java/)
[![TestNG](https://img.shields.io/badge/TestNG-7.10.2-red.svg)](https://testng.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9.6-blue.svg)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-nopCommerce%204.90.8-2496ED.svg)](https://hub.docker.com/r/nopcommerceteam/nopcommerce)

---

## 1. Executive Summary & Architecture

This project modernizes and rebuilds an end-to-end QA framework for **nopCommerce 4.90.8** (.NET 9 / ASP.NET Core) onto an enterprise Java stack:
- **Core Engine:** **Java 21 LTS** + **Playwright Java 1.48.0**
- **Test Runner:** **TestNG 7.10.2** with XML suite orchestration and parallel execution
- **Build System:** **Apache Maven 3.9.6**
- **Architecture Pattern:** Page Object Model (POM) across 12 distinct page classes with fluent action chaining
- **Traceability:** Custom `@TestCase("TC-...")` annotations enforcing bi-directional traceability to 104 specified test cases
- **Multi-Layer Coverage:** Storefront UI, Admin Portal, Playwright `APIRequestContext` HTTP route probing, Axe-core WCAG 2.1 AA accessibility audits, and MSSQL JDBC database persistence verification
- **CI/CD & DevOps:** Declarative Jenkins pipeline (`jenkins/Jenkinsfile`), secondary GitHub Actions smoke gate (`.github/workflows/smoke-gate.yml`), k6 load testing script, and Postman collection

```
nopCommerce QA Engineering Architecture
┌────────────────────────────────────────────────────────────────────────┐
│                        TestNG Suite Runner                             │
│   (suites/smoke.xml · suites/api.xml · suites/ui.xml · suites/db.xml)  │
└─────────────────────────────────┬──────────────────────────────────────┘
                                  │
         ┌────────────────────────┼────────────────────────┐
         ▼                        ▼                        ▼
┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│ Storefront & UI  │    │  API & Probing   │    │  DB Persistence  │
│ (Playwright POM) │    │(APIRequestContext│    │ (MSSQL JDBC Pool)│
└────────┬─────────┘    └────────┬─────────┘    └────────┬─────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                                 ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        Application Under Test (AUT)                    │
│   • Local Container: docker/docker-compose.yml (nopCommerce + MSSQL)   │
│   • Cloud Fallback: https://demo.nopcommerce.com (WAF Monitored)       │
└────────────────────────────────┬───────────────────────────────────────┘
                                 │
                                 ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     Retained Machine Evidence                          │
│   • Surefire XML: evidence/surefire-reports/*.xml                      │
│   • HTML Reports: evidence/reports/testng-index.html                   │
│   • Failure Screens & Traces: evidence/failures/*.png, *.zip           │
│   • Machine-Reconciled RTM: docs/traceability-matrix.md                │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Evidence Contract & Real Execution Statistics

In strict compliance with the **Evidence Contract**, all metrics reported below originate directly from machine-generated artifacts retained in `evidence/`:

| Metric | Machine Value | Artifact Source |
|---|---|---|
| **Total Test Cases Specified** | 104 | `docs/test-cases.csv` |
| **Automated Test Methods** | 47 (45.2%) | `@TestCase` annotations in `src/test/java/` |
| **API Integration Pass Rate** | 100% (6/6 Pass) | `evidence/surefire-reports/TEST-TestSuite.xml` |
| **Database Verification Status** | 4 Skipped (Env) | `evidence/surefire-reports/junitreports/TEST-*.DatabaseVerificationTest.xml` |
| **Public Storefront Gate Status** | 1 Failed (WAF) | `evidence/failures/testHomePageTitleAndHeaderRendering.png` |
| **Not Run (Manual / Blocked)** | 93 (89.4%) | `docs/traceability-matrix.md` |

### Known Environmental Blockers
- **DEF-001 (Blocker):** Public demo store (`https://demo.nopcommerce.com`) is guarded by Cloudflare Turnstile anti-bot challenges that intercept headless automated browser threads with `<title>Just a moment...</title>`. Failure screenshots and traces are captured in `evidence/failures/`.
- **ENV-001 (Config):** The MSSQL database container requires administrative elevation to start the Docker daemon on the host. `DatabaseVerificationTest` executes non-destructive connection checks and records `SKIPPED` per TestNG lifecycle when offline.

---

## 3. Repository Directory Structure

```
E2E_Automation_nopcommerce/
├── .github/
│   └── workflows/
│       └── smoke-gate.yml             # GitHub Actions secondary smoke gate
├── database/
│   └── queries/                       # SQL persistence verification queries
│       ├── verify_cart_items.sql
│       ├── verify_customer_registration.sql
│       └── verify_order_placement.sql
├── docker/
│   ├── docker-compose.yml             # Pinned nopCommerce 4.90.8 + MSSQL 2022
│   └── README.md                      # Docker container management guide
├── docs/
│   ├── application-overview.md        # nopCommerce 4.90.8 technical discovery
│   ├── project-charter.md             # Project charter & testing mandate
│   ├── requirements.md                # SRS with immutable requirement IDs
│   ├── risk-register.md               # Risk register (RSK-001 through RSK-011)
│   ├── test-strategy.md               # Test strategy document
│   ├── test-plan.md                   # Master test plan with entry/exit gates
│   ├── test-cases.csv                 # 104 granular test cases
│   ├── traceability-matrix.md         # Machine-reconciled RTM
│   ├── execution-summary.md           # Machine-generated execution summary
│   ├── defect-log.md                  # Defect triage register (DEF-001 to DEF-003)
│   ├── qa-sign-off.md                 # Formal self-assessed QA sign-off
│   └── test-closure-report.md         # Final test closure report
├── evidence/                          # Retained machine-generated artifacts
│   ├── failures/                      # Failure screenshots and trace archives
│   ├── performance/                   # k6 load test benchmark JSON summaries
│   ├── reports/                       # TestNG HTML reports
│   └── surefire-reports/              # Surefire XML execution outputs
├── jenkins/
│   ├── Jenkinsfile                    # Declarative parameterized pipeline
│   └── README.md                      # Jenkins CI setup and architecture guide
├── performance/
│   └── k6/
│       └── storefront-load.js         # k6 load script (homepage, catalog, search)
├── postman/
│   └── nopCommerce-exploratory-collection.json  # Postman HTTP probing collection
├── qa-management/                     # Jira, Xray, and Confluence QA templates
├── scripts/
│   └── reconcile_rtm.py               # Automated RTM reconciliation script
├── src/
│   └── test/
│       ├── java/com/qa/nopcommerce/
│       │   ├── annotation/            # Custom @TestCase annotation
│       │   ├── base/                  # BaseTest lifecycle and navigation
│       │   ├── config/                # ConfigReader properties provider
│       │   ├── database/              # DatabaseManager JDBC client
│       │   ├── factory/               # ThreadLocal PlaywrightFactory
│       │   ├── listeners/             # TestListener (screenshot/trace on fail)
│       │   ├── pages/                 # 12 Page Object Model classes
│       │   └── tests/                 # UI, API, A11Y, DB, and NFR test classes
│       └── resources/
│           ├── config/config.properties
│           └── suites/                # TestNG XML suites (smoke, api, ui, db, regression)
└── pom.xml                            # Maven project configuration
```

---

## 4. Quickstart & Verification Guide

### 4.1 Prerequisites
- **Java 21 LTS** (`java -version`)
- **Maven 3.9+** (`mvn -version`)
- **Google Chrome** installed locally, or Playwright browsers (`npx playwright install chromium`)
- **Python 3.10+** (for RTM reconciliation)

### 4.2 Verify Compilation
```bash
mvn clean test-compile
```

### 4.3 Run API Integration Test Suite (100% Pass)
```bash
# Using Maven Profile (Cross-platform: PowerShell, CMD, Bash)
mvn test -Papi

# Or using explicit suite path (Note: quotes required in Windows PowerShell)
mvn test "-DsuiteFile=src/test/resources/suites/api.xml"
```

### 4.4 Run Database Verification Suite (Graceful Skipped on Offline DB)
```bash
mvn test -Pdb
```

### 4.5 Run Accessibility Scans (WCAG 2.1 AA)
```bash
mvn test -Pa11y
```

### 4.6 Run Smoke Test Gate
```bash
mvn test -Psmoke
```

### 4.7 Run Full Regression Suite
```bash
# Headless mode (default in CI)
mvn test -Pregression

# Browser (Headed) mode
mvn test -Pregression -Dheadless=false
```

### 4.8 Reconcile Requirements Traceability Matrix (RTM)
```bash
python scripts/reconcile_rtm.py
```

### 4.9 Run with Containerized Local AUT (Docker)
```bash
cd docker
docker compose up -d
# Run tests against local container without Cloudflare Turnstile
mvn test -Pregression -DbaseUrl=http://localhost:5000 -Dheadless=false
docker compose down -v
```

---

## 5. Traceability Annotation Example

Every automated test method in the framework is strictly annotated with `@TestCase("TC-...")`:

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

The reconciliation tool `scripts/reconcile_rtm.py` dynamically links these annotations against `docs/test-cases.csv` and Surefire XML outputs, updating `docs/traceability-matrix.md` automatically on every test run.

---

## 6. License & Attribution

This project is licensed under the [MIT License](LICENSE).
Target Application Under Test: [nopCommerce](https://www.nopcommerce.com/) open-source e-commerce platform.
Project Lead & Automation Author: Deependra Singh.