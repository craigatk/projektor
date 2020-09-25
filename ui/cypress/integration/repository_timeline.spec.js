/// <reference types="Cypress" />

context("repository coverage", () => {
  it("should display repository timeline graph and link to individual test reports", () => {
    const repoName = "timeline-org/timeline-repo";

    cy.server();

    cy.route(
      "GET",
      `repo/${repoName}/timeline`,
      "fixture:repository/timeline.json"
    );

    const publicIds = [
      "JHLQTLR7XGQH",
      "GGHZI4NSIMSF",
      "RZYV6GVTUUOL",
      "PC6TVRUMGPOS",
    ];

    cy.visit(`http://localhost:1234/repository/${repoName}/timeline`);

    cy.testIdShouldExist("repository-timeline-graph");

    publicIds.forEach((publicId) =>
      cy.roleShouldExist(`dot-duration-${publicId}`)
    );

    const publicIdToClick = publicIds[1];
    cy.getByRole(`dot-duration-${publicIdToClick}`).click();
    cy.url().should("contain", `/tests/${publicIdToClick}`);
  });

  it("should display tooltip with timeline data on graph point hover", () => {
    const repoName = "timeline-org/timeline-repo";

    cy.server();

    cy.route(
      "GET",
      `repo/${repoName}/timeline`,
      "fixture:repository/timeline.json"
    );

    const publicId = "GGHZI4NSIMSF";

    cy.visit(`http://localhost:1234/repository/${repoName}/timeline`);

    cy.testIdShouldExist("repository-timeline-graph");

    cy.getByRole(`dot-duration-${publicId}`).trigger("mouseover");

    cy.testIdShouldExist("timeline-graph-tooltip");

    cy.getByTestId("timeline-tooltip-duration").should("contain", "10.258s");
    cy.getByTestId("timeline-tooltip-test-count").should("contain", "63 tests");
    cy.getByTestId("timeline-tooltip-run-date").should(
      "contain",
      "Sep 24th 2020"
    );
  });
});
