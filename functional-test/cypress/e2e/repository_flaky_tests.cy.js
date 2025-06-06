import _ from "lodash";

/// <reference types="Cypress" />

context("repository timeline", () => {
  it("displays flaky tests in repository", () => {
    const orgPart = Math.random().toString(36).substr(2, 7);
    const repoPart = "flaky-tests-repo";
    const repoName = `${orgPart}/${repoPart}`;

    _.times(5, () => {
      cy.readFile("cypress/fixtures/grouped-failing-tests-with-git.json").then(
        (resultsBlob) => {
          resultsBlob.metadata.git.repoName = repoName;

          cy.loadGroupedFixtureDataAndVisitTestRun(resultsBlob, "");
        },
      );
    });

    cy.getByTestId("nav-link-repository").click(); // From the test run page

    cy.getByTestId("nav-link-repo-flaky-tests").click(); // From the repo page

    cy.getByTestId("repository-flaky-tests-table");

    cy.getByTestId("flaky-test-case-name-1").should(
      "contain",
      "projektor.example.spock.FailingSpec",
    );
    cy.getByTestId("flaky-test-case-failure-count-1").should("contain", "5");
    cy.getByTestId("flaky-test-case-failure-percentage-1").should(
      "contain",
      "100%",
    );
  });
});
