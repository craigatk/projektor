const { exec } = require("child_process");
const waitForExpect = require("wait-for-expect");
const { extractTestRunId } = require("../util/parse_output");
const { fetchPerformanceResults } = require("../util/projektor_client");
const { verifyOutput } = require("../verify/cli_output_verify");

describe("Publishing performance results via CLI", () => {
  const serverPort = "8082";

  it("should publish performance results", async (done) => {
    exec(
      `yarn projektor-publish --serverUrl=http://localhost:${serverPort} --performance=performance/getRun.json`,
      async (error, stdout, stderr) => {
        verifyOutput(error, stdout, stderr, serverPort);
        expect(error).toBeNull();
        expect(stdout).not.toContain(
          "No test results files found in locations"
        );

        const testRunId = extractTestRunId(stdout);
        console.log("Test ID", testRunId);

        await waitForExpect(async () => {
          const performanceResultsResponse = await fetchPerformanceResults(
            testRunId,
            serverPort
          );
          expect(performanceResultsResponse.status).toEqual(200);
        });

        done();
      }
    );
  });

  it("when no performance results should log message", async (done) => {
    exec(
      `yarn projektor-publish --serverUrl=http://localhost:${serverPort} --performance=performance/doesNotExist.json`,
      async (error, stdout, stderr) => {
        expect(stdout).toContain(
          "No performance results files found in locations"
        );

        done();
      }
    );
  });
});
