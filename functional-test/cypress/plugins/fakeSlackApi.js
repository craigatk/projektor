const http = require("http");

// Stand-in for the Slack Web API that the server under test posts to (see SLACK_API_URL in build.gradle)
const FAKE_SLACK_API_PORT = 8091;

const startFakeSlackApi = () => {
  let messages = [];

  const server = http.createServer((req, res) => {
    let body = "";

    req.on("data", (chunk) => (body += chunk));
    req.on("end", () => {
      res.setHeader("Content-Type", "application/json");

      if (req.method === "POST" && req.url === "/chat.postMessage") {
        messages.push(JSON.parse(body));
        res.end(JSON.stringify({ ok: true }));
      } else {
        res.statusCode = 404;
        res.end(JSON.stringify({ ok: false, error: "unknown_method" }));
      }
    });
  });

  server.listen(FAKE_SLACK_API_PORT);

  return {
    slackMessages: () => messages,
    resetSlackMessages: () => {
      messages = [];

      return null;
    },
  };
};

module.exports = { startFakeSlackApi };
