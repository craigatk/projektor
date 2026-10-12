/// <reference types="Cypress" />

context("dashboard Slack channel", () => {
  const publicId = "1234567";
  const slackChannelUrl = "repo/craigatk/projektor/notification/slack";

  const interceptDashboard = (configFixture) => {
    cy.intercept("GET", `run/${publicId}/metadata/git`, {
      fixture: "metadata/git-metadata-with-pr-and-commit.json",
    });
    cy.intercept("GET", `run/${publicId}/summary`, {
      fixture: "test_run_summary.json",
    });
    cy.intercept("GET", "config", { fixture: configFixture });
    cy.intercept("GET", `run/${publicId}/attachments`, {
      fixture: "attachments/attachments_empty.json",
    });
    cy.intercept("GET", `run/${publicId}/coverage/exists`, {
      fixture: "coverage/coverage-does-not-exist.json",
    });
    cy.intercept("GET", `run/${publicId}/messages`, {
      fixture: "messages/one_message.json",
    });
    cy.intercept("GET", `run/${publicId}/badge/coverage`, "");
    cy.intercept("GET", `run/${publicId}/performance`, {});
    cy.intercept("GET", `run/${publicId}/quality`, {});
    cy.intercept("GET", `run/${publicId}/cases/failed`, {
      fixture: "failed_test_cases.json",
    });
  };

  it("should set Slack channel for repository from dashboard", () => {
    interceptDashboard("config/slack_config_enabled.json");

    cy.intercept("GET", slackChannelUrl, { statusCode: 204, body: "" });
    cy.intercept("PUT", slackChannelUrl, {
      statusCode: 200,
      body: { channel_name: "build-alerts" },
    }).as("saveChannel");

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.findByTestId("slack-channel-name").should("not.exist");
    cy.findByTestId("slack-channel-edit").should("contain", "Set channel");
    cy.findByTestId("slack-channel-edit").click();

    cy.findByTestId("slack-channel-input").type("#build-alerts");
    cy.findByTestId("slack-channel-save").click();

    cy.wait("@saveChannel")
      .its("request.body")
      .should("deep.equal", { channel_name: "#build-alerts" });

    cy.findByTestId("slack-channel-name").should("contain", "#build-alerts");
    cy.findByTestId("slack-channel-edit").should("contain", "Edit");
  });

  it("should not show Slack channel on dashboard when Slack is not enabled", () => {
    interceptDashboard("config/cleanup_config_disabled.json");

    cy.visit(`http://localhost:1234/tests/${publicId}`);

    cy.findByTestId("dashboard-summary-commit-sha").should("exist");
    cy.findByTestId("dashboard-summary-slack-channel").should("not.exist");
  });
});
