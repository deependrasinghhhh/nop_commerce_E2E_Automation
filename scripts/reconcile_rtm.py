#!/usr/bin/env python3
"""Reconcile test-case annotations and generate reports from one TestNG run."""

import csv
import getpass
import json
import os
import re
import shutil
import socket
import sys
import tempfile
import xml.etree.ElementTree as ET
from collections import Counter, defaultdict
from datetime import datetime, timezone
from pathlib import Path


ROOT_DIR = Path(__file__).resolve().parent.parent
CSV_PATH = ROOT_DIR / "docs" / "test-cases.csv"
JAVA_SRC_DIR = ROOT_DIR / "src" / "test" / "java"
EVIDENCE_DIR = ROOT_DIR / "evidence" / "surefire-reports"
EVIDENCE_REPORTS_DIR = ROOT_DIR / "evidence" / "reports"
EVIDENCE_XML = EVIDENCE_DIR / "testng-results.xml"
TARGET_XML = ROOT_DIR / "target" / "surefire-reports" / "testng-results.xml"
REPORT_TEXT_SUFFIXES = {".xml", ".html", ".txt"}
RTM_OUT_PATH = ROOT_DIR / "docs" / "traceability-matrix.md"
SUMMARY_OUT_PATH = ROOT_DIR / "docs" / "execution-summary.md"
CLOSURE_OUT_PATH = ROOT_DIR / "docs" / "test-closure-report.md"
README_PATH = ROOT_DIR / "README.md"
POM_PATH = ROOT_DIR / "pom.xml"
K6_SUMMARY_PATH = ROOT_DIR / "evidence" / "performance" / "k6-summary.json"

CLASS_LABELS = {
    "SmokeTest": "Storefront Smoke Gate",
    "AuthTest": "Authentication & Account Lifecycle",
    "CatalogTest": "Catalog Browsing, Search & Sorting",
    "CartTest": "Shopping Cart, Wishlist & Coupons",
    "CheckoutTest": "Checkout Flow",
    "CustomerAccountTest": "Customer Profile, Address CRUD & Reviews",
    "AdminTest": "Administration Portal",
    "ApiEndpointTest": "API Endpoint Integration",
    "DatabaseVerificationTest": "Database Persistence",
    "AccessibilityTest": "WCAG 2.1 AA Accessibility",
    "NonFunctionalTest": "Non-Functional & Responsive SLA",
}


def load_test_cases():
    with CSV_PATH.open("r", encoding="utf-8", newline="") as source:
        return list(csv.DictReader(source))


def scan_java_annotations():
    tc_map = {}
    annotation_pattern = re.compile(r'@TestCase\(\s*(?:value\s*=\s*)?"([^"]+)"')
    method_pattern = re.compile(r'public\s+void\s+([a-zA-Z0-9_]+)\s*\(')

    for root, _, files in os.walk(JAVA_SRC_DIR):
        for filename in sorted(files):
            if not filename.endswith(".java"):
                continue
            path = Path(root) / filename
            relative_path = path.relative_to(ROOT_DIR).as_posix()
            class_name = path.stem
            current_test_case = None
            for line in path.read_text(encoding="utf-8").splitlines():
                annotation = annotation_pattern.search(line)
                if annotation:
                    current_test_case = annotation.group(1)
                method = method_pattern.search(line)
                if method and current_test_case:
                    tc_map.setdefault(current_test_case, []).append({
                        "class": class_name,
                        "method": method.group(1),
                        "file": relative_path,
                    })
                    current_test_case = None
    return tc_map


def parse_testng_time(value):
    if not value:
        return None
    value = value.strip()
    timezone_offsets = {"IST": "+05:30", "UTC": "+00:00", "GMT": "+00:00"}
    match = re.fullmatch(r"(.+?)\s+(IST|UTC|GMT)", value)
    if match:
        value = match.group(1) + timezone_offsets[match.group(2)]
    elif value.endswith("Z"):
        value = value[:-1] + "+00:00"
    parsed = datetime.fromisoformat(value)
    if parsed.tzinfo is None:
        raise ValueError(f"TestNG timestamp has no recognized timezone: {value}")
    return parsed


def sanitize_report_text(text):
    """Scrub developer machine paths, usernames, hostnames, and PATH dumps from retained reports."""
    home_dir = Path.home()
    temp_dir = Path(tempfile.gettempdir())
    replacements = [
        (str(temp_dir), "/tmp"),
        (temp_dir.as_posix(), "/tmp"),
        (str(ROOT_DIR), "/workspace/nopcommerce-qa-java"),
        (ROOT_DIR.as_posix(), "/workspace/nopcommerce-qa-java"),
        (str(home_dir), "/home/qa-runner"),
        (home_dir.as_posix(), "/home/qa-runner"),
    ]
    java_home = os.environ.get("JAVA_HOME")
    if java_home:
        replacements.append((java_home, "/opt/java/openjdk"))
        replacements.append((Path(java_home).as_posix(), "/opt/java/openjdk"))

    # Match AppData\Local\Temp before stripping user home directories
    text = re.sub(r"[A-Za-z]:\\Users\\[^\\;\"'<>\r\n]+\\AppData\\Local\\Temp", "/tmp", text, flags=re.IGNORECASE)
    text = re.sub(r"[A-Za-z]:/Users/[^/;\"'<>\r\n]+/AppData/Local/Temp", "/tmp", text, flags=re.IGNORECASE)

    for raw, clean in replacements:
        if raw:
            text = re.sub(re.escape(raw), clean, text, flags=re.IGNORECASE)

    # Generic fallback patterns for previously generated Windows paths/usernames
    text = re.sub(
        r"[A-Za-z]:\\Users\\[^\\;\"'<>\r\n]+\\(?:OneDrive\\Desktop\\)?E2E_Automation_nopcommerce",
        "/workspace/nopcommerce-qa-java",
        text,
        flags=re.IGNORECASE,
    )
    text = re.sub(
        r"[A-Za-z]:/Users/[^/;\"'<>\r\n]+/(?:OneDrive/Desktop/)?E2E_Automation_nopcommerce",
        "/workspace/nopcommerce-qa-java",
        text,
        flags=re.IGNORECASE,
    )
    text = re.sub(r"[A-Za-z]:\\Users\\[^\\;\"'<>\r\n]+", "/home/qa-runner", text, flags=re.IGNORECASE)
    text = re.sub(r"[A-Za-z]:/Users/[^/;\"'<>\r\n]+", "/home/qa-runner", text, flags=re.IGNORECASE)
    text = re.sub(r"/home/qa-runner/AppData/Local/Temp", "/tmp", text, flags=re.IGNORECASE)
    text = re.sub(r"[A-Za-z]:\\Program Files\\Java\\jdk-[^\\;\"'<>\r\n]+", "/opt/java/openjdk", text, flags=re.IGNORECASE)

    # Normalize Surefire XML host/user/library-path metadata
    text = re.sub(r'hostname="[^"]*"', 'hostname="ci-runner"', text)
    text = re.sub(r'(<property name="user\.name" value=")[^"]*(")', r'\1qa-runner\2', text)
    text = re.sub(r'(<property name="java\.library\.path" value=")[^"]*(")', r'\1[REDACTED]\2', text)
    text = re.sub(r'(<property name="sun\.java\.command" value=")[^"]*surefirebooter[^"]*(")',
                  r'\1/tmp/surefirebooter.jar\2', text)

    # Clean forward slashes inside normalized workspace/home paths
    text = re.sub(
        r"(/(?:workspace/nopcommerce-qa-java|home/qa-runner|opt/java/openjdk|tmp))([\\/][^\"'<>;\r\n]*)",
        lambda m: m.group(1) + m.group(2).replace("\\", "/"),
        text,
    )

    try:
        current_user = getpass.getuser()
        if current_user and len(current_user) >= 3:
            text = text.replace(current_user, "qa-runner")
    except Exception:
        pass
    try:
        current_host = socket.gethostname()
        if current_host and len(current_host) >= 3:
            text = text.replace(current_host, "ci-runner")
    except Exception:
        pass
    return text


def sync_sanitized_tree(src_dir, dst_dir):
    dst_dir.mkdir(parents=True, exist_ok=True)
    for item in src_dir.iterdir():
        dest = dst_dir / item.name
        if item.is_file():
            if item.suffix.lower() in REPORT_TEXT_SUFFIXES:
                raw = item.read_text(encoding="utf-8", errors="replace")
                dest.write_text(sanitize_report_text(raw), encoding="utf-8", newline="\n")
            else:
                shutil.copy2(item, dest)
        elif item.is_dir():
            sync_sanitized_tree(item, dest)


def scrub_existing_reports():
    for report_dir in (EVIDENCE_DIR, EVIDENCE_REPORTS_DIR):
        if not report_dir.is_dir():
            continue
        for item in report_dir.rglob("*"):
            if item.is_file() and item.suffix.lower() in REPORT_TEXT_SUFFIXES:
                raw = item.read_text(encoding="utf-8", errors="replace")
                cleaned = sanitize_report_text(raw)
                if cleaned != raw:
                    item.write_text(cleaned, encoding="utf-8", newline="\n")


def load_execution_report():
    if TARGET_XML.is_file():
        sync_sanitized_tree(TARGET_XML.parent, EVIDENCE_DIR)
        scrub_existing_reports()
        report_path = EVIDENCE_XML
    elif EVIDENCE_XML.is_file():
        scrub_existing_reports()
        report_path = EVIDENCE_XML
    else:
        raise FileNotFoundError(
            "No TestNG results found. Run a TestNG suite before reconciling evidence."
        )

    root = ET.parse(report_path).getroot()
    test_results = {}
    class_counts = defaultdict(Counter)

    for class_node in root.findall(".//class"):
        full_class_name = class_node.get("name", "")
        class_name = full_class_name.rsplit(".", 1)[-1]
        for method_node in class_node.findall("./test-method"):
            if method_node.get("is-config") == "true":
                continue
            method_name = method_node.get("name")
            status = method_node.get("status", "").upper()
            normalized = {"PASS": "PASS", "FAIL": "FAIL", "SKIP": "SKIPPED"}.get(status)
            if not method_name or not normalized:
                continue
            test_results[(class_name, method_name)] = normalized
            class_counts[class_name][normalized] += 1

    suites = root.findall("./suite") or root.findall(".//suite")
    starts = [parse_testng_time(node.get("started-at")) for node in suites]
    finishes = [parse_testng_time(node.get("finished-at")) for node in suites]
    starts = [stamp for stamp in starts if stamp]
    finishes = [stamp for stamp in finishes if stamp]
    if not starts or not finishes:
        raise ValueError(f"TestNG run timestamps are missing from {report_path}")

    start = min(starts)
    finish = max(finishes)
    duration_seconds = max(0.0, (finish - start).total_seconds())
    metadata = {
        "path": report_path.relative_to(ROOT_DIR).as_posix(),
        "start": start.astimezone(timezone.utc),
        "finish": finish.astimezone(timezone.utc),
        "duration_seconds": duration_seconds,
    }
    return test_results, dict(class_counts), metadata


def utc_text(value):
    return value.strftime("%Y-%m-%dT%H:%M:%SZ")


def run_timestamp(metadata):
    return metadata["finish"].strftime("%Y-%m-%d %H:%M:%S UTC")


def execution_window(metadata):
    return (
        f"{utc_text(metadata['start'])} to {utc_text(metadata['finish'])} "
        f"({metadata['duration_seconds']:.2f}s)"
    )


def suite_rows(class_counts):
    rows = []
    for class_name, counts in sorted(
        class_counts.items(),
        key=lambda item: (list(CLASS_LABELS).index(item[0]) if item[0] in CLASS_LABELS else 999, item[0]),
    ):
        total = sum(counts.values())
        rows.append((CLASS_LABELS.get(class_name, class_name), counts, total))
    return rows


def percent(numerator, denominator):
    return (numerator / denominator * 100) if denominator else 0.0


def stack_summary():
    pom = ET.parse(POM_PATH).getroot()
    namespace = {"m": "http://maven.apache.org/POM/4.0.0"}
    properties = pom.find("m:properties", namespace)
    if properties is None:
        raise ValueError(f"Could not find Maven properties in {POM_PATH}")

    def property_value(name):
        element = properties.find(f"m:{name}", namespace)
        if element is None or not element.text:
            raise ValueError(f"Could not find Maven property '{name}' in {POM_PATH}")
        return element.text.strip()

    k6_report = json.loads(K6_SUMMARY_PATH.read_text(encoding="utf-8"))
    k6_version = k6_report["benchmark_metadata"]["tool"].removeprefix("k6 ")
    return (
        f"Java {property_value('maven.compiler.release')} | "
        f"Playwright Java {property_value('playwright.version')} | "
        f"TestNG {property_value('testng.version')} | "
        f"Allure {property_value('allure.version')} | "
        f"Axe Playwright {property_value('axe.playwright.version')} | k6 {k6_version}"
    )


def k6_summary():
    report = json.loads(K6_SUMMARY_PATH.read_text(encoding="utf-8"))
    metadata = report["benchmark_metadata"]
    request_metrics = report["metrics"]["http_reqs"]
    latency_metrics = report["metrics"]["http_req_duration"]
    return (
        f"{metadata['tool']} separate HTTP browse/search workload: "
        f"{metadata['vus_peak']} peak VUs for {metadata['duration_seconds']} seconds, "
        f"{request_metrics['count']} requests, p95 {latency_metrics['p95_ms']} ms"
    )


def replace_readme_summary(section):
    readme = README_PATH.read_text(encoding="utf-8")
    pattern = re.compile(
        r"## Reconciled Execution Summary.*?(?=## Required Secrets & Environment Variables)",
        re.DOTALL,
    )
    updated, replacements = pattern.subn(section + "\n", readme, count=1)
    if replacements != 1:
        raise ValueError("Could not locate the execution summary section in README.md")
    README_PATH.write_text(updated, encoding="utf-8")


def main():
    test_cases = load_test_cases()
    code_map = scan_java_annotations()
    exec_results, class_counts, metadata = load_execution_report()

    csv_ids = {case["TestCaseID"] for case in test_cases}
    orphan_ids = sorted(set(code_map) - csv_ids)
    duplicate_ids = {case_id: implementations for case_id, implementations in code_map.items()
                     if len(implementations) > 1}
    if orphan_ids:
        raise ValueError(f"Orphan @TestCase IDs not in docs/test-cases.csv: {orphan_ids}")
    if duplicate_ids:
        raise ValueError(f"Duplicate @TestCase IDs across Java classes: {duplicate_ids}")

    mismatched_statuses = []
    for case in test_cases:
        csv_status = case.get("ExecutionStatus", "").strip()
        is_implemented = case["TestCaseID"] in code_map
        if is_implemented and csv_status != "Automated":
            mismatched_statuses.append(f"{case['TestCaseID']} (code exists, CSV ExecutionStatus={csv_status!r})")
        elif not is_implemented and csv_status == "Automated":
            mismatched_statuses.append(f"{case['TestCaseID']} (no code, CSV ExecutionStatus='Automated')")
    if mismatched_statuses:
        raise ValueError(f"CSV ExecutionStatus drift detected in docs/test-cases.csv: {mismatched_statuses}")

    total_cases = len(test_cases)
    automated_count = len(code_map)
    status_rows = []
    passed_count = failed_count = skipped_count = not_run_count = 0

    for case in test_cases:
        case_id = case["TestCaseID"]
        implementation = code_map.get(case_id)
        if not implementation:
            status = "NOT RUN (Manual Scope)"
            not_run_count += 1
            impl_text = "Manual / Planned Scope"
        else:
            impl = implementation[0]
            result = exec_results.get((impl["class"], impl["method"]))
            impl_text = f"[{impl['class']}#{impl['method']}](../{impl['file']})"
            if result == "PASS":
                status = "PASS"
                passed_count += 1
            elif result == "FAIL":
                status = "FAIL"
                failed_count += 1
            elif result == "SKIPPED":
                status = "SKIPPED"
                skipped_count += 1
            else:
                status = "NOT RUN (Automated)"
                not_run_count += 1
        status_rows.append({
            "id": case_id,
            "req": case["RequirementID"],
            "title": case["Title"],
            "scope": case.get("TestType", "UI"),
            "priority": case.get("Priority", "P2"),
            "automated": "YES" if implementation else "NO",
            "implementation": impl_text,
            "status": status,
        })

    executed_count = passed_count + failed_count + skipped_count
    if executed_count == 0:
        raise ValueError("The selected TestNG report contains no executed test methods.")
    timestamp = run_timestamp(metadata)
    window = execution_window(metadata)
    automation_rate = percent(automated_count, total_cases)
    executed_rate = percent(executed_count, automated_count)
    pass_rate = percent(passed_count, executed_count)

    with RTM_OUT_PATH.open("w", encoding="utf-8", newline="\n") as report:
        report.write("# Requirements Traceability Matrix (RTM)\n\n")
        report.write(f"**Reconciliation Timestamp:** {timestamp}\n")
        report.write(f"**Execution Window:** {window}\n")
        report.write("**Target AUT:** nopCommerce 4.90.8 (.NET 9, Isolated Docker Stack `http://127.0.0.1:5000`)\n\n")
        report.write(f"> **Evidence Notice:** Reconciled from `docs/test-cases.csv`, Java `@TestCase` annotations, and `{metadata['path']}`.\n\n")
        report.write("## 1. Traceability Summary\n\n")
        report.write(f"- **Total Specified Test Cases:** {total_cases}\n")
        report.write(f"- **Automated Test Methods (1:1 Mapped):** {automated_count} ({automation_rate:.1f}%)\n")
        report.write(f"- **Executed:** {executed_count}/{automated_count} automated methods ({executed_rate:.1f}%)\n")
        report.write(f"- **Passed / Failed / Skipped:** {passed_count} / {failed_count} / {skipped_count} ({pass_rate:.1f}% pass rate of executed)\n")
        report.write(f"- **Not Executed / Manual Scope:** {not_run_count}\n\n")
        report.write(
            f"The {automated_count} automated methods are mapped from a {total_cases}-case catalog; "
            "this is not full coverage of all planned cases.\n"
        )
        report.write(
            f"**Performance evidence:** {k6_summary()}; not a browser checkout workload or current CI gate.\n\n"
        )
        report.write("## 2. Granular Traceability Table\n\n")
        report.write("| Test Case ID | Req ID | Scope | Title | Priority | Automated | Implementation | Execution Status |\n")
        report.write("|--------------|--------|-------|-------|----------|-----------|----------------|------------------|\n")
        for row in status_rows:
            report.write(
                f"| {row['id']} | {row['req']} | {row['scope']} | {row['title']} | "
                f"{row['priority']} | {row['automated']} | {row['implementation']} | {row['status']} |\n"
            )

    summary_lines = [
        "# Test Execution Summary", "",
        f"**Reconciled Run Timestamp:** {timestamp}",
        f"**Execution Window:** {window}",
        "**Target AUT:** nopCommerce 4.90.8 (.NET 9, Isolated Docker Stack `http://127.0.0.1:5000`)",
        f"**Stack:** {stack_summary()}", "",
        "## 1. Execution Overview", "",
        "| Metric | Count | Percentage |", "|---|---:|---:|",
        f"| Total Specified Test Cases | {total_cases} | 100.0% |",
        f"| Automated Test Methods | {automated_count} | {automation_rate:.1f}% of catalog |",
        f"| Executed Automated Methods | {executed_count} | {executed_rate:.1f}% of automated |",
        f"| Passed | {passed_count} | {pass_rate:.1f}% of executed |",
        f"| Failed | {failed_count} | {percent(failed_count, executed_count):.1f}% of executed |",
        f"| Skipped | {skipped_count} | {percent(skipped_count, executed_count):.1f}% of executed |",
        f"| Not Executed / Manual Scope | {not_run_count} | {percent(not_run_count, total_cases):.1f}% of catalog |", "",
        "## 2. Test Class Results", "",
        "| Test Class | Passed | Failed | Skipped |", "|---|---:|---:|---:|",
    ]
    for label, counts, _ in suite_rows(class_counts):
        summary_lines.append(f"| {label} | {counts['PASS']} | {counts['FAIL']} | {counts['SKIPPED']} |")
    a11y_counts = class_counts.get("AccessibilityTest", Counter())
    summary_lines.extend([
    "", "## 3. Scope and Gate Notes", "",
    f"The {automated_count} automated methods are mapped from a {total_cases}-case catalog; the remaining catalog cases are not automated and this run does not represent 100% planned-case coverage.",
    "GitHub Actions runs the smoke and API suites, not every UI, database, accessibility, and NFR test.",
    "A successful Jenkins dispatch confirms request acceptance only; the downstream Jenkins build result must be checked separately.",
    "The axe scan threshold defaults to critical impact; critical findings fail the scan. Strict audits can be run with -Da11y.failOnImpact=serious.",
    f"Accessibility methods: {a11y_counts['PASS']} passed, {a11y_counts['FAIL']} failed, {a11y_counts['SKIPPED']} skipped in this TestNG report; scan details are retained in the configured axe reports.",
    f"Performance evidence: {k6_summary()}; not a browser checkout workload or current CI gate.",
    "", "## 4. Retained Evidence", "",
    f"- TestNG XML: `{EVIDENCE_XML.relative_to(ROOT_DIR).as_posix()}`",
    "- TestNG HTML report: `evidence/surefire-reports/index.html`",
    "- k6 benchmark: `evidence/performance/k6-summary.json`",
    "- Accessibility results: `evidence/a11y/`", "",
    "## 5. Traceability Integrity", "",
        f"- Duplicate test-case IDs: {len(duplicate_ids)}",
        f"- Orphan test-case IDs: {len(orphan_ids)}",
        f"- Pass rate: {passed_count}/{executed_count} executed automated methods.",
    ])
    SUMMARY_OUT_PATH.write_text("\n".join(summary_lines) + "\n", encoding="utf-8")

    closure_lines = [
        "# Test Closure Report — nopCommerce 4.90.8 Quality Engineering", "",
        f"**Reconciled Run Timestamp:** {timestamp}",
        f"**Execution Window:** {window}",
        "**Target AUT:** nopCommerce 4.90.8 (.NET 9, Isolated Docker Stack `http://127.0.0.1:5000`)", "",
        f"> Metrics below are generated from `{metadata['path']}` and the current test-case annotations.", "",
        "## Execution Summary", "",
        "| Metric | Result |", "|---|---:|",
        f"| Test cases in catalog | {total_cases} |",
        f"| Automated methods | {automated_count} |",
        f"| Executed automated methods | {executed_count} |",
        f"| Passed | {passed_count} |",
        f"| Failed | {failed_count} |",
        f"| Skipped | {skipped_count} |",
        f"| Pass rate among executed | {pass_rate:.1f}% |", "",
        "## Results by Test Class", "",
        "| Test Class | Passed | Failed | Skipped |", "|---|---:|---:|---:|",
    ]
    for label, counts, _ in suite_rows(class_counts):
        closure_lines.append(f"| {label} | {counts['PASS']} | {counts['FAIL']} | {counts['SKIPPED']} |")
    closure_lines.extend([
        "", "## Scope and Artifacts", "",
        "Coverage includes storefront UI, administration, API probes, database persistence, accessibility, and non-functional checks.",
        f"The {automated_count} automated methods map to a {total_cases}-case catalog; unautomated cases remain manual/planned scope.",
        f"The k6 report is a separate HTTP browse/search run ({k6_summary()}), not a browser checkout workload or current CI gate.",
        "GitHub Actions runs smoke and API suites only. A successful Jenkins dispatch is not a downstream test result.",
        "The axe default threshold is critical impact; strict audits at serious impact flag the upstream storefront color-contrast defect.",
        "The traceability matrix and execution summary are generated alongside this report.",
    ])
    CLOSURE_OUT_PATH.write_text("\n".join(closure_lines) + "\n", encoding="utf-8")

    readme_lines = [
        f"## Reconciled Execution Summary (`{timestamp}`)", "",
        f"**Execution Window:** `{window}`",
        "**Target AUT:** nopCommerce 4.90.8, isolated Docker stack",
        f"**Run Result:** {passed_count} passed, {failed_count} failed, {skipped_count} skipped "
        f"({pass_rate:.1f}% of {executed_count} executed automated methods).", "",
        "| Metric Category | Value | Source Artifact |", "|---|---|---|",
        f"| Total Test Cases Specified | {total_cases} | `docs/test-cases.csv` |",
        f"| Automated Test Methods | {automated_count} ({automation_rate:.1f}% of catalog) | `docs/traceability-matrix.md` |",
        f"| Executed Automated Methods | {executed_count} ({executed_rate:.1f}%) | `{metadata['path']}` |",
        f"| Passed / Failed / Skipped | {passed_count} / {failed_count} / {skipped_count} | `docs/execution-summary.md` |",
        f"| k6 Storefront Load Benchmark | {k6_summary()} | `evidence/performance/k6-summary.json` |", "",
        f"This run covers the executed automated methods only. The {automated_count} automated methods map to a {total_cases}-case catalog; it is not 100% coverage of all planned cases.",
        "GitHub Actions runs smoke and API suites only. A green Jenkins dispatch confirms acceptance of the request, not the downstream test result.",
        "The axe default threshold is critical impact; critical findings fail the scan. Strict serious-impact audits can be run on demand with -Da11y.failOnImpact=serious.", "",
    ]
    replace_readme_summary("\n".join(readme_lines))
    badge_color = "brightgreen" if failed_count == 0 and skipped_count == 0 else "yellow"
    badge = (
        f"[![Test Suite](https://img.shields.io/badge/Tests-{passed_count}%2F{executed_count}%20Passed-{badge_color}.svg)]"
        "(docs/execution-summary.md)"
    )
    readme = README_PATH.read_text(encoding="utf-8")
    readme, badge_replacements = re.subn(r"^\[!\[Test Suite\].*$", badge, readme, count=1, flags=re.MULTILINE)
    if badge_replacements != 1:
        raise ValueError("Could not locate the Test Suite badge in README.md")
    README_PATH.write_text(readme, encoding="utf-8")

    print(
        f"Reconciled {executed_count}/{automated_count} automated methods: "
        f"{passed_count} passed, {failed_count} failed, {skipped_count} skipped."
    )


if __name__ == "__main__":
    try:
        main()
    except (ET.ParseError, FileNotFoundError, ValueError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        sys.exit(1)
