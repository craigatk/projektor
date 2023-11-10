describe("repository coverage", () => {
  it("should display repository coverage graph and link to individual test reports", () => {
    const repoName = "cov-org/cov-repo";

    cy.intercept("GET", `repo/${repoName}/coverage/timeline`, {
      fixture: "repository/coverage_timeline.json",
    });
    cy.intercept("GET", `repo/${repoName}/timeline`, {
      fixture: "repository/timeline.json",
    });
    cy.intercept("GET", `repo/${repoName}/performance/timeline`, {
      fixture: "repository/performance_timeline.json",
    });

    const publicIds = [
      "2XMM8MYQTKM0",
      "3RZRSBCSALZ2",
      "WJIHLB2MTRAW",
      "BYYUCDMQ5WJ6",
      "KRXBI9GH213D",
      "XPF0IHDJBLOO",
    ];

    cy.visit(`http://localhost:1234/repository/${repoName}`);

    cy.findByTestId("repository-coverage-timeline-graph").should("exist");

    publicIds.forEach((publicId) => {
      cy.findByRole(`dot-lineValue-${publicId}`).should("exist");

      cy.findByRole(`dot-branchValue-${publicId}`).should("exist");
    });

    const publicId = publicIds[0];
    cy.findByRole(`dot-lineValue-${publicId}`).click();

    cy.url().should("contain", `/tests/${publicId}`);
  });

  // This test is flaky in CI, ignoring it for now until I can fix it
  it.skip("should support finding coverage from all branches", () => {
    const repoName = "cov-org/cov-repo-all";

    cy.intercept("GET", `repo/${repoName}/badge/coverage`, {
      statusCode: 404,
    });

    cy.intercept("GET", `repo/${repoName}/coverage/timeline?branch=ALL`, {
      fixture: "repository/coverage_timeline.json",
    });
    cy.intercept("GET", `repo/${repoName}/coverage/timeline`, {
      fixture: "repository/coverage_timeline_empty.json",
    });

    const publicIds = [
      "2XMM8MYQTKM0",
      "3RZRSBCSALZ2",
      "WJIHLB2MTRAW",
      "BYYUCDMQ5WJ6",
      "KRXBI9GH213D",
      "XPF0IHDJBLOO",
    ];

    cy.visit(`http://localhost:1234/repository/${repoName}/coverage`);

    cy.findByTestId("repository-coverage-branch-type").click();
    cy.findByTestId("repository-coverage-branch-type-all").click();
    cy.findByTestId("repository-coverage-search-button").click();

    cy.findByTestId("repository-coverage-timeline-graph").should("exist");

    publicIds.forEach((publicId) => {
      cy.findByRole(`dot-lineValue-${publicId}`).should("exist");

      cy.findByRole(`dot-branchValue-${publicId}`).should("exist");
    });
  });

  it("should display tooltip with coverage data on graph point hover", () => {
    const repoName = "cov-org/cov-repo";

    cy.intercept("GET", `repo/${repoName}/coverage/timeline`, {
      fixture: "repository/coverage_timeline.json",
    });
    cy.intercept("GET", `repo/${repoName}/timeline`, {
      fixture: "repository/timeline.json",
    });
    cy.intercept("GET", `repo/${repoName}/performance/timeline`, {
      fixture: "repository/performance_timeline.json",
    });

    const publicId = "WJIHLB2MTRAW";

    cy.visit(`http://localhost:1234/repository/${repoName}`);

    cy.findByTestId("repository-coverage-timeline-graph").should("exist");

    cy.findByRole(`dot-lineValue-${publicId}`).trigger("mouseover");

    cy.findByTestId("coverage-timeline-graph-tooltip").should("exist");

    cy.findByTestId("tooltip-line-coverage-percentage").should(
      "contain",
      "95.4%"
    );
    cy.findByTestId("tooltip-branch-coverage-percentage").should(
      "contain",
      "68.95%"
    );
    cy.findByTestId("tooltip-run-date").should("contain", "Sep 13th 2020");
  });

  it("when no project name should link from repository on side nav to show repository coverage data", () => {
    const repoName = "cov-org/cov-repo";
    const testId = "WJIHLB2MTRAW";

    cy.intercept("GET", `run/${testId}/summary`, {
      fixture: "test_run_summary.json",
    });
    cy.intercept("GET", `run/${testId}/cases/failed`, {
      fixture: "failed_test_cases.json",
    });
    cy.intercept("GET", `run/${testId}/metadata/git`, {
      repo_name: repoName,
      org_name: "cov-org",
      branch_name: "main",
      project_name: null,
      is_main_branch: true,
    });
    cy.intercept("GET", `run/${testId}/coverage`, {
      fixture: "coverage/coverage-three-groups.json",
    });
    cy.intercept("GET", `run/${testId}/messages`, { messages: [] });

    cy.intercept("GET", `repo/${repoName}/coverage/timeline`, {
      fixture: "repository/coverage_timeline.json",
    });
    cy.intercept("GET", `repo/${repoName}/timeline`, {
      fixture: "repository/timeline.json",
    });
    cy.intercept("GET", `repo/${repoName}/performance/timeline`, {
      fixture: "repository/performance_timeline.json",
    });

    cy.visit(`http://localhost:1234/tests/${testId}`);

    cy.findByTestId("nav-link-repository").click();

    cy.findByTestId("repository-coverage-timeline-graph").should("exist");
    cy.findByRole(`dot-lineValue-${testId}`).should("exist");
  });

  it("when project name should link from repository on side nav of test run page to show repository coverage data", () => {
    const repoName = "cov-org/cov-repo";
    const publicId = "WJIHLB2MTRAW";
    const projectName = "cov-project";

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });
    cy.intercept("GET", `run/${publicId}/cases/failed`, {
      fixture: "failed_test_cases.json",
    });
    cy.intercept("GET", `run/${publicId}/metadata/git`, {
      repo_name: repoName,
      org_name: "cov-org",
      branch_name: "main",
      project_name: projectName,
      is_main_branch: true,
    });
    cy.intercept("GET", `run/${publicId}/coverage`, {
      fixture: "coverage/coverage-three-groups.json",
    });
    cy.intercept("GET", `run/${publicId}/messages`, { messages: [] });

    cy.intercept(
      "GET",
      `repo/${repoName}/project/${projectName}/coverage/timeline`,
      { fixture: "repository/coverage_timeline.json" }
    );
    cy.intercept("GET", `repo/${repoName}/project/${projectName}/timeline`, {
      fixture: "repository/timeline.json",
    });
    cy.intercept(
      "GET",
      `repo/${repoName}/project/${projectName}/performance/timeline`,
      {
        fixture: "repository/performance_timeline.json",
      }
    );

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.findByTestId("nav-link-repository").click();

    cy.findByTestId("repository-coverage-timeline-graph").should("exist");
    cy.findByRole(`dot-lineValue-${publicId}`).should("exist");
  });

  it("when project name should link from repository on side nav of repository page", () => {
    const repoName = "cov-org/cov-repo";
    const projectName = "cov-project";

    cy.intercept(
      "GET",
      `repo/${repoName}/project/${projectName}/coverage/timeline`,
      { fixture: "repository/coverage_timeline.json" }
    );

    cy.visit(
      `http://localhost:1234/repository/${repoName}/project/${projectName}`
    );

    cy.findByTestId("repository-coverage-timeline-graph").should("exist");

    cy.findByTestId("nav-link-repo-coverage").click();

    cy.findByTestId("repository-coverage-timeline-graph").should("exist");
    cy.url().should(
      "contain",
      `/repository/${repoName}/project/${projectName}`
    );
  });
});
