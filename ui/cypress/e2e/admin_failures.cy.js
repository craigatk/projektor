/// <reference types="Cypress" />

context("admin failures", () => {
  it("should display recent failures", () => {
    cy.intercept("GET", `failures/recent?count=10`, {
      fixture: "admin/recent_failures_2.json",
    });

    cy.visit(`http://localhost:1234/admin`);

    cy.findByTestId("admin-failures-title").should("exist");
    cy.findByTestId("admin-failures-table").should("exist");

    const firstPublicId = "SJAWN6PITO29";
    cy.findByTestId(`admin-failures-id-${firstPublicId}`).should(
      "contain",
      firstPublicId
    );
    cy.findByTestId(`admin-failures-message-${firstPublicId}`).should(
      "contain",
      "Problem saving test results: Unexpected IOException"
    );
    cy.findByTestId(`admin-failures-body-type-${firstPublicId}`).should(
      "contain",
      "TEST_RESULTS"
    );
    cy.findByTestId(`admin-failures-body-${firstPublicId}`).should("exist");
    cy.findByTestId(`admin-failures-created-timestamp-${firstPublicId}`).should(
      "contain",
      "March"
    );

    const secondPublicId = "X3NFUCTIPXNV";
    cy.findByTestId(`admin-failures-id-${secondPublicId}`).should(
      "contain",
      secondPublicId
    );
    cy.findByTestId(`admin-failures-message-${secondPublicId}`).should(
      "contain",
      "Problem parsing test results: Unexpected IOException"
    );
    cy.findByTestId(`admin-failures-body-type-${secondPublicId}`).should(
      "contain",
      "TEST_RESULTS"
    );
    cy.findByTestId(`admin-failures-body-${secondPublicId}`).should("exist");
    cy.findByTestId(
      `admin-failures-created-timestamp-${secondPublicId}`
    ).should("contain", "March");
  });

  it("should support changing the number of failures that are loaded", () => {
    cy.intercept("GET", `failures/recent?count=10`, {
      fixture: "admin/recent_failures_2.json",
    });

    cy.intercept("GET", `failures/recent?count=20`, {
      fixture: "admin/recent_failures_20.json",
    });

    cy.visit(`http://localhost:1234/admin`);

    cy.findByTestId("admin-failures-title").should("exist");
    cy.findByTestId("admin-failures-table").should("exist");

    const firstPublicIds = ["SJAWN6PITO29", "X3NFUCTIPXNV"];
    firstPublicIds.forEach((publicId) =>
      cy
        .findByTestId(`admin-failures-id-${publicId}`)
        .should("contain", publicId)
    );

    cy.findByTestId("admin-failures-count-field").type(
      "{selectall}{backspace}20",
      {
        delay: 50,
      }
    );

    cy.findByTestId("admin-failures-load-button").click();

    cy.findByTestId("admin-failures-table").should("exist");

    const secondPublicIds = ["DCPKTZMDG2Q0", "Z8SGT2YTPLHH", "OMHJFVJMZRDL"];
    secondPublicIds.forEach((publicId) =>
      cy
        .findByTestId(`admin-failures-id-${publicId}`)
        .should("contain", publicId)
    );
  });
});
