const fs = require("fs");
const { exec } = require("child_process");
const { extractTestRunId } = require("../util/parse_output");
const { fetchTestRunSummary } = require("../util/projektor_client");
const { verifyOutput } = require("../verify/cli_output_verify");

describe("Publishing and writing Slack message file via CLI", () => {
  const serverPort = "8082";

  afterEach(() => {
    if (fs.existsSync("projektor_failure_message.json")) {
      fs.unlinkSync("projektor_failure_message.json");
    }
  });

  it("should publish to server and write Slack message file when executed via CLI", async (done) => {
    exec(
      `yarn projektor-publish --serverUrl=http://localhost:${serverPort} --writeSlackMessageFile --projectName=the-project results/*.xml`,
      async (error, stdout, stderr) => {
        verifyOutput(error, stdout, stderr, serverPort);
        expect(error).toBeNull();

        const testRunId = extractTestRunId(stdout);
        console.log("Test ID", testRunId);

        const testRunSummaryResponse = await fetchTestRunSummary(
          testRunId,
          serverPort
        );
        expect(testRunSummaryResponse.status).toEqual(200);

        expect(fs.existsSync("projektor_failure_message.json")).toBeTruthy();

        done();
      }
    );
  });
});
