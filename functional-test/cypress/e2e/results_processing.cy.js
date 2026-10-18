/// <reference types="Cypress" />

context("results processing E2E", () => {
  it("should show error when test results fail to process", () => {
    cy.readFile("cypress/fixtures/grouped-invalid-tests-payload.json").then(
      (results) => {
        cy.request({
          method: "POST",
          url: "http://localhost:8080/groupedResults",
          body: results,
          failOnStatusCode: false,
        }).then((response) => {
          expect(response.status).to.eq(400);

          cy.visit(`http://localhost:8080/tests/${response.body.id}`, {
            retryOnStatusCodeFailure: true,
          });

          cy.getByTestId("results-processing-failed")
            .should("contain", "Error processing test results")
            .and("contain", "Problem parsing test results");
        });
      },
    );
  });

  it("should show error for a test run that doesn't exist", () => {
    cy.visit("http://localhost:8080/tests/NOTAREALRUN1");

    cy.getByTestId("results-not-found").should(
      "contain",
      "No test results found with this ID",
    );
  });
});
