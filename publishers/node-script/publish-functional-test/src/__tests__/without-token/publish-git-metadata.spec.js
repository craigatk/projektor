const { exec } = require("child_process");
const waitForExpect = require("wait-for-expect");
const {
  fetchTestRunSummary,
  fetchGitMetadata,
} = require("../util/projektor_client");
const { extractTestRunId } = require("../util/parse_output");
const { verifyOutput } = require("../verify/cli_output_verify");

describe("publish Git metadata functional spec", () => {
  const serverPort = "8082";

  it("should publish Git metadata along with results", async (done) => {
    exec(
      `env-cmd -f .git-env yarn projektor-publish --serverUrl=http://localhost:${serverPort} --projectName=my-project results/*.xml`,
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

        const gitMetadataResponse = await fetchGitMetadata(
          testRunId,
          serverPort
        );
        expect(gitMetadataResponse.status).toEqual(200);

        console.log("Git metadata response", gitMetadataResponse.data);

        expect(gitMetadataResponse.data.repo_name).toEqual(
          "projektor/projektor"
        );
        expect(gitMetadataResponse.data.branch_name).toEqual("main");
        expect(gitMetadataResponse.data.is_main_branch).toEqual(true);
        expect(gitMetadataResponse.data.project_name).toEqual("my-project");

        done();
      }
    );
  });
});
