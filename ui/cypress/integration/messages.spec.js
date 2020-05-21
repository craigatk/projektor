/// <reference types="Cypress" />

context("test run messages", () => {
  it("when one messages should show it on dashboard page", () => {
    const publicId = "12345";

    cy.server();

    cy.route(
      "GET",
      `run/${publicId}/summary`,
      "fixture:one_passing/test_run_summary.json"
    );
    cy.route("GET", `run/${publicId}`, "fixture:one_passing/test_run.json");

    cy.route(
      "GET",
      `run/${publicId}/messages`,
      "fixture:messages/one_message.json"
    );

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId("test-run-message-1").should(
      "contain",
      "Here is one message"
    );
  });

  it("when no messages should not display any dashboard page", () => {
    const publicId = "12345";

    cy.server();

    cy.route(
      "GET",
      `run/${publicId}/summary`,
      "fixture:one_passing/test_run_summary.json"
    );
    cy.route("GET", `run/${publicId}`, "fixture:one_passing/test_run.json");

    cy.route(
      "GET",
      `run/${publicId}/messages`,
      "fixture:messages/no_messages.json"
    );

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.testIdShouldExist("test-run-messages");

    cy.testIdShouldNotExist("test-run-message-1");
  });
});
