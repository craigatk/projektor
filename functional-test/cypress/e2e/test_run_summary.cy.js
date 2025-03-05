/// <reference types="Cypress" />

context("test run summary E2E", () => {
  it("should display test run summary and link to slow test cases", () => {
    cy.loadFixture(
      "cypress/fixtures/TEST-projektor.example.spock.FailingSpec.xml",
    );

    cy.getByTestId("test-count-list-passed").should("contain", "0");
    cy.getByTestId("test-count-list-failed").should("contain", "2");
    cy.getByTestId("test-count-list-skipped").should("contain", "0");
    cy.getByTestId("test-count-list-total").should("contain", "2");

    cy.getByTestId("test-run-average-duration").should("contain", "0.063s");
    cy.getByTestId("test-run-cumulative-duration").should("contain", "0.125s");
    cy.getByTestId("test-run-slowest-test-case-duration").should(
      "contain",
      "0.119s",
    );

    cy.testIdShouldExist("test-run-report-created-timestamp");

    cy.getByTestId("test-run-slow-test-cases-link").click();
    cy.getByTestId("slow-test-cases-title").should(
      "contain",
      "Slowest test cases",
    );
  });

  it("should link to failed test cases", () => {
    cy.loadFixture(
      "cypress/fixtures/TEST-projektor.example.spock.FailingSpec.xml",
    );

    cy.getByTestId("nav-link-failed-test-cases").click();

    cy.url().should("contain", "/failed");

    cy.getByTestId("test-case-title").should("have.length", 2);

    cy.getByTestId("test-case-title").should(
      "contain",
      "projektor.example.spock.FailingSpec should fail",
    );

    cy.getByTestId("test-case-title").should(
      "contain",
      "projektor.example.spock.FailingSpec should fail with output",
    );
  });
});
