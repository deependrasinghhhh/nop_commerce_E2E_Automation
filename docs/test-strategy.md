# Test Strategy — nopCommerce QA Engineering Project

> **Document ID:** STRAT-NOP-4.90.8  
> **Target AUT:** nopCommerce 4.90.8 on ASP.NET Core (.NET 9)  
> **Technology Stack:** Java 21 · Playwright Java 1.49.0 · TestNG 7.10.2 · Maven · Docker (MSSQL 2022)
> **Sign-off / Governance:** Self-assessed by the project author acting as QA Lead. No other reviewer or external stakeholder was involved.  
> **Base Evaluation Date:** 2026-11-13

---

## 1. Strategy Overview & Alignment

This Test Strategy defines the architectural approach, quality gates, execution tiers, and tooling principles for the end-to-end automation of nopCommerce 4.90.8. It operates under a strict **Evidence Contract**: all quality metrics, pass rates, and performance benchmarks must originate from retained, machine-generated artifacts (`evidence/`), never manual estimates.

### 1.1 Testing Objectives
1. **Critical Path Safeguarding:** Exercise the automated Registration, Login, Catalog, Cart, and Checkout journeys without implying coverage of every case in the test catalog.
2. **Multi-Layer Validation:** Validate functionality across three distinct tiers:
   * **Presentation Layer (UI):** Cross-browser user workflows with Playwright Java.
   * **Protocol Layer (HTTP):** Fast route health, status codes, and anti-forgery form POST validations via Playwright `APIRequestContext`.
   * **Data Persistence Layer (DB):** Read-only JDBC SQL assertions validating data integrity against MSSQL 2022.
3. **Automated Accessibility & Performance Baselines:** Enforce automated WCAG 2.1 AA scans with axe-core and local throughput profiling with k6.
4. **Deterministic Concurrency:** Enable parallel suite execution with zero cross-thread state leakage via `ThreadLocal<Playwright>`.

---

## 2. Test Pyramid & Scope Allocation

```
             / \
            /   \      Accessibility (Axe-core) & Performance (k6)
           / A11Y\     ~5-10 Scenarios (WCAG 2.1 AA & route latency)
          /-------\
         /   UI    \   End-to-End & Regression UI Tests (Playwright Java)
        /   TESTS   \  ~50-65 Scenarios (Storefront, Cart, Checkout, Admin)
       /-------------\
      / HTTP & DB INT \ HTTP Protocol Health + Direct MSSQL Validations
     /-----------------\ ~25-35 Scenarios (Route checks, tokens, DB queries)
```

| Layer | Primary Tool | Target Scope | Execution Frequency |
|---|---|---|---|
| **Storefront & Admin UI** | Playwright Java + TestNG | Full end-to-end user workflows, catalog browsing, cart mutations, checkout | PR Smokes & Nightly Regression |
| **HTTP Protocol & Form** | Playwright `APIRequestContext` | Response headers, status codes (200/302/400), anti-forgery token extraction | Every CI build (fast gate) |
| **Database Verification** | JDBC (Microsoft SQL Server) | Read-only assertions over customer, order/item, and storefront-created cart state; cleanup is limited to unique test data | Post-UI transactional tests |
| **Accessibility** | Deque axe-core Playwright | WCAG 2.1 A/AA scans on five storefront pages; serious and critical impact fail by default | Explicit suite run; not part of the current GitHub Actions gate |
| **Performance Profiling**| k6 | Separate HTTP browse/search request workload against the local AUT | Manual benchmark; not a current CI gate |

---

## 3. Automation Framework Architecture

### 3.1 Design Patterns
* **Page Object Model (POM):** Page classes such as `CartPage`, `CheckoutPage`, and `CustomerInfoPage` own page-specific locators and actions; tests perform assertions. The repository does not define shared Header/TopNav/MiniCart page-component classes.
* **Playwright lifecycle:** `PlaywrightFactory` owns thread-local Playwright, Browser, BrowserContext, and Page instances. It creates a fresh isolated context and page for each test method; no `DriverFactory` or separate `BrowserFactory` is used.

### 3.2 Thread-Safe Concurrency Architecture
Because Playwright Java instances are strictly **not thread-safe**, sharing instances across TestNG threads leads to non-deterministic race conditions and JVM crashes. The framework enforces strict thread isolation:

```java
public class PlaywrightFactory {
    private static final ThreadLocal<Playwright> tlPlaywright = new ThreadLocal<>();
    private static final ThreadLocal<Browser> tlBrowser = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> tlContext = new ThreadLocal<>();
    private static final ThreadLocal<Page> tlPage = new ThreadLocal<>();

    // Lifecycle methods instantiate per-thread and dispose cleanly in @AfterMethod
}
```

### 3.3 Synchronization & Locator Discipline
* **Zero Hardcoded Sleeps:** `Thread.sleep()` is categorically forbidden.
* **Auto-Waiting Assertions:** Playwright locator actions and `PlaywrightAssertions.assertThat(locator)` use the configured timeout (45,000 ms in `config.properties`).
* **Resilient User-Facing Locators:** Locators prioritize accessible roles and labels:
  1. `page.getByRole(...)`
  2. `page.getByLabel(...)`
  3. `page.getByPlaceholder(...)`
  4. `page.getByText(...)`
  5. Structural CSS locators (e.g., `#small-searchterms`) only when semantic roles are unavailable in the ASP.NET template. Positional XPaths are strictly prohibited.

---

## 4. Traceability & Evidence Mechanism

1. **Custom Test Case Annotation:**  
   Automated methods use `@TestCase(value = "TC-...", requirementId = "REQ-...")`, mapped to the requirements and catalog:
   ```java
   @Test(groups = {"smoke", "regression"})
   @TestCase(value = "TC-AUTH-001", requirementId = "REQ-AUTH-001")
   public void testSuccessfulUserRegistration() { ... }
   ```
2. **Automated Evidence Reconciliation:**  
   `scripts/reconcile_rtm.py` cross-references the Java annotations, catalog CSV, and the latest TestNG XML report. It generates:
   * Real Execution Summary (Pass / Fail / Skipped / Not Run)
   * Real Requirements Traceability Matrix (RTM) coverage percentages
3. **Artifact Retention Policy:**  
   All run logs, Surefire XMLs, Allure result bundles, failure screenshots, and Playwright zip traces are persisted in `evidence/`.

---

## 5. Test Data Management Strategy

1. **Dynamic Unique Data:** Customer tests use UUID-based email addresses. Test-created accounts and their test-owned data are deleted transactionally by exact generated email, including when a test fails; cleanup failures are logged and suppressed onto the original test failure.
2. **Deterministic Seed Data:** Catalog browsing and search tests rely on the default nopCommerce 4.90.8 sample catalog (e.g., `"Build your own computer"`, `"Apple MacBook Pro"`).
3. **Credential Security:**  
   * **Zero hardcoded credentials in version control.**
   * Sensitive values (admin password, DB credentials) reside in `.env` (git-ignored) and are loaded via a `ConfigReader`.
   * A clean `.env.example` file is maintained in the root directory.

---

## 6. Defect Management & Classification Protocol

### 6.1 Severity / Priority Classification Matrix

| Severity | Definition | Target SLA |
|---|---|---|
| **Critical** | System crash, data loss, primary checkout blocked, security vulnerability | Immediate blocker; halts release |
| **High** | Core feature broken without workaround (e.g., cart quantity update fails) | Must be fixed before release exit |
| **Medium** | Minor functional flaw, validation message discrepancy, UI styling misalignment | Evaluated for next maintenance iteration |
| **Low** | Cosmetic glitch, typo in static footer text | Backlog |

### 6.2 Authenticity & Simulation Protocol (Ground Rule 5)
* **Real Defects:** Identified genuine issues in nopCommerce 4.90.8 are logged with full reproduction steps, environment details, Playwright traces, and screenshots.
* **Simulated Defects:** For defect-lifecycle practice, deliberate bugs injected for demonstration purposes are explicitly labeled with `[SIMULATED]` across all reports, Jira templates, and summaries. Real and simulated defects are never blended.

---

## 7. Environments & Tooling Inventory

| Category | Component / Tool | Version / Spec | Role in Engagement |
|---|---|---|---|
| **Compiler target** | Java SE | 21 (`maven.compiler.release`) | Compilation target |
| **Build & Dep** | Apache Maven | 3.9.6 in the recorded local environment | Dependency resolution, build lifecycle, Surefire runner |
| **Automation** | Playwright Java | 1.49.0 | Browser automation and `APIRequestContext` |
| **Test Runner** | TestNG | 7.10.2 | Suite organization, grouping, parallel execution |
| **Accessibility** | Axe Playwright Java binding | 4.6.0 | axe-core DOM scans using WCAG 2.1 A/AA tags |
| **Database** | Microsoft SQL Server | 2022 Developer | Primary persistence engine for containerized AUT |
| **Database client / pool** | Microsoft JDBC / HikariCP | 12.8.1.jre11 / 5.1.0 | JDBC verification and cleanup |
| **Performance** | k6 | 0.54.0 in the retained benchmark | Separate HTTP browse/search run |
| **Reporting** | Allure / AspectJ | 2.29.0 / 1.9.22 | Allure results and TestNG attachments |
| **CI/CD** | Jenkins + GitHub Actions | Jenkinsfile / workflow YAML | Jenkins-selected TestNG suite; GitHub smoke and API gates |

## 8. Current Coverage, CI, and Known Findings

The test catalog contains **108 cases**, of which **61 automated methods** currently have `@TestCase` mappings (56.5% catalog automation). This is not 100% coverage of the planned catalog; the remaining cases are unautomated/manual scope. Execution counts are generated from TestNG reports and reconciled by `scripts/reconcile_rtm.py`.

The GitHub Actions workflow runs the selected smoke suite and a separate API suite. It does not execute the full UI regression, database, accessibility, or NFR suites. A green dispatch workflow for Jenkins confirms only that Jenkins accepted the build request; it does not wait for or verify the downstream test result.

The retained k6 summary is a separate **HTTP storefront browse/search** run (25 peak VUs, 110 seconds, 481 requests, reported p95 892.6 ms). It is not a browser checkout workload and is not a current CI gate.

The axe scans retain full rule and node data. The default failure threshold is `serious`, configurable through `a11y.failOnImpact` / `A11Y_FAIL_ON_IMPACT`; reports are written under `a11y.reportDirectory` (default `evidence/a11y`). The current saved scans show serious `color-contrast` findings, including white text on `#4ab2f1` at 2.35:1 where 4.5:1 is expected for normal text. The automation repository contains no nopCommerce storefront CSS to repair this product defect; scans must fail/report it rather than exclude the rule.
