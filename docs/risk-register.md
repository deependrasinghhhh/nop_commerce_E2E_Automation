# Quality & Technical Risk Register — nopCommerce QA Engineering Project

> **Document ID:** RSK-NOP-4.90.8  
> **Status:** Active (Reviewed & Self-Assessed)  
> **Scope:** Technical, Tooling, Infrastructure, and Quality Risks  
> **Last Reviewed:** 2026-11-25  

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
| `RSK-005` | Tooling | **Playwright Java Thread Safety Concurrency Violation**<br>Playwright Java objects (`Playwright`, `BrowserContext`, `Page`) are NOT thread-safe. Concurrent TestNG threads sharing instances will corrupt state and crash JVM. | 3 | 3 | **9 (Crit)** | Enforce strict isolation using `ThreadLocal<Playwright>`, `ThreadLocal<Browser>`, `ThreadLocal<BrowserContext>`, and `ThreadLocal<Page>` in `PlaywrightFactory`. Close contexts in `@AfterMethod` and browsers in `@AfterClass`. | Run single-threaded (`-DthreadCount=1`) on resource-constrained hosts while retaining per-method context isolation. | Mitigated |
| `RSK-006` | Tooling | **TestNG `-Dgroups` Friction with Suite XML Files**<br>Passing `-Dgroups=smoke` from the Maven command line when Surefire is configured with `suiteXmlFiles` can fail to filter or conflict with suite XML group definitions. | 3 | 2 | **6 (High)** | Provide discrete, dedicated TestNG XML files per execution tier (`smoke.xml`, `regression.xml`, `ui.xml`, `api.xml`, `db.xml`, `a11y.xml`, `nfr.xml`) exposed via Maven profiles (`-Psmoke`, `-Pregression`, etc.). | Target individual classes directly with `-Dtest=...` (`TestListener` remains bound via Surefire properties). | Mitigated |
| `RSK-007` | Tooling | **Allure TestNG Attachment & Runtime Metadata Gaps**<br>Allure reporting can miss runtime metadata or failure attachments if listeners are bypassed or rely solely on AspectJ `@Step` weaving. | 2 | 2 | **4 (Med)** | Register `TestListener` explicitly in Surefire and suite XMLs to attach failure screenshots, Playwright trace `.zip` archives, `environment.properties`, `categories.json`, and `executor.json` directly without AspectJ load-time weaving. | Retain TestNG Surefire XML/HTML reports (`evidence/surefire-reports/`) as the primary auditable source of truth. | Mitigated |
| `RSK-008` | Process | **Evidence Integrity & Hand-Typed Metrics**<br>QA documentation often suffers from manual data entry error, resulting in fictional pass rates, fake sprint tags, and unverified coverage numbers. | 2 | 3 | **6 (High)** | Enforce `scripts/reconcile_rtm.py` (`--verify-only` in CI, `--update-baseline` on full regression) to verify 1:1 parity across `requirements.md`, `test-cases.csv`, `@TestCase` annotations, and `testng-results.xml`. | Reject partial-suite baseline overwrites unless `--allow-partial` is explicitly requested. | Mitigated |
| `RSK-009` | Process | **Accessibility Scan Flakiness & Scope Creep**<br>Automated axe-core scans may produce false positives on third-party elements or inconsistent results on dynamic DOM mutations. | 2 | 2 | **4 (Med)** | Scope accessibility tests strictly to stable core pages (`/`, `/login`, `/register`, `/cart`, `/desktops`) and assert against WCAG 2.1 AA rules using `com.deque.html.axe-core:playwright`. | Exclude dynamic third-party widgets or iframe wrappers via AxeBuilder selector exclusion rules if noisy. | Active |
| `RSK-010` | Automation | **Fragile Selectors & UI Timing Flakiness**<br>Hardcoded positional XPaths or unwaited DOM reads break on theme or markup updates and cause parallel flakiness. | 2 | 2 | **4 (Med)** | Use resilient element ID, attribute, and semantic CSS/text locators (`#Email`, `data-valmsg-for`, `.filter(setHasText(...))`) with zero positional XPaths, backed by `BasePage` bounded waits (`waitForVisible`, `waitForCondition`, `PlaywrightAssertions`). | Add defensive visibility waits before optional text extraction on async UI banners. | Mitigated |
| `RSK-011` | Infrastructure | **MSSQL JDBC Connectivity, Pool Leakage & Credential Drift**<br>Direct DB verification queries may fail due to port 1433 binding conflicts, JDBC driver incompatibility, unclosed connections exhausting the pool, or credentials hardcoded into test classes. | 2 | 3 | **6 (High)** | Build a thread-safe `DatabaseManager` using HikariCP with try-with-resources (failing fast on pool faults and closed via `TestListener.onFinish(ISuite)`). Externalize all credentials to `.env` / system properties with `.env.example` template. Validate connectivity with `@BeforeClass` in `DatabaseVerificationTest`. | If DB container is down locally, DB tests skip cleanly; when `CI=true` or `-Ddb.failIfUnavailable=true`, fail loudly. | Mitigated |
| `RSK-012` | Accessibility | **Upstream Storefront Color-Contrast Debt (`DEF-A11Y-001`)**<br>All 5 audited storefront pages (`/`, `/register`, `/login`, `/cart`, `/desktops`) contain WCAG 2.1 AA `serious` `color-contrast` violations (`15, 4, 5, 3, and 13` nodes respectively), notably white text on `#4ab2f1` primary buttons at 2.35:1 vs. 4.5:1. Storefront CSS lives in the upstream `nopcommerceteam/nopcommerce:4.90.8` image and cannot be patched from this repo. | 3 | 2 | **6 (High)** | Retain full axe-core violation trees under `evidence/a11y/` and gate CI at `critical` impact (`a11y.failOnImpact=critical`) so severe regressions fail builds while upstream CSS contrast debt remains transparently tracked. | Run strict accessibility audits on demand with `-Pa11y -Da11y.failOnImpact=serious` to verify node-level contrast remediation once upstream theme CSS is updated. | Accepted Debt |

---

## 3. Risk Review Schedule

The Risk Register will be re-evaluated at the conclusion of each milestone:
* **Milestone 4 Checkpoint:** Re-verify `RSK-003` and `RSK-004` (Docker and MSSQL readiness).
* **Milestone 5 Checkpoint:** Re-verify `RSK-005` (ThreadLocal safety) and `RSK-006` (TestNG suite filtering).
* **Milestone 7 Checkpoint:** Re-verify `RSK-007` (AspectJ weaver / Allure status).
* **Milestone 9 Checkpoint:** Re-verify `RSK-008` (Evidence traceability completeness).
