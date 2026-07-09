import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    environment: "jsdom",
    globals: true,
    include: ["src/**/*.spec.{ts,tsx}"],
    setupFiles: ["./vitest.setup.ts"],
    coverage: {
      enabled: true,
      provider: "v8",
      reporter: ["clover", "text"],
      reportsDirectory: "coverage",
    },
    reporters: [
      "default",
      ["junit", { outputFile: "jestResults/results.xml" }],
    ],
  },
});
