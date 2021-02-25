module.exports = {
    "roots": [
        "<rootDir>/src"
    ],
    testMatch: ['**/without-token/*.spec.js'],
    "reporters": [ "default", [ "jest-junit",
        {
            "outputName": "junit-without-token.xml",
            "includeConsoleOutput": true,
            "outputDirectory": "testResults",
            "classNameTemplate": "{classname}",
            "usePathForSuiteName": "true"
        }
    ] ]
}
