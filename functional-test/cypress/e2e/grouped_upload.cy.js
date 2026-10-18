/// <reference types="Cypress" />

// Polls the API until the test run includes every upload, since later uploads are appended in the background
const waitForTotalTestCount = (publicId, expectedTotalTestCount, attempt = 0) =>
  cy
    .request({
      url: `http://localhost:8080/run/${publicId}/summary`,
      failOnStatusCode: false,
    })
    .then((response) => {
      if (
        response.status !== 200 ||
        response.body.total_test_count !== expectedTotalTestCount
      ) {
        if (attempt >= 60) {
          throw new Error(
            `Test run ${publicId} didn't reach ${expectedTotalTestCount} tests, last response ${JSON.stringify(response.body)}`,
          );
        }
        cy.wait(500);
        waitForTotalTestCount(publicId, expectedTotalTestCount, attempt + 1);
      }
    });

context("grouped upload E2E", () => {
  it("should combine results uploaded with the same group into one test run", () => {
    // A repo and group of their own, so a group claimed by an earlier run of this spec isn't reused
    const uniqueSuffix = Date.now();
    const repoName = `grouped-upload-org/repo-${uniqueSuffix}`;
    const group = `B${uniqueSuffix}`;

    const withGroup = (results) => {
      results.metadata.git.repoName = repoName;
      results.metadata.group = group;
      return results;
    };

    cy.readFile("cypress/fixtures/grouped-passing-tests-with-git.json").then(
      (passingResults) => {
        cy.readFile(
          "cypress/fixtures/grouped-failing-tests-with-git.json",
        ).then((failingResults) => {
          cy.request(
            "POST",
            "http://localhost:8080/groupedResults",
            withGroup(passingResults),
          ).then((firstResponse) => {
            cy.request(
              "POST",
              "http://localhost:8080/groupedResults",
              withGroup(failingResults),
            ).then((secondResponse) => {
              const publicId = firstResponse.body.id;
              expect(secondResponse.body.id).to.eq(publicId);

              // 3 passing tests from the first upload and 2 failing tests from the second
              waitForTotalTestCount(publicId, 5);

              cy.visit(`http://localhost:8080/tests/${publicId}`, {
                retryOnStatusCodeFailure: true,
              });

              cy.getByTestId("test-count-list-total").should("contain", "5");
              cy.getByTestId("test-count-list-passed").should("contain", "3");
              cy.getByTestId("test-count-list-failed").should("contain", "2");
            });
          });
        });
      },
    );
  });
});
