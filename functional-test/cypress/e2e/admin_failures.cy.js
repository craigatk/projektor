/// <reference types="Cypress" />

context("admin failures", () => {
  it("should display recent failure", () => {
    cy.loadGroupedFixture(
      "cypress/fixtures/grouped-invalid-tests-payload.json",
    );

    cy.url().then((url) => {
      const urlParts = url.split("/");
      const publicId = urlParts[urlParts.length - 1];

      cy.visit(`http://localhost:8080/admin`);

      cy.testIdShouldExist("admin-failures-title");
      cy.testIdShouldExist("admin-failures-table");

      cy.getByTestId(`admin-failures-id-${publicId}`).should(
        "contain",
        publicId,
      );
      cy.getByTestId(`admin-failures-message-${publicId}`).should(
        "contain",
        "Problem parsing test results: Unexpected close tag",
      );
      cy.getByTestId(`admin-failures-body-type-${publicId}`).should(
        "contain",
        "TEST_RESULTS",
      );
      cy.testIdShouldExist(`admin-failures-body-${publicId}`);
      cy.testIdShouldExist(`admin-failures-created-timestamp-${publicId}`);
    });
  });
});
