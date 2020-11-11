import http from 'k6/http';
import { check, group } from "k6";
import { createGroupedResultsPayload } from './util/resultsPayload.js'
import { createSetup } from "./util/groupedResultsSetup.js";
import { statusCheck200 } from "./util/statusCheck.js";

export let options = {
    stages: [
        { duration: "30s", target: 50 }
    ]
};

const resultsPayload = createGroupedResultsPayload(1)

export const setup = createSetup(resultsPayload, 5)

export default function (data) {
    const testId = data.testId;

    group('fetch test run details', () => {
        const testRunResponse = http.get(`http://localhost:8080/run/${testId}`);
        check(testRunResponse, statusCheck200);

        const testRunSummaryResponse = http.get(`http://localhost:8080/run/${testId}/summary`);
        check(testRunSummaryResponse, statusCheck200);
    })
};