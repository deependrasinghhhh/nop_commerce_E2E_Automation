# Quality & Technical Risk Register — nopCommerce QA Engineering Project

> **Document ID:** RSK-NOP-4.90.8  
> **Status:** Active (Reviewed & Self-Assessed)  
> **Scope:** Technical, Tooling, Infrastructure, and Quality Risks  
> **Base Evaluation Date:** October 2026  

---

## 1. Risk Evaluation Matrix

Risk scores are assessed based on **Likelihood (L)** and **Impact (I)** on a 3-point scale (1: Low, 2: Medium, 3: High).  
**Risk Priority Score (RPN) = Likelihood × Impact.**

* **Critical (RPN 7–9):** Requires immediate architectural safeguards and blocking mitigation before proceeding.
* **High (RPN 5–6):** Requires explicit technical designs and continuous monitoring.
* **Medium (RPN 3–4):** Manageable through standard test design patterns and tooling defaults.
* **Low (RPN 1–2):** Acceptable with basic documentation.

---

## 2. Risk Inventory

| Risk ID | Category | Title & Threat Scenario | L | I | RPN | Mitigation Strategy | Contingency Plan | Status |
|---|---|---|---|---|---|---|---|---|
| `RSK-001` | Technical | **Absence of Core REST API**<br>nopCommerce lacks native REST endpoints without a commercial plugin. Attempting to run classic REST API tests will fail or lead to fabricated mocks. | 3 | 3 | **9 (Crit)** | Re-scope the API testing layer honestly to HTTP protocol level: route status codes, redirect sequences, and form POST payloads using Playwright `APIRequestContext`. | If specific data fixtures are needed, use direct read-only SQL queries or UI-driven setup rather than mocking API endpoints. | Mitigated |
| `RSK-002` | Technical | **ASP.NET Anti-Forgery Token Validation**<br>State-changing HTTP POSTs (e.g., login, register) fail with HTTP 400/404 if the hidden `__RequestVerificationToken` and cookie are missing. | 3 | 2 | **6 (High)** | Implement an HTTP client utility in Playwright that issues a prior GET request, parses the anti-forgery token from the DOM, and passes it alongside session cookies in POST headers. | Fall back to UI-based browser actions if token parsing fails on complex forms. | Active |
| `RSK-003` | Infrastructure | **Public Demo Hourly Data Reset Drift**<br>The public demo at `demo.nopcommerce.com` resets state every 60 minutes and is subject to concurrent multi-tenant edits, causing non-deterministic automation failures. | 3 | 3 | **9 (Crit)** | Decouple automated test execution from the public demo. Use Docker Compose to spin up an isolated, local nopCommerce 4.90.8 container + MSSQL database for all automation. | Use public demo solely for quick read-only exploratory discovery, never for CI/CD gates. | Active |
| `RSK-004` | Infrastructure | **MSSQL Linux Container TLS/SSL Handshake Friction**<br>Recent Linux distributions and .NET runtimes enforce TLS 1.3, which can cause connection failures to MSSQL containers on Linux with `"Encryption(ssl/tls) handshake failed"`. | 2 | 3 | **6 (High)** | Configure the SQL Server connection string with `TrustServerCertificate=True;Encrypt=False` and use Microsoft SQL Server 2022 Developer edition image. | Add explicit OpenSSL configuration adjustments to the container environment if handshake errors occur. | Active |
| `RSK-005` | Tooling | **Playwright Java Thread Safety Concurrency Violation**<br>Playwright Java objects (`Playwright`, `BrowserContext`, `Page`) are NOT thread-safe. Concurrent TestNG threads sharing instances will corrupt state and crash JVM. | 3 | 3 | **9 (Crit)** | Enforce strict isolation using `ThreadLocal<Playwright>`, `ThreadLocal<Browser>`, and `ThreadLocal<Page>` in `BrowserFactory`. Clean up contexts in `@AfterMethod`. | Enforce single-threaded execution (`parallel="none"`) in initial baseline suites until ThreadLocal factory is proven stable. | Active |
| `RSK-006` | Tooling | **TestNG `-Dgroups` Friction with Suite XML Files**<br>Passing `-Dgroups=smoke` from the Maven command line when Surefire is configured with `suiteXmlFiles` can fail to filter or conflict with suite XML group definitions. | 3 | 2 | **6 (High)** | Provide discrete, dedicated TestNG XML files per execution tier (`smoke.xml`, `regression.xml`, `full.xml`) rather than relying solely on `-Dgroups` overrides over a single XML. | Verify `mvn test -DsuiteXmlFile=...` in Milestone 5 before building multi-suite CI pipelines. | Active |
| `RSK-007` | Tooling | **Allure TestNG AspectJ Weaver Integration Failure**<br>Allure TestNG reporting fails to capture steps and attachments if the AspectJ weaver `-javaagent` is not configured or resolved correctly in Surefire `argLine`. | 2 | 2 | **4 (Med)** | Rely on native TestNG HTML/XML reports as the primary reporting mechanism. Treat Allure as a secondary enhancement, marked NOT VERIFIED until proven in M7. | Keep TestNG's native Surefire and HTML reports as the uncompromised source of truth. | Monitored |
| `RSK-008` | Process | **Evidence Integrity & Hand-Typed Metrics**<br>QA documentation often suffers from manual data entry error, resulting in fictional pass rates, fake sprint tags, and unverified coverage numbers. | 2 | 3 | **6 (High)** | Implement an automated RTM generator script (in Milestone 3/4) that cross-references `@TestCase` annotations in code with `test-cases.csv` and Surefire XML results. | Any metric without a corresponding file in `evidence/` remains marked `NOT RUN` or `NOT VERIFIED`. | Active |
| `RSK-009` | Process | **Accessibility Scan Flakiness & Scope Creep**<br>Automated axe-core scans may produce false positives on third-party elements or inconsistent results on dynamic DOM mutations. | 2 | 2 | **4 (Med)** | Scope accessibility tests strictly to stable core pages (`/`, `/login`, `/register`, `/cart`, PDP) and assert against WCAG 2.1 AA rules using the official Deque Playwright Java binding. | Exclude dynamic third-party widgets or iframe wrappers via AxeBuilder selector exclusion rules if noisy. | Active |
| `RSK-010` | Automation | **Fragile CSS/XPath Selectors & UI Flakiness**<br>Hardcoded layout-dependent CSS or XPath selectors break on theme or markup updates, causing maintenance fatigue. | 2 | 2 | **4 (Med)** | Adhere strictly to user-facing locators (`getByRole`, `getByLabel`, `getByPlaceholder`, `getByText`) provided natively by Playwright. Avoid brittle positional XPaths. | Implement explicit Playwright web assertions with built-in auto-waiting (`assertThat(locator).toBeVisible()`). | Active |
| `RSK-011` | Infrastructure | **MSSQL JDBC Connectivity, Pool Leakage & Credential Drift**<br>Direct DB verification queries may fail due to port 1433 binding conflicts, JDBC driver incompatibility, unclosed connections exhausting the pool, or credentials hardcoded into test classes. | 2 | 3 | **6 (High)** | Build a thread-safe `DatabaseManager` using HikariCP or standard JDBC try-with-resources. Externalize all credentials to `.env` / system properties with `.env.example` template. Validate connectivity with an early ping in `@BeforeSuite`. | If DB container is down or unreachable during execution, soft-fail DB assertions with clear diagnostic logs while preserving UI test isolation. | Active |

---

## 3. Risk Review Schedule

The Risk Register will be re-evaluated at the conclusion of each milestone:
* **Milestone 4 Checkpoint:** Re-verify `RSK-003` and `RSK-004` (Docker and MSSQL readiness).
* **Milestone 5 Checkpoint:** Re-verify `RSK-005` (ThreadLocal safety) and `RSK-006` (TestNG suite filtering).
* **Milestone 7 Checkpoint:** Re-verify `RSK-007` (AspectJ weaver / Allure status).
* **Milestone 9 Checkpoint:** Re-verify `RSK-008` (Evidence traceability completeness).
