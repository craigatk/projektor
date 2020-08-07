const { exec, execSync } = require("child_process");
const waitForExpect = require("wait-for-expect");
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
        expect(error).toBeNull();
        expect(stdout).not.toContain(
          "No test results files found in locations"
        );

        const testRunId = extractTestRunId(stdout);
        console.log("Test ID", testRunId);

        await waitForExpect(async () => {
          const testRunSummaryResponse = await fetchTestRunSummary(
            testRunId,
            serverPort
          );
          expect(testRunSummaryResponse.status).toEqual(200);

          expect(testRunSummaryResponse.data.id).toEqual(testRunId);
          expect(testRunSummaryResponse.data.total_test_count).toEqual(4);
        });

        done();
      }
    );
  });

  it("should exit with non-zero exit code when configured via CLI and there are failing tests", async (done) => {
    exec(
      `yarn projektor-publish --serverUrl=http://localhost:${serverPort} --exitWithFailure results-failure/*.xml`,
      async (error, stdout, stderr) => {
        verifyOutput(error, stdout, stderr, serverPort);

        expect(error.code).toBe(1);

        done();
      }
    );
  });

  it("should log message when no test results found", () => {
    const stdout = execSync(
      `yarn projektor-publish --serverUrl=http://localhost:${serverPort} does-not-exist/*.xml`
    ).toString();

    console.log(stdout);

    expect(stdout).toContain(
      "No test results files found in locations does-not-exist/*.xml"
    );
  });
});
