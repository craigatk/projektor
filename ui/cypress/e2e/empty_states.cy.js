/// <reference types="Cypress" />

context("empty states", () => {
  const repoName = "empty-org/empty-repo";

  it("when repository has no test runs should say so on timeline page", () => {
    cy.intercept("GET", `repo/${repoName}/timeline`, { statusCode: 204 });

    cy.visit(`http://localhost:1234/repository/${repoName}/timeline`);

    cy.getByTestId("repo-no-timeline").should(
      "contain",
      `No test execution information available for repository ${repoName}`,
    );
  });

  it("when repository has no coverage should say so on coverage page", () => {
    cy.intercept("GET", `repo/${repoName}/coverage/exists`, {
      fixture: "coverage/coverage-does-not-exist.json",
    });
    cy.intercept("GET", `repo/${repoName}/coverage/timeline`, {
      statusCode: 204,
    });

    cy.visit(`http://localhost:1234/repository/${repoName}/coverage`);

    cy.getByTestId("repo-page-no-coverage").should("exist");
  });

  it("when repository has no performance results should say so on performance page", () => {
    cy.intercept("GET", `repo/${repoName}/performance/timeline`, {
      statusCode: 204,
    });

    cy.visit(`http://localhost:1234/repository/${repoName}/performance`);

    cy.getByTestId("repository-no-performance").should("exist");
  });

  it("when repository has no flaky tests should say so on flaky tests page", () => {
    // The server responds with no content when there aren't any flaky tests
    cy.intercept("GET", new RegExp(`repo/${repoName}/tests/flaky`), {
      statusCode: 204,
    });

    cy.visit(`http://localhost:1234/repository/${repoName}/tests/flaky`);

    cy.getByTestId("repository-no-flaky-tests").should(
      "contain",
      `No flaky tests found in repository ${repoName}`,
    );
  });

  it("when organization has no repositories with coverage should say so", () => {
    const orgName = "empty-org";

    // The server responds with no content when no repositories have coverage
    cy.intercept("GET", `org/${orgName}/coverage`, { statusCode: 204 });

    cy.visit(`http://localhost:1234/organization/${orgName}`);

    cy.getByTestId("organization-coverage-no-details").should(
      "contain",
      `No repositories found with coverage in organization ${orgName}`,
    );
  });

  it("when test run has no code quality reports should say so on code quality page", () => {
    const publicId = "12345";

    cy.interceptTestRunBasicRequests(publicId);
    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });
    cy.intercept("GET", `run/${publicId}/quality`, {
      fixture: "quality/empty_code_quality_reports.json",
    });

    cy.visit(`http://localhost:1234/tests/${publicId}/quality`);

    cy.getByTestId("code-quality-reports-not-found").should("exist");
  });
});
