export function createGroupedResultsPayload(numTestSuites) {
    const failingSpecResultsXml = open('../test-fixtures/src/main/resources/TEST-projektor.example.spock.FailingSpec.xml');
    const passingSpecResultsXml = open('../test-fixtures/src/main/resources/TEST-projektor.example.spock.PassingSpec.xml');

    const resultsPayload = JSON.stringify({
        groupedTestSuites: [
            {
                groupName: "group1",
                testSuitesBlob: [...Array(numTestSuites).keys()].map(() => failingSpecResultsXml).join("\n")
            },
            {
                groupName: "group2",
                testSuitesBlob: [...Array(numTestSuites).keys()].map(() => passingSpecResultsXml).join("\n")
            }
        ]
    });

    return resultsPayload
}

export const createLongOutputGroupedResultsPayload = (numTestSuites) => {
    const longOutputSpecResultsXml = open('../test-fixtures/src/main/resources/TEST-projektor.example.spock.LongOutputSpec.xml');

    const resultsPayload = JSON.stringify({
        groupedTestSuites: [
            {
                groupName: "group1",
                testSuitesBlob: [...Array(numTestSuites).keys()].map(() => longOutputSpecResultsXml).join("\n")
            }
        ]
    });

    return resultsPayload
}

export const resultsParams = {headers: {"Content-Type": "application/json"}};