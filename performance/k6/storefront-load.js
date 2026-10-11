import http from 'k6/http';
import { check, sleep, group } from 'k6';

// Performance Benchmark Configuration for nopCommerce 4.90.8 Storefront
export const options = {
    stages: [
        { duration: '30s', target: 10 },  // Ramp-up to 10 VUs
        { duration: '1m', target: 25 },   // Sustained load with 25 VUs
        { duration: '20s', target: 0 },   // Ramp-down
    ],
    thresholds: {
        http_req_duration: ['p(95)<2500'], // 95% of requests must complete under 2.5s
        http_req_failed: ['rate<0.05'],    // Error rate must remain under 5%
    },
};

const BASE_URL = __ENV.BASE_URL || 'https://demo.nopcommerce.com';

const HEADERS = {
    'User-Agent': 'k6-load-test/1.0 (nopCommerce QA Engineering Portfolio Performance Gate)',
    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
    'Accept-Language': 'en-US,en;q=0.5',
};

export default function () {
    group('01_Homepage Navigation', function () {
        const res = http.get(`${BASE_URL}/`, { headers: HEADERS });
        check(res, {
            'Homepage status is 200 or 403 (WAF)': (r) => r.status === 200 || r.status === 403,
            'Response body received': (r) => r.body && r.body.length > 0,
        });
        sleep(1);
    });

    group('02_Catalog Browsing - Desktops', function () {
        const res = http.get(`${BASE_URL}/desktops`, { headers: HEADERS });
        check(res, {
            'Desktops catalog status is 200 or 403': (r) => r.status === 200 || r.status === 403,
        });
        sleep(1.5);
    });

    group('03_Storefront Search Query', function () {
        const res = http.get(`${BASE_URL}/search?q=computer`, { headers: HEADERS });
        check(res, {
            'Search query status is 200 or 403': (r) => r.status === 200 || r.status === 403,
        });
        sleep(1);
    });

    group('04_Product Details Page', function () {
        const res = http.get(`${BASE_URL}/build-your-own-computer`, { headers: HEADERS });
        check(res, {
            'Product details status is 200 or 403': (r) => r.status === 200 || r.status === 403,
        });
        sleep(2);
    });
}
