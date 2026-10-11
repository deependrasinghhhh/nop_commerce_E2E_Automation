# Master Test Plan — nopCommerce 4.90.8 Release

> **Document ID:** PLAN-NOP-4.90.8  
> **Release Target:** nopCommerce 4.90.8 (.NET 9, Docker Image: `nopcommerceteam/nopcommerce:4.90.8`)  
> **Target Database:** Microsoft SQL Server 2022 (`mcr.microsoft.com/mssql/server:2022-latest`)  
> **Author & Approver:** Project Author acting as QA Lead (Self-assessed; no other reviewer was involved).  
> **Base Execution Date:** October 2026  

---

## 1. Plan Overview & Objectives

This Master Test Plan operationalizes the quality assurance mandate for the nopCommerce 4.90.8 containerized deployment. It governs test execution schedules, suite structuring, resource allocation, and enforceable quality criteria.

### 1.1 Quality Goals
* **P1 Commercial Flow Integrity:** 100% automated pass rate across critical registration, login, search, cart, and checkout paths.
* **Backend State Reliability:** 100% pass rate on transactional database assertions (Customer registration persistence, Order creation rows, Cart state).
* **Defect Discovery & Resolution:** 0 open Critical (P1) or High (P2) defects at milestone completion.
* **Automated Evidence Generation:** Machine-generated Surefire and Allure test reports retained in `evidence/`.

---

## 2. Features Under Test

### 2.1 In-Scope Features (Grounded in Verified AUT)

| Module / Area | Test Types | Key Requirements Covered |
|---|---|---|
| **Authentication & Accounts** | UI, HTTP POST, DB | `REQ-AUTH-001` to `008`, `REQ-DB-001` |
| **Catalog & Navigation** | UI, Search Suggest, Sorting | `REQ-CAT-001` to `007` |
| **Shopping Cart & Wishlist** | UI, Interception, DB | `REQ-CART-001` to `007`, `REQ-DB-003` |
| **End-to-End Checkout** | UI (Guest & Registered), DB | `REQ-CHK-001` to `007`, `REQ-DB-002` |
| **Customer Engagement** | UI (Info, Address, Reviews) | `REQ-CUST-001` to `005` |
| **Admin Operations** | UI (Auth, Dashboard, Catalog, Orders) | `REQ-ADM-001` to `004` |
| **HTTP Protocol Health** | Playwright `APIRequestContext` | `REQ-API-001` to `005` |
| **Database Persistence** | Read-Only JDBC against MSSQL | `REQ-DB-001` to `004` |
| **Accessibility Compliance** | Deque axe-core Playwright | `REQ-A11Y-001`, `002` |
| **Performance Latency** | k6 Load Script | `REQ-NFR-003` |

### 2.2 Features Excluded from Testing (With Real Rationales)
* **REST API Endpoints:** Excluded because core nopCommerce does not bundle a REST API (requires commercial paid Web API plugin).
* **Live Payment Gateway Gateways:** Excluded to prevent external charges and sandbox flakiness; tests use built-in "Check / Money Order".
* **Outbound Email Transmission:** Excluded because external SMTP servers are not provisioned; form submissions verify UI success prompts and database logs.
* **B2B Advanced Modules:** Excluded as they require custom enterprise catalog configurations outside the core baseline store.

---

## 3. Enforceable Quality Criteria

Unlike conventional placeholder test plans, the criteria below represent **strict, enforceable gates** in the execution workflow:

### 3.1 Entry Criteria (Prerequisites to Initiate Test Runs)
1. **Container Availability:** `nopcommerce_web` container is running and responds with HTTP `200 OK` on `http://localhost:5000` (or configured port) within 60 seconds.
2. **Database Availability:** `nopcommerce_db` container is reachable on port `1433`, and JDBC connection healthcheck succeeds in `@BeforeSuite`.
3. **Configuration Ready:** Local `.env` file exists with valid non-default credentials populated from `.env.example`.
4. **Clean Build:** `mvn clean compile test-compile` passes with zero compilation errors.

### 3.2 Suspension Criteria (Triggers to Halt Execution)
Execution halts immediately upon any of the following blocking conditions:
1. **Container Crash:** The nopCommerce web container stops responding (HTTP 500/502/Connection Refused) across all endpoints.
2. **Database Connectivity Breakdown:** Three (3) consecutive JDBC connection timeout exceptions during test execution.
3. **Authentication Blocker:** Complete failure of `REQ-AUTH-001` or `REQ-AUTH-005` rendering downstream customer flows unexecutable.
4. **High Test Environment Instability:** Flakiness rate exceeding 20% on the smoke suite due to environment exhaustion.

### 3.3 Resumption Criteria (Conditions to Resume Testing)
1. Identified root cause resolved (container restarted, DB health restored, or code defect mitigated).
2. Smoke test suite (`mvn test -DsuiteXmlFile=src/test/resources/suites/smoke.xml`) executes with 100% pass rate.
3. Test environment state reset via clean seed migration or fresh container spin-up.

### 3.4 Exit Criteria (Prerequisites for Test Sign-Off)
1. **100% P1 Execution:** All Priority 1 (Smoke & Critical) test cases executed with 100% pass rate.
2. **≥ 95% Regression Pass Rate:** Overall regression suite pass rate ≥ 95% (with zero unresolved Critical/High defects).
3. **Zero Critical a11y Violations:** Automated axe-core scan detects zero critical-severity violations on primary storefront templates.
4. **Evidence Persisted:** TestNG XML/HTML results, Allure results, and performance k6 summary JSON are archived in `evidence/`.
5. **Machine-Reconciled RTM:** The Requirements Traceability Matrix is generated directly from execution evidence.

---

## 4. Test Suites & Execution Architecture

To avoid known Maven/Surefire group filtering friction (`-Dgroups` vs `suiteXmlFiles`), testing is structured around dedicated TestNG suite XML files:

| Suite Name | XML Configuration File | Target Scope | Target Execution Time |
|---|---|---|---|
| **Smoke Suite** | `src/test/resources/suites/smoke.xml` | Critical P1 tests (Auth, Basic Search, Cart Add, Checkout baseline) | < 3 minutes |
| **Regression Suite** | `src/test/resources/suites/regression.xml` | Full functional UI + Customer + Admin + DB tests | < 12 minutes |
| **API Protocol Suite** | `src/test/resources/suites/api.xml` | Fast HTTP route health, anti-forgery, form POSTs | < 45 seconds |
| **Accessibility Suite** | `src/test/resources/suites/a11y.xml` | WCAG 2.1 AA automated axe scans | < 2 minutes |
| **Full Validation Suite**| `src/test/resources/suites/full.xml` | End-to-end combination of all suites | < 15 minutes |

---

## 5. Test Deliverables & Retention

| Deliverable Artifact | Destination Path | Retention Rule |
|---|---|---|
| Test Requirements Specification | `docs/requirements.md` | Version-controlled in Git |
| Test Strategy & Plan | `docs/test-strategy.md`, `docs/test-plan.md` | Version-controlled in Git |
| Test Scenarios & Catalog | `docs/test-cases.csv` | Machine-readable baseline |
| Automated Test Code | `src/test/java/...` | Java 21 POM implementation |
| Surefire XML Execution Reports | `evidence/surefire-*.xml` | Retained execution artifact |
| Failure Evidence (Screenshots/Traces)| `evidence/failures/` | Persisted on test failure |
| k6 Performance Summary | `evidence/k6-summary-*.json` | Machine-generated benchmark |
| Final Test Closure & Sign-off | `docs/test-closure-report.md` | Self-assessed summary |

---

## 6. Execution Approvals & Sign-Off

> **Sign-off Governance:**  
> This Master Test Plan is self-assessed by the project author acting as QA Lead. No external stakeholders, Product Owners, or Development Leads are fabricated. Quality criteria are enforced directly through automated CI/CD pipeline assertions and artifact verification.
