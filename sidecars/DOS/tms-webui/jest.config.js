const { createDefaultPreset } = require("ts-jest");

const tsJestPreset = createDefaultPreset();
const tsJestTransformCfg = tsJestPreset ? tsJestPreset.transform : {};

/** @type {import("jest").Config} **/
module.exports = {
  testEnvironment: "node",
  transform: {
    ...tsJestTransformCfg,
  },
  testMatch: ['**/tests/**/*.test.ts'],
  collectCoverage: true,
  coverageDirectory: "coverage",
  coverageReporters: ["json", "json-summary", "text", "lcov"],
  collectCoverageFrom: [
    'tests/**/*.{ts,tsx}',
    'src/**/*.{ts,tsx}'
  ],
  coveragePathIgnorePatterns: [
    '/node_modules/',
  ],
  moduleFileExtensions: ['ts', 'tsx', 'js', 'json'],
};
