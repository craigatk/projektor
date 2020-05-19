import http from 'k6/http';
import {check} from "k6";
import { createLongOutputGroupedResultsPayload, resultsParams } from './util/resultsPayload.js'
import { statusCheck200 } from "./util/statusCheck.js";

export let options = {
    stages: [
        {duration: "60s", target: 10}
    ]
};

const payload = createLongOutputGroupedResultsPayload(100)

export default function () {
    const response = http.post(
        `http://localhost:8080/groupedResults/`,
        payload,
        resultsParams
    );

    check(response, statusCheck200);
};