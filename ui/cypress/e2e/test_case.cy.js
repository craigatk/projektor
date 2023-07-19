/// <reference types="Cypress" />

context("test case", () => {
  it("should show test suite system out and err", () => {
    const publicId = "12345";
    const testSuiteIdx = 1;
    const testCaseIdx = 2;

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });

    cy.intercept("GET", `run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`, {
      fixture: "failed_test_case_2.json",
    });

    cy.intercept("GET", `run/${publicId}/suite/${testSuiteIdx}/systemOut`, {
      fixture: "test_output_system_out.json",
    });

    cy.intercept("GET", `run/${publicId}/suite/${testSuiteIdx}/systemErr`, {
      fixture: "test_output_system_err.json",
    });

    cy.visit(
      `http://localhost:1234/tests/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}/`
    );

    cy.getByTestId("test-case-failure-text").should(
      "contain",
      "Condition not satisfied"
    );
    cy.url().should("contain", "/failure");

    cy.getByTestId("test-case-tab-system-out").click();
    cy.getCodeText().should("contain", "System out line 1");
    cy.url().should("contain", "/systemOut");

    cy.getCodeTextLineNumber(2).click();
    cy.url().should("contain", "l=2");

    cy.getByTestId("test-case-tab-system-err").click();
    cy.getCodeText().should("contain", "System err line 1");
    cy.url().should("contain", "/systemErr");
    cy.url().should("not.contain", "l=2");

    cy.getCodeTextLineNumber(3).click();
    cy.url().should("contain", "l=3");

    cy.getByTestId("test-case-tab-failure").click();
    cy.url().should("not.contain", "l=");

    cy.getByTestId("test-case-failure-text").should(
      "contain",
      "Condition not satisfied"
    );
  });

  it("when test case passed should redirect to summary tab", () => {
    const publicId = "12345";
    const testSuiteIdx = 1;
    const testCaseIdx = 1;

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "one_passing/test_run_summary.json",
    });

    cy.intercept("GET", `run/${publicId}`, {
      fixture: "one_passing/test_run.json",
    });

    cy.intercept("GET", `run/${publicId}/suite/${testSuiteIdx}`, {
      fixture: "one_passing/test_suite.json",
    });

    cy.intercept("GET", `run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`, {
      fixture: "one_passing/test_case.json",
    });

    cy.visit(`http://localhost:1234/tests/${publicId}/suite/${testSuiteIdx}`);

    cy.getTestCaseLinkInList(testSuiteIdx, testCaseIdx).click();

    cy.getByTestId("test-case-summary-name").should("contain", "should pass");
    cy.getByTestId("test-case-summary-class-name").should(
      "contain",
      "PassingSpec"
    );
    cy.getByTestId("test-case-summary-package-name").should(
      "contain",
      "projektor.example.spock"
    );
  });

  it("when no package should not link back to package and not include it in summary", () => {
    const publicId = "12345";
    const testSuiteIdx = 5;
    const testCaseIdx = 1;

    cy.server();

    cy.route(
      "GET",
      `run/${publicId}/summary`,
      "fixture:cypress/test_run_summary.json"
    );
    cy.route("GET", `run/${publicId}`, "fixture:cypress/test_run.json");

    cy.route(
      "GET",
      `run/${publicId}/suite/${testSuiteIdx}`,
      "fixture:cypress/test_suite_side_nav.json"
    );

    cy.route(
      "GET",
      `run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`,
      "fixture:cypress/test_case_side_nav.json"
    );

    cy.visit(
      `http://localhost:1234/tests/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`
    );

    const testCaseName = "side nav should link to failed test cases";
    const testCaseClassName = "should link to failed test cases";

    cy.getByTestId("test-case-summary-name").should("contain", testCaseName);
    cy.getByTestId("test-case-summary-class-name").should(
      "contain",
      testCaseClassName
    );
    cy.getByTestId("test-case-summary-duration").should("contain", "2.63s");

    cy.getByTestId("test-case-summary-package-name").should("not.exist");

    cy.getByTestId("breadcrumb-link-package-name").should("not.exist");

    cy.getByTestId("breadcrumb-link-class-name").click();

    cy.getBreadcrumbEndingText().should("contain", "side nav");
    cy.getByTestId("breadcrumb-link-package-name").should("not.exist");
  });

  it("when test case failed should show failure details", () => {
    const publicId = "12345";

    const testSuiteIdx = 1;
    const testCaseIdx = 2;

    cy.server();

    cy.route("GET", `run/${publicId}/summary`, "fixture:test_run_summary.json");
    cy.route("GET", `run/${publicId}`, "fixture:test_run.json");
    cy.route(
      "GET",
      `run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`,
      "fixture:failed_test_case_2.json"
    );

    cy.visit(
      `http://localhost:1234/tests/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`
    );

    cy.getByTestId("test-case-failure-text").should(
      "contain",
      "Condition not satisfied"
    );
  });
});
