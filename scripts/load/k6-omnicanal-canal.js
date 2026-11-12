/**
 * Load test k6 — cible 1000 trx/s, latence P99 < 1s
 *
 * Usage:
 *   k6 run -e BASE_URL=http://localhost:8080 -e API_KEY=change-me-mobile \
 *     scripts/load/k6-omnicanal-canal.js
 *
 * Ramp vers 1000 VUs (≈ 1000 trx/s si chaque VU fait 1 req/s).
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const errorRate = new Rate('errors');
const latency = new Trend('canal_latency', true);

export const options = {
  scenarios: {
    ramp_to_1k: {
      executor: 'ramping-arrival-rate',
      startRate: 50,
      timeUnit: '1s',
      preAllocatedVUs: 200,
      maxVUs: 1200,
      stages: [
        { duration: '30s', target: 200 },
        { duration: '30s', target: 500 },
        { duration: '1m', target: 1000 },
        { duration: '2m', target: 1000 },
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<800', 'p(99)<1000'],
    errors: ['rate<0.01'],
    canal_latency: ['p(99)<1000'],
  },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const API_KEY = __ENV.API_KEY || 'change-me-mobile';

export default function () {
  const ref = `k6-${__VU}-${__ITER}-${Date.now()}`;
  const payload = JSON.stringify({
    sourceCodeFse: __ENV.FSE_SRC || '0011',
    destCodeFse: __ENV.FSE_DST || '0011',
    serviceType: 'TRANSFERT',
    referenceExterne: ref,
    param: [
      { codeParametre: 'montant', valeur: '1000' },
      { codeParametre: 'compteDebiteur', valeur: '0011000003090' },
      { codeParametre: 'compteCredit', valeur: '0011000000002' },
    ],
  });

  const res = http.post(`${BASE}/api/v1/omnicanalPresentation`, payload, {
    headers: {
      'Content-Type': 'application/json',
      'X-Api-Key': API_KEY,
      'Idempotency-Key': `k6-${__VU}-${__ITER}-${Date.now()}`,
    },
    timeout: '2s',
  });

  latency.add(res.timings.duration);
  const ok = check(res, {
    'status 2xx': (r) => r.status >= 200 && r.status < 300,
    'under 1s': (r) => r.timings.duration < 1000,
  });
  errorRate.add(!ok);
}
