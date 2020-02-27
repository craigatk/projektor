const { exec } = require("child_process");
const { extractTestRunId } = require("../util/parse_output");
const {
  fetchTestRunSummary,
  fetchAttachment
} = require("../util/projektor_client");

describe("Publishing from config file with token", () => {
  const serverPort = "8083";

  it("should publish to server with attachments when configured with config file and using token", async done => {
    exec(
      `env-cmd -f .token-env yarn projektor-publish --configFile=src/__tests__/with-token/projektor.json`,
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

        const attachment1Response = await fetchAttachment(
          "attachment1.txt",
          testRunId,
          serverPort
        );
        expect(attachment1Response.status).toEqual(200);
        expect(attachment1Response.data).toEqual("Here is attachment 1");

        const attachment2Response = await fetchAttachment(
          "attachment2.txt",
          testRunId,
          serverPort
        );
        expect(attachment2Response.status).toEqual(200);
        expect(attachment2Response.data).toEqual("Here is attachment 2");

        done();
      }
    );
  });
});
