import http from 'k6/http';
import {check} from "k6";

export let options = {
    stages: [
        {duration: "60s", target: 100}
    ]
};

const params =  { headers: { "Content-Type": "application/json" } };

const failingSpecResultsXml = open('../test-fixtures/src/main/resources/TEST-FailingSpec.xml');
const passingSpecResultsXml = open('../test-fixtures/src/main/resources/TEST-PassingSpec.xml');

const request = {
    test_results: [
        failingSpecResultsXml,
        passingSpecResultsXml
    ]
};

export default function () {

    const payload = JSON.stringify(request);

    const response = http.post(
        `http://localhost:8080/results/`,
        payload,
        params
    );

    check(response, {
        "status is 200": (r) => r.status === 200
    })
    ;
};