/// <reference types="Cypress" />

context("test run with slow tests", () => {
  it("should link to slow test cases", () => {
    const publicId = "12345";

    cy.interceptTestRunBasicRequests(publicId);

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });

    cy.intercept("GET", `run/${publicId}`, { fixture: "test_run.json" });

    cy.intercept("GET", `run/${publicId}/cases/slow`, {
      fixture: "slow_test_cases.json",
    });

    cy.intercept("GET", `run/${publicId}/cases/failed`, {
      fixture: "failed_test_cases.json",
    });

    cy.intercept("GET", `run/${publicId}/suite/3/case/1`, {
      fixture: "slow_test_case_1.json",
    });

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId("nav-link-slow").click();

    cy.getByTestId("slow-test-cases-title").should(
      "contain",
      "Slowest test cases"
    );

    const testSuiteIdx = 3;
    const testCaseIdx = 1;

    cy.getByTestId(`test-case-name-${testSuiteIdx}-${testCaseIdx}`).should(
      "contain",
      "projektor.example.spock.OutputSpec.should include system out and system err"
    );

    cy.getTestCaseLinkInList(testSuiteIdx, testCaseIdx).click();

    cy.getBreadcrumbPackgeNameLink().should(
      "contain",
      "projektor.example.spock"
    );
    cy.getBreadcrumbClassNameLink().should("contain", "OutputSpec");
    cy.getBreadcrumbEndingText().should(
      "contain",
      "should include system out and system err"
    );
  });
});
