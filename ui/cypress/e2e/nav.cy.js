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

  it("should navigate between test run sections from side nav", () => {
    const publicId = "12345";

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });
    cy.intercept("GET", new RegExp(`run/${publicId}$`), {
      fixture: "test_run.json",
    });
    cy.intercept("GET", new RegExp(`run/${publicId}/cases/failed$`), {
      fixture: "failed_test_cases.json",
    });

    cy.interceptTestRunBasicRequests(publicId);

    cy.visit(`http://localhost:1234/tests/${publicId}/failed`);
    cy.getByTestId("failed-tests-title").should("contain", "Failed tests");

    cy.getByTestId("nav-link-all").click();
    cy.url().should("contain", `/tests/${publicId}/all`);
    cy.getByTestId("test-run-all-tests-title").should("contain", "All tests");

    cy.getByTestId("nav-link-dashboard").click();
    cy.url().should("match", new RegExp(`/tests/${publicId}/?$`));
    cy.getByTestId("test-count-list-total").should("contain", "4");

    cy.getByTestId("nav-link-docs")
      .closest("a")
      .should("have.attr", "href", "https://projektor.dev/docs/");
  });
});
