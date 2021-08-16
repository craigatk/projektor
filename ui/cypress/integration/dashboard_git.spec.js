context("dashboard git metadata", () => {
  it("should show Git pull request number and commit SHA on dashboard", () => {
    const publicId = "12345";

    cy.intercept("GET", `run/${publicId}/metadata/git`, {
      fixture: "metadata/git-metadata-with-pr-and-commit.json",
    });

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });
    cy.intercept("GET", "config", {});
    cy.intercept("GET", `run/${publicId}/coverage/exists`, {
      fixture: "coverage/coverage-does-not-exist.json",
    });
    cy.intercept("GET", `run/${publicId}/coverage`, {
      fixture: "coverage/coverage-three-groups.json",
    });
    cy.intercept("GET", `run/${publicId}/messages`, {
      fixture: "messages/one_message.json",
    });
    cy.intercept("GET", `run/${publicId}/badge/coverage`, "");
    cy.intercept("GET", `run/${publicId}/performance`, {});

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.findByTestId("dashboard-summary-pull-request-number")
      .should("exist")
      .should("contain", "42");
    cy.findByTestId("dashboard-summary-pull-request-link")
      .should("have.attr", "href")
      .and("equal", "http://localhost:1234/craigatk/projektor/pull/42");

    cy.findByTestId("dashboard-summary-commit-sha")
      .should("exist")
      .should("contain", "2a6638c");
    cy.findByTestId("dashboard-summary-commit-sha-link")
      .should("have.attr", "href")
      .and(
        "equal",
        "http://localhost:1234/craigatk/projektor/commit/d7efb34a60091f0d9516660a371c290092a6638c"
      );
  });
});
