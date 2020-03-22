const verifyOutput = (error, stdout, stderr, serverPort) => {
  if (error) {
    console.log(`error: ${error.message}`);
  }
  if (stderr) {
    console.log(`stderr: ${stderr}`);
  }
  console.log(`stdout: ${stdout}`);

  expect(stdout).toContain(
    `View Projektor results at http://localhost:${serverPort}/tests/`
  );
};

module.exports = {
  verifyOutput,
};
