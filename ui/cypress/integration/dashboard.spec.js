/// <reference types="Cypress" />

context("dashboard", () => {
  it("should show failed test case summaries on dashboard page", () => {
    const publicId = "12345";

    cy.server();

    cy.route("GET", `run/${publicId}/summary`, "fixture:test_run_summary.json");

    cy.route(
      "GET",
      `run/${publicId}/cases/failed`,
      "fixture:failed_test_cases.json"
    );

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId("failed-tests-title").should("contain", "Failed tests");

    cy.getByTestId("test-case-title").should("have.length", 2);

    cy.getByTestId("test-case-title").should(
      "contain",
      "projektor.example.spock.FailingSpec should fail"
    );

    cy.getByTestId("test-case-title").should(
      "contain",
      "projektor.example.spock.FailingSpec should fail with output"
    );
  });

  it("when tests all passed should show test suite list on dashboard", () => {
    const publicId = "12345";

    cy.server();

    cy.route(
      "GET",
      `run/${publicId}/summary`,
      "fixture:one_passing/test_run_summary.json"
    );
    cy.route("GET", `run/${publicId}`, "fixture:one_passing/test_run.json");

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId("test-run-all-tests-title").should("contain", "All tests");

    cy.getByTestId("test-suite-class-name-1").should(
      "contain",
      "projektor.example.spock.PassingSpec"
    );
  });

  it("when fetching test run fails should render error message", () => {
    const publicId = "12345";

    cy.server();

    cy.route(
      "GET",
      `run/${publicId}/summary`,
      "fixture:one_passing/test_run_summary.json"
    );
    cy.route({
      method: "GET",
      url: `run/${publicId}`,
      status: 404,
      response: {}
    });

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId("test-run-all-tests-title").should("contain", "All tests");

    cy.getByTestId("loading-section-error").should(
      "contain",
      "Error loading data from server"
    );
  });
});
