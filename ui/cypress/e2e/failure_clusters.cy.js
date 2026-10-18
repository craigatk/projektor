/// <reference types="Cypress" />

context("failure clusters", () => {
  const publicId = "12345";
  const clusterKey = "spock-comparison-failure";

  beforeEach(() => {
    cy.interceptTestRunBasicRequests(publicId);

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });
    cy.intercept("GET", new RegExp(`run/${publicId}$`), {
      fixture: "test_run.json",
    });
    cy.intercept("GET", new RegExp(`run/${publicId}/cases/failed$`), {
      fixture: "failed_test_cases.json",
    });
  });

  it("should group failed tests that share a root cause", () => {
    cy.intercept("GET", `run/${publicId}/cases/failed/clusters`, {
      fixture: "failure_clusters/failure_clusters.json",
    });
    cy.intercept("GET", new RegExp(`run/${publicId}/suite/1/case/2$`), {
      fixture: "failed_test_case_2.json",
    });

    cy.visit(`http://localhost:1234/tests/${publicId}/failed`);

    cy.getByTestId("failure-cluster-headline").should(
      "contain",
      "2 tests failed from 1 root cause: SpockComparisonFailure",
    );
    cy.getByTestId(`failure-cluster-count-${clusterKey}`).should(
      "contain",
      "2 tests",
    );
    cy.getByTestId(`failure-cluster-message-${clusterKey}`).should(
      "contain",
      "Condition not satisfied",
    );

    // Without AI analysis enabled there's no option to analyze the root cause
    cy.getByTestId(`failure-cluster-analyze-${clusterKey}`).should("not.exist");

    cy.getByTestId(`failure-cluster-toggle-tests-${clusterKey}`).click();
    cy.getByTestId("failure-cluster-test-link-1-1").should(
      "contain",
      "projektor.example.spock.FailingSpec.should fail",
    );
    cy.getByTestId("failure-cluster-test-link-1-2").click();

    cy.url().should("contain", `/tests/${publicId}/suite/1/case/2/failure`);
    cy.getByTestId("test-case-failure-text").should(
      "contain",
      "Condition not satisfied",
    );
  });

  it("when AI analysis enabled should analyze a cluster's root cause", () => {
    cy.intercept("GET", "config", {
      fixture: "config/ai_config_enabled.json",
    });
    cy.intercept("GET", `run/${publicId}/cases/failed/clusters`, {
      fixture: "failure_clusters/failure_clusters.json",
    });
    cy.intercept(
      "GET",
      `run/${publicId}/cases/failed/clusters/${clusterKey}/analysis`,
      { fixture: "ai/failure_cluster_analysis.json" },
    );

    cy.visit(`http://localhost:1234/tests/${publicId}/failed`);

    cy.getByTestId(`failure-cluster-analyze-${clusterKey}`).click();

    cy.getByTestId(`failure-cluster-analysis-${clusterKey}`)
      .should("contain", "the shared setup returns stale data")
      .and("contain", "projektor.example.spock.FailingSpec.should fail");
  });

  it("when every failure has a different root cause should not show clusters", () => {
    cy.intercept("GET", `run/${publicId}/cases/failed/clusters`, {
      fixture: "failure_clusters/failure_clusters_unrelated.json",
    });

    cy.visit(`http://localhost:1234/tests/${publicId}/failed`);

    cy.getByTestId("test-case-title").should("have.length", 2);
    cy.getByTestId("failure-cluster-summary").should("not.exist");
  });
});
