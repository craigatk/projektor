/// <reference types="Cypress" />

context("test case debug context", () => {
  const publicId = "12345";

  beforeEach(() => {
    cy.interceptTestRunBasicRequests(publicId);

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });
    cy.intercept("GET", new RegExp(`run/${publicId}$`), {
      fixture: "test_run.json",
    });
  });

  it("should show debug context for failed test case and copy it", () => {
    cy.intercept("GET", new RegExp(`run/${publicId}/suite/1/case/2$`), {
      fixture: "failed_test_case_2.json",
    });
    cy.intercept("GET", `run/${publicId}/suite/1/case/2/debug-context`, {
      fixture: "ai/test_case_debug_context.json",
    });

    cy.visit(`http://localhost:1234/tests/${publicId}/suite/1/case/2/failure`);

    cy.getByTestId("test-case-tab-debug-context").click();

    cy.url().should("contain", "/debugContext");
    cy.getByTestId("test-case-debug-context-text").should(
      "contain",
      "# Test Failure: should fail with output",
    );

    // The copy button copies with document.execCommand on a temporary selection, which doesn't reach the
    // clipboard in headless Electron, so record what's selected when it copies
    cy.window().then((win) => {
      cy.stub(win.document, "execCommand")
        .callsFake((command) => {
          win.copiedText = win.getSelection().toString();
          return command === "copy";
        })
        .as("execCommand");
    });

    cy.getByTestId("test-case-debug-context-copy-button").click();
    cy.getByTestId("test-case-debug-context-copied").should("be.visible");

    cy.get("@execCommand").should("have.been.calledWith", "copy");
    cy.fixture("ai/test_case_debug_context.json").then((debugContext) => {
      cy.window().its("copiedText").should("eq", debugContext.markdown);
    });
  });

  it("should not show debug context tab for passed test case", () => {
    cy.intercept("GET", new RegExp(`run/${publicId}/suite/1/case/1$`), {
      fixture: "one_passing/test_case.json",
    });

    cy.visit(`http://localhost:1234/tests/${publicId}/suite/1/case/1`);

    cy.url().should("contain", "/summary");
    cy.getByTestId("test-case-tab-debug-context").should("not.exist");
  });
});
