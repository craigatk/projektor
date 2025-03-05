context("test run URL support", () => {
  it("should view test run summary page when URL has a trailing slash", () => {
    cy.loadFixture(
      "cypress/fixtures/TEST-projektor.example.spock.FailingSpec.xml",
      "/",
    );

    cy.getByTestId("test-count-list-total").should("contain", "2");
  });
});
