const { exec } = require("child_process");
const waitForExpect = require("wait-for-expect");
const { extractTestRunId } = require("../util/parse_output");
const {
  fetchTestRunSummary,
  fetchCoverage,
} = require("../util/projektor_client");
const { verifyOutput } = require("../verify/cli_output_verify");

describe("Publishing with coverage via CLI", () => {
  const serverPort = "8082";

  it("should publish results and coverage to server configured in config file", async (done) => {
    exec(
      `yarn projektor-publish --configFile=src/__tests__/without-token/projektor-coverage.json`,
      async (error, stdout, stderr) => {
        verifyOutput(error, stdout, stderr, serverPort);
        expect(error).toBeNull();
        expect(stdout).not.toContain(
          "No test results files found in locations"
        );

        expect(stdout).toContain("Sending 1 coverage result(s) to Projektor server")
        expect(stdout).toContain("Finished sending coverage");

        const testRunId = extractTestRunId(stdout);
        console.log("Test ID", testRunId);

        const coverageResponse = await fetchCoverage(testRunId, serverPort);
        expect(coverageResponse.status).toEqual(200);

        console.log("Coverage data", coverageResponse.data);

        expect(
          coverageResponse.data.overall_stats.line_stat.covered_percentage
        ).toBe(90.5);

        done();
      }
    );
  });
});
