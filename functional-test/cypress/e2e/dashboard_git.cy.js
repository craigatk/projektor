/// <reference types="Cypress" />

context("dashboard Git metadata E2E", () => {
  it("should show pull request number and commit SHA on dashboard", () => {
    cy.readFile(
      "cypress/fixtures/grouped-failing-tests-with-pr-and-commit.json",
    ).then((results) => {
      // A repo of its own, so this run doesn't change other specs' repository data
      results.metadata.git.repoName = `dashboard-git-org/repo-${Date.now()}`;

      cy.loadGroupedFixtureDataAndVisitTestRun(results);
    });

    // Shown as links to GitHub, since the functional test server sets a GitHub base URL
    cy.findByTestId("dashboard-summary-pull-request-number").should(
      "contain",
      "42",
    );
    cy.findByTestId("dashboard-summary-pull-request-link")
      .should("have.attr", "href")
      .and("contain", "/pull/42");
    // The dashboard shows the last 7 characters of the commit SHA
    cy.findByTestId("dashboard-summary-commit-sha").should(
      "contain",
      "ad1a7a9",
    );
    cy.findByTestId("dashboard-summary-commit-sha-link")
      .should("have.attr", "href")
      .and("contain", "/commit/2a6638c4a1b5bba7d7bf0f5e0bf1f1be3ad1a7a9");
  });
});
