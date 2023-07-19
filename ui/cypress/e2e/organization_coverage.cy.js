/// <reference types="Cypress" />

context("organization coverage", () => {
  it("should display coverage for each repo and link to repo page", () => {
    const orgName = "cov-org";
    const repoName = "cov-org/cov-repo";

    cy.intercept("GET", `org/${orgName}/coverage`, {
      fixture: "organization/organization_coverage.json",
    });

    cy.intercept("GET", `repo/${repoName}/coverage/timeline`, {
      fixture: "repository/coverage_timeline.json",
    });

    cy.visit(`http://localhost:1234/organization/${orgName}`);

    cy.testIdShouldExist("organization-coverage-details");

    cy.getByTestId("coverage-name-1").click();

    cy.testIdShouldExist("repository-coverage-timeline-graph");

    const publicId = "2XMM8MYQTKM0";

    cy.roleShouldExist(`dot-lineValue-${publicId}`);
  });

  it("should display coverage for each repo and link to latest test run page", () => {
    const orgName = "cov-org";

    cy.intercept("GET", `org/${orgName}/coverage`, {
      fixture: "organization/organization_coverage.json",
    });

    cy.visit(`http://localhost:1234/organization/${orgName}`);

    cy.testIdShouldExist("organization-coverage-details");

    const publicId = "AF5EZOPSKX2K";

    cy.intercept("GET", `run/${publicId}`, {
      fixture: "test_run.json",
    });

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });

    cy.intercept("GET", `run/${publicId}/cases/failed`, {
      fixture: "failed_test_cases_6.json",
    });

    cy.getByTestId("line-coverage-row-1-covered-percentage-link").click();

    cy.testIdShouldExist("dashboard-summary-title");

    cy.url().should("contain", `/tests/${publicId}`);
  });
});
