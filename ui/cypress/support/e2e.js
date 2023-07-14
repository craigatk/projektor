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

  cy.intercept("GET", "config", {});

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

  cy.intercept("GET", `run/${publicId}/performance`, {});
});
