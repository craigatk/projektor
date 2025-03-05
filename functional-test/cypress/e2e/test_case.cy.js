/// <reference types="Cypress" />

context("test case E2E", () => {
  it("can go directly to a test case", () => {
    cy.loadFixture(
      "cypress/fixtures/TEST-projektor.example.spock.FailingSpec.xml",
      "/suite/1/case/1",
    );

    cy.getBreadcrumbEndingText().should("contain", "should fail");
  });
});
