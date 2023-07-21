/// <reference types="Cypress" />

context("test suite", () => {
  it("should link from test case details to test suite", () => {
    const publicId = "12345";

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });

    cy.intercept("GET", `run/${publicId}/suite/1`, {
      fixture: "test_suite_failing.json",
    });

    cy.intercept("GET", `run/${publicId}/suite/1/case/2`, {
      fixture: "failed_test_case_2.json",
    });

    cy.interceptTestRunBasicRequests(publicId);

    cy.visit(`http://localhost:1234/tests/${publicId}/suite/1/case/2`);

    cy.getBreadcrumbPackgeNameLink().should(
      "contain",
      "projektor.example.spock"
    );

    cy.getBreadcrumbClassNameLink().should("contain", "FailingSpec");

    cy.getBreadcrumbEndingText().should("contain", "should fail with output");

    cy.getByTestId("breadcrumb-link-class-name").click();

    cy.getBreadcrumbPackgeNameLink().should(
      "contain",
      "projektor.example.spock"
    );
    cy.getBreadcrumbEndingText().should("contain", "FailingSpec");
  });

  it("should show test suite system out and err", () => {
    const publicId = "12345";
    const testSuiteIdx = 3;

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });

    cy.intercept("GET", `run/${publicId}/suite/${testSuiteIdx}`, {
      fixture: "test_suite_with_output.json",
    });

    cy.intercept("GET", `run/${publicId}/suite/${testSuiteIdx}/systemOut`, {
      fixture: "test_output_system_out.json",
    });

    cy.intercept("GET", `run/${publicId}/suite/${testSuiteIdx}/systemErr`, {
      fixture: "test_output_system_err.json",
    });

    cy.interceptTestRunBasicRequests(publicId);

    cy.visit(`http://localhost:1234/tests/${publicId}/suite/${testSuiteIdx}`);

    cy.getBreadcrumbPackgeNameLink().should(
      "contain",
      "projektor.example.spock"
    );
    cy.getBreadcrumbEndingText().should("contain", "OutputSpec");

    cy.getByTestId("test-suite-tab-system-out").click();
    cy.getCodeText().should("contain", "System out line 1");
    cy.url().should("contain", "/systemOut");

    cy.getByTestId("test-suite-tab-system-err").click();
    cy.getCodeText().should("contain", "System err line 1");
    cy.url().should("contain", "/systemErr");
  });
});
