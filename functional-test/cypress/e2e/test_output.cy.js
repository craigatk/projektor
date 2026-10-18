/// <reference types="Cypress" />

context("test output E2E", () => {
  it("should show test suite system out and system err", () => {
    cy.loadGroupedFixture("cypress/fixtures/grouped-output.json", "/suite/1");

    cy.getBreadcrumbEndingText().should("contain", "OutputSpec");

    cy.getByTestId("test-suite-tab-system-out").click();
    cy.getCodeText().should("contain", "System out line 1");

    cy.getByTestId("test-suite-tab-system-err").click();
    cy.getCodeText().should("contain", "System err line 1");
  });

  it("should show test case system out and system err when the output is at the test case level", () => {
    cy.loadGroupedFixture(
      "cypress/fixtures/grouped-output.json",
      "/suite/2/case/1",
    );

    cy.getBreadcrumbEndingText().should(
      "contain",
      "should save compressed grouped test results",
    );

    cy.getByTestId("test-case-tab-system-out").click();
    cy.getCodeText().should("contain", "LoggerContext");

    cy.getByTestId("test-case-tab-system-err").click();
    cy.getCodeText().should("contain", "System error");
  });
});
