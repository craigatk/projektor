/// <reference types="Cypress" />

context("attachments", () => {
  it("can load and view attachments", () => {
    cy.loadGroupedFixtureWithAttachments(
      "cypress/fixtures/grouped-passing-tests.json",
      [
        {
          attachmentPath: "cypress/fixtures/test-attachment.txt",
          attachmentName: "test-attachment.txt",
        },
      ]
    );

    cy.getByTestId("test-suite-group-name-1").should("contain", "Group1");

    cy.getByTestId("nav-link-attachments").click();

    cy.getByTestId("attachments-title").should("contain", "Attachments");

    cy.getByTestId("attachment-file-name-test-attachment.txt").should(
      "contain",
      "test-attachment.txt"
    );

    cy.getByTestId("attachment-file-name-test-attachment.txt").click();
    cy.url().should("contain", "/attachments/test-attachment.txt");
  });
});
