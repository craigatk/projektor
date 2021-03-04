/// <reference types="Cypress" />

context("admin failures", () => {
  it("should display recent failures", () => {
    cy.server();

    cy.route(
      "GET",
      `failures/recent?count=10`,
      "fixture:admin/recent_failures.json"
    );

    cy.visit(`http://localhost:1234/admin`);

    cy.testIdShouldExist("admin-failures-title");
    cy.testIdShouldExist("admin-failures-table");

    const firstPublicId = "SJAWN6PITO29";
    cy.getByTestId(`admin-failures-id-${firstPublicId}`).should(
      "contain",
      firstPublicId
    );
    cy.getByTestId(`admin-failures-message-${firstPublicId}`).should(
      "contain",
      "Problem saving test results: Unexpected IOException"
    );
    cy.getByTestId(`admin-failures-body-type-${firstPublicId}`).should(
      "contain",
      "TEST_RESULTS"
    );
    cy.testIdShouldExist(`admin-failures-body-${firstPublicId}`);
    cy.getByTestId(`admin-failures-created-timestamp-${firstPublicId}`).should(
      "contain",
      "March"
    );

    const secondPublicId = "X3NFUCTIPXNV";
    cy.getByTestId(`admin-failures-id-${secondPublicId}`).should(
      "contain",
      secondPublicId
    );
    cy.getByTestId(`admin-failures-message-${secondPublicId}`).should(
      "contain",
      "Problem parsing test results: Unexpected IOException"
    );
    cy.getByTestId(`admin-failures-body-type-${secondPublicId}`).should(
      "contain",
      "TEST_RESULTS"
    );
    cy.testIdShouldExist(`admin-failures-body-${secondPublicId}`);
    cy.getByTestId(`admin-failures-created-timestamp-${secondPublicId}`).should(
      "contain",
      "March"
    );
  });
});
