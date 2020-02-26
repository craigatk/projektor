Cypress.Commands.add("loadFixture", (fixturePath, visitUri = "") =>
    cy.readFile(
        fixturePath
    ).then(resultsBlob =>
        cy.request("POST", `http://localhost:8080/results`, resultsBlob).then(resp =>
            cy.visit(`http://localhost:8080/tests/${resp.body.id}${visitUri}`, {
                retryOnStatusCodeFailure: true
            })
        )
    )
);

Cypress.Commands.add("loadGroupedFixture", (fixturePath, visitUri = "") =>
    cy.readFile(
        fixturePath
    ).then(resultsBlob =>
        cy.request("POST", `http://localhost:8080/groupedResults`, resultsBlob).then(resp =>
            cy.visit(`http://localhost:8080/tests/${resp.body.id}${visitUri}`, {
                retryOnStatusCodeFailure: true
            })
        )
    )
);

Cypress.Commands.add("loadGroupedFixtureWithAttachment", (fixturePath, attachmentPath, attachmentName, visitUri = "") =>
    cy.readFile(
        attachmentPath
    ).then(attachmentContents => {
        cy.readFile(
            fixturePath
        ).then(resultsBlob =>
            cy.request("POST", `http://localhost:8080/groupedResults`, resultsBlob).then(resp => {
                const publicId = resp.body.id;
                cy.request("POST", `http://localhost:8080/run/${publicId}/attachments/${attachmentName}`, attachmentContents).then(() => {
                    cy.visit(`http://localhost:8080/tests/${publicId}${visitUri}`, {
                        retryOnStatusCodeFailure: true
                    })
                })

            })
        )
    })
);