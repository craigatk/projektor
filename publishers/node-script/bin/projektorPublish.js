#!/usr/bin/env node

require("../src").runCLI(process.argv.slice(2), process.env.PROJEKTOR_TOKEN, "projektor.json");
