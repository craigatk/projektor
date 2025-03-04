# Changelog

## Projektor server

Full list of releases and the packaged server .jar file for each release at: https://github.com/craigatk/projektor/releases

* v5.2.0
  * Adding support for test case failure analysis with ChatGPT
* v5.1.1
  * Fix sorting by duration on all-tests page
* v5.1.0
  * Upgrading server to ktor 3 - along with additional dependency updates
* v5.0.0
  * BREAKING CHANGE: Projektor server is now built with Java 17 and requires Java 17+ to run
* v4.39.0
  * Adding API endpoint for fetching recent test runs for a repo
* v4.38.1
  * Handling Jest code coverage reports in root directory with no packages
* v4.38.0
  * Adding API endpoint for repo flaky tests
* v4.37.0
  * Adding test run badge to main tests dashboard page
* v4.36.1
  * Adding link to tests readme to repo page
* v4.36.0
  * Adding readme page for current test status in repo
* v4.35.0
  * Use mainline when no branch specified when getting repo current coverage
* v4.34.0
  * Adding /api endpoints for getting org and repo current coverage percentage
* v4.33.2
  * Adding test file name to failed tests view on dashboard
* v4.33.1
  * Updating version of follow-redirects to 1.15.4
* v4.33.0
  * Making server max payload size configurable and fixing UI styling regression
* v4.32.0
  * Allowing larger payload sizes and updating dependencies
* v4.31.0
  * Another round of dependency updates
* v4.30.0
  * Upgrading to Kotlin 1.8.22 and other dependency updates
* v4.29.0
  * Dependency upgrades, including OpenTelemetry 1.21.0
* v4.28.0
  * Dependency upgrades, including ktor v2
* v4.27.0
  * Upgrading dependencies across the board
* v4.26.1
  * Improving failure message on home page in certain cases
* v4.26.0
  * Upgrading to React 18
* v4.25.3
  * Improving graph with low code coverage
* v4.25.2
  * Fixing organization coverage page when one repo has no coverage data
* v4.25.1
  * Upgrading async dependency
* v4.25.0
  * Dependency updates in server and UI
* v4.24.0
  * Dependency updates across the board
* v4.23.2
  * Server dependency upgrades, including jOOQ 3.16
* v4.23.1
  * Server dependency updates, including Parcel 2.1.1 and maintained material table fork
* v4.23.0
  * Dependency upgrades, including React 17
* v4.22.4
  * Hide coverage section on UI if no coverage data
* v4.22.3
  * Dependency updates, including logback 1.2.9
* v4.22.2
  * Supporting Cobertura code coverage files without DocType header
* v4.22.1
  * Dependency version upgrades, including logback 1.2.8
* v4.22.0
  * Making failed test case name text selection easier
