import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * K6 load test script for expression evaluation API.
 * Tests the /api/expressions/evaluate endpoint with SLA definitions.
 * 
 * Module: rule-engine-loadtest-scripts
 * Layer: Load Tests
 */

export const options = {
  stages: [
    { duration: '30s', target: 50 },
    { duration: '1m', target: 50 },
    { duration: '30s', target: 100 },
    { duration: '1m', target: 100 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<300', 'p(99)<600'],
    http_req_failed: ['rate<0.01'],
    http_reqs: ['rate>150'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const payload = JSON.stringify({
    expressionString: 'customer.age >= 18 && order.total > 100',
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

  const response = http.post(`${BASE_URL}/api/expressions/evaluate`, payload, params);

  check(response, {
    'status is 200': (r) => r.status === 200,
    'response has value field': (r) => JSON.parse(r.body).value !== undefined,
    'response time < 300ms': (r) => r.timings.duration < 300,
  });

  sleep(1);
}

