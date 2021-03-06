import http from 'k6/http';
import {check} from "k6";
import { createGroupedResultsPayload, resultsParams } from './util/resultsPayload.js'
import { statusCheck200 } from "./util/statusCheck.js";

export let options = {
    stages: [
        {duration: "60s", target: 10}
    ]
};

const payload = createGroupedResultsPayload(1)

export default function () {
    const response = http.post(
        `http://localhost:8080/groupedResults/`,
        payload,
        resultsParams
    );

    check(response, statusCheck200);
};