/// <reference types="Cypress" />

context("repository coverage", () => {
  it("should display repository coverage graph and link to individual test reports", () => {
    const repoName = "cov-org/cov-repo";

    cy.server();

    cy.route(
      "GET",
      `repo/${repoName}/coverage/timeline`,
      "fixture:repository/coverage_timeline.json"
    );

    const publicIds = [
      "2XMM8MYQTKM0",
      "3RZRSBCSALZ2",
      "WJIHLB2MTRAW",
      "BYYUCDMQ5WJ6",
      "KRXBI9GH213D",
      "XPF0IHDJBLOO",
    ];

    cy.visit(`http://localhost:1234/repository/${repoName}`);

    cy.testIdShouldExist("repository-coverage-timeline-graph");

    publicIds.forEach((publicId) =>
      cy.roleShouldExist(`dot-lineValue-${publicId}`)
    );

    const publicIdToClick = publicIds[0];
    cy.getByRole(`dot-lineValue-${publicIdToClick}`).click();
    cy.url().should("contain", `/tests/${publicIdToClick}`);
  });

  it("should display tooltip with coverage data on graph point hover", () => {
    const repoName = "cov-org/cov-repo";

    cy.server();

    cy.route(
      "GET",
      `repo/${repoName}/coverage/timeline`,
      "fixture:repository/coverage_timeline.json"
    );

    const publicId = "WJIHLB2MTRAW";

    cy.visit(`http://localhost:1234/repository/${repoName}`);

    cy.testIdShouldExist("repository-coverage-timeline-graph");

    cy.getByRole(`dot-lineValue-${publicId}`).trigger("mouseover");

    cy.testIdShouldExist("coverage-timeline-graph-tooltip");

    cy.getByTestId("tooltip-line-coverage-percentage").should(
      "contain",
      "95.4%"
    );
    cy.getByTestId("tooltip-branch-coverage-percentage").should(
      "contain",
      "68.95%"
    );
    cy.getByTestId("tooltip-run-date").should("contain", "Sep 13th 2020");
  });

  it("when no project name should link from repository on side nav to show repository coverage data", () => {
    const repoName = "cov-org/cov-repo";
    const publicId = "WJIHLB2MTRAW";

    cy.server();

    cy.route("GET", `run/${publicId}/summary`, "fixture:test_run_summary.json");

    cy.route(
      "GET",
      `run/${publicId}/cases/failed`,
      "fixture:failed_test_cases.json"
    );

    cy.route("GET", `run/${publicId}/metadata/git`, {
      repo_name: repoName,
      org_name: "cov-org",
      branch_name: "main",
      project_name: null,
      is_main_branch: true,
    });

    cy.route(
      "GET",
      `repo/${repoName}/coverage/timeline`,
      "fixture:repository/coverage_timeline.json"
    );

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId("nav-link-repository").click();

    cy.testIdShouldExist("repository-coverage-timeline-graph");
    cy.roleShouldExist(`dot-lineValue-${publicId}`);
  });

  it("when project name should link from repository on side nav of test run page to show repository coverage data", () => {
    const repoName = "cov-org/cov-repo";
    const publicId = "WJIHLB2MTRAW";
    const projectName = "cov-project";

    cy.server();

    cy.route("GET", `run/${publicId}/summary`, "fixture:test_run_summary.json");

    cy.route(
      "GET",
      `run/${publicId}/cases/failed`,
      "fixture:failed_test_cases.json"
    );

    cy.route("GET", `run/${publicId}/metadata/git`, {
      repo_name: repoName,
      org_name: "cov-org",
      branch_name: "main",
      project_name: projectName,
      is_main_branch: true,
    });

    cy.route(
      "GET",
      `repo/${repoName}/project/${projectName}/coverage/timeline`,
      "fixture:repository/coverage_timeline.json"
    );

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId("nav-link-repository").click();

    cy.testIdShouldExist("repository-coverage-timeline-graph");
    cy.roleShouldExist(`dot-lineValue-${publicId}`);
  });

  it("when project name should link from repository on side nav of repository page", () => {
    const repoName = "cov-org/cov-repo";
    const projectName = "cov-project";

    cy.server();

    cy.route(
      "GET",
      `repo/${repoName}/project/${projectName}/coverage/timeline`,
      "fixture:repository/coverage_timeline.json"
    );

    cy.visit(
      `http://localhost:1234/repository/${repoName}/project/${projectName}`
    );

    cy.testIdShouldExist("repository-coverage-timeline-graph");

    cy.getByTestId("nav-link-repo-coverage").click();

    cy.testIdShouldExist("repository-coverage-timeline-graph");
    cy.url().should(
      "contain",
      `/repository/${repoName}/project/${projectName}`
    );
  });
});
