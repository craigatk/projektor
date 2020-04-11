import http from 'k6/http';
import { check } from "k6";
import { createGroupedResultsPayload } from './resultsPayload.js'
import { createSetup } from './groupedResultsSetup.js'
import { statusCheck200 } from "./statusCheck.js";

export let options = {
    stages: [
        { duration: "60s", target: 50 }
    ],
    setupTimeout: "30s"
};

const resultsPayload = createGroupedResultsPayload(100)

export const setup = createSetup(resultsPayload, 10)

const getParams = {headers: {"Accept-Encoding": "gzip"}};

export default function (data) {
    const testId = data.testId;

    const testRunResponse = http.get(`http://localhost:8080/run/${testId}/cases/failed`, getParams);
    check(testRunResponse, statusCheck200);
};