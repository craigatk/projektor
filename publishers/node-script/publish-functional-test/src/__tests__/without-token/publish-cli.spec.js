const { exec } = require("child_process");

describe("Publishing via CLI", () => {
  it("should publish to server when executed via CLI", async done => {
    exec(
      "yarn projektor-publish --serverUrl=http://localhost:8082 results/*.xml",
      (error, stdout, stderr) => {
        if (error) {
          console.log(`error: ${error.message}`);
        }
        if (stderr) {
          console.log(`stderr: ${stderr}`);
        }
        console.log(`stdout: ${stdout}`);

        expect(stdout).toContain(
          "View Projektor results at http://localhost:8082/tests/"
        );

        done();
      }
    );
  });
});
