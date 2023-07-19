context("Pinning and unpinning test run", () => {
  it("should pin and unpin test run", () => {
    cy.loadGroupedFixture("cypress/fixtures/grouped-passing-tests.json");

    cy.getByTestId("test-run-header-pin-link").should("contain", "pin");
    cy.getByTestId("nav-link-pin").should("contain", "Pin");

    cy.getByTestId("test-run-header-pin-link").click();

    cy.getByTestId("test-run-header-unpin-link").should("contain", "unpin");
    cy.getByTestId("nav-link-unpin").should("contain", "Unpin");

    cy.getByTestId("nav-link-unpin").click();

    cy.getByTestId("test-run-header-pin-link").should("contain", "pin");
    cy.getByTestId("nav-link-pin").should("contain", "Pin");
  });
});
