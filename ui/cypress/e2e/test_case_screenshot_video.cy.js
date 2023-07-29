describe("test case with Cypress attachments", () => {
  it("should display link to test case screenshot and video on test failure page", () => {
    const publicId = "12345";

    const testCaseIdx = 2;
    const testSuiteIdx = 1;

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });

    cy.intercept("GET", `run/${publicId}/attachments`, {
      fixture: "attachments/cypress-screenshot-video-attachments.json",
    });

    cy.intercept("GET", `run/${publicId}/cases/failed`, {
      fixture: "cypress/failed_test_cases_with_screenshot_and_video.json",
    });

    cy.intercept(
      "GET",
      `run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`,
      { fixture: "cypress/failed_test_case_with_screenshot_and_video.json" },
    );

    cy.intercept(
      "GET",
      `run/${publicId}/attachments/test%20run%20with%20attachments%20--%20should%20list%20attachments%20on%20attachments%20page%20(failed).png`,
      {
        fixture:
          "attachments/test run with attachments -- should list attachments on attachments page (failed).png",
      },
    );
    cy.intercept("GET", `run/${publicId}/attachments/attachments.spec.js.mp4`, {
      fixture: "attachments/attachments.spec.js.mp4",
    });

    cy.interceptTestRunBasicRequests(publicId);

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId(
      `test-case-failure-screenshot-${testSuiteIdx}-${testCaseIdx}`,
    )
      .should("have.attr", "src")
      .and(
        "contain",
        "/attachments/test run with attachments -- should list attachments on attachments page (failed).png",
      );

    cy.getByTestId(
      `test-case-screenshot-link-${testSuiteIdx}-${testCaseIdx}`,
    ).click();

    cy.getByTestId(
      `test-case-failure-screenshot-${testSuiteIdx}-${testCaseIdx}`,
    ).should("exist");
  });

  it("should display link to test case video on test failure page", () => {
    const publicId = "12345";

    const testCaseIdx = 2;
    const testSuiteIdx = 1;

    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });

    cy.intercept("GET", `run/${publicId}/attachments`, {
      fixture: "attachments/cypress-screenshot-video-attachments.json",
    });

    cy.intercept("GET", `run/${publicId}/cases/failed`, {
      fixture: "cypress/failed_test_cases_with_screenshot_and_video.json",
    });

    cy.intercept(
      "GET",
      `run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`,
      { fixture: "cypress/failed_test_case_with_screenshot_and_video.json" },
    );

    cy.intercept(
      "GET",
      `run/${publicId}/attachments/test%20run%20with%20attachments%20--%20should%20list%20attachments%20on%20attachments%20page%20(failed).png`,
      {
        fixture:
          "attachments/test run with attachments -- should list attachments on attachments page (failed).png",
      },
    );
    cy.intercept("GET", `run/${publicId}/attachments/attachments.spec.js.mp4`, {
      fixture: "attachments/attachments.spec.js.mp4",
    });

    cy.interceptTestRunBasicRequests(publicId);

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.getByTestId(
      `test-case-video-link-${testSuiteIdx}-${testCaseIdx}`,
    ).click();

    cy.getByTestId("test-case-failure-video").should("exist");
  });
});
