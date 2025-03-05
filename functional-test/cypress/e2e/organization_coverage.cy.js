/// <reference types="Cypress" />

context("organization coverage", () => {
  it("displays the repos with coverage", () => {
    const org = Math.random().toString(36).substr(2, 7);

    const loadMoreCoverageFunc = (publicId) =>
      cy.loadCoverageReport("server-app-jacocoTestReport.xml", publicId);

    const loadLessCoverageFunc = (publicId) =>
      cy.loadCoverageReport(
        "server-app-reduced-jacocoTestReport.xml",
        publicId,
      );

    cy.readFile("cypress/fixtures/grouped-passing-tests-with-git.json").then(
      (resultsBlob) => {
        resultsBlob.metadata.git.repoName = `${org}/more-coverage`;

        cy.loadGroupedFixtureDataAndVisitTestRun(
          resultsBlob,
          "",
          loadMoreCoverageFunc,
        );
      },
    );

    cy.readFile("cypress/fixtures/grouped-passing-tests-with-git.json").then(
      (resultsBlob) => {
        resultsBlob.metadata.git.repoName = `${org}/less-coverage`;

        cy.loadGroupedFixtureDataAndVisitTestRun(
          resultsBlob,
          "",
          loadLessCoverageFunc,
        );
      },
    );

    cy.getByTestId("nav-link-organization").click();

    cy.getByTestId("coverage-name-1").should("contain", `${org}/less-coverage`);
    cy.getByTestId("coverage-name-2").should("contain", `${org}/more-coverage`);

    cy.getByTestId("line-coverage-row-1-covered-percentage").should(
      "contain",
      "87.22%",
    );
    cy.getByTestId("statement-coverage-row-1-covered-percentage").should(
      "contain",
      "94.15%",
    );
    cy.getByTestId("branch-coverage-row-1-covered-percentage").should(
      "contain",
      "60.89%",
    );

    cy.getByTestId("line-coverage-row-2-covered-percentage").should(
      "contain",
      "97.44%",
    );
    cy.getByTestId("statement-coverage-row-2-covered-percentage").should(
      "contain",
      "96.34%",
    );
    cy.getByTestId("branch-coverage-row-2-covered-percentage").should(
      "contain",
      "77.02%",
    );
  });
});
