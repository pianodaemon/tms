const { createDefaultPreset } = require("ts-jest");

const tsJestPreset = createDefaultPreset();
const tsJestTransformCfg = tsJestPreset ? tsJestPreset.transform : {};

/** @type {import("jest").Config} **/
module.exports = {
  testEnvironment: "node",
  transform: {
    ...tsJestTransformCfg,
  },
  collectCoverage: true,
  coverageDirectory: "coverage",
  coverageReporters: ["json", "json-summary", "text", "lcov"],
  collectCoverageFrom: [
    'src/**/*.{ts,tsx}',      // ✅ this still works
    '!src/**/*.d.ts',
    '!src/**/__tests__/**',   // ✅ this still avoids collecting coverage from test files
  ],
  coveragePathIgnorePatterns: [
    '/node_modules/',
    // ❌ remove '/tests/' — no longer relevant
  ],
  moduleFileExtensions: ['ts', 'tsx', 'js', 'json'],
};
