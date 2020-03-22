const { exec } = require("child_process");
const { extractTestRunId } = require("../util/parse_output");
const { fetchTestRunSummary } = require("../util/projektor_client");
const { verifyOutput } = require("../verify/cli_output_verify");

describe("Publishing via CLI", () => {
  const serverPort = "8082";
  it("should publish to server when executed via CLI", async (done) => {
    exec(
      `yarn projektor-publish --serverUrl=http://localhost:${serverPort} results/*.xml`,
      async (error, stdout, stderr) => {
        verifyOutput(error, stdout, stderr, serverPort);

        const testRunId = extractTestRunId(stdout);
        console.log("Test ID", testRunId);

        const testRunSummaryResponse = await fetchTestRunSummary(
          testRunId,
          serverPort
        );
        expect(testRunSummaryResponse.status).toEqual(200);

        expect(testRunSummaryResponse.data.id).toEqual(testRunId);
        expect(testRunSummaryResponse.data.total_test_count).toEqual(4);

        done();
      }
    );
  });
});
