module.exports = {
  "roots": [
    "<rootDir>/src"
  ],
  preset: 'ts-jest',
  testMatch: ['**/*.spec.{ts,tsx}'],
  "transform": {
      "^.+\\.(ts|tsx|js)$": ["ts-jest", {
          "tsconfig": {
            "allowJs": true
          }
        }
      ]
  },

  "transformIgnorePatterns": [
    "<rootDir>/node_modules/(?!pretty-bytes)",
    "^.+\\.module\\.(css|sass|scss)$"
  ],
  "reporters": [ "default", "jest-junit" ],
  collectCoverage: true,
  coverageReporters: ["clover", "text"],
  "testEnvironment": "jsdom",
  "moduleNameMapper": {
    "^.+\\.module\\.(css|sass|scss)$": "identity-obj-proxy",
    "\\.(css|less|scss|sss|styl)$": "<rootDir>/node_modules/jest-css-modules"
  },
  globals: {
    "ts-jest": {
      diagnostics: false
    }
  },
}

process.env = Object.assign(process.env, { API_BASE_URL: 'http://localhost:8080/' });
