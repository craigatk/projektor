describe("Cypress test failure with attachment screenshot", () => {
    it("should display screenshot alongside failure", () => {
        cy.loadGroupedFixtureWithAttachment(
            "cypress/fixtures/cypress/cypress-grouped-failing-test-with-attachment.json",
            "cypress/fixtures/cypress/test run with attachments -- should list attachments on attachments page (failed).png",
            "test run with attachments -- should list attachments on attachments page (failed).png"
        );

        cy.getByTestId("nav-link-attachments").should("exist");

        cy.getByTestId("test-case-failure-screenshot-1-1")
            .should("have.attr", "src")
            .and(
                "contain",
                "/attachments/test run with attachments -- should list attachments on attachments page (failed).png"
            );
    })
})