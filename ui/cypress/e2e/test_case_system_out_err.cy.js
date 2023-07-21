context("test case system out and system err", () => {
  it("should show test case system out and system err when the output is at the test case level", () => {
    const publicId = "12345";
    const testSuiteIdx = 1;
    const testCaseIdx = 2;

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });

    cy.intercept(
      "GET",
      `run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`,
      {
        fixture: "failed_test_case_system_out_err_test_case_level.json",
      }
    );

    cy.intercept(
      "GET",
      `run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}/systemOut`,
      {
        fixture: "test_output_system_out.json",
      }
    );

    cy.intercept(
      "GET",
      `run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}/systemErr`,
      {
        fixture: "test_output_system_err.json",
      }
    );

    cy.interceptTestRunBasicRequests(publicId);

    cy.visit(
      `http://localhost:1234/tests/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`
    );

    cy.getByTestId("test-case-failure-text").should(
      "contain",
      "Condition not satisfied"
    );

    cy.getByTestId("test-case-tab-system-out").click();
    cy.getCodeText().should("contain", "System out line 1");

    cy.getByTestId("test-case-tab-system-err").click();
    cy.getCodeText().should("contain", "System err line 1");
  });
});
