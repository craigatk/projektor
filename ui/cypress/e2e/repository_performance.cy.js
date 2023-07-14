/// <reference types="Cypress" />

describe("repository performance", () => {
  it("should display performance timeline on home page", () => {
    const repoName = "performance-org/performance-repo";

    cy.server();

    cy.route(
      "GET",
      `repo/${repoName}/performance/timeline`,
      "fixture:repository/performance_timeline.json"
    );

    const publicIds = ["EYL6XMND5HOO", "QWPCWITIDJ8L", "BRDYYHG9DQAU"];

    cy.visit(`http://localhost:1234/repository/${repoName}/`);

    cy.testIdShouldExist("repository-performance-timeline-graph");

    cy.getByTestId("performance-timeline-title-1").should(
      "contain",
      "perf-test"
    );

    publicIds.forEach((publicId) =>
      cy.roleShouldExist(`dot-requestsPerSecond-${publicId}`)
    );

    publicIds.forEach((publicId) => cy.roleShouldExist(`dot-p95-${publicId}`));

    const publicIdToClick = publicIds[1];
    cy.getByRole(`dot-p95-${publicIdToClick}`).click();
    cy.url().should("contain", `/tests/${publicIdToClick}`);
  });

  it("should display performance timeline on performance page", () => {
    const repoName = "performance-org/performance-repo";

    cy.server();

    cy.route(
      "GET",
      `repo/${repoName}/performance/timeline`,
      "fixture:repository/performance_timeline.json"
    );

    const publicIds = ["EYL6XMND5HOO", "QWPCWITIDJ8L", "BRDYYHG9DQAU"];

    cy.visit(`http://localhost:1234/repository/${repoName}/`);

    cy.getByTestId("nav-link-repo-performance").click();

    cy.testIdShouldExist("repository-performance-timeline-graph");

    cy.getByTestId("performance-timeline-title-1").should(
      "contain",
      "perf-test"
    );

    publicIds.forEach((publicId) =>
      cy.roleShouldExist(`dot-requestsPerSecond-${publicId}`)
    );

    publicIds.forEach((publicId) => cy.roleShouldExist(`dot-p95-${publicId}`));
  });

  it("should display tooltip with timeline data on graph point hover", () => {
    const repoName = "performance-org/performance-repo";

    cy.server();

    cy.route(
      "GET",
      `repo/${repoName}/performance/timeline`,
      "fixture:repository/performance_timeline.json"
    );

    const publicId = "QWPCWITIDJ8L";

    cy.visit(`http://localhost:1234/repository/${repoName}`);

    cy.testIdShouldExist("repository-performance-timeline-graph");

    cy.getByRole(`dot-p95-${publicId}`).trigger("mouseover");

    cy.testIdShouldExist("performance-timeline-graph-tooltip");

    cy.getByTestId("performance-timeline-tooltip-average").should(
      "contain",
      "32.684ms"
    );
    cy.getByTestId("performance-timeline-tooltip-p95").should(
      "contain",
      "60.027ms"
    );
    cy.getByTestId("performance-timeline-tooltip-max").should(
      "contain",
      "382.67ms"
    );
    cy.getByTestId("performance-timeline-tooltip-rps").should(
      "contain",
      "496.002"
    );
  });
});
