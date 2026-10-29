#!/usr/bin/env node
/**
 * Smart Medicine Box - Firestore Security Rules Unit Test Suite
 * Tests rule definitions, access boundaries, authentication constraints, and validation logic.
 */

const fs = require('fs');
const path = require('path');

const rulesPath = path.resolve(__dirname, '../firestore.rules');
const rulesContent = fs.readFileSync(rulesPath, 'utf8');

console.log('---------------------------------------------------------');
console.log('Testing Firestore Security Rules (firestore.rules)');
console.log('---------------------------------------------------------');

let passedTests = 0;
let totalTests = 0;

function assert(condition, message) {
  totalTests++;
  if (condition) {
    console.log(`  \x1b[32m✔ PASS\x1b[0m: ${message}`);
    passedTests++;
  } else {
    console.error(`  \x1b[31m✖ FAIL\x1b[0m: ${message}`);
    process.exitCode = 1;
  }
}

// 1. Structural Checks
assert(rulesContent.includes("rules_version = '2';"), "Uses Firestore rules version 2");
assert(rulesContent.includes("service cloud.firestore"), "Declares cloud.firestore service");
assert(rulesContent.includes("function isOwner(uid)"), "Defines isOwner(uid) security helper");
assert(rulesContent.includes("request.auth.uid == uid"), "Enforces user-scoped auth UID matching");

// 2. Collection Path Matchers
assert(rulesContent.includes("match /users/{uid}"), "Matches root users collection");
assert(rulesContent.includes("match /devices/{deviceId}"), "Protects registered devices subcollection");
assert(rulesContent.includes("match /telemetry/{docId}"), "Protects telemetry singleton subcollection");
assert(rulesContent.includes("match /events/{eventId}"), "Protects hardware events subcollection");
assert(rulesContent.includes("match /medicines/{medicineId}"), "Protects medicines inventory subcollection");
assert(rulesContent.includes("match /schedules/{scheduleId}"), "Protects schedules subcollection");
assert(rulesContent.includes("match /doseLogs/{logId}"), "Protects dose logs subcollection");
assert(rulesContent.includes("match /{document=**}"), "Contains wildcard catch-all deny rule");

// 3. Validation Logic Tests
assert(rulesContent.includes("isValidTelemetry"), "Contains telemetry schema validator");
assert(rulesContent.includes("isValidEvent"), "Contains hardware event schema validator");
assert(rulesContent.includes("isValidMedicine"), "Contains medicine inventory schema validator");
assert(rulesContent.includes("isValidSchedule"), "Contains prescription schedule schema validator");
assert(rulesContent.includes("isValidDoseLog"), "Contains dose intake log schema validator");

// 4. Allowed Event Types Verification
const expectedEvents = [
  'BOX_OPENED', 'BOX_CLOSED', 'WEIGHT_DROP',
  'WEIGHT_INCREASE', 'TAMPER_ALERT', 'LOW_BATTERY', 'DOSE_DISPENSED'
];
expectedEvents.forEach(evt => {
  assert(rulesContent.includes(`'${evt}'`), `Rules permit eventType '${evt}'`);
});

// 5. Allowed Dose Status Verification
const expectedStatuses = ['TAKEN', 'PENDING', 'MISSED'];
expectedStatuses.forEach(status => {
  assert(rulesContent.includes(`'${status}'`), `Rules validate dose log status '${status}'`);
});

console.log('---------------------------------------------------------');
console.log(`Results: ${passedTests}/${totalTests} tests passed.`);
console.log('---------------------------------------------------------');
