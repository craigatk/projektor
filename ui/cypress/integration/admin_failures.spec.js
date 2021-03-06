/// <reference types="Cypress" />

context("admin failures", () => {
  it("should display recent failures", () => {
    cy.server();

    cy.route(
      "GET",
      `failures/recent?count=10`,
      "fixture:admin/recent_failures_2.json"
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

  it("should support changing the number of failures that are loaded", () => {
    cy.server();

    cy.route(
      "GET",
      `failures/recent?count=10`,
      "fixture:admin/recent_failures_2.json"
    );

    cy.route(
      "GET",
      `failures/recent?count=20`,
      "fixture:admin/recent_failures_20.json"
    );

    cy.visit(`http://localhost:1234/admin`);

    cy.testIdShouldExist("admin-failures-title");
    cy.testIdShouldExist("admin-failures-table");

    const firstPublicIds = ["SJAWN6PITO29", "X3NFUCTIPXNV"];
    firstPublicIds.forEach((publicId) =>
      cy
        .getByTestId(`admin-failures-id-${publicId}`)
        .should("contain", publicId)
    );

    cy.getByTestId("admin-failures-count-field").type("{selectall}{backspace}20", {
      delay: 50,
    });

    cy.getByTestId("admin-failures-load-button").click()

    cy.testIdShouldExist("admin-failures-table");

    const secondPublicIds = ["DCPKTZMDG2Q0", "Z8SGT2YTPLHH", "OMHJFVJMZRDL"];
    secondPublicIds.forEach((publicId) =>
        cy
            .getByTestId(`admin-failures-id-${publicId}`)
            .should("contain", publicId)
    );
  });
});
