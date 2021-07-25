// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//

import chaiColors from "chai-colors";
chai.use(chaiColors);

Cypress.Commands.add("getByTestId", testId =>
  cy.get(`[data-testid="${testId}"]`)
);
Cypress.Commands.add("testIdShouldExist", testId =>
    cy.getByTestId(testId).should("exist")
);
Cypress.Commands.add("testIdShouldNotExist", testId =>
    cy.getByTestId(testId).should("not.exist")
);

Cypress.Commands.add("getBreadcrumbPackgeNameLink", () =>
  cy.getByTestId("breadcrumb-link-package-name")
);
Cypress.Commands.add("getBreadcrumbClassNameLink", () =>
  cy.getByTestId("breadcrumb-link-class-name")
);
Cypress.Commands.add("getBreadcrumbEndingText", () =>
  cy.getByTestId("breadcrumb-ending-text")
);

Cypress.Commands.add("getTestCaseLinkInList", (testSuiteIdx, testCaseIdx) =>
    cy.getByTestId(`test-case-name-link-${testSuiteIdx}-${testCaseIdx}`)
);

Cypress.Commands.add("getCodeText", () => cy.getByTestId("code-text"));
Cypress.Commands.add("getCodeTextLine", (lineIdx, highlighted) =>
  cy.getByTestId(`code-text-line-${lineIdx}-${highlighted}`)
);
Cypress.Commands.add("getCodeTextLineNumber", (lineIdx) =>
    cy.getByTestId(`code-text-line-number-${lineIdx}`)
);
Cypress.Commands.add("getCodeTextLineNotHighlighted", (lineIdx) =>
    cy.getCodeTextLine(lineIdx, false)
);
Cypress.Commands.add("getCodeTextLineHighlighted", (lineIdx) =>
    cy.getCodeTextLine(lineIdx, true)
);
Cypress.Commands.add("codeLineShouldBeHighlighted", lineIdx =>
  cy
    .getCodeTextLineHighlighted(lineIdx)
    .should("have.css", "background-color")
    .and("be.colored", "#F9F9F9")
);
Cypress.Commands.add("codeLineShouldNotBeHighlighted", lineIdx =>
  cy
    .getCodeTextLineNotHighlighted(lineIdx)
    .should("have.css", "background-color")
    .and("not.be.colored", "#F5F5F5")
);

Cypress.Commands.add("holdShift", () =>
  cy.get("body").type("{shift}", { release: false })
);
Cypress.Commands.add("releaseShift", () =>
  cy.get("body").type("{shift}", { release: true })
);

Cypress.Commands.add("getByRole", role =>
    cy.get(`[role="${role}"]`)
);
Cypress.Commands.add("roleShouldExist", role =>
    cy.getByRole(role).should("exist")
);