#!/usr/bin/env node

require("../src").run(process.argv.slice(2), process.env.PROJEKTOR_TOKEN, "projektor.json");
