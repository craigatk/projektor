const { exec } = require("child_process");
const waitForExpect = require("wait-for-expect");
const { extractTestRunId } = require("../util/parse_output");
const {
  fetchTestRunSummary,
  fetchAttachment,
  fetchAttachments,
} = require("../util/projektor_client");
const { verifyOutput } = require("../verify/cli_output_verify");

describe("Publishing attachments", () => {
  const serverPort = "8082";

  it("should publish to server with attachments when one exceeds max size", async (done) => {
    exec(
      `yarn projektor-publish --serverUrl=http://localhost:${serverPort} --attachmentMaxSizeMB=0.05 --attachments="attachments-large/*" results/*.xml`,
      async (error, stdout, stderr) => {
        verifyOutput(error, stdout, stderr, serverPort);
        expect(stderr).toContain(
          `Error sending attachment projektor-results-dashboard.png to Projektor server http://localhost:${serverPort} Request body larger than maxBodyLength limit`
        );

        const testRunId = extractTestRunId(stdout);
        console.log("Test ID", testRunId);

        const testRunSummaryResponse = await fetchTestRunSummary(
          testRunId,
          serverPort
        );
        expect(testRunSummaryResponse.status).toEqual(200);
        expect(testRunSummaryResponse.data.id).toEqual(testRunId);

        await waitForExpect(async () => {
          const attachments = await fetchAttachments(testRunId, serverPort);
          expect(attachments.data.attachments.length).toBe(2);
        });

        await waitForExpect(async () => {
          const attachment1Response = await fetchAttachment(
            "attachment1.txt",
            testRunId,
            serverPort
          );
          expect(attachment1Response.status).toEqual(200);
          expect(attachment1Response.data).toEqual("Here is attachment 1");
        });

        await waitForExpect(async () => {
          const attachment2Response = await fetchAttachment(
            "attachment2.txt",
            testRunId,
            serverPort
          );
          expect(attachment2Response.status).toEqual(200);
          expect(attachment2Response.data).toEqual("Here is attachment 2");
        });

        done();
      }
    );
  }, 15000);
});
