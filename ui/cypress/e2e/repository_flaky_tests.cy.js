/// <reference types="Cypress" />

context("repository flaky tests", () => {
  it("should display flaky tests on repository flaky test page", () => {
    const repoName = "flaky-org/flaky-repo";

    cy.intercept(
      "GET",
      `repo/${repoName}/tests/flaky?threshold=5&max_runs=50&branch_type=MAINLINE`,
      { fixture: "repository/flaky_tests.json" },
    );

    cy.intercept("GET", "config", {
      fixture: "config/server_config_disabled.json",
    });

    cy.visit(`http://localhost:1234/repository/${repoName}`);

    cy.getByTestId("nav-link-repo-flaky-tests").click();

    cy.testIdShouldExist("repository-flaky-tests-table");

    cy.getByTestId("flaky-test-case-name-1").should(
      "contain",
      "projektor.testsuite.GetTestSuiteApplicationTest.should fetch grouped test suite from database",
    );
    cy.getByTestId("flaky-test-case-failure-percentage-1").should(
      "contain",
      "70.35%",
    );
    cy.getByTestId("flaky-test-case-failure-count-1").should("contain", "5");
    cy.getByTestId("flaky-test-case-first-failure-1").should(
      "contain",
      "Sep 22nd 2020",
    );
    cy.getByTestId(`flaky-test-case-first-failure-1`)
      .should("have.attr", "href")
      .and("equal", "/tests/32FBHG6FDL89/suite/1/case/2");
    cy.getByTestId("flaky-test-case-latest-failure-1").should(
      "contain",
      "Sep 30th 2020",
    );
    cy.getByTestId(`flaky-test-case-latest-failure-1`)
      .should("have.attr", "href")
      .and("equal", "/tests/32FBHG6FDL8S/suite/1/case/3");

    cy.getByTestId("flaky-test-case-name-2").should(
      "contain",
      "projektor.example.spock.FailingSpec.should fail with output",
    );
    cy.getByTestId("flaky-test-case-failure-percentage-2").should(
      "contain",
      "60.25%",
    );
    cy.getByTestId("flaky-test-case-failure-count-2").should("contain", "4");

    cy.getByTestId("flaky-test-case-name-3").should(
      "contain",
      "projektor.example.spock.FailingSpec.should fail",
    );
    cy.getByTestId("flaky-test-case-failure-percentage-3").should(
      "contain",
      "50.15%",
    );
    cy.getByTestId("flaky-test-case-failure-count-3").should("contain", "3");
  });

  it("should link from test case name to specific test run", () => {
    const repoName = "flaky-org/flaky-repo";

    cy.intercept(
      "GET",
      `repo/${repoName}/tests/flaky?threshold=5&max_runs=50&branch_type=MAINLINE`,
      { fixture: "repository/flaky_tests.json" },
    );

    cy.intercept("GET", "config", {
      fixture: "config/server_config_disabled.json",
    });

    const publicId = "32FBHG6FDL8S";
    const testSuiteIdx = 1;
    const testCaseIdx = 1;

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });
    cy.intercept("GET", `run/${publicId}`, { fixture: "test_run.json" });
    cy.intercept(
      "GET",
      `run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`,
      { fixture: "failed_test_case_2.json" },
    );

    cy.visit(`http://localhost:1234/repository/${repoName}`);

    cy.getByTestId("nav-link-repo-flaky-tests").click();

    cy.testIdShouldExist("repository-flaky-tests-table");

    cy.getByTestId("flaky-test-case-name-1").click();

    cy.getByTestId("test-case-failure-text").should(
      "contain",
      "Condition not satisfied",
    );
  });

  it("should support changing max runs, flaky threshold and branch type", () => {
    const repoName = "flaky-org/flaky-repo";

    cy.intercept(
      "GET",
      `repo/${repoName}/tests/flaky?threshold=5&max_runs=50&branch_type=MAINLINE`,
      { fixture: "repository/flaky_tests.json" },
    );

    cy.intercept("GET", "config", {
      fixture: "config/server_config_disabled.json",
    });

    cy.visit(`http://localhost:1234/repository/${repoName}`);

    cy.findByTestId("nav-link-repo-flaky-tests").click();

    cy.testIdShouldExist("repository-flaky-tests-table");

    cy.findByTestId("flaky-test-case-name-1").should(
      "contain",
      "projektor.testsuite.GetTestSuiteApplicationTest.should fetch grouped test suite from database",
    );

    cy.findByTestId("flaky-test-case-name-2").should(
      "contain",
      "projektor.example.spock.FailingSpec.should fail with output",
    );

    cy.intercept(
      "GET",
      `repo/${repoName}/tests/flaky?threshold=2&max_runs=20&branch_type=ALL`,
      { fixture: "repository/flaky_tests_two_tests.json" },
    );

    cy.findByTestId("flaky-tests-threshold").type("{selectall}{backspace}2", {
      delay: 50,
    });
    cy.findByTestId("flaky-tests-max-runs").type("{selectall}{backspace}20", {
      delay: 50,
    });
    cy.findByTestId("flaky-tests-branch-type").click();
    cy.findByTestId("flaky-tests-branch-type-all").click();
    cy.findByTestId("flaky-tests-search-button").click();

    cy.findByTestId("flaky-test-case-name-1").should(
      "contain",
      "projektor.flaky.FlakyTest1",
    );

    cy.findByTestId("flaky-test-case-name-2").should(
      "contain",
      "projektor.flaky.FlakyTest2",
    );

    cy.url().should("contain", "max=20");
    cy.url().should("contain", "threshold=2");
    cy.url().should("contain", "branch_type=all");
  });

  it("should support going directly to flaky test page with params set", () => {
    const repoName = "flaky-org/flaky-repo";

    cy.intercept(
      "GET",
      `repo/${repoName}/tests/flaky?threshold=4&max_runs=30&branch_type=ALL`,
      { fixture: "repository/flaky_tests_two_tests.json" },
    );

    cy.visit(
      `http://localhost:1234/repository/${repoName}/tests/flaky?max=30&threshold=4&branch_type=all`,
    );

    cy.intercept("GET", "config", {
      fixture: "config/server_config_disabled.json",
    });

    cy.testIdShouldExist("repository-flaky-tests-table");

    cy.getByTestId("flaky-test-case-name-1").should(
      "contain",
      "projektor.flaky.FlakyTest1",
    );

    cy.getByTestId("flaky-test-case-name-2").should(
      "contain",
      "projektor.flaky.FlakyTest2",
    );
  });
});
