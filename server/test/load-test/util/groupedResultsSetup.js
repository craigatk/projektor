import { resultsParams } from "./resultsPayload.js";
import http from 'k6/http';
import { sleep } from "k6";

export const createSetup = (resultsPayload, sleepTime) => () => {
    const resultsResponse = http.post(
        `http://localhost:8080/groupedResults/`,
        resultsPayload,
        resultsParams
    );

    const testId = JSON.parse(resultsResponse.body).id

    console.log("Test ID: " + testId)

    console.log("Sleeping for a bit while test run is saved")
    sleep(sleepTime)

    return { testId: testId }
}