# Test Strategy — nopCommerce QA Engineering Project

> **Document ID:** STRAT-NOP-4.90.8  
> **Target AUT:** nopCommerce 4.90.8 on ASP.NET Core (.NET 9)  
> **Technology Stack:** Java 21 LTS · Playwright Java 1.63.0 · TestNG · Maven · Docker (MSSQL 2022)  
> **Sign-off / Governance:** Self-assessed by the project author acting as QA Lead. No other reviewer or external stakeholder was involved.  
> **Base Evaluation Date:** October 2026  

---

## 1. Strategy Overview & Alignment

This Test Strategy defines the architectural approach, quality gates, execution tiers, and tooling principles for the end-to-end automation of nopCommerce 4.90.8. It operates under a strict **Evidence Contract**: all quality metrics, pass rates, and performance benchmarks must originate from retained, machine-generated artifacts (`evidence/`), never manual estimates.

### 1.1 Testing Objectives
1. **Critical Path Safeguarding:** Guarantee core commercial journeys (Registration, Login, Catalog, Cart, and multi-step Checkout) remain 100% functional through automated smoke and regression gates.
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
| **Database Verification** | JDBC (Microsoft SQL Server) | Read-only state verification (`[Customer]`, `[Order]`, `[ShoppingCartItem]`) | Post-UI transactional tests |
| **Accessibility** | Deque axe-core Playwright | WCAG 2.1 AA compliance scans on primary templates (`/`, `/register`, `/cart`) | Weekly / Release gate |
| **Performance Profiling**| k6 | Local Docker container HTTP throughput & response latency baselines | Milestone release verification |

---

## 3. Automation Framework Architecture

### 3.1 Design Patterns
* **Page Object Model (POM):** Clean separation between page structure (locators, component actions) and test assertions. Page classes return either `this` (fluent chaining) or the subsequent landing page object.
* **Component-Based Modularity:** Reusable global components (Header, TopNav, MiniCart, Footer, ConfirmationBar) are modeled as distinct reusable objects shared across pages.
* **Factory Pattern:** Centralized `DriverFactory` / `BrowserFactory` managing browser instance lifecycles based on configuration (`chromium`, `firefox`, `webkit`).

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
* **Auto-Waiting Assertions:** All verifications use Playwright's `PlaywrightAssertions.assertThat(locator)` which leverages smart auto-retrying with exponential backoff up to the configured timeout (default: 5000 ms).
* **Resilient User-Facing Locators:** Locators prioritize accessible roles and labels:
  1. `page.getByRole(...)`
  2. `page.getByLabel(...)`
  3. `page.getByPlaceholder(...)`
  4. `page.getByText(...)`
  5. Structural CSS locators (e.g., `#small-searchterms`) only when semantic roles are unavailable in the ASP.NET template. Positional XPaths are strictly prohibited.

---

## 4. Traceability & Evidence Mechanism

1. **Custom Test Case Annotation:**  
   Every `@Test` method is decorated with `@TestCase("TC-...")`, mapping directly to the System Requirements Specification (`docs/requirements.md`) and the test catalog (`docs/test-cases.csv`):
   ```java
   @Test(groups = {"smoke", "regression"})
   @TestCase("TC-AUTH-001")
   public void testSuccessfulUserRegistration() { ... }
   ```
2. **Automated Evidence Reconciliation:**  
   A dedicated Java utility/TestNG listener scans the Surefire XML outputs post-execution, cross-referencing `@TestCase` IDs against the master CSV. It generates:
   * Real Execution Summary (Pass / Fail / Skipped / Not Run)
   * Real Requirements Traceability Matrix (RTM) coverage percentages
3. **Artifact Retention Policy:**  
   All run logs, Surefire XMLs, Allure result bundles, failure screenshots, and Playwright zip traces are persisted in `evidence/`.

---

## 5. Test Data Management Strategy

1. **Dynamic Unique Data:** To prevent collisions during parallel or re-runs, customer emails and test identifiers are generated dynamically using UUID/timestamp builders (e.g., `qa_user_1728638400@testmail.com`).
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
| **Runtime** | Java SE | 21 LTS | Primary compilation & execution runtime |
| **Build & Dep** | Apache Maven | 3.9+ | Dependency resolution, build lifecycle, Surefire runner |
| **Automation** | Playwright Java | 1.63.0 (latest stable) | Browser automation, APIRequestContext client |
| **Test Runner** | TestNG | 7.11.0 | Suite organization, grouping, parallel execution |
| **Accessibility** | Deque axe-core | 4.4.1+ (via Maven) | Automated WCAG 2.1 AA DOM scanning |
| **Database** | Microsoft SQL Server | 2022 Developer | Primary persistence engine for containerized AUT |
| **Performance** | k6 | Latest stable CLI | HTTP route load and latency baseline benchmarking |
| **Reporting** | TestNG Native + Allure | 2.29.0 (AspectJ 1.9.22) | Primary execution reports + failure attachments |
| **CI/CD** | Jenkins + GitHub Actions | Jenkinsfile / YAML | Primary full pipeline (Jenkins), secondary smoke gate (GHA) |
