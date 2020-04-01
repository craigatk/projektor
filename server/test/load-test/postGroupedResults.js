import http from 'k6/http';
import {check} from "k6";

export let options = {
    stages: [
        {duration: "60s", target: 100}
    ]
};

const params = {headers: {"Content-Type": "application/json"}};

const failingSpecResultsXml = open('../test-fixtures/src/main/resources/TEST-projektor.example.spock.FailingSpec.xml');
const passingSpecResultsXml = open('../test-fixtures/src/main/resources/TEST-projektor.example.spock.PassingSpec.xml');

const request = {
    groupedTestSuites: [
        {
            groupName: "group1",
            testSuitesBlob: failingSpecResultsXml
        },
        {
            groupName: "group2",
            testSuitesBlob: passingSpecResultsXml
        }
    ]
};

const payload = JSON.stringify(request);

export default function () {

    const response = http.post(
        `http://localhost:8080/groupedResults/`,
        payload,
        params
    );

    check(response, {
        "status is 200": (r) => r.status === 200
    });
};