/// <reference types="Cypress" />

context("repository home page", () => {
  it("should display repository timeline and coverage graphs on home page", () => {
    const repoName = "timeline-org/timeline-repo";

    cy.server();

    cy.route(
      "GET",
      `repo/${repoName}/timeline`,
      "fixture:repository/timeline.json"
    );

    cy.route(
      "GET",
      `repo/${repoName}/coverage/timeline`,
      "fixture:repository/coverage_timeline.json"
    );

    cy.visit(`http://localhost:1234/repository/${repoName}`);

    cy.testIdShouldExist("repository-timeline-graph");

    cy.testIdShouldExist("repository-coverage-timeline-graph");
  });
});
