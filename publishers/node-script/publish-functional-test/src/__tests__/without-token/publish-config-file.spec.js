const { exec } = require("child_process");
const { verifyOutput } = require("../verify/cli_output_verify");

describe("Publishing via config file", () => {
  const serverPort = "8082";

  it("should exit with non-zero exit code when configured via config file", async (done) => {
    exec(
      `yarn projektor-publish --configFile=src/__tests__/without-token/projektor-exit-with-failure.json`,
      async (error, stdout, stderr) => {
        verifyOutput(error, stdout, stderr, serverPort);

        expect(error.code).toBe(1);

        done();
      }
    );
  });
});
