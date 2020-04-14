import http from 'k6/http';
import { check, group } from "k6";
import { createLongOutputGroupedResultsPayload } from './util/resultsPayload.js'
import { createSetup } from './util/groupedResultsSetup.js'
import { statusCheck200 } from "./util/statusCheck.js";

export let options = {
    stages: [
        { duration: "120s", target: 50 }
    ],
    setupTimeout: "30s"
};

const resultsPayload = createLongOutputGroupedResultsPayload(100)

export const setup = createSetup(resultsPayload, 5)

export default function (data) {
    const testId = data.testId;

    group("get test suite", () => {
        const testSuiteResponse = http.get(`http://localhost:8080/run/${testId}/suite/1`);
        check(testSuiteResponse, statusCheck200);
    })

    group("get test suite system out", () => {
        const testSuiteSystemOutResponse = http.get(`http://localhost:8080/run/${testId}/suite/1/systemOut`);
        check(testSuiteSystemOutResponse, statusCheck200);
    })
};