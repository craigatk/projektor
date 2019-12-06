/// <reference types="Cypress" />

context("results processing", () => {
  it("when test results still processing should display message", () => {
    const publicId = "12345";

    cy.server();

    cy.route({
      method: "GET",
      url: `run/${publicId}/summary`,
      status: 404,
      response: {}
    });

    cy.route(
      "GET",
      `results/${publicId}/status`,
      "fixture:processing_status/processing_status_processing.json"
    );

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId("results-still-processing").should(
      "contain",
      "Your test results are still processing"
    );

    cy.route(
      "GET",
      `results/${publicId}/status`,
      "fixture:processing_status/processing_status_success.json"
    );

    cy.route(
      "GET",
      `run/${publicId}/summary`,
      "fixture:one_passing/test_run_summary.json"
    );
    cy.route("GET", `run/${publicId}`, "fixture:one_passing/test_run.json");

    cy.wait(1000);

    cy.getByTestId("test-run-all-tests-title").should("contain", "All tests");
  });

  it("should display error message when processing results failed", () => {
    const publicId = "12345";

    cy.server();

    cy.route({
      method: "GET",
      url: `run/${publicId}/summary`,
      status: 404,
      response: {}
    });

    cy.route(
      "GET",
      `results/${publicId}/status`,
      "fixture:processing_status/processing_status_error.json"
    );

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId("results-processing-failed").should(
      "contain",
      "Error processing test results"
    );
    cy.getByTestId("results-processing-failed").should(
      "contain",
      "Failed to parse results"
    );
  });
});
