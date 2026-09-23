/// <reference types="Cypress" />

context("test case history", () => {
  const publicId = "12345";
  const testSuiteIdx = 1;
  const testCaseIdx = 2;

  beforeEach(() => {
    cy.intercept("GET", "config", {
      fixture: "config/server_config_disabled.json",
    });

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });

    cy.intercept(
      "GET",
      new RegExp(`run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}$`),
      {
        fixture: "failed_test_case_2.json",
      },
    );
  });

  it("should show where the current failure streak started", () => {
    cy.intercept(
      "GET",
      `run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}/history`,
      {
        fixture: "test_case_history/failing_history.json",
      },
    );

    cy.visit(
      `http://localhost:1234/tests/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}/`,
    );

    cy.getByTestId("test-case-tab-history").click();
    cy.url().should("contain", "/history");

    cy.getByTestId("test-case-history-failure-streak")
      .should("contain", "Sep 5th 2026")
      .and("contain", "bbbbbbb")
      .and("contain", "Last passed")
      .and("contain", "aaaaaaa");

    cy.getByTestId("test-case-history-first-failure-link")
      .should("have.attr", "href")
      .and("equal", "/tests/FAIL1/suite/1/case/2");
    cy.getByTestId("test-case-history-last-passed-link")
      .should("have.attr", "href")
      .and("equal", "/tests/PASS2/suite/1/case/2");

    cy.getByTestId("test-case-history-strip")
      .children()
      .should("have.length", 5);
    cy.getByTestId("test-case-history-strip-PASS1")
      .should("have.attr", "href")
      .and("equal", "/tests/PASS1/suite/1/case/2");

    cy.testIdShouldExist("test-case-history-duration-chart");

    cy.getByTestId("test-case-history-row-1-result").should(
      "contain",
      "failed",
    );
    cy.getByTestId("test-case-history-row-2-result").should(
      "contain",
      "failed",
    );
    cy.getByTestId("test-case-history-row-3-result").should(
      "contain",
      "skipped",
    );
    cy.getByTestId("test-case-history-row-4-result").should(
      "contain",
      "passed",
    );
    cy.getByTestId("test-case-history-row-5-result").should(
      "contain",
      "passed",
    );
  });

  it("should show message when there is no history", () => {
    cy.intercept(
      "GET",
      `run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}/history`,
      {
        fixture: "test_case_history/empty_history.json",
      },
    );

    cy.visit(
      `http://localhost:1234/tests/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}/history`,
    );

    cy.getByTestId("test-case-history-empty").should(
      "contain",
      "No history available",
    );
  });
});
