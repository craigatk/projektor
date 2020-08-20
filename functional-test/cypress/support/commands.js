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
  "loadGroupedFixture",
  (fixturePath, visitUri = "", loadingFunc = null) =>
    cy.readFile(fixturePath).then((resultsBlob) =>
      cy
        .request("POST", `http://localhost:8080/groupedResults`, resultsBlob)
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
    )
);

Cypress.Commands.add(
  "loadGroupedFixtureWithAttachment",
  (fixturePath, attachmentPath, attachmentName, visitUri = "") =>
    cy.readFile(attachmentPath).then((attachmentContents) => {
      cy.readFile(fixturePath).then((resultsBlob) =>
        cy
          .request("POST", `http://localhost:8080/groupedResults`, resultsBlob)
          .then((resp) => {
            const publicId = resp.body.id;
            cy.request({
              method: "POST",
              url: `http://localhost:8080/run/${publicId}/attachments/${attachmentName}`,
              body: attachmentContents,
              retryOnStatusCodeFailure: true,
            }).then(() => {
              cy.visit(`http://localhost:8080/tests/${publicId}${visitUri}`, {
                retryOnStatusCodeFailure: true,
              });
            });
          })
      );
    })
);
