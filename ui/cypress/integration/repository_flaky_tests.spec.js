/// <reference types="Cypress" />

context("repository flaky tests", () => {
  it("should display flaky tests on repository home page", () => {
    const repoName = "flaky-org/flaky-repo";

    cy.server();

    cy.route(
      "GET",
      `repo/${repoName}/tests/flaky`,
      "fixture:repository/flaky_tests.json"
    );

    cy.visit(`http://localhost:1234/repository/${repoName}`);

    cy.testIdShouldExist("repository-flaky-tests-table");

    cy.getByTestId("flaky-test-case-name-1").should(
      "contain",
      "projektor.testsuite.GetTestSuiteApplicationTest.should fetch grouped test suite from database"
    );
    cy.getByTestId("flaky-test-case-failure-percentage-1").should(
      "contain",
      "70.35%"
    );
    cy.getByTestId("flaky-test-case-failure-count-1").should("contain", "5");

    cy.getByTestId("flaky-test-case-name-2").should(
      "contain",
      "projektor.example.spock.FailingSpec.should fail with output"
    );
    cy.getByTestId("flaky-test-case-failure-percentage-2").should(
      "contain",
      "60.25%"
    );
    cy.getByTestId("flaky-test-case-failure-count-2").should("contain", "4");

    cy.getByTestId("flaky-test-case-name-3").should(
      "contain",
      "projektor.example.spock.FailingSpec.should fail"
    );
    cy.getByTestId("flaky-test-case-failure-percentage-3").should(
      "contain",
      "50.15%"
    );
    cy.getByTestId("flaky-test-case-failure-count-3").should("contain", "3");
  });

  it("should link from repository side menu to flaky tests page", () => {
    const repoName = "flaky-org/flaky-repo";

    cy.server();

    cy.route(
      "GET",
      `repo/${repoName}/tests/flaky`,
      "fixture:repository/flaky_tests.json"
    );

    cy.visit(`http://localhost:1234/repository/${repoName}`);

    cy.getByTestId("nav-link-repo-flaky-tests").click();

    cy.testIdShouldExist("repository-flaky-tests-table");
  });

  it("should link from test case name to specific test run", () => {
    const repoName = "flaky-org/flaky-repo";

    cy.server();

    cy.route(
      "GET",
      `repo/${repoName}/tests/flaky`,
      "fixture:repository/flaky_tests.json"
    );

    const publicId = "32FBHG6FDL8S";
    const testSuiteIdx = 1;
    const testCaseIdx = 1;

    cy.route("GET", `run/${publicId}/summary`, "fixture:test_run_summary.json");
    cy.route("GET", `run/${publicId}`, "fixture:test_run.json");
    cy.route(
      "GET",
      `run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`,
      "fixture:failed_test_case_2.json"
    );

    cy.visit(`http://localhost:1234/repository/${repoName}`);

    cy.testIdShouldExist("repository-flaky-tests-table");

    cy.getByTestId("flaky-test-case-name-1").click();

    cy.getByTestId("test-case-failure-text").should(
      "contain",
      "Condition not satisfied"
    );
  });
});
