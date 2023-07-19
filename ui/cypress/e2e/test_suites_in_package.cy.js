/// <reference types="Cypress" />

context("test run with failed test cases", () => {
  it("should link from test case details to test suites in package", () => {
    const publicId = "12345";

    const packageName = "projektor.example.spock";

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });

    cy.intercept("GET", `run/${publicId}/suites?package=${packageName}`, {
      fixture: "test_case_summaries_in_package.json",
    });

    cy.intercept("GET", `run/${publicId}/suite/1/case/1`, {
      fixture: "failed_test_case_1.json",
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

    cy.getByTestId("breadcrumb-link-package-name").click();

    cy.getByTestId("test-suite-package-name-header").should(
      "contain",
      packageName
    );
  });
});
