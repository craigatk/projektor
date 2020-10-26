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

  it("should display file-level coverage data", () => {
    const loadCoverageFunc = (publicId) =>
        cy.loadCoverageReport("server-app-jacocoTestReport.xml", publicId);

    cy.loadGroupedFixture(
        "cypress/fixtures/grouped-passing-tests.json",
        "",
        loadCoverageFunc
    );

    cy.getByTestId("nav-link-coverage").click();

    cy.getByTestId("coverage-title").should("contain", "Coverage");

    cy.getByTestId("nav-link-coverage").click();

    cy.getByTestId("coverage-name-1").should("contain", "server-app");
    cy.getByTestId("coverage-name-1").click();

    cy.getByTestId("coverage-file-name-1").should(
        "contain",
        "projektor/Application.kt"
    );
    cy.getByTestId(
        "coverage-file-line-coverage-row-1-covered-percentage"
    ).should("contain", "95.4%");
    cy.getByTestId("branch-coverage-row-1-covered-percentage").should(
        "contain",
        "85.71%"
    );
    cy.getByTestId("coverage-file-missed-lines-1").should(
        "contain",
        "102, 103, 104, 105"
    );
    cy.getByTestId("coverage-file-partial-lines-1").should(
        "contain",
        "92, 101"
    );
  });
});
