#!/usr/bin/env python3
"""Reconcile test-case annotations and generate reports from one TestNG run."""

import csv
import os
import re
import shutil
import sys
import xml.etree.ElementTree as ET
from collections import Counter, defaultdict
from datetime import datetime, timezone
from pathlib import Path


ROOT_DIR = Path(__file__).resolve().parent.parent
CSV_PATH = ROOT_DIR / "docs" / "test-cases.csv"
JAVA_SRC_DIR = ROOT_DIR / "src" / "test" / "java"
EVIDENCE_DIR = ROOT_DIR / "evidence" / "surefire-reports"
EVIDENCE_XML = EVIDENCE_DIR / "testng-results.xml"
TARGET_XML = ROOT_DIR / "target" / "surefire-reports" / "testng-results.xml"
RTM_OUT_PATH = ROOT_DIR / "docs" / "traceability-matrix.md"
SUMMARY_OUT_PATH = ROOT_DIR / "docs" / "execution-summary.md"
CLOSURE_OUT_PATH = ROOT_DIR / "docs" / "test-closure-report.md"
README_PATH = ROOT_DIR / "README.md"

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


def load_execution_report():
    if TARGET_XML.is_file():
        if EVIDENCE_DIR.exists():
            shutil.rmtree(EVIDENCE_DIR)
        shutil.copytree(TARGET_XML.parent, EVIDENCE_DIR)
        report_path = EVIDENCE_XML
    elif EVIDENCE_XML.is_file():
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
        "**Stack:** Java 21 LTS | Playwright Java 1.49.0 | TestNG 7.10.2 | Allure 2.29.0 | Axe-core 4.10.0 | k6 0.54.0", "",
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
    summary_lines.extend([
        "", "## 3. Retained Evidence", "",
        f"- TestNG XML: `{EVIDENCE_XML.relative_to(ROOT_DIR).as_posix()}`",
        "- TestNG HTML report: `evidence/surefire-reports/index.html`",
        "- k6 benchmark: `evidence/performance/k6-summary.json` (separate load-test run)",
        "- Accessibility results: `evidence/a11y/`", "",
        "## 4. Traceability Integrity", "",
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
        "The k6 benchmark is a separate run and is documented in `evidence/performance/k6-summary.json`.",
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
        "| k6 Storefront Load Benchmark | Separate run; see benchmark artifact | `evidence/performance/k6-summary.json` |", "",
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
