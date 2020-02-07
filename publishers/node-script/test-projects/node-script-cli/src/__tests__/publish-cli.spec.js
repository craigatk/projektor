const { exec } = require("child_process");
const ServerMock = require("mock-http-server");

describe("Publishing via CLI", () => {
  const server = new ServerMock({ host: "localhost", port: 9001 });

  beforeAll(done => {
    console.log("Starting mock server");
    server.start(done);
  });

  afterAll(done => {
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

    exec(
      "yarn projektor-publish --serverUrl=http://localhost:9001 results/*.xml",
      (error, stdout, stderr) => {
        if (error) {
          console.log(`error: ${error.message}`);
        }
        if (stderr) {
          console.log(`stderr: ${stderr}`);
        }
        console.log(`stdout: ${stdout}`);

        expect(stdout).toContain(
          "View Projektor results at http://localhost:9001/tests/12345"
        );

        done();
      }
    );
  });
});
