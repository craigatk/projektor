/// <reference types="Cypress" />

context("repository timeline", () => {
  it("displays test run over time for the repository", () => {
    const orgPart = Math.random().toString(36).substr(2, 7);
    const repoPart = "timeline-repo";
    const repoName = `${orgPart}/${repoPart}`;

    cy.readFile("cypress/fixtures/grouped-failing-tests-with-git.json").then(
      (resultsBlob) => {
        resultsBlob.metadata.ci = true;
        resultsBlob.metadata.git.repoName = repoName;

        cy.loadGroupedFixtureDataAndVisitTestRun(resultsBlob, "");

        cy.getByTestId("test-count-list-failed").should("contain", "2");
      },
    );

    cy.wait(1000);

    cy.readFile("cypress/fixtures/grouped-passing-tests-with-git.json")
      .then((resultsBlob) => {
        resultsBlob.metadata.ci = true;
        resultsBlob.metadata.git.repoName = repoName;

        return cy.loadGroupedFixtureData(resultsBlob);
      })
      .then((publicId) => {
        cy.visit(`http://localhost:8080/tests/${publicId}`, {
          retryOnStatusCodeFailure: true,
        });

        cy.getByTestId("test-count-list-passed").should("contain", "3");

        cy.getByTestId("nav-link-repository").click(); // From the test run page

        cy.getByTestId("nav-link-repo-timeline").click(); // From the repo page

        cy.getByTestId("repository-timeline-graph");

        cy.findByRole(`dot-duration-${publicId}`, { timeout: 30000 }).trigger(
          "mouseover",
        );

        cy.testIdShouldExist("timeline-graph-tooltip");

        cy.getByTestId("timeline-tooltip-duration").should("contain", "0.460s");
        cy.getByTestId("timeline-tooltip-test-count").should(
          "contain",
          "3 tests",
        );
        cy.getByTestId("timeline-tooltip-average-duration").should(
          "contain",
          "0.153s",
        );
      });
  });
});
