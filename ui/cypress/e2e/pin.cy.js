/// <reference types="Cypress" />

context("tests with pins", () => {
  it("when cleanup enabled should allow pinning of report from side menu", () => {
    const publicId = "12345";

    cy.intercept("GET", `run/${publicId}/attachments`, {
      fixture: "attachments/attachments_empty.json",
    });

    cy.intercept("GET", `run/${publicId}`, {
      fixture: "one_passing/test_run.json",
    });

    cy.intercept("GET", `/run/${publicId}/summary`, {
      fixture: "one_passing/test_run_summary.json",
    });

    cy.intercept("GET", `/config`, {
      fixture: "config/cleanup_config_enabled.json",
    });

    cy.intercept("GET", `/run/${publicId}/attributes`, {
      statusCode: 404,
      body: "",
    });

    cy.intercept("POST", `/run/${publicId}/attributes/pin`, {
      fixture: "attributes/pinned.json",
    }).as("pin");

    cy.intercept("POST", `/run/${publicId}/attributes/unpin`, {
      fixture: "attributes/not_pinned.json",
    }).as("unpin");

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId("nav-link-pin").click();

    cy.getByTestId("nav-link-unpin").click();
  });

  it("when cleanup enabled should allow pinning of report from dashboard", () => {
    const publicId = "12345";

    cy.intercept("GET", `run/${publicId}/attachments`, {
      fixture: "attachments/attachments_empty.json",
    });

    cy.intercept("GET", `run/${publicId}`, {
      fixture: "one_passing/test_run.json",
    });

    cy.intercept("GET", `/run/${publicId}/summary`, {
      fixture: "one_passing/test_run_summary.json",
    });

    cy.intercept("GET", `/config`, {
      fixture: "config/cleanup_config_enabled.json",
    });

    cy.intercept("GET", `/run/${publicId}/attributes`, {
      statusCode: 404,
      body: "",
    });

    cy.intercept("POST", `/run/${publicId}/attributes/pin`, {
      fixture: "attributes/pinned.json",
    }).as("pin");

    cy.intercept("POST", `/run/${publicId}/attributes/unpin`, {
      fixture: "attributes/not_pinned.json",
    }).as("unpin");

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId("test-run-header-pin-link").click();

    cy.getByTestId("test-run-header-unpin-link").click();
  });

  it("when cleanup disabled should not show pin link in side menu or in dashboard", () => {
    const publicId = "12345";

    cy.intercept("GET", `run/${publicId}/attachments`, {
      fixture: "attachments/attachments_empty.json",
    });

    cy.intercept("GET", `run/${publicId}`, {
      fixture: "one_passing/test_run.json",
    });

    cy.intercept("GET", `/run/${publicId}/summary`, {
      fixture: "one_passing/test_run_summary.json",
    });

    cy.intercept("GET", `/config`, {
      fixture: "config/cleanup_config_disabled.json",
    });

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.testIdShouldNotExist("nav-link-pin");

    cy.testIdShouldNotExist("test-run-header-pin-link");
  });
});
