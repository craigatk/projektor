const { exec } = require("child_process");
const ServerMock = require("mock-http-server");

describe("Publishing with config from config file with custom path", () => {
  const server = new ServerMock({ host: "localhost", port: 9002 });

  beforeAll(done => {
    console.log("Starting mock server");
    server.start(done);
  });

  afterAll(done => {
    console.log("Stopping mock server");
    server.stop(done);
  });

  it("should publish when executed via CLI with custom path", async done => {
    server.on({
      method: "POST",
      path: "/results",
      reply: {
        status: 200,
        headers: { "content-type": "application/json" },
        body: JSON.stringify({ uri: "/tests/12345" })
      }
    });

    exec("yarn projektor-publish --configFile=projektor.config.test.json", (error, stdout, stderr) => {
      if (error) {
        console.log(`error: ${error.message}`);
      }
      if (stderr) {
        console.log(`stderr: ${stderr}`);
      }
      console.log(`stdout: ${stdout}`);

      expect(stdout).toContain(
        "View Projektor results at http://localhost:9002/tests/12345"
      );

      done();
    });
  });
});
