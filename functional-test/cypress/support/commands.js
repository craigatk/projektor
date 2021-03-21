Cypress.Commands.add("loadFixture", (fixturePath, visitUri = "") =>
  cy.readFile(fixturePath).then((resultsBlob) =>
    cy
      .request("POST", `http://localhost:8080/results`, resultsBlob)
      .then((resp) =>
        cy.visit(`http://localhost:8080/tests/${resp.body.id}${visitUri}`, {
          retryOnStatusCodeFailure: true,
        })
      )
  )
);

Cypress.Commands.add(
  "loadGroupedFixtureDataAndVisitTestRun",
  (fixtureData, visitUri = "", loadingFunc = null) =>
    cy
      .request({
        method: "POST",
        url: `http://localhost:8080/groupedResults`,
        body: fixtureData,
        failOnStatusCode: false,
      })
      .then((resp) => {
        const publicId = resp.body.id;

        if (loadingFunc) {
          loadingFunc(publicId).then(() => {
            cy.visit(`http://localhost:8080/tests/${publicId}${visitUri}`, {
              retryOnStatusCodeFailure: true,
            });
          });
        } else {
          cy.visit(`http://localhost:8080/tests/${publicId}${visitUri}`, {
            retryOnStatusCodeFailure: true,
          });
        }
      })
);

Cypress.Commands.add("loadGroupedFixtureData", (fixtureData) =>
  cy
    .request("POST", `http://localhost:8080/groupedResults`, fixtureData)
    .then((resp) => {
      const publicId = resp.body.id;

      return publicId;
    })
);

Cypress.Commands.add(
  "loadGroupedFixture",
  (fixturePath, visitUri = "", loadingFunc = null) =>
    cy
      .readFile(fixturePath)
      .then((resultsBlob) =>
        cy.loadGroupedFixtureDataAndVisitTestRun(
          resultsBlob,
          visitUri,
          loadingFunc
        )
      )
);

Cypress.Commands.add(
  "loadGroupedFixtureWithAttachments",
  (fixturePath, attachments, visitUri = "") =>
    cy.readFile(fixturePath).then((resultsBlob) =>
      cy
        .request("POST", `http://localhost:8080/groupedResults`, resultsBlob)
        .then((resp) => {
          const publicId = resp.body.id;

          cy.wrap(attachments)
            .each((attachment) => {
              const { attachmentName, attachmentPath } = attachment;
              cy.readFile(attachmentPath).then((attachmentContents) => {
                cy.request({
                  method: "POST",
                  url: `http://localhost:8080/run/${publicId}/attachments/${attachmentName}`,
                  body: attachmentContents,
                  retryOnStatusCodeFailure: true,
                });
              });
            })
            .then(() => {
              cy.visit(`http://localhost:8080/tests/${publicId}${visitUri}`, {
                retryOnStatusCodeFailure: true,
              });
            });
        })
    )
);

Cypress.Commands.add("loadCoverageReport", (fileName, publicId) => {
  cy.readFile(`cypress/fixtures/${fileName}`).then((coverageFileContents) =>
    cy.request({
      method: "POST",
      url: `http://localhost:8080/run/${publicId}/coverage`,
      body: coverageFileContents,
      retryOnStatusCodeFailure: true,
    })
  );
});
