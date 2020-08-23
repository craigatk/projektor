/// <reference types="Cypress" />

context("test run with coverage data", () => {
  it("should show overall coverage stats on home page", () => {
    const publicId = "12345";

    cy.server();

    cy.route("GET", `run/${publicId}/summary`, "fixture:test_run_summary.json");

    cy.route(
      "GET",
      `run/${publicId}/cases/failed`,
      "fixture:failed_test_cases.json"
    );

    cy.route(
      "GET",
      `run/${publicId}/coverage/overall`,
      "fixture:coverage/large-stats.json"
    );

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.testIdShouldExist("coverage-summary-title");

    cy.getByTestId("coverage-graph-title-line").should(
      "contain",
      "Line 97.44%"
    );
    cy.getByTestId("coverage-graph-title-statement").should(
      "contain",
      "Statement 96.34%"
    );
    cy.getByTestId("coverage-graph-title-branch").should(
      "contain",
      "Branch 77.02%"
    );
  });

  it("should show coverage stats on coverage page", () => {
    const publicId = "12345";

    cy.server();

    cy.route("GET", `run/${publicId}/summary`, "fixture:test_run_summary.json");

    cy.route(
      "GET",
      `run/${publicId}/cases/failed`,
      "fixture:failed_test_cases.json"
    );

    cy.route(
      "GET",
      `run/${publicId}/coverage/overall`,
      "fixture:coverage/large-stats.json"
    );

    cy.route(
      "GET",
      `run/${publicId}/coverage/exists`,
      "fixture:coverage/coverage-exists.json"
    );

    cy.route(
      "GET",
      `run/${publicId}/coverage`,
      "fixture:coverage/coverage-three-groups.json"
    );

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId("nav-link-coverage").click();

    cy.getByTestId("coverage-title").should("contain", "Coverage");

    cy.getByTestId("coverage-graph-title-line").should("contain", "Line");
    cy.getByTestId("coverage-graph-title-statement").should(
      "contain",
      "Statement"
    );
    cy.getByTestId("coverage-graph-title-branch").should("contain", "Branch");

    cy.getByTestId("name-server-app").should("contain", "server-app");
    cy.getByTestId("name-junit-results-parser").should(
      "contain",
      "junit-results-parser"
    );
    cy.getByTestId("name-jacoco-xml-parser").should(
      "contain",
      "jacoco-xml-parser"
    );
  });

  it("should show not show overall branch stats on home page when there aren't any", () => {
    const publicId = "12345";

    cy.server();

    cy.route("GET", `run/${publicId}/summary`, "fixture:test_run_summary.json");

    cy.route(
      "GET",
      `run/${publicId}/cases/failed`,
      "fixture:failed_test_cases.json"
    );

    cy.route(
      "GET",
      `run/${publicId}/coverage/overall`,
      "fixture:coverage/no-branch-stat.json"
    );

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.testIdShouldExist("coverage-summary-title");

    cy.getByTestId("coverage-graph-title-line").should(
      "contain",
      "Line 92.31%"
    );
    cy.getByTestId("coverage-graph-title-statement").should(
      "contain",
      "Statement 79.31%"
    );
    cy.testIdShouldNotExist("coverage-graph-title-branch");
  });

  it("should not show coverage section when no coverage data available", () => {
    const publicId = "12345";

    cy.server();

    cy.route("GET", `run/${publicId}/summary`, "fixture:test_run_summary.json");

    cy.route(
      "GET",
      `run/${publicId}/cases/failed`,
      "fixture:failed_test_cases.json"
    );

    cy.route({
      method: "GET",
      url: `run/${publicId}/coverage/overall`,
      status: 404,
      response: {},
    });

    cy.route(
      "GET",
      `run/${publicId}/coverage/exists`,
      "fixture:coverage/coverage-does-not-exist.json"
    );

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.testIdShouldNotExist("coverage-summary-title");

    cy.testIdShouldNotExist("nav-link-coverage");
  });
});
