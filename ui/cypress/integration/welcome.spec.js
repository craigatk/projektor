/// <reference types="Cypress" />

context("welcome", () => {
  it("should display welcome message on home screen", () => {
    cy.visit(`http://localhost:1234/`);

    cy.getByTestId("welcome-title").should("contain", "Welcome to Projektor");
  });
});
