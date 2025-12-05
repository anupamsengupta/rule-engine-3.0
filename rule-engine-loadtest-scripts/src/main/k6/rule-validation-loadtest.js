import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * K6 load test script for rule validation API.
 * Tests the /api/rules/validate endpoint with SLA definitions.
 * 
 * Module: rule-engine-loadtest-scripts
 * Layer: Load Tests
 */

export const options = {
  stages: [
    { duration: '30s', target: 50 },   // Ramp up to 50 users
    { duration: '1m', target: 50 },    // Stay at 50 users
    { duration: '30s', target: 100 },  // Ramp up to 100 users
    { duration: '1m', target: 100 },   // Stay at 100 users
    { duration: '30s', target: 0 },    // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'], // 95% of requests < 500ms, 99% < 1000ms
    http_req_failed: ['rate<0.01'],                 // Error rate < 1%
    http_reqs: ['rate>100'],                        // Throughput > 100 req/s
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const payload = JSON.stringify({
    ruleId: 'rule-1',
    ruleName: 'Test Rule',
    conditions: [
      {
        attributeCode: 'customer.age',
        attributeType: 'NUMBER',
        operator: 'GTE',
        targetValue: 18
      }
    ],
    context: {
      'customer.age': 25,
      'order.total': 150.0
    }
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const response = http.post(`${BASE_URL}/api/rules/validate`, payload, params);

  check(response, {
    'status is 200': (r) => r.status === 200,
    'response has passed field': (r) => JSON.parse(r.body).passed !== undefined,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);
}

