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
      "projektor/Application.kt"
    );
    cy.getByTestId(
      "coverage-file-line-coverage-row-1-covered-percentage"
    ).should("contain", "95.4%");
    cy.getByTestId("branch-coverage-row-1-covered-percentage").should(
      "contain",
      "85.71%"
    );
    cy.getByTestId("coverage-file-missed-lines-1").should(
      "contain",
      "102, 103, 104, 105"
    );
    cy.getByTestId("coverage-file-partial-lines-1").should(
      "contain",
      "92, 101"
    );

    // Should filter out files with no lines of code (in this case an interface)
    cy.findAllByText("projektor/attachment/AttachmentRepository.kt").should(
      "not.exist"
    );
  });
});
