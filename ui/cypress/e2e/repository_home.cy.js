/// <reference types="Cypress" />

context("repository home page", () => {
  it("should display repository timeline and coverage graphs on home page", () => {
    const repoName = "timeline-org/timeline-repo";

    cy.intercept("GET", `repo/${repoName}/timeline`, {
      fixture: "repository/timeline.json",
    });

    cy.intercept("GET", `repo/${repoName}/coverage/exists`, {
      fixture: "repository/coverage_exists_true.json",
    });
    cy.intercept("GET", `repo/${repoName}/coverage/timeline`, {
      fixture: "repository/coverage_timeline.json",
    });

    cy.visit(`http://localhost:1234/repository/${repoName}`);

    cy.testIdShouldExist("repository-timeline-graph");

    cy.testIdShouldExist("repository-coverage-timeline-graph");
  });
});
