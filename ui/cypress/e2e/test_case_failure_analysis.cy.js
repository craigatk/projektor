/// <reference types="Cypress" />

context("test case failure analysis", () => {
  const publicId = "12345";

  const interceptFailedTestCase = () => {
    cy.interceptTestRunBasicRequests(publicId);

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });
    cy.intercept("GET", new RegExp(`run/${publicId}$`), {
      fixture: "test_run.json",
    });
    cy.intercept("GET", new RegExp(`run/${publicId}/suite/1/case/2$`), {
      fixture: "failed_test_case_2.json",
    });
  };

  it("when AI analysis enabled should show failure analysis", () => {
    interceptFailedTestCase();

    // Registered after the basic requests so it replaces their config
    cy.intercept("GET", "config", {
      fixture: "config/ai_config_enabled.json",
    });
    cy.intercept("GET", `run/${publicId}/suite/1/case/2/analysis`, {
      fixture: "ai/test_case_failure_analysis.json",
    });

    cy.visit(`http://localhost:1234/tests/${publicId}/suite/1/case/2/failure`);

    cy.getByTestId("test-case-tab-failure-analysis").click();

    cy.url().should("contain", "/analysis");
    cy.getByTestId("test-case-failure-analysis-text").should(
      "contain",
      "the calculation is off by two",
    );
  });

  it("when AI analysis disabled should not show failure analysis tab", () => {
    interceptFailedTestCase();

    cy.visit(`http://localhost:1234/tests/${publicId}/suite/1/case/2/failure`);

    cy.getByTestId("test-case-failure-text").should(
      "contain",
      "Condition not satisfied",
    );
    cy.getByTestId("test-case-tab-failure-analysis").should("not.exist");
  });
});
