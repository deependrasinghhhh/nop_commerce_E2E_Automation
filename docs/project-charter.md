# Project Charter — nopCommerce QA Engineering Project

> **Project:** nopCommerce QA Portfolio Rebuild  
> **Technology Stack:** Java 21 LTS · Playwright Java · TestNG · Maven  
> **AUT:** nopCommerce v4.90.8 (.NET 9, Dockerized)  
> **Sign-off / Reviewer Status:** Self-assessed by the project author acting as QA Lead. No other reviewer or stakeholder was involved.  
> **Execution Base Date:** October 2026  

---

## 1. Project Background and Objective

### 1.1 Context & Transparency Declaration
This repository is a **self-directed portfolio simulation** of an end-to-end QA Automation Engineering engagement. It is an explicit rebuild of an earlier TypeScript/Playwright Test implementation onto an enterprise Java ecosystem: **Java 21 LTS + Playwright Java + TestNG + Maven**.

To maintain absolute credibility and professional integrity:
* **No fabricated stakeholders:** No imaginary Product Owners, DevOps Engineers, or Scrum Masters are cited as signing off on deliverables.
* **No fictional builds or sprints:** Identifiers reflect real Git commit SHAs, reproducible Docker image digests, and real execution timestamps.
* **Evidence-driven metrics:** Every test execution count, pass percentage, defect tally, and latency measurement reported in this project derives strictly from machine-generated artifacts (`target/surefire-reports`, Allure results, k6 summaries). Zero metrics are typed by hand.

### 1.2 Core Objectives
1. **Full-Stack QA Architecture:** Build a robust, scalable Page Object Model (POM) automation framework with thread-safe parallel execution (`ThreadLocal<Playwright>`).
2. **Deterministic Test Execution:** Provide 80–120 high-value automated test scenarios spanning functional UI, HTTP protocol/form validation, accessibility, and database verification.
3. **Rigorous Traceability:** Link requirements to tests to results via a custom `@TestCase` annotation and an automated RTM generator script.
4. **Isolated & Reproducible Infrastructure:** Containerize the AUT and database using Docker Compose (`nopcommerceteam/nopcommerce:4.90.8` + Microsoft SQL Server 2022).
5. **Modern CI/CD Pipeline:** Demonstrate primary Jenkins pipeline execution (from environment bring-up through regression, a11y, and reporting) and a secondary GitHub Actions smoke gate.

---

## 2. Application Under Test (AUT) Profile

* **Application:** nopCommerce
* **Verified Version:** 4.90.8 (Official release: September 2026)
* **Architecture:** ASP.NET Core (.NET 9), Linq2DB ORM, MVC Server-Side Rendering
* **Container Distribution:** `nopcommerceteam/nopcommerce:4.90.8` (Docker Hub)
* **Target Database:** Microsoft SQL Server 2022 Developer Edition (`mcr.microsoft.com/mssql/server:2022-latest`)
* **Primary Test Target:** Locally containerized nopCommerce instance (`http://localhost:5000` or `http://localhost:80`)
* **Reference Public Demo:** `https://demo.nopcommerce.com/` (used solely for exploratory discovery in Milestone 0)

---

## 3. Scope of Testing

### 3.1 In-Scope Modules & Capabilities
1. **Storefront Functional UI:**
   * User Registration, Login, Logout, Password Recovery (`/register`, `/login`, `/passwordrecovery`)
   * Catalog Browsing, Multi-tier Navigation, Search with auto-suggest, Product Filtering and Sorting
   * Product Details Page: Attribute selection, pricing validation, stock availability
   * Shopping Cart & Wishlist: Add, update quantities, remove items, estimate shipping
   * End-to-End Checkout: Multi-step checkout flow (Guest & Registered user) with standard "Check / Money Order" payment
   * Customer Account: Profile updates, address management, order history inspection
   * Community & Feedback: Product review submission, contact form validation
2. **Admin Operations UI:**
   * Administrative Authentication (`/Admin`)
   * Dashboard metric overview
   * Catalog product search and verification
   * Order status lifecycle inspection
3. **HTTP Protocol & Form Validation (API Layer):**
   * Verification of core HTTP routes, response status codes, redirects, and headers
   * Form submission validation (POST) including ASP.NET `__RequestVerificationToken` anti-forgery handling
   * Storefront AJAX interaction interception (e.g., mini-cart update, search suggestions)
4. **Accessibility (a11y):**
   * Automated WCAG 2.1 AA compliance scans using `com.deque.html.axe-core:playwright` on key storefront viewports
5. **Database Verification (DB Layer):**
   * Direct, read-only JDBC queries against MS SQL Server to verify backend state persistence post-UI actions (e.g., customer record creation, order persistence)
6. **Performance Profiling:**
   * Baseline load and latency testing of core web routes using k6 against the local containerized instance

### 3.2 Explicitly Out-of-Scope (With Rationales)
* **REST API Regression Testing:** nopCommerce core does not expose a public REST API out of the box (requires commercial paid Web API plugin). Testing will strictly cover HTTP protocol and form layers.
* **Third-Party Payment Gateways:** External live payment processing (e.g., real Stripe, PayPal, Authorize.Net gateways) is excluded; built-in sandbox/offline methods ("Check / Money Order") will be used.
* **Third-Party Email Server Delivery:** SMTP outbound delivery to external mailboxes is excluded; form actions will be validated via UI confirmation banners and database queues.
* **B2B Complex Modules:** Advanced v4.90 B2B enterprise workflows (Request for Quote, Year-Make-Model search) requiring complex custom catalog setup.

---

## 4. Quality Governance & Non-Negotiable Rules

1. **Evidence Contract:** Every statistic reported in closure documentation must reference a persisted physical artifact in `evidence/`. If a test was not executed, its status is recorded as `NOT RUN`.
2. **Defect Authenticity:** Genuine defects discovered in nopCommerce 4.90.8 will be documented with complete reproduction steps and Playwright traces. Simulated defects used to demonstrate defect lifecycle tooling must be explicitly prefixed with `[SIMULATED]`.
3. **Traceability by Code:** All automated test methods must declare `@TestCase("TC-...")`. An automated generator reconciles this against `docs/requirements.md` and Surefire XML results.
4. **Strict Technology Discipline:** Core automation is restricted to Java 21 LTS, Playwright Java, TestNG, and Maven. No mixing with Selenium or JUnit.

---

## 5. Milestone Road Map

| Milestone | Deliverable Summary | Verification Gate |
|---|---|---|
| **M0: Discovery** | AUT versioning, DB engine selection, API availability reality check | Verified in `docs/application-overview.md` |
| **M1: Charter & Requirements** | Project Charter, Functional/Non-Functional Requirements, Risk Register | Verified requirements hierarchy and risk matrix |
| **M2: Strategy & Plan** | Test Strategy, Master Test Plan, Environment & Data Strategy | Entry/Exit criteria defined |
| **M3: Scenarios & RTM** | 80–120 test cases in CSV, Traceability Matrix generator design | Generated RTM baseline |
| **M4: Environment** | Docker Compose with pinned nopCommerce 4.90.8 + MSSQL 2022 | Verified container startup logs |
| **M5: Automation Skeleton** | Maven POM, Playwright `ThreadLocal` factory, passing smoke test | Working `mvn test` execution log |
| **M6: Automation Coverage** | UI, HTTP/Form, A11y, DB verification test suites | Fully passing automated test suites |
| **M7: Reporting** | Native TestNG HTML/XML + Allure reporting with failure listeners | Persisted report artifacts |
| **M8: CI/CD Pipeline** | Primary Jenkinsfile pipeline + GitHub Actions secondary gate | Executed CI pipeline logs |
| **M9: Closure & Sign-off** | Self-assessed QA Sign-Off report generated directly from evidence | Persisted evidence artifacts in `evidence/` |
