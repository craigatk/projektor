describe("test failure", () => {
  it("should display Cypress test suite name in test failure list", () => {
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

    cy.findByTestId("test-case-title-1-1").should(
      "contain",
      "Test Run with Attachments",
    );
    cy.findByTestId("test-case-title-1-1").should(
      "contain",
      "should list attachments on attachments page",
    );
  });
});
