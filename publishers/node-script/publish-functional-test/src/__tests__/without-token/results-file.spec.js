const fs = require("fs");
const { exec } = require("child_process");
const { extractTestRunId } = require("../util/parse_output");
const { verifyOutput } = require("../verify/cli_output_verify");

describe("results file functional spec", () => {
  const serverPort = "8082";

  const resultsFileName = "projektor_report.json";

  afterEach(() => {
    if (fs.existsSync(resultsFileName)) {
      fs.unlinkSync(resultsFileName);
    }
  });

  it("should write results file to disk when running in CI", async (done) => {
    exec(
      `env-cmd -f .ci-env yarn projektor-publish --serverUrl=http://localhost:${serverPort} results/*.xml`,
      async (error, stdout, stderr) => {
        verifyOutput(error, stdout, stderr, serverPort);
        expect(error).toBeNull();

        const testRunId = extractTestRunId(stdout);
        console.log("Test ID", testRunId);

        expect(fs.existsSync(resultsFileName)).toBeTruthy();

        const resultsFileContents = fs.readFileSync(resultsFileName).toString();
        console.log("Results file contents", resultsFileContents);

        const resultsFileJson = JSON.parse(resultsFileContents);

        expect(resultsFileJson.id).toBe(testRunId);
        expect(resultsFileJson.reportUrl).toBe(
          `http://localhost:${serverPort}/tests/${testRunId}`
        );

        done();
      }
    );
  });

  it("should not write results file to disk when not running in CI", async (done) => {
    exec(
      `env-cmd -f .no-ci-env yarn projektor-publish --serverUrl=http://localhost:${serverPort} results/*.xml`,
      async (error, stdout, stderr) => {
        verifyOutput(error, stdout, stderr, serverPort);
        expect(error).toBeNull();

        const testRunId = extractTestRunId(stdout);
        console.log("Test ID", testRunId);

        expect(fs.existsSync(resultsFileName)).toBeFalsy();

        done();
      }
    );
  });
});
