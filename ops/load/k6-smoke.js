/**
 * Smoke charge — endpoints publics API (Sprint 4 PRD).
 * Prérequis : k6 installé (https://k6.io/docs/get-started/installation/)
 * Usage : k6 run -e BASE_URL=http://localhost:8080 ops/load/k6-smoke.js
 */
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 5,
  duration: '30s',
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<2000'],
  },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const stations = http.get(`${BASE}/api/stations?page=0&size=5`);
  check(stations, { 'stations 200': (r) => r.status === 200 });

  const suggest = http.get(`${BASE}/api/destinations/suggest?q=bi`);
  check(suggest, { 'suggest 200': (r) => r.status === 200 });

  const health = http.get(`${BASE}/actuator/health`);
  check(health, { 'health 200': (r) => r.status === 200 });

  sleep(0.3);
}
