const { exec, execSync } = require("child_process");
const waitForExpect = require("wait-for-expect");
const {
  fetchTestRunSummary,
  fetchResultsMetadata,
} = require("../util/projektor_client");
const { extractTestRunId } = require("../util/parse_output");
const { verifyOutput } = require("../verify/cli_output_verify");

describe("publish results metadata functional spec", () => {
  const serverPort = "8082";

  it("should publish CI=true when running in CI", (done) => {
    exec(
      `env-cmd -f .ci-env yarn projektor-publish --serverUrl=http://localhost:${serverPort} results/*.xml`,
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

        const metadataResponse = await fetchResultsMetadata(
          testRunId,
          serverPort
        );
        expect(metadataResponse.status).toEqual(200);

        console.log("Results metadata response", metadataResponse.data);

        expect(metadataResponse.data.ci).toEqual(true);

        done();
      }
    );
  });

  it("should publish CI=false when running in CI", (done) => {
    exec(
      `env-cmd -f .no-ci-env yarn projektor-publish --serverUrl=http://localhost:${serverPort} results/*.xml`,
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

        const metadataResponse = await fetchResultsMetadata(
          testRunId,
          serverPort
        );
        expect(metadataResponse.status).toEqual(200);

        console.log("Results metadata response", metadataResponse.data);

        expect(metadataResponse.data.ci).toEqual(false);

        done();
      }
    );
  });

  it("when group results is true and build number set should append results", async () => {
    const stdout = execSync(
      `env-cmd -f .build-number-env yarn projektor-publish --serverUrl=http://localhost:${serverPort} --groupResults=true "results/*.xml"`
    ).toString();

    const testRunId = extractTestRunId(stdout);
    console.log("Test ID", testRunId);

    await waitForExpect(async () => {
      const testRunSummaryResponse = await fetchTestRunSummary(
        testRunId,
        serverPort
      );
      expect(testRunSummaryResponse.status).toEqual(200);
    });

    const testRunSummaryResponse = await fetchTestRunSummary(
      testRunId,
      serverPort
    );
    const initialTotalTestCount = testRunSummaryResponse.data.total_test_count;

    const secondStdout = execSync(
      `env-cmd -f .build-number-env yarn projektor-publish --serverUrl=http://localhost:${serverPort} --groupResults=true results/*.xml`
    ).toString();

    const secondTestRunId = extractTestRunId(secondStdout);
    expect(secondTestRunId).toEqual(testRunId);

    await waitForExpect(async () => {
      const testRunSummaryResponse = await fetchTestRunSummary(
        testRunId,
        serverPort
      );
      expect(testRunSummaryResponse.status).toEqual(200);
      expect(testRunSummaryResponse.data.total_test_count).toBeGreaterThan(
        initialTotalTestCount
      );
    });
  }, 15000);
});
