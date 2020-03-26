/// <reference types="Cypress" />

context("dashboard", () => {
  it("should show test run summary data on dashboard page", () => {
    const publicId = "12345";

    cy.server();

    cy.route("GET", `run/${publicId}/summary`, "fixture:test_run_summary.json");

    cy.route(
      "GET",
      `run/${publicId}/cases/failed`,
      "fixture:failed_test_cases.json"
    );

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId("test-count-list-passed").should("contain", "2");
    cy.getByTestId("test-count-list-failed").should("contain", "2");
    cy.getByTestId("test-count-list-skipped").should("contain", "0");
    cy.getByTestId("test-count-list-total").should("contain", "4");

    cy.getByTestId("test-run-report-created-timestamp").should(
      "contain",
      "March 25th 2020, 7:42:32 pm"
    );
  });

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
