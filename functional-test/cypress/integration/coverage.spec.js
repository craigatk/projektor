/// <reference types="Cypress" />

context("test coverage data", () => {
  it("should display overall coverage data on summary page", () => {
    const loadCoverageFunc = (publicId) =>
      cy
        .readFile("cypress/fixtures/server-app-jacocoTestReport.xml")
        .then((coverageFileContents) =>
          cy.request({
            method: "POST",
            url: `http://localhost:8080/run/${publicId}/coverage`,
            body: coverageFileContents,
            retryOnStatusCodeFailure: true,
          })
        );

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
  });
});
