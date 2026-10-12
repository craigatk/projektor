context("Slack channel for repository", () => {
  it("should save, show, and remove Slack channel from test run dashboard", () => {
    const repoName = `slack-org/repo-${Date.now()}`;
    const channelName = "build-alerts";

    cy.task("resetSlackMessages");

    cy.readFile("cypress/fixtures/grouped-passing-tests-with-git.json").then(
      (groupedResults) => {
        groupedResults.metadata.git.repoName = repoName;

        cy.loadGroupedFixtureDataAndVisitTestRun(groupedResults);
      },
    );

    cy.findByTestId("slack-channel-name").should("not.exist");
    cy.findByTestId("slack-channel-edit").should("contain", "Set channel");
    cy.findByTestId("slack-channel-edit").click();

    cy.findByTestId("slack-channel-dialog-description").should(
      "contain",
      repoName,
    );
    cy.findByTestId("slack-channel-input").type(`#${channelName}`);
    cy.findByTestId("slack-channel-save").click();

    cy.findByTestId("slack-channel-name").should("contain", `#${channelName}`);

    // Saving the channel posts a confirmation message to it
    cy.task("slackMessages").then((slackMessages) => {
      expect(slackMessages).to.have.length(1);
      expect(slackMessages[0].channel).to.equal(channelName);
      expect(slackMessages[0].text).to.contain(repoName);
    });

    // Channel should still be set after loading the page again
    cy.reload();

    cy.findByTestId("slack-channel-name").should("contain", `#${channelName}`);
    cy.findByTestId("slack-channel-edit").should("contain", "Edit");
    cy.findByTestId("slack-channel-edit").click();

    cy.findByTestId("slack-channel-input").should("have.value", channelName);
    cy.findByTestId("slack-channel-remove").click();

    cy.findByTestId("slack-channel-name").should("not.exist");
    cy.findByTestId("slack-channel-edit").should("contain", "Set channel");

    cy.reload();

    cy.findByTestId("dashboard-summary-slack-channel").should("exist");
    cy.findByTestId("slack-channel-name").should("not.exist");
  });
});
