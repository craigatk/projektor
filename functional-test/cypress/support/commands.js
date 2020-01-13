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
