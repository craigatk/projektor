/// <reference types="Cypress" />

context("grouped test suites", () => {
  it("can go load test suites in a package", () => {
    cy.loadGroupedFixture("cypress/fixtures/grouped-passing-tests.json");

    cy.getByTestId("test-suite-group-name-1").should("contain", "Group1");
  });
});
