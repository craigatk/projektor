module.exports = {
    "roots": [
        "<rootDir>/src"
    ],
    testMatch: ['**/with-token/*.spec.js'],
    "reporters": [ "default", [ "jest-junit",
        {
            "outputName": "junit-with-token.xml",
            "includeConsoleOutput": "true",
            "outputDirectory": "testResults",
            "classNameTemplate": "{classname}",
            "usePathForSuiteName": "true"
        }
    ] ]
}
