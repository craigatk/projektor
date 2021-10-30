const { exec } = require("child_process");
const waitForExpect = require("wait-for-expect");
const {
  fetchTestRunSummary,
  fetchCodeQuality,
} = require("../util/projektor_client");
const { extractTestRunId } = require("../util/parse_output");
const { verifyOutput } = require("../verify/cli_output_verify");

describe("publish code quality functional spec", () => {
  const serverPort = "8082";

  it("should publish code quality reports", (done) => {
    exec(
      `yarn projektor-publish --serverUrl=http://localhost:${serverPort} --codeQuality=code-quality-reports/*.txt results/*.xml`,
      async (error, stdout, stderr) => {
        verifyOutput(error, stdout, stderr, serverPort);
        expect(error).toBeNull();

        const testRunId = extractTestRunId(stdout);
        console.log("Test ID", testRunId);

        await waitForExpect(async () => {
          const testRunSummaryResponse = await fetchTestRunSummary(
            testRunId,
            serverPort
          );
          expect(testRunSummaryResponse.status).toEqual(200);
        });

        await waitForExpect(async () => {
          const codeQualityResponse = await fetchCodeQuality(
            testRunId,
            serverPort
          );
          expect(codeQualityResponse.status).toEqual(200);

          console.log("Code quality response", codeQualityResponse.data);

          const codeQualityReports = codeQualityResponse.data.reports;
          expect(codeQualityReports.length).toBe(2);

          const codeQualityFile1 = codeQualityReports.find(
            (codeQualityFile) =>
              codeQualityFile.file_name === "codeQuality1.txt"
          );
          expect(codeQualityFile1.contents).toEqual("Code quality 1");

          const codeQualityFile2 = codeQualityReports.find(
            (codeQualityFile) =>
              codeQualityFile.file_name === "codeQuality2.txt"
          );
          expect(codeQualityFile2.contents).toEqual("Code quality 2");
        });

        done();
      }
    );
  });
});
