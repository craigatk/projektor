const { exec } = require("child_process");
const { extractTestRunId } = require("../util/parse_output");
const { fetchTestRunSummary } = require("../util/projektor_client");

describe("Publishing via CLI with token", () => {
  const serverPort = "8083";

  it("should publish to server when executed via CLI with token", async done => {
    exec(
      `env-cmd -f .token-env yarn projektor-publish --serverUrl=http://localhost:${serverPort} results/*.xml`,
      async (error, stdout, stderr) => {
        if (error) {
          console.log(`error: ${error.message}`);
        }
        if (stderr) {
          console.log(`stderr: ${stderr}`);
        }
        console.log(`stdout: ${stdout}`);

        expect(stdout).toContain(
          `View Projektor results at http://localhost:${serverPort}/tests/`
        );

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
