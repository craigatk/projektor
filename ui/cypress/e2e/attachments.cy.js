/// <reference types="Cypress" />

context("test run with attachments", () => {
  it("should list attachments on attachments page", () => {
    const publicId = "12345";

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "attachments/test_run_summary_with_attachments.json",
    });

    cy.intercept("GET", `run/${publicId}`, {
      fixture: "test_run.json",
    });

    cy.intercept("GET", `run/${publicId}/attachments`, {
      fixture: "attachments/attachments.json",
    });

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId("nav-link-attachments").click();

    cy.getByTestId("attachments-title").should("contain", "Attachments");

    cy.getByTestId("attachment-file-name-test-attachment.txt").should(
      "contain",
      "test-attachment.txt",
    );
    cy.getByTestId("attachment-file-size-test-attachment.txt").should(
      "contain",
      "30 B",
    );

    cy.getByTestId("attachment-file-name-test-run-summary.png").should(
      "contain",
      "test-run-summary.png",
    );
    cy.getByTestId("attachment-file-size-test-run-summary.png").should(
      "contain",
      "32.2 kB",
    );

    cy.getByTestId("attachment-file-name-test-attachment.txt").click();
    cy.url().should("contain", "/attachments/test-attachment.txt");
  });

  it("should not show attachments nav link when run has no attachments", () => {
    const publicId = "12345";

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "attachments/test_run_summary_with_attachments.json",
    });

    cy.intercept("GET", `run/${publicId}`, {
      fixture: "test_run.json",
    });

    cy.intercept("GET", `run/${publicId}/attachments`, {
      fixture: "attachments/attachments_empty.json",
    });

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.testIdShouldNotExist("nav-link-attachments");
  });
});
