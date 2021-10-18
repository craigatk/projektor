context("code quality reports", () => {
  it("should display code quality reports", () => {
    cy.readFile("cypress/fixtures/grouped-code-quality.json").then(
      (resultsBlob) => {
        cy.loadGroupedFixtureDataAndVisitTestRun(resultsBlob, "");
      }
    );

    cy.findByTestId("code-quality-summary-title").should("contain", "Code quality")

    cy.findByText("code_quality.txt");
    cy.findByText("linter.txt").click();

    cy.findByTestId("code-quality-title").should("contain", "Code quality")

    cy.findByText("Linter line 1");
    cy.findByText("Linter line 2");
  });
});
