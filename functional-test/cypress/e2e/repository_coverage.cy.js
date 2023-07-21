/// <reference types="Cypress" />

context("repository coverage", () => {
  it("displays coverage over time for the repository", () => {
    const orgPart = Math.random().toString(36).substr(2, 7);
    const repoPart = "cov-repo";
    const repoName = `${orgPart}/${repoPart}`;

    const loadMoreCoverageFunc = (publicId) =>
      cy.loadCoverageReport("server-app-jacocoTestReport.xml", publicId);

    const loadLessCoverageFunc = (publicId) =>
      cy.loadCoverageReport(
        "server-app-reduced-jacocoTestReport.xml",
        publicId
      );

    cy.readFile("cypress/fixtures/grouped-passing-tests-with-git.json").then(
      (resultsBlob) => {
        resultsBlob.metadata.git.repoName = repoName;

        cy.loadGroupedFixtureDataAndVisitTestRun(
          resultsBlob,
          "",
          loadMoreCoverageFunc
        );
      }
    );

    cy.readFile("cypress/fixtures/grouped-passing-tests-with-git.json").then(
      (resultsBlob) => {
        resultsBlob.metadata.git.repoName = repoName;

        cy.loadGroupedFixtureDataAndVisitTestRun(
          resultsBlob,
          "",
          loadLessCoverageFunc
        );
      }
    );

    cy.getByTestId("nav-link-repository").click();

    cy.getByTestId("repository-coverage-timeline-graph");
  });
});
