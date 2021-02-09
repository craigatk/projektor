/// <reference types="Cypress" />

context("test run with failed test cases", () => {
  it("should show failed test case summaries on failed tests page", () => {
    const publicId = "12345";

    cy.server();

    cy.route("GET", `run/${publicId}/summary`, "fixture:test_run_summary.json");

    cy.route("GET", `run/${publicId}`, "fixture:test_run.json");

    cy.route(
      "GET",
      `run/${publicId}/cases/failed`,
      "fixture:failed_test_cases.json"
    );

    cy.route(
      "GET",
      `run/${publicId}/suite/1/case/2`,
      "fixture:failed_test_case_2.json"
    );

    cy.visit(`http://localhost:1234/tests/${publicId}/failed`);

    cy.getByTestId("failed-tests-title").should("contain", "Failed tests");

    cy.getByTestId("test-case-title").should("have.length", 2);

    cy.getByTestId("test-case-title").should(
      "contain",
      "projektor.example.spock.FailingSpec should fail"
    );

    cy.getByTestId("test-case-title").should(
      "contain",
      "projektor.example.spock.FailingSpec should fail with output"
    );

    cy.getByTestId("test-case-summary-failure-link-1-2").click();

    cy.getBreadcrumbPackgeNameLink().should(
      "contain",
      "projektor.example.spock"
    );

    cy.getBreadcrumbClassNameLink().should("contain", "FailingSpec");
    cy.getBreadcrumbEndingText().should("contain", "should fail with output");
  });

  it("should link from failed tests page to failed test case system out and system err", () => {
    const publicId = "12345";
    const testSuiteIdx = 1;
    const testCaseIdx = 2;

    cy.server();

    cy.route("GET", `run/${publicId}/summary`, "fixture:test_run_summary.json");

    cy.route("GET", `run/${publicId}`, "fixture:test_run.json");

    cy.route(
      "GET",
      `run/${publicId}/cases/failed`,
      "fixture:failed_test_cases.json"
    );

    cy.route(
      "GET",
      `run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`,
      "fixture:failed_test_case_2.json"
    );

    cy.route(
      "GET",
      `run/${publicId}/suite/${testSuiteIdx}/systemOut`,
      "fixture:test_output_system_out.json"
    );

    cy.route(
      "GET",
      `run/${publicId}/suite/${testSuiteIdx}/systemErr`,
      "fixture:test_output_system_err.json"
    );

    cy.visit(`http://localhost:1234/tests/${publicId}/failed`);

    cy.getByTestId(
      `test-case-summary-system-out-link-${testSuiteIdx}-${testCaseIdx}`
    ).click();

    cy.getCodeText().should("contain", "System out line 1");

    cy.go("back");

    cy.getByTestId(
      `test-case-summary-system-err-link-${testSuiteIdx}-${testCaseIdx}`
    ).click();

    cy.getCodeText().should("contain", "System err line 1");
  });
});
