const { exec } = require("child_process");

describe("publishing invalid test results", () => {
  const serverPort = "8082";
  const serverUrl = `http://localhost:${serverPort}`;

  it("should log error message from server when publishing invalid test results", (done) => {
    exec(
      `yarn projektor-publish --serverUrl=${serverUrl} results-invalid/*.xml`,
      async (error, stdout, stderr) => {
        expect(stderr).toContain(
          `Error publishing results to Projektor server ${serverUrl}`
        );
        expect(stderr).toContain(`Request failed with status code 400`);
        expect(stderr).toContain(`Error from server`);
        expect(stderr).toContain(`Unexpected close tag`);

        done();
      }
    );
  });
});
