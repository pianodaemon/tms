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
  collectCoverageFrom: ["src/**/*.ts", "!**/*.d.ts"],
  coverageReporters: ["json", "json-summary", "text", "lcov"],
  coveragePathIgnorePatterns: [
    "/node_modules/",
    "tests/", // or wherever your tests live
  ],
  // map coverage back to source with source maps
  mapCoverage: true,
  // This ensures coverage is collected from files that are imported in tests
  coverageProvider: "v8",
  moduleFileExtensions: ["ts", "tsx", "js", "json", "node"],
};
