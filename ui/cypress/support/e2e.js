// ***********************************************************
// This example support/index.js is processed and
// loaded automatically before your test files.
//
// This is a great place to put global configuration and
// behavior that modifies Cypress.
//
// You can change the location of this file or turn off
// automatically serving support files with the
// 'supportFile' configuration option.
//
// You can read more here:
// https://on.cypress.io/configuration
// ***********************************************************

import "../../../cypress-common/support/commands";
import "@testing-library/cypress/add-commands";

Cypress.Commands.add("interceptTestRunBasicRequests", (publicId) => {
  cy.intercept("GET", `results/${publicId}/status`, { status: "SUCCESS" });

  cy.intercept("GET", `run/${publicId}/metadata/git`, {
    fixture: "metadata/git-metadata-with-github-base-url.json",
  });

  cy.intercept("GET", "config", {
    fixture: "config/server_config_disabled.json",
  });

  cy.intercept("GET", `run/${publicId}/coverage/exists`, {
    fixture: "coverage/coverage-does-not-exist.json",
  });

  cy.intercept("GET", `run/${publicId}/coverage`, {
    fixture: "coverage/coverage-three-groups.json",
  });

  cy.intercept("GET", `run/${publicId}/messages`, {
    fixture: "messages/one_message.json",
  });

  cy.intercept("GET", `run/${publicId}/badge/coverage`, "");
  cy.intercept("GET", `run/${publicId}/badge/tests`, "");

  cy.intercept("GET", `run/${publicId}/performance`, {});
});

Cypress.on("uncaught:exception", (err, runnable) => {
  // Recharts throws an error with "ResizeObserver loop completed with undelivered notifications"
  // so don't fail the test when that happens
  // Ref https://docs.cypress.io/api/cypress-api/catalog-of-events#Uncaught-Exceptions
  if (
    err.message.includes(
      "ResizeObserver loop completed with undelivered notifications",
    )
  ) {
    return false;
  }
  // We still want to ensure there are no other unexpected errors, so we let them fail the test
});
