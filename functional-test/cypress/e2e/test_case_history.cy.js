/// <reference types="Cypress" />

context("test case history", () => {
  it("shows test case history and where the current failures started", () => {
    const orgPart = Math.random().toString(36).substr(2, 7);
    const repoName = `${orgPart}/test-case-history-repo`;

    cy.readFile("cypress/fixtures/grouped-failing-tests-with-git.json").then(
      (failingResults) => {
        const passingBlob = failingResults.groupedTestSuites[0].testSuitesBlob
          .replace(/<failure[\s\S]*?<\/failure>/g, "")
          .replace('failures="2"', 'failures="0"');

        const buildResults = (passed, day) => {
          const results = JSON.parse(JSON.stringify(failingResults));
          results.metadata.git.repoName = repoName;
          results.metadata.git.commitSha = `${passed ? "aaaa" : "ffff"}${day}000000`;
          results.metadata.createdTimestamp = `2026-09-0${day}T12:00:00Z`;
          if (passed) {
            results.groupedTestSuites[0].testSuitesBlob = passingBlob;
          }
          return results;
        };

        cy.loadGroupedFixtureData(buildResults(true, 1));
        cy.loadGroupedFixtureData(buildResults(true, 2));
        cy.loadGroupedFixtureData(buildResults(false, 3));
        cy.loadGroupedFixtureDataAndVisitTestRun(
          buildResults(false, 4),
          "/suite/1/case/1/history",
        );
      },
    );

    cy.getByTestId("test-case-history-failure-streak", { timeout: 15000 })
      .should("contain", "Failing since")
      .and("contain", "ffff300")
      .and("contain", "Last passed")
      .and("contain", "aaaa200");

    cy.getByTestId("test-case-history-strip")
      .children()
      .should("have.length", 4);

    cy.getByTestId("test-case-history-row-1-result").should(
      "contain",
      "failed",
    );
    cy.getByTestId("test-case-history-row-2-result").should(
      "contain",
      "failed",
    );
    cy.getByTestId("test-case-history-row-3-result").should(
      "contain",
      "passed",
    );
    cy.getByTestId("test-case-history-row-4-result").should(
      "contain",
      "passed",
    );

    cy.getByTestId("test-case-history-last-passed-link").click();

    cy.url().should("contain", "/suite/1/case/1");
    cy.getByTestId("test-case-summary-result").should("contain", "passed");
  });
});
