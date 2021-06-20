module.exports = {
  "roots": [
    "<rootDir>/src"
  ],
  preset: 'ts-jest',
  testMatch: ['**/*.spec.{ts,tsx}'],
  "reporters": [ "default", "jest-junit" ],
  collectCoverage: true,
  coverageReporters: ["clover", "text"],
  "testEnvironment": "jsdom"
}

process.env = Object.assign(process.env, { API_BASE_URL: 'http://localhost:8080/' });
