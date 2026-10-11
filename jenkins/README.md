# Jenkins CI/CD Pipeline Documentation

This directory contains the production-grade Declarative Jenkins Pipeline (`Jenkinsfile`) for continuous integration, regression gating, and containerized execution of the nopCommerce 4.90.8 QA framework.

---

## 1. Pipeline Architecture

```
[Trigger / Webhook] 
       │
       ▼
[Stage 1: Toolchain Setup (Java 21, Maven 3.9)]
       │
       ▼
[Stage 2: Container Lifecycle Startup (Docker Compose: nopCommerce + MSSQL)]
       │
       ▼
[Stage 3: Playwright Browser Installation (Chromium / Webkit / Firefox)]
       │
       ▼
[Stage 4: Automated Test Execution (Maven Surefire + TestNG Suite)]
       │
       ▼
[Stage 5: RTM Traceability Reconciliation (scripts/reconcile_rtm.py)]
       │
       ▼
[Post: Publish JUnit XML, HTML Reports, Failure Screenshots, and Container Teardown]
```

---

## 2. Parameter Reference

| Parameter | Type | Default | Description |
|---|---|---|---|
| `ENVIRONMENT` | Choice | `container` | Target environment: `container` (local Docker instance) or `demo_fallback` (live demo site). |
| `SUITE` | Choice | `suites/api.xml` | TestNG XML suite definition to run (`smoke.xml`, `api.xml`, `ui.xml`, `regression.xml`, `db.xml`). |
| `BROWSER` | Choice | `chromium` | Target browser engine: `chromium`, `firefox`, or `webkit`. |
| `HEADLESS` | Boolean | `true` | Executes browser in headless mode (headless = true in CI agents). |
| `THREAD_COUNT`| String | `2` | Parallel thread count across test classes. |

---

## 3. Jenkins Agent Prerequisites

1. **JDK 21 LTS** configured in Global Tool Configuration as `Java-21`.
2. **Maven 3.9+** configured as `Maven-3.9`.
3. **Docker & Docker Compose** installed on Jenkins agent with permission to bind ports `5000` (web) and `1433` (MSSQL).
4. **Plugins Required**:
   - `HTML Publisher Plugin` (for TestNG index report rendering).
   - `JUnit Plugin` (for test result trend graphs).
   - `AnsiColor Plugin` (for colored Maven test logs).
   - `Pipeline Utility Steps` (for reading and archiving JSON/YAML).

---

## 4. Retained Artifacts in Build Archive

Each Jenkins pipeline run archives:
- `target/surefire-reports/*.xml`: Machine-readable JUnit XML test results.
- `evidence/reports/`: Full TestNG HTML report (`index.html` and `emailable-report.html`).
- `evidence/failures/**`: Failure screenshots (`.png`) and Playwright trace archives (`.zip`) captured on assertion timeouts.
- `docs/traceability-matrix.md`: Machine-reconciled RTM reflecting the exact build output.
