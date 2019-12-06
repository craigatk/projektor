/// <reference types="Cypress" />

context("test suites in package E2E", () => {
  it("can go load test suites in a package", () => {
    cy.loadFixture(
      "cypress/fixtures/TEST-projektor.example.spock.FailingSpec.xml",
      "/suites/package/projektor.example.spock"
    );

    cy.getByTestId("test-suite-class-name-1").should("contain", "projektor.example.spock.FailingSpec");
  });
});
