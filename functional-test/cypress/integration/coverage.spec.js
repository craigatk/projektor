/// <reference types="Cypress" />

context("test coverage data", () => {
  it("should display coverage data from one coverage report", () => {
    const loadCoverageFunc = (publicId) =>
      cy.loadCoverageReport("server-app-jacocoTestReport.xml", publicId);

    cy.loadGroupedFixture(
      "cypress/fixtures/grouped-passing-tests.json",
      "",
      loadCoverageFunc
    );

    cy.getByTestId("coverage-graph-title-line").should(
      "contain",
      "Line 97.44%"
    );
    cy.getByTestId("coverage-graph-title-statement").should(
      "contain",
      "Statement 96.34%"
    );
    cy.getByTestId("coverage-graph-title-branch").should(
      "contain",
      "Branch 77.02%"
    );

    cy.getByTestId("nav-link-coverage").click();

    cy.getByTestId("coverage-title").should("contain", "Coverage");

    cy.getByTestId("coverage-graph-title-line").should(
      "contain",
      "Line 97.44%"
    );
    cy.getByTestId("coverage-graph-title-statement").should(
      "contain",
      "Statement 96.34%"
    );
    cy.getByTestId("coverage-graph-title-branch").should(
      "contain",
      "Branch 77.02%"
    );
  });
});
