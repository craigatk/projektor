/// <reference types="Cypress" />

describe("Performance results", () => {
  it("should display performance results on dashboard", () => {
    cy.loadGroupedFixture(
      "cypress/fixtures/performance/grouped-results-k6-get-run.json"
    );

    cy.getByTestId("performance-results-title").should(
      "contain",
      "Performance tests"
    );

    cy.getByTestId("performance-result-name-1").should("contain", "perf.json");
    cy.getByTestId("performance-result-average-1").should("contain", "11.072 ms");
    cy.getByTestId("performance-result-p95-1").should("contain", "22.294 ms");
    cy.getByTestId("performance-result-maximum-1").should("contain", "410.53 ms");
    cy.getByTestId("performance-result-requests-per-second-1").should(
      "contain",
      "1019.924"
    );
    cy.getByTestId("performance-result-request-count-1").should(
      "contain",
      "66707"
    );
  });
});
