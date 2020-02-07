const { exec } = require("child_process");
const ServerMock = require("mock-http-server");
const fs = require("fs");

describe("Publishing with config from config file", () => {
  const server = new ServerMock({ host: "localhost", port: 9000 });

  beforeAll(done => {
    fs.renameSync("projektor.test.json", "projektor.json");
    console.log("Starting mock server");
    server.start(done);
  });

  afterAll(done => {
    fs.renameSync("projektor.json", "projektor.test.json");
    console.log("Stopping mock server");
    server.stop(done);
  });

  it("should publish when executed via CLI", async done => {
    server.on({
      method: "POST",
      path: "/results",
      reply: {
        status: 200,
        headers: { "content-type": "application/json" },
        body: JSON.stringify({ uri: "/tests/12345" })
      }
    });

    exec("yarn projektor-publish", (error, stdout, stderr) => {
      if (error) {
        console.log(`error: ${error.message}`);
      }
      if (stderr) {
        console.log(`stderr: ${stderr}`);
      }
      console.log(`stdout: ${stdout}`);

      expect(stdout).toContain(
        "View Projektor results at http://localhost:9000/tests/12345"
      );

      done();
    });
  });
});
