import http from 'k6/http';
import { check } from "k6";

export let options = {
    stages: [
        { duration: "60s", target: 100 }
    ]
};

export default function () {
    const publicId = "y9zvGnJJAX";

    const response = http.get(`http://localhost:8080/run/${publicId}`);

    check(response, {
        "status is 200": (r) => r.status === 200
    });
};