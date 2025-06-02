const { createDefaultPreset } = require("ts-jest");

const tsJestTransformCfg = createDefaultPreset().transform;

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
    'src/**/*.ts',      // ✅ Include source files
    '!src/**/*.d.ts',   // ❌ Exclude type declarations
  ],
  coveragePathIgnorePatterns: [
    '/node_modules/',
    '/tests/',          // ✅ Ignore your test files if they live outside src
  ],
  moduleFileExtensions: ['ts', 'tsx', 'js', 'json'],
  globals: {
    'ts-jest': {
      tsconfig: 'tsconfig.json',  // ✅ Make sure this is correct
      diagnostics: false,         // Optional: silence type warnings in tests
    },
  },
};
