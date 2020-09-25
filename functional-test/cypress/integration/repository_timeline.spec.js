/// <reference types="Cypress" />

context("repository timeline", () => {
    it("displays test run over time for the repository", () => {
        const orgPart = Math.random().toString(36).substr(2, 7);
        const repoPart = "timeline-repo";
        const repoName = `${orgPart}/${repoPart}`;

        cy.readFile("cypress/fixtures/grouped-passing-tests-with-git.json").then(
            (resultsBlob) => {
                resultsBlob.metadata.ci = true;
                resultsBlob.metadata.git.repoName = repoName;

                cy.loadGroupedFixtureData(resultsBlob, "");
            }
        );

        cy.readFile("cypress/fixtures/grouped-passing-tests-with-git.json").then(
            (resultsBlob) => {
                resultsBlob.metadata.ci = true;
                resultsBlob.metadata.git.repoName = repoName;

                cy.loadGroupedFixtureData(resultsBlob, "");
            }
        );

        cy.getByTestId("nav-link-repository").click(); // From the test run page

        cy.getByTestId("nav-link-repo-timeline").click(); // From the repo page

        cy.getByTestId("repository-timeline-graph");
    });
});
