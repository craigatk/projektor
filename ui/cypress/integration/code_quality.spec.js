context("code quality", () => {
  it("should list code quality reports on code quality page", () => {
    const publicId = "12345";

    cy.intercept("GET", `run/${publicId}/quality`, {
      fixture: "quality/code_quality_reports.json",
    });

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });
    cy.interceptTestRunBasicRequests(publicId);

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.findByTestId("nav-link-code-quality").click();

    cy.findByTestId("code-quality-title").should("contain", "Code quality");
    cy.findByTestId("code-text-line-content-1").should(
      "contain",
      "github-line-1"
    );

    cy.findByText("server-app-ktlint.txt").click();
    cy.findByTestId("code-text-line-content-1").should(
      "contain",
      "server-app-line-1"
    );

    cy.findByText("notifications-github-ktlint.txt").click();
    cy.findByTestId("code-text-line-content-1").should(
      "contain",
      "github-line-1"
    );
  });

  it("should show code quality reports on dashboard", () => {
    const publicId = "12345";

    cy.intercept("GET", `run/${publicId}/quality`, {
      fixture: "quality/code_quality_reports.json",
    });

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });
    cy.interceptTestRunBasicRequests(publicId);

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.findByText("server-app-ktlint.txt").click();
    cy.findByTestId("code-text-line-content-1").should(
      "contain",
      "server-app-line-1"
    );
  });

  it("should go directly to specific code quality report", () => {
    const publicId = "12345";

    cy.intercept("GET", `run/${publicId}/quality`, {
      fixture: "quality/code_quality_reports.json",
    });

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });
    cy.interceptTestRunBasicRequests(publicId);

    cy.visit(`http://localhost:1234/tests/${publicId}/quality/report/2`);

    cy.findByTestId("code-text-line-content-1").should(
      "contain",
      "server-app-line-1"
    );
    cy.findByTestId("code-text-line-content-2").should(
      "contain",
      "server-app-line-2"
    );
  });
});
