describe("Cypress test failure with attachment", () => {
  it("should display screenshot alongside failure", () => {
    cy.loadGroupedFixtureWithAttachments(
      "cypress/fixtures/cypress/cypress-grouped-failing-test-with-attachment.json",
      [
        {
          attachmentPath:
            "cypress/fixtures/cypress/test run with attachments -- should list attachments on attachments page (failed).png",
          attachmentName:
            "test run with attachments -- should list attachments on attachments page (failed).png",
        },
      ],
    );

    cy.getByTestId("nav-link-attachments").should("exist");

    cy.getByTestId("test-case-failure-screenshot-1-1")
      .should("have.attr", "src")
      .and(
        "contain",
        "/attachments/test run with attachments -- should list attachments on attachments page (failed).png",
      );

    cy.getByTestId("test-case-screenshot-link-1-1").click();

    cy.getByTestId("test-case-failure-screenshot-1-1")
      .should("have.attr", "src")
      .and(
        "contain",
        "/attachments/test run with attachments -- should list attachments on attachments page (failed).png",
      );
  });

  it("should display screenshot alongside failure and link to video", () => {
    cy.loadGroupedFixtureWithAttachments(
      "cypress/fixtures/cypress/cypress-grouped-failing-test-with-attachment.json",
      [
        {
          attachmentPath:
            "cypress/fixtures/cypress/test run with attachments -- should list attachments on attachments page (failed).png",
          attachmentName:
            "test run with attachments -- should list attachments on attachments page (failed).png",
        },
        {
          attachmentPath: "cypress/fixtures/cypress/attachments.spec.js.mp4",
          attachmentName: "attachments.spec.js.mp4",
        },
      ],
    );

    cy.getByTestId("test-case-failure-screenshot-1-1")
      .should("have.attr", "src")
      .and(
        "contain",
        "/attachments/test run with attachments -- should list attachments on attachments page (failed).png",
      );

    cy.getByTestId("test-case-video-link-1-1").click();

    cy.getByTestId("test-case-failure-video").should("exist");
  });
});
