context("test suite system out and system err", () => {
  it("should highlight multiple system out lines when shift-clicking on test case output", () => {
    const publicId = "12345";
    const testSuiteIdx = 1;
    const testCaseIdx = 2;

    cy.server();

    cy.route("GET", `run/${publicId}/summary`, "fixture:test_run_summary.json");

    cy.route(
      "GET",
      `run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`,
      "fixture:failed_test_case_2.json"
    );

    cy.route(
      "GET",
      `run/${publicId}/suite/${testSuiteIdx}/systemOut`,
      "fixture:test_suite_with_output_system_out.json"
    );

    cy.visit(
      `http://localhost:1234/tests/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`
    );

    cy.getByTestId("test-case-failure-text").should(
      "contain",
      "Condition not satisfied"
    );

    cy.getByTestId("test-case-tab-system-out").click();
    cy.getCodeText().should("contain", "System out line 1");

    cy.getCodeTextLine(2).click();
    cy.codeLineShouldBeHighlighted(2);
    cy.codeLineShouldNotBeHighlighted(1);
    cy.codeLineShouldNotBeHighlighted(3);

    cy.holdShift()
      .getCodeTextLine(5)
      .click()
      .releaseShift();
    cy.codeLineShouldBeHighlighted(2);
    cy.codeLineShouldBeHighlighted(3);
    cy.codeLineShouldBeHighlighted(4);
    cy.codeLineShouldBeHighlighted(5);
    cy.codeLineShouldNotBeHighlighted(1);
    cy.codeLineShouldNotBeHighlighted(6);

    cy.getCodeTextLine(9).click();
    cy.codeLineShouldBeHighlighted(9);
    cy.codeLineShouldNotBeHighlighted(2);
    cy.codeLineShouldNotBeHighlighted(3);
    cy.codeLineShouldNotBeHighlighted(4);
    cy.codeLineShouldNotBeHighlighted(5);

    cy.holdShift()
      .getCodeTextLine(7)
      .click()
      .releaseShift();
    cy.codeLineShouldBeHighlighted(7);
    cy.codeLineShouldBeHighlighted(8);
    cy.codeLineShouldBeHighlighted(9);
  });
});
