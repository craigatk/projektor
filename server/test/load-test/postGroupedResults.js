import http from 'k6/http';
import {check} from "k6";
import { createGroupedResultsPayload, resultsParams } from './resultsPayload.js'

export let options = {
    stages: [
        {duration: "60s", target: 100}
    ]
};

const payload = createGroupedResultsPayload(1)

export default function () {
    const response = http.post(
        `http://localhost:8080/groupedResults/`,
        payload,
        resultsParams
    );

    check(response, {
        "status is 200": (r) => r.status === 200
    });
};