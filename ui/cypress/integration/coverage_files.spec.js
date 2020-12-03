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

  it("should show links to files in GitHub when GitHub base URL and coverage file paths set", () => {
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
      `run/${publicId}/metadata/git`,
      "fixture:metadata/git-metadata-with-github-base-url.json"
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
      "fixture:coverage/coverage-files-with-file-path.json"
    );

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId("nav-link-coverage").click();

    cy.getByTestId("coverage-name-3").should("contain", "server-app");
    cy.getByTestId("coverage-name-3").click();

    cy.getByTestId("coverage-file-name-1").should(
      "contain",
      "projektor/attachment/AttachmentDatabaseRepository.kt"
    );

    cy.getByTestId(`coverage-file-1-file-name-link`)
      .should("have.attr", "href")
      .and(
        "equal",
        "http://localhost:1234/craigatk/projektor/blob/main/src/main/kotlin/projektor/attachment/AttachmentDatabaseRepository.kt"
      );

    cy.getByTestId("coverage-file-missed-lines-1").should("contain", "39, 41");
    cy.getByTestId("coverage-file-partial-lines-1").should("contain", "34, 36");

    cy.getByTestId("coverage-file-1-missed-line-link-39")
      .should("have.attr", "href")
      .and(
        "equal",
        "http://localhost:1234/craigatk/projektor/blob/main/src/main/kotlin/projektor/attachment/AttachmentDatabaseRepository.kt#L39"
      );

    cy.getByTestId("coverage-file-1-missed-line-link-41")
      .should("have.attr", "href")
      .and(
        "equal",
        "http://localhost:1234/craigatk/projektor/blob/main/src/main/kotlin/projektor/attachment/AttachmentDatabaseRepository.kt#L41"
      );

    cy.getByTestId("coverage-file-1-partial-line-link-34")
      .should("have.attr", "href")
      .and(
        "equal",
        "http://localhost:1234/craigatk/projektor/blob/main/src/main/kotlin/projektor/attachment/AttachmentDatabaseRepository.kt#L34"
      );

    cy.getByTestId("coverage-file-1-partial-line-link-36")
      .should("have.attr", "href")
      .and(
        "equal",
        "http://localhost:1234/craigatk/projektor/blob/main/src/main/kotlin/projektor/attachment/AttachmentDatabaseRepository.kt#L36"
      );
  });
});
