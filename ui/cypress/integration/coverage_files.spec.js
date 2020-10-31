/// <reference types="Cypress" />

describe("file-level coverage", () => {
  it("should show file-level coverage on page", () => {
    const publicId = "12345";

    cy.server();

    cy.route("GET", `run/${publicId}/summary`, "fixture:test_run_summary.json");

    cy.route(
      "GET",
      `run/${publicId}/cases/failed`,
      "fixture:failed_test_cases.json"
    );

    cy.route(
      "GET",
      `run/${publicId}/coverage`,
      "fixture:coverage/coverage-three-groups.json"
    );

    cy.route(
      "GET",
      `run/${publicId}/coverage/exists`,
      "fixture:coverage/coverage-exists.json"
    );

    cy.route(
      "GET",
      `run/${publicId}/coverage/server-app/files`,
      "fixture:coverage/coverage-files-server-app.json"
    );

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId("nav-link-coverage").click();

    cy.getByTestId("coverage-name-3").should("contain", "server-app");
    cy.getByTestId("coverage-name-3").click();

    cy.getByTestId("coverage-file-name-1").should(
      "contain",
      "projektor/cleanup/CleanupScheduledJob.kt"
    );
    cy.getByTestId(
      "coverage-file-line-coverage-row-1-covered-percentage"
    ).should("contain", "71.43%");
    cy.getByTestId("branch-coverage-row-1-covered-percentage").should(
      "contain",
      "100%"
    );
    cy.getByTestId("coverage-file-missed-lines-1").should(
      "contain",
      "16, 18, 20, 21"
    );
    cy.getByTestId("coverage-file-partial-lines-1").should("be.empty");

    // Should filter out files with no lines of code (in this case an interface)
    cy.findAllByText("projektor/attachment/AttachmentRepository.kt").should(
      "not.exist"
    );
  });
});
