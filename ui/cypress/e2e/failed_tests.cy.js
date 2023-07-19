/// <reference types="Cypress" />

describe("test run with failed test cases", () => {
  it("should show failed test case summaries on failed tests page", () => {
    const publicId = "123io1";

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });

    cy.intercept("GET", `run/${publicId}`, { fixture: "test_run.json" });

    cy.intercept("GET", `run/${publicId}/cases/failed`, {
      fixture: "failed_test_cases.json",
    });

    cy.intercept("GET", `run/${publicId}/suite/1/case/2`, {
      fixture: "failed_test_case_2.json",
    });

    cy.interceptTestRunBasicRequests(publicId);

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
    const publicId = "23132";
    const testSuiteIdx = 1;
    const testCaseIdx = 2;

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });

    cy.intercept("GET", `run/${publicId}`, { fixture: "test_run.json" });

    cy.intercept("GET", `run/${publicId}/cases/failed`, {
      fixture: "failed_test_cases.json",
    });

    cy.intercept(
      "GET",
      `run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`,
      { fixture: "failed_test_case_2.json" }
    );

    cy.intercept("GET", `run/${publicId}/suite/${testSuiteIdx}/systemOut`, {
      fixture: "test_output_system_out.json",
    });

    cy.intercept("GET", `run/${publicId}/suite/${testSuiteIdx}/systemErr`, {
      fixture: "test_output_system_err.json",
    });

    cy.interceptTestRunBasicRequests(publicId);

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

  it("should show test cases collapsed initially when 6 failed test cases", () => {
    const publicId = "321908";

    const testCaseIndexes = [1, 2, 3, 4, 5, 6];

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });

    cy.intercept("GET", `run/${publicId}/cases/failed`, {
      fixture: "failed_test_cases_6.json",
    });

    cy.interceptTestRunBasicRequests(publicId);

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.findByTestId("test-failure-collapse-all-link").should("not.exist");
    testCaseIndexes.forEach((testCaseIdx) => {
      cy.findByTestId(`test-case-failure-text-1-${testCaseIdx}`).should(
        "not.exist"
      );
    });

    cy.findByTestId("test-failure-expand-all-link").click();
    testCaseIndexes.forEach((testCaseIdx) => {
      cy.findByTestId(`test-case-failure-text-1-${testCaseIdx}`).should(
        "exist"
      );
    });

    cy.findByTestId("test-failure-collapse-all-link").click();

    cy.findByTestId(`test-case-summary-1-2`).click();
    cy.findByTestId(`test-case-failure-text-1-2`).should(
      "contain",
      "Condition not satisfied2"
    );
  });
});
