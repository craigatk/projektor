import http from 'k6/http';
import { check, group } from "k6";
import { createLongOutputGroupedResultsPayload } from './util/resultsPayload.js'
import { createSetup } from './util/groupedResultsSetup.js'
import { statusCheck200 } from "./util/statusCheck.js";

export let options = {
    stages: [
        { duration: "60s", target: 50 }
    ],
    setupTimeout: "30s"
};

const resultsPayload = createLongOutputGroupedResultsPayload(100)

export const setup = createSetup(resultsPayload, 5)

export default function (data) {
    const testId = data.testId;

    const testSuiteSystemOutResponse = http.get(`http://localhost:8080/run/${testId}/suite/1/systemOut`);
    check(testSuiteSystemOutResponse, statusCheck200);
};