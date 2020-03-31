import http from 'k6/http';
import { check, group } from "k6";

export let options = {
    stages: [
        { duration: "120s", target: 50 }
    ]
};

export default function () {
    const publicId = "IPJKBPXBKY8B";

    const statusCheck200 = {
        "status is 200": (r) => r.status === 200
    }

    group('fetch test run details', () => {
        const testRunResponse = http.get(`http://localhost:8080/run/${publicId}`);
        check(testRunResponse, statusCheck200);

        const testRunSummaryResponse = http.get(`http://localhost:8080/run/${publicId}/summary`);
        check(testRunSummaryResponse, statusCheck200);
    })
};