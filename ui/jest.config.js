module.exports = {
  "roots": [
    "<rootDir>/src"
  ],
  preset: 'ts-jest',
  testMatch: ['<rootDir>/src/**/?(*.)spec.{ts,tsx}'],
}

process.env = Object.assign(process.env, { API_BASE_URL: 'http://localhost:8080/' });
