const { exec } = require("child_process");

describe("Publishing via CLI with token", () => {
  it("should publish to server when executed via CLI with token", async done => {
    exec(
      "env-cmd -f .token-env yarn projektor-publish --serverUrl=http://localhost:8083 results/*.xml",
      (error, stdout, stderr) => {
        if (error) {
          console.log(`error: ${error.message}`);
        }
        if (stderr) {
          console.log(`stderr: ${stderr}`);
        }
        console.log(`stdout: ${stdout}`);

        expect(stdout).toContain(
          "View Projektor results at http://localhost:8083/tests/"
        );

        done();
      }
    );
  });
});
