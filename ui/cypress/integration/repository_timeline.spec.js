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

    cy.findByTestId("repository-timeline-graph").should("exist");

    publicIds.forEach((publicId) =>
      cy.findByRole(`dot-duration-${publicId}`).should("exist")
    );

    const publicIdToClick = publicIds[1];
    cy.findByRole(`dot-duration-${publicIdToClick}`).click();

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

    cy.findByTestId("repository-timeline-graph").should("exist");

    cy.findByRole(`dot-duration-${publicId}`).trigger("mouseover");

    cy.findByTestId("timeline-graph-tooltip").should("exist");

    cy.findByTestId("timeline-tooltip-duration").should("contain", "10.258s");
    cy.findByTestId("timeline-tooltip-test-count").should(
      "contain",
      "63 tests"
    );
    cy.findByTestId("timeline-tooltip-average-duration").should(
      "contain",
      "0.163s"
    );
    cy.findByTestId("timeline-tooltip-run-date").should(
      "contain",
      "Sep 24th 2020"
    );
  });
});
