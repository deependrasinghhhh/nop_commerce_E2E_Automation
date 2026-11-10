import http from 'k6/http';
import { check, sleep, group, fail } from 'k6';

// =============================================================================
// Fail-Fast Target Validation: Require Explicit BASE_URL & Forbid Public Demo
// =============================================================================
const RAW_BASE_URL = (__ENV.BASE_URL || '').trim();

if (!RAW_BASE_URL) {
    throw new Error(
        'Missing required BASE_URL environment variable. ' +
        'Pass an explicit isolated nopCommerce URL, e.g.: k6 run -e BASE_URL=http://127.0.0.1:5000 performance/k6/storefront-load.js'
    );
}

if (/demo\.nopcommerce\.com/i.test(RAW_BASE_URL)) {
    throw new Error(
        `Refusing to execute load test against shared public demo (${RAW_BASE_URL}). ` +
        'Point BASE_URL to a dedicated, isolated nopCommerce test container.'
    );
}

const BASE_URL = RAW_BASE_URL.replace(/\/+$/, '');

export const options = {
    stages: [
        { duration: '30s', target: 10 }, // Ramp-up to 10 VUs
        { duration: '1m', target: 25 },  // Sustained load with 25 VUs
        { duration: '20s', target: 0 },  // Ramp-down
    ],
    thresholds: {
        http_req_duration: ['p(95)<2500'], // 95% of requests must complete under 2.5s
        http_req_failed: ['rate<0.01'],   // Error rate must remain under 1%
        checks: ['rate>0.99'],            // At least 99% of content assertions must pass
    },
};

const HEADERS = {
    'User-Agent': 'k6-load-test/1.0 (nopCommerce Isolated Performance Suite)',
    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
    'Accept-Language': 'en-US,en;q=0.5',
};

export function setup() {
    const probe = http.get(`${BASE_URL}/`, { headers: HEADERS, timeout: '10s' });
    if (probe.status !== 200 || !probe.body || !probe.body.includes('nopCommerce')) {
        fail(`Target AUT health check failed at ${BASE_URL}/ (HTTP ${probe.status}). Aborting load test.`);
    }
    return { baseUrl: BASE_URL };
}

export default function (data) {
    const targetUrl = data.baseUrl;

    group('01_Homepage Navigation', function () {
        const res = http.get(`${targetUrl}/`, { headers: HEADERS });
        check(res, {
            'Homepage status is 200 OK': (r) => r.status === 200,
            'Homepage HTML contains header-logo and search box': (r) =>
                Boolean(r.body && r.body.includes('header-logo') && r.body.includes('small-searchterms')),
        });
        sleep(1);
    });

    group('02_Catalog Browsing - Desktops', function () {
        const res = http.get(`${targetUrl}/desktops`, { headers: HEADERS });
        check(res, {
            'Desktops catalog status is 200 OK': (r) => r.status === 200,
            'Desktops page renders category title and product grid': (r) =>
                Boolean(r.body && r.body.includes('Desktops') && r.body.includes('product-item')),
        });
        sleep(1.5);
    });

    group('03_Storefront Search Query', function () {
        const res = http.get(`${targetUrl}/search?q=computer`, { headers: HEADERS });
        check(res, {
            'Search query status is 200 OK': (r) => r.status === 200,
            'Search response contains search-results container': (r) =>
                Boolean(r.body && r.body.includes('search-results') && r.body.includes('Build your own computer')),
        });
        sleep(1);
    });

    group('04_Product Details Page', function () {
        const res = http.get(`${targetUrl}/build-your-own-computer`, { headers: HEADERS });
        check(res, {
            'Product details status is 200 OK': (r) => r.status === 200,
            'Product details body contains title and add-to-cart control': (r) =>
                Boolean(r.body && r.body.includes('Build your own computer') && r.body.includes('add-to-cart-button')),
        });
        sleep(2);
    });
}

export function handleSummary(data) {
    const iterations = data.metrics.iterations ? data.metrics.iterations.values.count : 0;
    const reqsPerIteration = 4;
    const setupReqs = 1;
    const totalHttpReqs = data.metrics.http_reqs ? data.metrics.http_reqs.values.count : iterations * reqsPerIteration + setupReqs;
    const failedReqs = data.metrics.http_req_failed ? data.metrics.http_req_failed.values.passes : 0;
    const passedReqs = totalHttpReqs - failedReqs;

    const summaryPayload = {
        benchmark_metadata: {
            tool: 'k6 v0.54.0',
            target_aut: 'nopCommerce 4.90.8 (.NET 9)',
            target_base_url: BASE_URL,
            scenario: 'Storefront Browse & Search Load Test (4 requests/iteration + 1 setup probe)',
            timestamp: new Date().toISOString(),
            vus_peak: 25,
            duration_seconds: 110,
        },
        metrics: {
            iterations: {
                count: iterations,
                requests_per_iteration: reqsPerIteration,
                setup_probe_requests: setupReqs,
                rate: data.metrics.iterations ? Number(data.metrics.iterations.values.rate.toFixed(4)) : 0,
            },
            http_reqs: {
                count: totalHttpReqs,
                rate: data.metrics.http_reqs ? Number(data.metrics.http_reqs.values.rate.toFixed(4)) : 0,
            },
            http_req_duration: {
                avg_ms: data.metrics.http_req_duration ? Number(data.metrics.http_req_duration.values.avg.toFixed(2)) : 0,
                min_ms: data.metrics.http_req_duration ? Number(data.metrics.http_req_duration.values.min.toFixed(2)) : 0,
                med_ms: data.metrics.http_req_duration ? Number(data.metrics.http_req_duration.values.med.toFixed(2)) : 0,
                max_ms: data.metrics.http_req_duration ? Number(data.metrics.http_req_duration.values.max.toFixed(2)) : 0,
                p90_ms: data.metrics.http_req_duration ? Number(data.metrics.http_req_duration.values['p(90)'].toFixed(2)) : 0,
                p95_ms: data.metrics.http_req_duration ? Number(data.metrics.http_req_duration.values['p(95)'].toFixed(2)) : 0,
            },
            http_req_failed: {
                failed_requests: failedReqs,
                succeeded_requests: passedReqs,
                failure_rate: data.metrics.http_req_failed ? Number(data.metrics.http_req_failed.values.rate.toFixed(4)) : 0,
            },
            checks: {
                passes: data.metrics.checks ? data.metrics.checks.values.passes : 0,
                fails: data.metrics.checks ? data.metrics.checks.values.fails : 0,
                pass_rate: data.metrics.checks ? Number(data.metrics.checks.values.rate.toFixed(4)) : 1.0,
            },
        },
        thresholds: {
            'http_req_duration:p(95)<2500': {
                ok: data.metrics.http_req_duration ? data.metrics.http_req_duration.values['p(95)'] < 2500 : true,
                actual_value_ms: data.metrics.http_req_duration ? Number(data.metrics.http_req_duration.values['p(95)'].toFixed(2)) : 0,
            },
            'http_req_failed:rate<0.01': {
                ok: data.metrics.http_req_failed ? data.metrics.http_req_failed.values.rate < 0.01 : true,
                actual_rate: data.metrics.http_req_failed ? Number(data.metrics.http_req_failed.values.rate.toFixed(4)) : 0,
            },
            'checks:rate>0.99': {
                ok: data.metrics.checks ? data.metrics.checks.values.rate > 0.99 : true,
                actual_rate: data.metrics.checks ? Number(data.metrics.checks.values.rate.toFixed(4)) : 1.0,
            },
        },
    };

    return {
        'evidence/performance/k6-summary.json': JSON.stringify(summaryPayload, null, 2),
    };
}
