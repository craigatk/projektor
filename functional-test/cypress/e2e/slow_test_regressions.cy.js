/// <reference types="Cypress" />

context("slow test regressions", () => {
  it("flags tests slower than their baseline on the slow tests page", () => {
    const orgPart = Math.random().toString(36).substr(2, 7);
    const repoName = `${orgPart}/slow-test-regressions-repo`;

    const testSuiteXml = (saveOrderTime, loadHistoryTime, calculateTaxTime) =>
      `<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="projektor.example.spock.DurationRegressionSpec" tests="3" skipped="0" failures="0" errors="0" timestamp="2026-10-01T13:15:45" hostname="ci-runner" time="10.0">
  <testcase name="should save order" classname="projektor.example.spock.DurationRegressionSpec" time="${saveOrderTime}"/>
  <testcase name="should load order history" classname="projektor.example.spock.DurationRegressionSpec" time="${loadHistoryTime}"/>
  <testcase name="should calculate tax" classname="projektor.example.spock.DurationRegressionSpec" time="${calculateTaxTime}"/>
</testsuite>`;

    const buildResults = (testSuitesBlob, daysAgo) => ({
      metadata: {
        ci: true,
        createdTimestamp: new Date(
          Date.now() - daysAgo * 24 * 60 * 60 * 1000,
        ).toISOString(),
        git: {
          repoName,
          branchName: "main",
          isMainBranch: true,
        },
      },
      groupedTestSuites: [
        {
          groupName: "Group1",
          groupLabel: "unitTest",
          directory: "/test/unit",
          testSuitesBlob,
        },
      ],
    });

    const baselineXml = testSuiteXml("1.200", "2.500", "0.400");

    cy.loadGroupedFixtureData(buildResults(baselineXml, 4));
    cy.loadGroupedFixtureData(buildResults(baselineXml, 3));
    cy.loadGroupedFixtureData(buildResults(baselineXml, 2));
    // "should calculate tax" is only 25% slower, under the default 50% threshold
    cy.loadGroupedFixtureDataAndVisitTestRun(
      buildResults(testSuiteXml("4.800", "4.000", "0.500"), 0),
      "/slow",
    );

    cy.getByTestId("slow-test-cases-title").should(
      "contain",
      "Slowest test cases",
    );

    cy.getByTestId("slow-test-regressions-title", { timeout: 15000 }).should(
      "contain",
      "Slower than baseline",
    );
    cy.getByTestId("slowest-test-cases-section-title").should(
      "contain",
      "Slowest in this run",
    );

    cy.getByTestId("slow-test-regressions-headline").should(
      "contain",
      "2 tests more than 50% slower than their baseline",
    );
    cy.getByTestId("slow-test-regressions").should(
      "contain",
      "previous 3 CI runs",
    );

    cy.getByTestId("slow-test-regression-change-1-1").should(
      "contain",
      "+3.600s (+300%)",
    );
    cy.getByTestId("slow-test-regression-change-1-2").should(
      "contain",
      "+1.500s (+60%)",
    );
    cy.testIdShouldNotExist("slow-test-regression-1-3");

    cy.getByTestId("slow-test-regression-link-1-1").click();

    cy.url().should("contain", "/suite/1/case/1");
  });
});
