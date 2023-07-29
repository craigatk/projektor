/// <reference types="Cypress" />

context("side nav", () => {
  it("should link to failed test cases", () => {
    const publicId = "12345";

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });

    cy.intercept("GET", `run/${publicId}`, { fixture: "test_run.json" });

    cy.intercept("GET", `run/${publicId}/cases/failed`, {
      fixture: "failed_test_cases.json",
    });

    cy.interceptTestRunBasicRequests(publicId);

    cy.visit(`http://localhost:1234/tests/${publicId}`);

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
