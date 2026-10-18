/// <reference types="Cypress" />

context("all tests", () => {
  it("should list every test suite in the test run", () => {
    const publicId = "12345";

    cy.interceptTestRunBasicRequests(publicId);

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });
    cy.intercept("GET", new RegExp(`run/${publicId}$`), {
      fixture: "test_run.json",
    });

    cy.visit(`http://localhost:1234/tests/${publicId}/all`);

    cy.getByTestId("test-run-all-tests-title").should("contain", "All tests");

    cy.getByTestId("test-suite-class-name-1").should(
      "contain",
      "projektor.example.spock.PassingSpec",
    );
    cy.getByTestId("test-suite-class-name-2").should(
      "contain",
      "projektor.example.spock.FailingSpec",
    );
    cy.getByTestId("test-suite-class-name-3").should(
      "contain",
      "projektor.example.spock.OutputSpec",
    );
  });
});
