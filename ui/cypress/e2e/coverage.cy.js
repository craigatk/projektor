/// <reference types="Cypress" />

context("test run with coverage data", () => {
  it("should show overall coverage stats on home page", () => {
    const publicId = "192301";

    cy.server();

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });

    cy.intercept("GET", `run/${publicId}/cases/failed`, {
      fixture: "failed_test_cases.json",
    });

    cy.intercept("GET", `run/${publicId}/coverage`, {
      fixture: "coverage/coverage-three-groups.json",
    });

    cy.interceptTestRunBasicRequests(publicId);

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.testIdShouldExist("coverage-summary-title");

    cy.getByTestId("coverage-graph-title-line").should(
      "contain",
      "Line 97.31%"
    );
    cy.getByTestId("coverage-graph-title-statement").should(
      "contain",
      "Statement 96.23%"
    );
    cy.getByTestId("coverage-graph-title-branch").should(
      "contain",
      "Branch 77.02%"
    );
  });

  it("should show overall coverage stats on home page with previous run", () => {
    const publicId = "13438";

    cy.server();

    cy.route("GET", `run/${publicId}/summary`, "fixture:test_run_summary.json");

    cy.route(
      "GET",
      `run/${publicId}/cases/failed`,
      "fixture:failed_test_cases.json"
    );

    cy.route(
      "GET",
      `run/${publicId}/coverage`,
      "fixture:coverage/coverage-three-groups-previous-run.json"
    );

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.testIdShouldExist("coverage-summary-title");

    cy.getByTestId("coverage-graph-title-line").should(
      "contain",
      "Line 95.82% +0.5%"
    );
    cy.getByTestId("coverage-graph-title-statement").should(
      "contain",
      "Statement 96.06% +0.05"
    );
    cy.getByTestId("coverage-graph-title-branch").should(
      "contain",
      "Branch 77.02% +8.07%"
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

    cy.getByTestId("coverage-name-1").should("contain", "junit-results-parser");
    cy.getByTestId("coverage-name-2").should("contain", "jacoco-xml-parser");
    cy.getByTestId("coverage-name-3").should("contain", "server-app");
  });

  it("should not show coverage section when no coverage data available", () => {
    const publicId = "10832";

    cy.server();

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });
    cy.intercept("GET", `results/${publicId}/status`, { status: "SUCCESS" });
    cy.intercept("GET", "config", {});
    cy.intercept("GET", `run/${publicId}/messages`, {
      fixture: "messages/one_message.json",
    });
    cy.intercept("GET", `run/${publicId}/badge/coverage`, "");
    cy.intercept("GET", `run/${publicId}/performance`, {});
    cy.intercept("GET", `run/${publicId}/cases/failed`, {
      fixture: "failed_test_cases.json",
    });
    cy.intercept("GET", `run/${publicId}/quality`, {
      fixture: "quality/empty_code_quality_reports.json",
    });

    cy.intercept("GET", `run/${publicId}/coverage`, {
      status: 204,
      response: {},
    });

    cy.intercept("GET", `run/${publicId}/coverage/exists`, {
      fixture: "coverage/coverage-does-not-exist.json",
    });

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.testIdShouldNotExist("coverage-summary-title");

    cy.testIdShouldNotExist("nav-link-coverage");
  });
});
