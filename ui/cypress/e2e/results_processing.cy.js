/// <reference types="Cypress" />

context("results processing", () => {
  it("when test results still processing should display message", () => {
    const publicId = "12345";

    cy.intercept("GET", `run/${publicId}`, {
      statusCode: 404,
      body: {},
    });

    cy.intercept("GET", `run/${publicId}/summary`, {
      statusCode: 404,
      body: {},
    });

    cy.intercept("GET", `results/${publicId}/status`, {
      fixture: "processing_status/processing_status_processing.json",
    });

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId("results-still-processing").should(
      "contain",
      "Your test results are still processing",
    );

    cy.intercept("GET", `results/${publicId}/status`, {
      fixture: "processing_status/processing_status_success.json",
    });

    cy.intercept("GET", `run/${publicId}/summary`, {
      statusCode: 200,
      fixture: "one_passing/test_run_summary.json",
    });

    cy.intercept("GET", `run/${publicId}`, {
      fixture: "one_passing/test_run.json",
    });

    cy.interceptTestRunBasicRequests(publicId);

    cy.wait(1000);

    cy.getByTestId("test-run-all-tests-title").should("contain", "All tests");
  });

  it("should display error message when processing results failed", () => {
    const publicId = "12345";

    cy.intercept("GET", `run/${publicId}`, {
      statusCode: 404,
      body: {},
    });

    cy.intercept("GET", `run/${publicId}/summary`, {
      statusCode: 404,
      body: {},
    });

    cy.intercept("GET", `results/${publicId}/status`, {
      fixture: "processing_status/processing_status_error.json",
    });

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId("results-processing-failed").should(
      "contain",
      "Error processing test results",
    );
    cy.getByTestId("results-processing-failed").should(
      "contain",
      "Failed to parse results",
    );
  });
});
